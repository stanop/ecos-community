package ru.citeck.ecos.records;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.repo.jscript.ValueConverter;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.ActionResult;
import ru.citeck.ecos.action.group.ActionResults;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecordsServiceJS extends AlfrescoScopableProcessorExtension {

    @Autowired
    private RecordsService recordsService;

    private static ValueConverter converter = new ValueConverter();
    private static ObjectMapper objectMapper = new ObjectMapper();

    public ActionResult<RecordRef>[] executeAction(Object nodes,
                                                   Object config) {

        Collection<RecordRef> records = toRecords(nodes);
        GroupActionConfig actionConfig = convertConfig(config, GroupActionConfig.class);

        return toArray(recordsService.executeAction(records, actionConfig));
    }

    public Iterable<RecordRef> getIterableRecords(Object recordsQuery) {
        RecordsQuery convertedQuery = convertConfig(recordsQuery, RecordsQuery.class);
        return recordsService.getIterableRecords(convertedQuery);
    }

    public Iterable<RecordRef> getIterableRecords(String sourceId, Object recordsQuery) {
        RecordsQuery convertedQuery = convertConfig(recordsQuery, RecordsQuery.class);
        return recordsService.getIterableRecords(sourceId, convertedQuery);
    }

    public Collection<RecordRef> toRecords(Object nodes) {
        Object jNodes = converter.convertValueForJava(nodes);
        final List<RecordRef> resultList = new ArrayList<>();
        if (jNodes instanceof List) {
            for (Object obj : (List) jNodes) {
                resultList.add(JavaScriptImplUtils.getRecordRef(obj));
            }
        } else if (jNodes instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jNodes;
            for (int i = 0; i < jsonArray.length(); i++) {
                resultList.add(JavaScriptImplUtils.getRecordRef(jsonArray.opt(i)));
            }
        } else if (jNodes instanceof Iterable) {
            @SuppressWarnings("unchecked")
            Iterable<Object> iterableNodes = (Iterable<Object>) jNodes;
            iterableNodes.forEach(r -> resultList.add(JavaScriptImplUtils.getRecordRef(r)));
        }
        return resultList;
    }

    private static <T> ActionResult<T>[] toArray(ActionResults<T> results) {
        @SuppressWarnings("unchecked")
        ActionResult<T>[] result = new ActionResult[results.getResults().size()];
        return results.getResults().toArray(result);
    }

    private static <T> T convertConfig(Object config, Class<T> type) {
        if (config == null) {
            return null;
        }
        Object configObj = converter.convertValueForJava(config);
        return objectMapper.convertValue(configObj, type);
    }

    @Autowired
    public void setRecordsService(RecordsService recordsService) {
        this.recordsService = recordsService;
    }
}
