package ru.citeck.ecos.flowable.form.view;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.flowable.form.model.FormField;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.flowable.form.FlowableNodeViewProvider;
import ru.citeck.ecos.invariants.Feature;
import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantScope;
import ru.citeck.ecos.invariants.view.NodeField;
import ru.citeck.ecos.invariants.view.NodeViewRegion;
import ru.citeck.ecos.service.namespace.EcosNsPrefixResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class FieldConverter<T extends FormField> {

    @Autowired
    protected EcosNsPrefixResolver prefixResolver;
    @Autowired
    protected DictionaryService dictionaryService;

    public NodeField convertField(T field) {

        NodeField.Builder fieldBuilder = new NodeField.Builder(prefixResolver);

        List<NodeViewRegion> regions = new ArrayList<>();
        createMandatoryRegion(field).ifPresent(regions::add);
        createLabelRegion(field).ifPresent(regions::add);
        createInputRegion(field).ifPresent(regions::add);
        createSelectRegion(field).ifPresent(regions::add);

        QName fieldName = QName.createQName(FlowableNodeViewProvider.FLOWABLE_DEFAULT_NS_URI, field.getId());

        QName datatype = getDataType();
        DataTypeDefinition typeDefinition = dictionaryService.getDataType(datatype);

        fieldBuilder.property(fieldName);
        fieldBuilder.template("row");
        fieldBuilder.datatype(datatype);
        fieldBuilder.javaclass(typeDefinition.getJavaClassName());
        fieldBuilder.invariants(getInvariants(field, fieldName));
        fieldBuilder.regions(regions);

        return fieldBuilder.build();
    }

    public abstract String getSupportedFieldType();

    protected Optional<NodeViewRegion> createMandatoryRegion(T field) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("mandatory")
                                             .template("mandatory")
                                             .build());
    }

    protected Optional<NodeViewRegion> createLabelRegion(T field) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("label")
                                             .template("label")
                                             .templateParams(Collections.singletonMap("text", field.getName()))
                                             .build());
    }

    protected Optional<NodeViewRegion> createInputRegion(T field) {
        return Optional.of(new NodeViewRegion.Builder(prefixResolver)
                                             .name("input")
                                             .template("text")
                                             .build());
    }

    protected Optional<NodeViewRegion> createSelectRegion(T field) {
        return Optional.empty();
    }

    protected List<InvariantDefinition> getInvariants(T field, QName fieldName) {

        List<InvariantDefinition> invariants = new ArrayList<>();

        InvariantDefinition.Builder invBuilder = new InvariantDefinition.Builder(prefixResolver);
        invBuilder.pushScope(fieldName, InvariantScope.AttributeScopeKind.PROPERTY);

        if (field.isRequired()) {
            invariants.add(invBuilder.feature(Feature.MANDATORY)
                                     .explicit(true)
                                     .build());
        }

        if (field.isReadOnly()) {
            invariants.add(invBuilder.feature(Feature.PROTECTED)
                                     .explicit(true)
                                     .build());
        }

        if (field.getValue() != null) {
            invariants.add(invBuilder.feature(Feature.DEFAULT)
                                     .explicit(field.getValue())
                                     .build());
        }

        return invariants;
    }

    protected abstract QName getDataType();

}
