/*
 * Copyright (C) 2008-2015 Citeck LLC.
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
package ru.citeck.ecos.invariants;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface InvariantService {
    
    /**
     * Register delegate for attribute type support.
     * 
     * @param attributeType
     */
    public void registerAttributeType(InvariantAttributeType attributeType);
    
    /**
     * Get supported attribute types.
     * 
     * @return
     */
    public Set<QName> getSupportedAttributeTypes();
    
    /**
     * Register new invariant language implementation.
     * 
     * @param language
     */
    public void registerLanguage(InvariantLanguage language);
    
    /**
     * Get set of supported invariant languages.
     * @return
     */
    public Set<String> getSupportedLanguages();
    
    /**
     * Deploy invariants definition file.
     * @param source
     */
    public void deployDefinition(InputStream source, String sourceId, InvariantPriority priority);
    
    /**
     * Undeploy invariants definition file.
     * @param source
     */
    public void undeployDefinition(String sourceId);
    
    /**
     * Get invariants for specified class (type or aspect).
     * Invariants should be ordered by priority (highest priority first).
     * 
     * @param className
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> getInvariants(QName className);
    
    /**
     * Get invariants for specified class (type or aspect) and attributes (properties and/or associations).
     * Invariants should be ordered by priority (highest priority first).
     * 
     * @param className name of class (type or aspect)
     * @param attributeNames names of attributes (properties and/or associations)
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> getInvariants(QName className, Collection<QName> attributeNames);

    /**
     * Get invariants for specified class (type or aspect) and attributes (properties and/or associations).
     * Invariants should be ordered by priority (highest priority first).
     *
     * @param className name of class (type or aspect)
     * @param attributeNames names of attributes (properties and/or associations)
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> getInvariants(Collection<QName> classNames, Collection<QName> attributeNames);

    /**
     * Get invariants for specified class (type or aspect) and attributes (properties and/or associations).
     * Invariants should be ordered by priority (highest priority first).
     *
     * @param className name of class (type or aspect)
     * @param attributeNames names of attributes (properties and/or associations)
     * @param nodeRef
     * @param mode
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> getInvariants(Collection<QName> classNames, Collection<QName> attributeNames, NodeRef nodeRef, String mode);

    /**
     * Get invariants for specified classes (types or aspects).
     * Invariants should be ordered by priority (highest priority first).
     * 
     * @param classNames class names (types or aspects)
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> getInvariants(Collection<QName> classNames);
    
    /**
     * Get invariants for specified node.
     * Invariants should be ordered by priority (highest priority first).
     * 
     * @param nodeRef
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> getInvariants(NodeRef nodeRef);
    
    /**
     * Get invariants for specified nod and attributes (properties and/or associations)e.
     * Invariants should be ordered by priority (highest priority first).
     * 
     * @param nodeRef
     * @param attributeNames names of attributes (properties and/or associations)
     * @return ordered list of invariants
     */
    public List<InvariantDefinition> getInvariants(NodeRef nodeRef, Collection<QName> attributeNames);
    
    /**
     * Evaluate specified invariant on specified model.
     * 
     * @param invariant
     * @param model
     * @return
     */
    public Object evaluateInvariant(InvariantDefinition invariant, Map<String, Object> model);
    
    /**
     * Execute all matching invariants for the specified node.
     * It considers 'value' and 'default' invariants to set attribute values 
     *   and 'valid' invariant to validate node state.
     * 
     * @param nodeRef
     */
    public void executeInvariants(NodeRef nodeRef);
    
    /**
     * Execute specified invariants for the specified node.
     * It considers 'value' and 'default' invariants to set attribute values 
     *   and 'valid' invariant to validate node state.
     * 
     * @param nodeRef
     * @param invariants
     * @param model
     */
    public void executeInvariants(NodeRef nodeRef, List<InvariantDefinition> invariants, Map<String, Object> model);

    /**
     * Execute specified invariants for the specified node, that was just created.
     * It considers 'value' and 'default' invariants to set attribute values 
     *   and 'valid' invariant to validate node state.
     * 
     * @param nodeRef
     * @param invariants
     * @param model
     */
    public void executeInvariantsForNewNode(NodeRef nodeRef, List<InvariantDefinition> invariants, Map<String, Object> model);

}