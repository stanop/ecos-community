package ru.citeck.ecos.icase.activity;

import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ActivityModel;
import ru.citeck.ecos.model.LifeCycleModel;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public class CaseActivityServiceImpl implements CaseActivityService {

    public static final String STATE_NOT_STARTED = "Not started";
    public static final String STATE_STARTED = "Started";
    public static final String STATE_COMPLETED = "Completed";

    private static final Log log = LogFactory.getLog(CaseActivityService.class);

    private DictionaryService dictionaryService;
    private PolicyComponent policyComponent;
    private NodeService nodeService;

    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityStartedPolicy> onActivityStartedDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityStoppedPolicy> onActivityStoppedDelegate;
    private ClassPolicyDelegate<CaseActivityPolicies.OnCaseActivityResetPolicy> onActivityResetDelegate;

    public void init() {
        onActivityStartedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityStartedPolicy.class);
        onActivityStoppedDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityStoppedPolicy.class);
        onActivityResetDelegate = policyComponent.registerClassPolicy(CaseActivityPolicies.OnCaseActivityResetPolicy.class);
    }

    @Override
    public void startActivity(NodeRef activityRef) {
        if(!setState(activityRef, STATE_STARTED)) return;

        nodeService.setProperty(activityRef, ActivityModel.PROP_ACTUAL_START_DATE, new Date());

        HashSet<QName> classes = new HashSet<QName>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));
        CaseActivityPolicies.OnCaseActivityStartedPolicy policy = onActivityStartedDelegate.get(classes);
        policy.onCaseActivityStarted(activityRef);
    }

    @Override
    public void stopActivity(NodeRef activityRef) {
        if(!setState(activityRef, STATE_COMPLETED)) return;

        nodeService.setProperty(activityRef, ActivityModel.PROP_ACTUAL_END_DATE, new Date());

        HashSet<QName> classes = new HashSet<QName>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));
        CaseActivityPolicies.OnCaseActivityStoppedPolicy policy = onActivityStoppedDelegate.get(classes);
        policy.onCaseActivityStopped(activityRef);
    }

    private boolean setState(NodeRef nodeRef, String state) {
        if (!nodeService.exists(nodeRef)) {
            return false;
        }
        String currentState = (String) nodeService.getProperty(nodeRef, LifeCycleModel.PROP_STATE);
        if(currentState == null) currentState = STATE_NOT_STARTED;
        
        if(state.equals(currentState)
                || currentState.equals(STATE_NOT_STARTED) && !state.equals(STATE_STARTED)
                || currentState.equals(STATE_STARTED) && !state.equals(STATE_COMPLETED)) {
            return false;
        }
        nodeService.setProperty(nodeRef, LifeCycleModel.PROP_STATE, state);
        return true;
    }

    @Override
    public NodeRef getDocument(NodeRef activityRef) {
        ChildAssociationRef parent = nodeService.getPrimaryParent(activityRef);
        while (parent.getParentRef() != null
                && RepoUtils.isSubType(parent.getParentRef(), ActivityModel.TYPE_ACTIVITY, nodeService, dictionaryService)) {
            parent = nodeService.getPrimaryParent(parent.getParentRef());
        }
        return parent.getParentRef();
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef) {
        return getActivities(nodeRef, RegexQNamePattern.MATCH_ALL);
    }

    @Override
    public List<NodeRef> getActivities(NodeRef nodeRef, QNamePattern type) {
        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef, ActivityModel.ASSOC_ACTIVITIES, type);

        if (children == null || children.isEmpty()) {
            return new ArrayList<>(0);
        }

        List<Pair<NodeRef, Integer>> indexedChildren = new ArrayList<>(children.size());
        for (ChildAssociationRef child : children) {
            NodeRef childRef = child.getChildRef();
            Integer index = (Integer)nodeService.getProperty(childRef, ActivityModel.PROP_INDEX);
            indexedChildren.add(new Pair<>(childRef, index != null ? index : 0));
        }

        Collections.sort(indexedChildren, new Comparator<Pair<NodeRef, Integer>>() {
            @Override
            public int compare(Pair<NodeRef, Integer> child0,
                               Pair<NodeRef, Integer> child1) {
                return Integer.compare(child0.getSecond(), child1.getSecond());
            }
        });

        List<NodeRef> result = new ArrayList<>(indexedChildren.size());
        for (Pair<NodeRef, Integer> child : indexedChildren) {
            result.add(child.getFirst());
        }
        return result;
    }

    @Override
    public void reset(NodeRef nodeRef) {
        QName nodeType = nodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(nodeType, ActivityModel.TYPE_ACTIVITY)) {
            resetActivity(nodeRef);
        } else {
            resetActivitiesInChildren(nodeRef);
        }
    }

    private void resetActivity(NodeRef activityRef) {

        nodeService.setProperty(activityRef, ActivityModel.PROP_ACTUAL_START_DATE, null);
        nodeService.setProperty(activityRef, ActivityModel.PROP_ACTUAL_END_DATE, null);
        nodeService.setProperty(activityRef, LifeCycleModel.PROP_STATE, STATE_NOT_STARTED);

        HashSet<QName> classes = new HashSet<QName>(DictionaryUtils.getNodeClassNames(activityRef, nodeService));
        CaseActivityPolicies.OnCaseActivityResetPolicy policy = onActivityResetDelegate.get(classes);
        policy.onCaseActivityReset(activityRef);

        resetActivitiesInChildren(activityRef);
    }

    private void resetActivitiesInChildren(NodeRef nodeRef) {
        List<NodeRef> children = RepoUtils.getChildrenByAssoc(nodeRef, ActivityModel.ASSOC_ACTIVITIES, nodeService);
        for(NodeRef activityRef : children) {
            resetActivity(activityRef);
        }
    }

    @Override
    public void setParent(NodeRef activityRef, NodeRef newParent) {
        mandatoryActivity("activityRef", activityRef);
        mandatoryNodeRef("newParent", newParent);

        ChildAssociationRef assocRef = nodeService.getPrimaryParent(activityRef);
        NodeRef parent = assocRef.getParentRef();

        if (!parent.equals(newParent)) {
            if (!nodeService.hasAspect(newParent, ActivityModel.ASPECT_HAS_ACTIVITIES)) {
                throw new IllegalArgumentException("New parent doesn't have aspect 'hasActivities'");
            }
            nodeService.moveNode(activityRef, newParent, ActivityModel.ASSOC_ACTIVITIES,
                                                         ActivityModel.ASSOC_ACTIVITIES);
        }
    }

    @Override
    public void setIndex(NodeRef activityRef, int newIndex) {
        mandatoryActivity("activityRef", activityRef);

        ChildAssociationRef assocRef = nodeService.getPrimaryParent(activityRef);
        NodeRef parent = assocRef.getParentRef();

        List<NodeRef> activities = getActivities(parent);

        if (newIndex >= activities.size()) {
            newIndex = activities.size() - 1;
        }
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex == activities.indexOf(activityRef)) return;

        activities.remove(activityRef);
        activities.add(newIndex, activityRef);

        for (int i = 0; i < activities.size(); i++) {
            nodeService.setProperty(activities.get(i), ActivityModel.PROP_INDEX, i);
        }
    }

    private void mandatoryActivity(String paramName, NodeRef activityRef) {
        mandatoryNodeRef(paramName, activityRef);
        QName type = nodeService.getType(activityRef);
        if (!dictionaryService.isSubClass(type, ActivityModel.TYPE_ACTIVITY)) {
            throw new IllegalArgumentException(paramName + " must inherit activ:activity");
        }
    }

    private void mandatoryNodeRef(String paramName, NodeRef nodeRef) {
        if (nodeRef == null) {
            throw new IllegalArgumentException(paramName + " is a mandatory parameter");
        } else if (!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("Parameter " + paramName + " have incorrect " +
                                               "NodeRef: " + nodeRef + ". The node doesn't exists.");
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
}
