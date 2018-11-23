package ru.citeck.ecos.menu.resolvers;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JournalsResolver extends AbstractJournalsResolver {

    private static final String ID = "JOURNALS";
    private static final String LIST_ID_KEY = "listId";

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String journalsListId = getParam(params, context, LIST_ID_KEY);
        /* put journalsListId to context */
        Map<String, String> contextParams = context.getParams();
        if (contextParams == null) {
            contextParams = new HashMap<>();
        }
        contextParams.put(LIST_ID_KEY, journalsListId);
        context.setParams(contextParams);
        return queryJournalsRefs(journalsListId).stream()
                .map(nodeRef -> constructItem(nodeRef, context))
                .collect(Collectors.toList());
    }

    private List<NodeRef> queryJournalsRefs(String journalList) {
        if (StringUtils.isEmpty(journalList)) {
            return Collections.emptyList();
        }
        NodeRef parent = new NodeRef("workspace://SpacesStore/journal-meta-f-lists");
        return FTSQuery.create()
                .parent(parent).and()
                .type(JournalsModel.TYPE_JOURNALS_LIST).and()
                .exact(ContentModel.PROP_NAME, journalList)
                .transactional().query(searchService)
                .stream().findFirst()
                .map(this::journalsFromList)
                .orElse(Collections.emptyList());
    }

    private List<NodeRef> journalsFromList(NodeRef nodeRef) {
        return nodeService.getTargetAssocs(nodeRef, JournalsModel.ASSOC_JOURNALS).stream()
                .map(AssociationRef::getTargetRef)
                .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return ID;
    }

}
