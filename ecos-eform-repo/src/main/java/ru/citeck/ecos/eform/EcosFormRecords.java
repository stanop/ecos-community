package ru.citeck.ecos.eform;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.eform.model.EcosFormModel;
import ru.citeck.ecos.eform.provider.RepoFormProvider;
import ru.citeck.ecos.node.DisplayNameService;
import ru.citeck.ecos.records.source.alf.AlfDictionaryRecords;
import ru.citeck.ecos.records.source.alf.AlfNodesRecordsDAO;
import ru.citeck.ecos.records.source.alf.search.CriteriaAlfNodesSearch;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.query.RecordsQuery;
import ru.citeck.ecos.records2.request.query.RecordsQueryResult;
import ru.citeck.ecos.records2.source.dao.local.CrudRecordsDAO;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EcosFormRecords extends CrudRecordsDAO<EcosFormModel> {

    public static final String ID = "eform";

    private static final String ECOS_FORM_KEY = "ECOS_FORM";

    private static final RecordRef DEFAULT_FORM_ID = RecordRef.create(ID , "DEFAULT");
    private static final RecordRef ECOS_FORM_ID = RecordRef.create(ID , "ECOS_FORM");

    private static final Set<RecordRef> SYSTEM_FORMS = new HashSet<>(Arrays.asList(DEFAULT_FORM_ID, ECOS_FORM_ID));

    private AlfNodesRecordsDAO alfNodesRecordsDAO;
    private EcosFormService eformFormService;

    private RepoFormProvider repoFormProvider;

    @Autowired
    public EcosFormRecords(EcosFormService eformFormService,
                           AlfNodesRecordsDAO alfNodesRecordsDAO,
                           RepoFormProvider repoFormProvider) {
        setId(ID);
        this.repoFormProvider = repoFormProvider;
        this.eformFormService = eformFormService;
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
    }

    @Override
    public List<EcosFormModel> getValuesToMutate(List<RecordRef> records) {

        return records.stream().map(record -> {

            if (record.getId().isEmpty()) {
                return new EcosFormModel();
            }

            Optional<EcosFormModel> model = eformFormService.getFormById(record.getId());
            if (!model.isPresent()) {
                throw new IllegalArgumentException("Form with id " + record.getId() + " not found!");
            }

            return new EcosFormModel(model.get());

        }).collect(Collectors.toList());
    }

    @Override
    public RecordsMutResult save(List<EcosFormModel> values) {

        RecordsMutResult result = new RecordsMutResult();

        for (EcosFormModel model : values) {
            result.addRecord(new RecordMeta(eformFormService.save(model)));
        }

        return result;
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {

        List<RecordMeta> resultRecords = new ArrayList<>();

        deletion.getRecords()
                .stream()
                .filter(r -> !SYSTEM_FORMS.contains(r))
                .forEach(r -> {
                    repoFormProvider.delete(r.getId());
                    resultRecords.add(new RecordMeta(r));
                });

        RecordsDelResult result = new RecordsDelResult();
        result.setRecords(resultRecords);
        return result;
    }

    @Override
    public List<EcosFormModel> getMetaValues(List<RecordRef> records) {

        List<EcosFormModel> models = new ArrayList<>();

        for (RecordRef recordRef : records) {

            if (NodeRef.isNodeRef(recordRef.getId())) {

                Optional<EcosFormModel> model = repoFormProvider.getContentData(
                                new NodeRef(recordRef.getId())).flatMap(ContentData::getData);
                model.ifPresent(models::add);

            } else {

                if (recordRef.getId().isEmpty()) {

                    EcosFormModel form = new EcosFormModel();
                    form.setId("");
                    form.setTitle(I18NUtil.getMessage("ecosForms_model.type.ecosForms_form.title"));
                    models.add(form);

                } else {

                    EcosFormModel form = repoFormProvider.getFormById(recordRef.getId());
                    if (form != null) {
                        models.add(form);
                    }
                }
            }
        }

        return models;
    }

    @Override
    public RecordsQueryResult<EcosFormModel> getMetaValues(RecordsQuery recordsQuery) {

        String lang = recordsQuery.getLanguage();
        RecordsQueryResult<EcosFormModel> result = new RecordsQueryResult<>();

        if (lang.equals(SearchService.LANGUAGE_FTS_ALFRESCO) || lang.equals(CriteriaAlfNodesSearch.LANGUAGE)) {

            RecordsQueryResult<RecordRef> records = alfNodesRecordsDAO.queryRecords(recordsQuery);

            result.setHasMore(records.getHasMore());
            result.setTotalCount(records.getTotalCount());
            result.setRecords(getMetaValues(records.getRecords()));

            return result;
        }

        Query query = recordsQuery.getQuery(Query.class);
        Optional<EcosFormModel> form = Optional.empty();

        if (StringUtils.isNotBlank(query.formKey)) {

            form = eformFormService.getFormByKey(Arrays.stream(query.formKey.split(","))
                                                       .filter(StringUtils::isNotBlank)
                                                       .collect(Collectors.toList()));

        } else if (query.record != null) {

            if (ID.equals(query.record.getSourceId())) {

                form = eformFormService.getFormByKey(ECOS_FORM_KEY);

            } else {
                form = eformFormService.getFormByRecord(query.record, query.isViewMode);
            }
        }

        if (!form.isPresent()) {
            return result;
        }

        result.setRecords(Collections.singletonList(form.get()));
        result.setTotalCount(1);

        return result;
    }

    static class Query {
        @Getter @Setter private String formKey;
        @Getter @Setter private RecordRef record;
        @Getter @Setter private Boolean isViewMode;
    }
}