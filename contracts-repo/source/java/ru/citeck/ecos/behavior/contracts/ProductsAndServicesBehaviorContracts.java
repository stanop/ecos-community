/*
 * Copyright (C) 2008-2016 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */

package ru.citeck.ecos.behavior.contracts;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.PaymentsModel;
import ru.citeck.ecos.model.ProductsAndServicesModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This behaviour react to change in "products and services" and calculate total amount in Payment
 * @author Roman.Makarskiy on 04.04.2016.
 */
public class ProductsAndServicesBehaviorContracts implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

    private NodeService nodeService;
    private PolicyComponent policyComponent;
    private String namespace;
    private String type;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void init() {
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME,
                QName.createQName(namespace, type), new JavaBehaviour(this,
                        "onCreateNode",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                QName.createQName(namespace, type), new JavaBehaviour(this,
                        "onUpdateProperties",
                        Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
        this.policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                QName.createQName(namespace, type), new JavaBehaviour(this,
                        "beforeDeleteNode",
                        Behaviour.NotificationFrequency.EVERY_EVENT));
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        updateTotalAmountInPayment(nodeRef);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssociationRef) {
        NodeRef pasEntityRef = childAssociationRef.getChildRef();
        if (!nodeService.exists(pasEntityRef)) {
            return;
        }
        updateTotalAmountInPayment(pasEntityRef);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> map, Map<QName, Serializable> map1) {
        if (!nodeService.exists(nodeRef)) {
            return;
        }
        updateTotalAmountInPayment(nodeRef);
    }

    private BigDecimal getTotalAmount(NodeRef nodeRef) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<AssociationRef> products;
        if (nodeService.exists(nodeRef)) {
            products = nodeService.getTargetAssocs(nodeRef, ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES);
            for (AssociationRef associationRef : products) {
                BigDecimal amount = new BigDecimal ((double) nodeService.getProperty(associationRef.getTargetRef(), ProductsAndServicesModel.PROP_TOTAL), MathContext.DECIMAL64);
                totalAmount = totalAmount.add(amount);
            }
        }
        return totalAmount;
    }

    private void updateTotalAmountInPayment(NodeRef nodeRef) {
        List<AssociationRef> payments = nodeService.getSourceAssocs(nodeRef, ProductsAndServicesModel.ASSOC_CONTAINS_PRODUCTS_AND_SERVICES);
        if (!payments.isEmpty()) {
            for (AssociationRef paymentRef : payments) {
                BigDecimal totalAmount  = getTotalAmount(paymentRef.getSourceRef());
                BigDecimal currentlyAmount = new BigDecimal((double) nodeService.getProperty(paymentRef.getSourceRef(), PaymentsModel.PROP_PAYMENT_AMOUNT), MathContext.DECIMAL64);
                if (!Objects.equals(totalAmount, currentlyAmount)) {
                    nodeService.setProperty(paymentRef.getSourceRef(), PaymentsModel.PROP_PAYMENT_AMOUNT, totalAmount);
                }
            }
        }
    }
}
