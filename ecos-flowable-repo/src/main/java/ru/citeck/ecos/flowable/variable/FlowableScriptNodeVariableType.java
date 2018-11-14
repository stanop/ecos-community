package ru.citeck.ecos.flowable.variable;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.flowable.engine.common.api.FlowableException;
import org.flowable.variable.api.types.ValueFields;
import org.flowable.variable.api.types.VariableType;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * @author Roman Makarskiy
 */
public class FlowableScriptNodeVariableType implements VariableType {

    public static final String TYPE_NAME = "flwAlfrescoScriptNode";

    private ServiceRegistry serviceRegistry;

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    @Override
    public boolean isCachable() {
        return true;
    }

    @Override
    public boolean isAbleToStore(Object value) {
        if (value == null) {
            return true;
        }

        return ScriptNode.class.isAssignableFrom(value.getClass());
    }

    @Override
    public void setValue(Object value, ValueFields valueFields) {
        String textValue = null;
        if (value != null) {
            if (!(value instanceof FlowableActivitiScriptNode)) {
                throw new FlowableException("Passed value is not an instance of FlowableActivitiScriptNode, cannot set variable value.");
            }
            NodeRef reference = (((FlowableActivitiScriptNode) value).getNodeRef());
            if (reference != null) {
                // Use the string representation of the NodeRef
                textValue = reference.toString();
            }
        }
        valueFields.setTextValue(textValue);
    }

    @Override
    public Object getValue(ValueFields valueFields) {
        ScriptNode scriptNode = null;
        String nodeRefString = valueFields.getTextValue();
        if (nodeRefString != null) {

            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();

            FlowableActivitiScriptNode node;
            try {
                node = new FlowableActivitiScriptNode((new NodeRef(nodeRefString)), serviceRegistry, scope);
            } finally {
                Context.exit();
            }

            scriptNode = node;
        }
        return scriptNode;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
