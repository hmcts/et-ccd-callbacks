package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import uk.gov.hmcts.ccd.sdk.api.CaseType;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.Jurisdiction;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesMultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesMultipleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.MultipleComplexTypes;
import uk.gov.hmcts.et.common.model.ccd.MultipleRole;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleProdDefinition;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.MultipleCaseState;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.ACCESS_PROFILES;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.CATEGORIES;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.COMPLEX_FIELDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.EVENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.EVENT_FIELDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.EVENT_GRANTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.PUBLISH_FIELDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.SEARCH_FIELDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.STATE_GRANTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.MultipleDefinitionRows.TABS;

final class MultipleDefinitionSupport {

    private MultipleDefinitionSupport() {
    }

    enum Variant {
        CFTLIB_ENGLAND_WALES(1, false, false),
        CFTLIB_SCOTLAND(2, true, false),
        PROD_ENGLAND_WALES(4, false, true),
        PROD_SCOTLAND(8, true, true);

        private final int bit;
        private final boolean scotland;
        private final boolean prod;

        Variant(int bit, boolean scotland, boolean prod) {
            this.bit = bit;
            this.scotland = scotland;
            this.prod = prod;
        }

        boolean includes(int mask) {
            return (mask & bit) != 0;
        }
    }

    static void configure(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        String caseType = variant.scotland ? "ET_Scotland_Multiple" : "ET_EnglandWales_Multiple";
        String name = variant.scotland ? "Scotland - Multiples (RET)" : "Eng/Wales - Multiples";
        String description = variant.scotland ? name : "England/Wales - Multiples";
        builder.caseType(
                CaseType.builder()
                        .id(caseType)
                        .name(name)
                        .description(description)
                        .liveFrom(LocalDate.of(2017, 1, 1))
                        .printableDocumentsUrl(
                                "${CCD_DEF_URL}/callback/jurisdictions/EMPLOYMENT/"
                                        + "case-types/"
                                        + caseType
                                        + "/documents")
                        .enableForDeletion(false)
                        .build());
        builder.jurisdiction(
                Jurisdiction.builder()
                        .id("EMPLOYMENT")
                        .name("Employment")
                        .description("Employment")
                        .shuttered(true)
                        .build());
        builder.omitDefaultLiveFrom();
        builder.omitCaseHistory();
        builder.legacyCaseAuthorisationIdColumn();
        builder.retainCaseRoleLiveFrom();
        builder.omitFieldAuthorisationInference();
        builder.schemaProfile(profile(variant));
        builder.applicableRoles(MultipleRole.values());
        omitFieldOnlyCaseTypeRoles(builder, variant);
        builder.registerComplexTypes(MultipleComplexTypes.all());
        registerFixedLists(builder, variant);
        configureStates(builder, variant);
        configureAccessProfiles(builder, variant);
        configureEvents(builder, variant);
        configureTabs(builder, variant);
        configureCategories(builder, variant);
        configureSearch(builder, variant);
    }

    private static Class<?> profile(Variant variant) {
        return switch (variant) {
            case CFTLIB_ENGLAND_WALES -> EnglandWalesMultipleCftlibDefinition.class;
            case CFTLIB_SCOTLAND -> ScotlandMultipleCftlibDefinition.class;
            case PROD_ENGLAND_WALES -> EnglandWalesMultipleProdDefinition.class;
            case PROD_SCOTLAND -> ScotlandMultipleProdDefinition.class;
        };
    }

    private static void omitFieldOnlyCaseTypeRoles(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        builder.omitCaseTypeAuthorisation(
                MultipleRole.EMPLOYMENT_CASEWORKER,
                MultipleRole.EMPLOYMENT_JUDGE,
                MultipleRole.CITIZEN,
                MultipleRole.ACAS_API,
                MultipleRole.GS_PROFILE,
                variant.scotland
                        ? MultipleRole.ENGLAND_WALES_CASEWORKER
                        : MultipleRole.SCOTLAND_CASEWORKER,
                variant.scotland ? MultipleRole.ENGLAND_WALES_JUDGE : MultipleRole.SCOTLAND_JUDGE);
        if (variant.prod) {
            builder.omitCaseTypeAuthorisation(MultipleRole.LEGAL_REP);
        }
    }

    private static void registerFixedLists(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        switch (variant) {
            case CFTLIB_ENGLAND_WALES -> MultipleFixedLists.registerCftlibEnglandWales(builder);
            case CFTLIB_SCOTLAND -> MultipleFixedLists.registerCftlibScotland(builder);
            case PROD_ENGLAND_WALES -> MultipleFixedLists.registerProdEnglandWales(builder);
            case PROD_SCOTLAND -> MultipleFixedLists.registerProdScotland(builder);
            default -> throw new IllegalStateException("Unsupported variant " + variant);
        }
    }

    private static void configureStates(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        for (var grant : STATE_GRANTS) {
            if (variant.includes(grant.mask())) {
                builder.grant(
                        MultipleCaseState.valueOf(grant.id()),
                        permissions(grant.crud()),
                        grant.role());
            }
        }
    }

    private static void configureAccessProfiles(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        for (var spec : ACCESS_PROFILES) {
            if (!variant.includes(spec.mask())) {
                continue;
            }
            var profile = builder.caseRoleToAccessProfile(spec.role());
            if (spec.legacy()) {
                profile.legacyIdamRole();
            }
            profile.retainLiveFrom()
                    .omitDisabled()
                    .accessProfiles(spec.accessProfiles().split(","));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureEvents(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        for (var spec : EVENTS) {
            if (!variant.includes(spec.mask())) {
                continue;
            }
            Event.EventBuilder event = event(builder, spec);
            event.name(spec.name())
                    .description(spec.description())
                    .explicitGrants()
                    .omitStateAuthorisationInference();
            if (spec.displayOrder() == null) {
                event.omitDisplayOrder();
            } else {
                event.displayOrder(spec.displayOrder());
            }
            applyEventMetadata(event, spec);
            for (var grant : EVENT_GRANTS) {
                if (variant.includes(grant.mask()) && grant.id().equals(spec.id())) {
                    event.grant(permissions(grant.crud()), grant.role());
                }
            }
            configureEventFields(event, spec.id(), variant);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Event.EventBuilder event(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder,
            MultipleDefinitionRows.EventSpec spec) {
        Event.EventBuilder result;
        if (spec.preState() == null) {
            result =
                    builder.event(spec.id())
                            .initialState(MultipleCaseState.valueOf(spec.postState()));
        } else if ("*".equals(spec.preState())) {
            result = builder.event(spec.id()).forAllStates();
        } else if (spec.preState().equals(spec.postState())) {
            result = builder.event(spec.id()).forState(MultipleCaseState.valueOf(spec.preState()));
        } else {
            String target = "*".equals(spec.postState()) ? spec.preState() : spec.postState();
            result =
                    builder.event(spec.id())
                            .forStateTransition(
                                    MultipleCaseState.valueOf(spec.preState()),
                                    MultipleCaseState.valueOf(target));
        }
        if ("*".equals(spec.postState())) {
            result.postStateWildcard();
        } else if (spec.postState().contains("(")) {
            result.postStateExpression(spec.postState());
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private static void applyEventMetadata(
            Event.EventBuilder event, MultipleDefinitionRows.EventSpec spec) {
        if (spec.condition() != null) {
            event.showCondition(spec.condition());
        }
        if (spec.showSummary() == null) {
            event.omitShowSummary();
        } else if (spec.showSummary()) {
            event.showSummary();
        }
        if (spec.showNotes() == null) {
            event.omitShowEventNotes();
        } else if (spec.showNotes()) {
            event.showEventNotes();
        }
        if (spec.publish() == null) {
            event.omitPublish();
        } else if (spec.publish()) {
            event.publishToCamunda();
        }
        if (spec.endButton() == null) {
            event.omitEndButtonLabel();
        } else {
            event.endButtonLabel(spec.endButton());
        }
        callback(event, Webhook.AboutToStart, spec.aboutToStart());
        callback(event, Webhook.AboutToSubmit, spec.aboutToSubmit());
        callback(event, Webhook.Submitted, spec.submitted());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureEventFields(
            Event.EventBuilder event, String eventId, Variant variant) {
        FieldCollection.FieldCollectionBuilder fields = event.fields();
        for (var spec : EVENT_FIELDS) {
            if (!variant.includes(spec.mask()) || !spec.eventId().equals(eventId)) {
                continue;
            }
            Field.FieldBuilder field =
                    fields.field(spec.fieldId())
                            .context(context(spec.context()))
                            .page(spec.pageId())
                            .pageDisplayOrder(spec.pageDisplayOrder())
                            .pageFieldDisplayOrder(spec.pageFieldDisplayOrder());
            if (!spec.pageColumn()) {
                field.omitPageColumnNumber();
            }
            if (spec.showSummary() != null) {
                field.showSummaryChangeOption(spec.showSummary());
            }
            if (spec.condition() != null) {
                field.showCondition(spec.condition());
            }
            if (spec.retainHidden() != null) {
                field.retainHiddenValue(spec.retainHidden());
            }
            if (spec.midEvent() != null) {
                field.externalMidEventCallbackUrl(spec.midEvent());
            }
            if (spec.pageCondition() != null) {
                field.pageShowCondition(spec.pageCondition());
            }
            if (spec.pageLabel() != null) {
                field.pageLabel(spec.pageLabel());
            }
            applyPublish(field, spec, variant);
            configureComplexFields(field, eventId, spec.fieldId(), variant);
        }
    }

    @SuppressWarnings("rawtypes")
    private static void applyPublish(
            Field.FieldBuilder field,
            MultipleDefinitionRows.EventFieldSpec eventField,
            Variant variant) {
        for (var publish : PUBLISH_FIELDS) {
            if (variant.includes(publish.mask())
                    && publish.eventId().equals(eventField.eventId())
                    && publish.fieldId().equals(eventField.fieldId())
                    && publish.pageId().equals(eventField.pageId())
                    && publish.fieldOrder().equals(eventField.pageFieldDisplayOrder())) {
                if (publish.value() == null) {
                    field.omitPublish();
                } else if ("Y".equals(publish.value())) {
                    field.publish();
                } else {
                    field.doNotPublish();
                }
                return;
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureComplexFields(
            Field.FieldBuilder root, String eventId, String caseFieldId, Variant variant) {
        var specs =
                Arrays.stream(COMPLEX_FIELDS)
                        .filter(
                                spec ->
                                        variant.includes(spec.mask())
                                                && spec.eventId().equals(eventId)
                                                && spec.caseFieldId().equals(caseFieldId))
                        .toList();
        if (specs.isEmpty()) {
            return;
        }
        FieldCollection.FieldCollectionBuilder complex =
                root.complex(MultipleComplexTypes.BundleType.class);
        if (specs.getFirst().rowId() != null) {
            complex.eventToComplexTypeId(specs.getFirst().rowId());
        }
        for (var spec : specs) {
            Field.FieldBuilder field =
                    complex.field(spec.elementCode())
                            .context(context(spec.context()))
                            .fieldDisplayOrder(spec.order());
            if (spec.label() != null) {
                field.caseEventFieldLabel(spec.label());
            }
            if (spec.hint() != null) {
                field.caseEventFieldHint(spec.hint());
            }
            if (spec.condition() != null) {
                field.showCondition(spec.condition());
            }
            if (spec.retainHidden() != null) {
                field.retainHiddenValue(spec.retainHidden());
            }
            if (spec.defaultValue() != null) {
                field.defaultValue(spec.defaultValue());
            }
            if (Boolean.TRUE.equals(spec.publish())) {
                field.publish();
            }
        }
    }

    private static DisplayContext context(String value) {
        return switch (value) {
            case "READONLY" -> DisplayContext.ReadOnly;
            case "MANDATORY" -> DisplayContext.Mandatory;
            case "OPTIONAL" -> DisplayContext.Optional;
            default -> DisplayContext.Complex;
        };
    }

    @SuppressWarnings("rawtypes")
    private static void callback(Event.EventBuilder event, Webhook hook, String url) {
        if (url != null) {
            event.externalCallbackUrl(hook, url);
        }
    }

    private static Set<Permission> permissions(String crud) {
        EnumSet<Permission> result = EnumSet.noneOf(Permission.class);
        for (char permission : crud.toCharArray()) {
            result.add(Permission.valueOf(String.valueOf(permission)));
        }
        return result;
    }

    private static void configureTabs(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        Map<String, Tab.TabBuilder<MultipleData, MultipleRole>> tabs = new LinkedHashMap<>();
        for (var spec : TABS) {
            if (!variant.includes(spec.mask())) {
                continue;
            }
            var tab =
                    tabs.computeIfAbsent(
                            spec.tabId(), ignored -> createTab(builder, variant, spec));
            if (spec.channel() == null) {
                tab.withoutChannel();
            }
            tab.field(
                    spec.fieldId(),
                    spec.condition(),
                    spec.fieldOrder(),
                    spec.displayContext(),
                    spec.label(),
                    spec.tabOrder());
        }
    }

    private static Tab.TabBuilder<MultipleData, MultipleRole> createTab(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder,
            Variant variant,
            MultipleDefinitionRows.TabSpec first) {
        var tab = builder.tab(first.tabId(), first.label()).displayOrder(first.tabOrder());
        long rowsWithMetadata =
                Arrays.stream(TABS)
                        .filter(
                                spec ->
                                        variant.includes(spec.mask())
                                                && spec.tabId().equals(first.tabId()))
                        .filter(spec -> spec.label() != null)
                        .count();
        if (rowsWithMetadata == 1) {
            tab.metadataOnFirstFieldOnly();
        }
        return tab;
    }

    private static void configureCategories(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        for (var spec : CATEGORIES) {
            if (variant.includes(spec.mask())) {
                builder.categories(MultipleRole.EMPLOYMENT_API)
                        .categoryID(spec.id())
                        .categoryLabel(spec.label())
                        .displayOrder(spec.order())
                        .parentCategoryID(spec.parent());
            }
        }
    }

    private static void configureSearch(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder, Variant variant) {
        for (var spec : SEARCH_FIELDS) {
            if (!variant.includes(spec.mask())) {
                continue;
            }
            switch (spec.kind()) {
                case SEARCH_INPUT ->
                        builder.searchInputFields().field(spec.fieldId(), spec.label());
                case SEARCH_RESULT ->
                        builder.searchResultFields().field(spec.fieldId(), spec.label());
                case WORK_INPUT ->
                        builder.workBasketInputFields().field(spec.fieldId(), spec.label());
                case WORK_RESULT ->
                        builder.workBasketResultFields().field(spec.fieldId(), spec.label());
                default -> throw new IllegalStateException("Unsupported search kind " + spec.kind());
            }
        }
    }
}
