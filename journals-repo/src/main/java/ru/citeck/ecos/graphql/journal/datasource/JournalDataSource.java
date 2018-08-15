package ru.citeck.ecos.graphql.journal.datasource;

import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.JGqlPageInfoInput;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeInfo;
import ru.citeck.ecos.graphql.journal.record.JGqlAttributeValue;
import ru.citeck.ecos.graphql.journal.record.JGqlRecordsConnection;
import ru.citeck.ecos.journals.records.RecordsResult;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface JournalDataSource {

    JGqlRecordsConnection getRecords(GqlContext context,
                                     String query,
                                     String language,
                                     JGqlPageInfoInput pageInfo);

    RecordsResult getIds(GqlContext context,
                         String query,
                         String language,
                         JGqlPageInfoInput pageInfo);

    List<JGqlAttributeValue> getMetadata(GqlContext context,
                                         List<RemoteRef> remoteRefList);

    default boolean isSupportsSplitLoading() {
        return false;
    }

    Optional<JGqlAttributeInfo> getAttributeInfo(String attributeName);

    default List<String> getDefaultAttributes() {
        return Collections.emptyList();
    }
}
