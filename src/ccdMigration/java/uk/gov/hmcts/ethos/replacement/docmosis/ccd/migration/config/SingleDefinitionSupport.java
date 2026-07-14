package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import uk.gov.hmcts.ccd.sdk.api.CaseType;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.DisplayContext;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.Jurisdiction;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesSingleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesSingleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandSingleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandSingleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.SingleComplexTypes;
import uk.gov.hmcts.et.common.model.ccd.SingleRole;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.AccessProfileSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.CaseAuthSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.ComplexFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.GrantSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.RoleSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.StandaloneComplexSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.ACCESS_PROFILES;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.APPLICABLE_ROLES;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.CASE_AUTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.COMPLEX_FIELDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EVENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EVENT_FIELDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EVENT_GRANTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.STATE_GRANTS;

@SuppressWarnings({
    "checkstyle:NeedBraces",
    "checkstyle:RightCurlyAlone",
    "PMD.ControlStatementBraces",
    "PMD.ShortMethodName",
    "PMD.UnusedPrivateField"
})
final class SingleDefinitionSupport {
    private SingleDefinitionSupport() {}

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

    static void configure(ConfigBuilder<CaseData, CaseState, SingleRole> builder, Variant variant) {
        String caseType = variant.scotland ? "ET_Scotland" : "ET_EnglandWales";
        builder.caseType(
                CaseType.builder()
                        .id(caseType)
                        .name(
                                variant.scotland
                                        ? "Scotland - Singles (RET) "
                                        : "Eng/Wales - Singles")
                        .description(
                                variant.scotland
                                        ? "Scotland - Singles (RET)"
                                        : "England/Wales - Singles")
                        .liveFrom(LocalDate.of(2017, 1, 1))
                        .printableDocumentsUrl(
                                "${CCD_DEF_URL}/callback/jurisdictions/EMPLOYMENT/case-types/"
                                        + caseType
                                        + "/documents")
                        .enableForDeletion(true)
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
        configureRoles(builder, variant);
        builder.registerComplexTypes(SingleComplexTypes.all());
        registerFixedLists(builder, variant);
        configureStates(builder, variant);
        configureAccessProfiles(builder, variant);
        configureEvents(builder, variant, EVENTS, EVENT_FIELDS, COMPLEX_FIELDS, EVENT_GRANTS);
        configureEvents(
                builder,
                variant,
                SingleEt1ClaimIntakeRows.EVENTS,
                SingleEt1ClaimIntakeRows.EVENT_FIELDS,
                SingleEt1ClaimIntakeRows.COMPLEX_FIELDS,
                SingleEt1ClaimIntakeRows.EVENT_GRANTS);
        configureEvents(
                builder,
                variant,
                SingleEt3ResponseIntakeRows.EVENTS,
                SingleEt3ResponseIntakeRows.EVENT_FIELDS,
                SingleEt3ResponseIntakeRows.COMPLEX_FIELDS,
                SingleEt3ResponseIntakeRows.EVENT_GRANTS);
        configureEvents(
                builder,
                variant,
                SingleEt3ProcessingRows.EVENTS,
                SingleEt3ProcessingRows.EVENT_FIELDS,
                SingleEt3ProcessingRows.COMPLEX_FIELDS,
                SingleEt3ProcessingRows.EVENT_GRANTS);
        SingleEt3ProcessingRows.configureComplexTypeAccess(builder);
        configureEvents(
                builder,
                variant,
                SingleHearingManagementRows.EVENTS,
                SingleHearingManagementRows.EVENT_FIELDS,
                SingleHearingManagementRows.COMPLEX_FIELDS,
                SingleHearingManagementRows.EVENT_GRANTS);
        SingleHearingManagementRows.configureComplexTypeAccess(builder, variant);
        configureEvents(
                builder,
                variant,
                SingleHearingDocumentsRows.EVENTS,
                SingleHearingDocumentsRows.EVENT_FIELDS,
                SingleHearingDocumentsRows.COMPLEX_FIELDS,
                SingleHearingDocumentsRows.STANDALONE_COMPLEX_FIELDS,
                SingleHearingDocumentsRows.EVENT_GRANTS);
        configureEvents(
                builder,
                variant,
                SingleCorrespondenceDocumentsRows.EVENTS,
                SingleCorrespondenceDocumentsRows.EVENT_FIELDS,
                SingleCorrespondenceDocumentsRows.COMPLEX_FIELDS,
                SingleCorrespondenceDocumentsRows.EVENT_GRANTS);
    }

    private static Class<?> profile(Variant variant) {
        return switch (variant) {
            case CFTLIB_ENGLAND_WALES -> EnglandWalesSingleCftlibDefinition.class;
            case CFTLIB_SCOTLAND -> ScotlandSingleCftlibDefinition.class;
            case PROD_ENGLAND_WALES -> EnglandWalesSingleProdDefinition.class;
            case PROD_SCOTLAND -> ScotlandSingleProdDefinition.class;
        };
    }

    private static void configureRoles(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder, Variant variant) {
        Set<SingleRole> applicable = EnumSet.noneOf(SingleRole.class);
        Set<SingleRole> caseAuthorised = EnumSet.noneOf(SingleRole.class);
        Set<SingleRole> retained = EnumSet.noneOf(SingleRole.class);
        for (RoleSpec spec : APPLICABLE_ROLES)
            if (variant.includes(spec.mask())) applicable.add(spec.role());
        for (CaseAuthSpec spec : CASE_AUTH)
            if (variant.includes(spec.mask())) {
                caseAuthorised.add(spec.role());
                if (spec.liveFrom()) retained.add(spec.role());
            }
        builder.applicableRoles(applicable.toArray(SingleRole[]::new));
        builder.omitCaseTypeAuthorisation(
                applicable.stream()
                        .filter(role -> !caseAuthorised.contains(role))
                        .toArray(SingleRole[]::new));
        builder.includeCaseRolesInCaseTypeAuthorisation(
                caseAuthorised.stream()
                        .filter(role -> role.getRole().startsWith("["))
                        .toArray(SingleRole[]::new));
        builder.retainCaseTypeAuthorisationLiveFrom(retained.toArray(SingleRole[]::new));
    }

    private static void registerFixedLists(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder, Variant variant) {
        switch (variant) {
            case CFTLIB_ENGLAND_WALES ->
                    SingleFixedLists.registerEnglandWalesSingleCftlibDefinition(builder);
            case CFTLIB_SCOTLAND ->
                    SingleFixedLists.registerScotlandSingleCftlibDefinition(builder);
            case PROD_ENGLAND_WALES ->
                    SingleFixedLists.registerEnglandWalesSingleProdDefinition(builder);
            case PROD_SCOTLAND -> SingleFixedLists.registerScotlandSingleProdDefinition(builder);
            default -> throw new IllegalStateException("Unsupported variant " + variant);
        }
    }

    private static void configureStates(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder, Variant variant) {
        for (GrantSpec grant : STATE_GRANTS)
            if (variant.includes(grant.mask())) {
                builder.grant(
                        CaseState.valueOf(grant.id()), permissions(grant.crud()), grant.role());
            }
    }

    private static void configureAccessProfiles(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder, Variant variant) {
        for (AccessProfileSpec spec : ACCESS_PROFILES) {
            if (!variant.includes(spec.mask())) continue;
            var profile = builder.caseRoleToAccessProfile(spec.role());
            if (spec.legacy()) profile.legacyIdamRole();
            if (spec.readonly()) profile.readonly();
            profile.omitDisabled().accessProfiles(spec.accessProfiles().split(","));
            if (spec.liveFrom() != null) profile.liveFrom(LocalDate.parse(spec.liveFrom()));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureEvents(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder,
            Variant variant,
            EventSpec[] events,
            EventFieldSpec[] eventFields,
            ComplexFieldSpec[] complexFields,
            GrantSpec... eventGrants) {
        configureEvents(
                builder,
                variant,
                events,
                eventFields,
                complexFields,
                new StandaloneComplexSpec[0],
                eventGrants);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureEvents(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder,
            Variant variant,
            EventSpec[] events,
            EventFieldSpec[] eventFields,
            ComplexFieldSpec[] complexFields,
            StandaloneComplexSpec[] standaloneComplexFields,
            GrantSpec... eventGrants) {
        for (EventSpec spec : events) {
            if (!variant.includes(spec.mask())) continue;
            Event.EventBuilder<CaseData, SingleRole, CaseState> event = event(builder, spec);
            event.name(spec.name())
                    .description(spec.description())
                    .explicitGrants()
                    .omitStateAuthorisationInference();
            if (spec.displayOrder() == null) event.omitDisplayOrder();
            else event.displayOrder(spec.displayOrder());
            applyEventMetadata(event, spec);
            for (GrantSpec grant : eventGrants)
                if (variant.includes(grant.mask()) && grant.id().equals(spec.id())) {
                    event.grant(permissions(grant.crud()), grant.role());
                }
            configureEventFields(
                    event,
                    spec.id(),
                    variant,
                    eventFields,
                    complexFields,
                    standaloneComplexFields);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Event.EventBuilder<CaseData, SingleRole, CaseState> event(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder, EventSpec spec) {
        Event.EventBuilder<CaseData, SingleRole, CaseState> result;
        if (spec.preState() == null) {
            result = builder.event(spec.id()).initialState(CaseState.valueOf(spec.postState()));
        } else if ("*".equals(spec.preState())) {
            result = builder.event(spec.id()).forAllStates();
        } else {
            EnumSet<CaseState> preStates = states(spec.preState());
            if ("*".equals(spec.postState()) || spec.postState().contains("(")) {
                result = builder.event(spec.id()).forStates(preStates);
            } else if (preStates.size() == 1
                    && preStates.iterator().next().name().equals(spec.postState())) {
                result = builder.event(spec.id()).forStates(preStates);
            } else {
                result =
                        builder.event(spec.id())
                                .forStateTransition(preStates, CaseState.valueOf(spec.postState()));
            }
        }
        if ("*".equals(spec.postState())) result.postStateWildcard();
        else if (spec.postState().contains("(")) result.postStateExpression(spec.postState());
        if (spec.preState() != null && !"*".equals(spec.preState())) {
            result.preStateOrder(
                    Arrays.stream(spec.preState().split(";"))
                            .map(CaseState::valueOf)
                            .toArray(CaseState[]::new));
        }
        return result;
    }

    private static EnumSet<CaseState> states(String value) {
        EnumSet<CaseState> result = EnumSet.noneOf(CaseState.class);
        for (String state : value.split(";")) {
            result.add(CaseState.valueOf(state));
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private static void applyEventMetadata(
            Event.EventBuilder<CaseData, SingleRole, CaseState> event, EventSpec spec) {
        if (spec.condition() != null) event.showCondition(spec.condition());
        yn(spec.showSummary(), event::showSummary, event::omitShowSummary);
        yn(spec.showNotes(), event::showEventNotes, event::omitShowEventNotes);
        yn(spec.publish(), event::publishToCamunda, event::omitPublish);
        if (spec.endButtonLabel() == null) event.omitEndButtonLabel();
        else event.endButtonLabel(spec.endButtonLabel());
        if (spec.significant()) event.significantEvent();
        if (spec.ttl() != null) event.ttlIncrement(spec.ttl());
        callback(event, Webhook.AboutToStart, spec.aboutToStart());
        callback(event, Webhook.AboutToSubmit, spec.aboutToSubmit());
        callback(event, Webhook.Submitted, spec.submitted());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureEventFields(
            Event.EventBuilder<CaseData, SingleRole, CaseState> event,
            String eventId,
            Variant variant,
            EventFieldSpec[] eventFields,
            ComplexFieldSpec[] complexFields,
            StandaloneComplexSpec... standaloneComplexFields) {
        FieldCollection.FieldCollectionBuilder fields = event.fields();
        for (EventFieldSpec spec : eventFields) {
            if (!variant.includes(spec.mask()) || !spec.eventId().equals(eventId)) continue;
            Field.FieldBuilder field =
                    fields.field(spec.fieldId())
                            .context(context(spec.context()))
                            .page(spec.pageId());
            if (spec.pageOrder() != null) field.pageDisplayOrder(spec.pageOrder());
            if (spec.fieldOrder() != null) {
                field.pageFieldDisplayOrder(spec.fieldOrder());
            } else {
                field.omitPageFieldDisplayOrder();
            }
            if (!spec.pageColumn()) field.omitPageColumnNumber();
            if (spec.showSummary() != null)
                field.showSummaryChangeOption("Y".equals(spec.showSummary()));
            if (spec.condition() != null) field.showCondition(spec.condition());
            if (spec.retainHidden() != null) field.retainHiddenValue(spec.retainHidden());
            if (spec.pageCondition() != null) field.pageShowCondition(spec.pageCondition());
            if (spec.pageLabel() != null) field.pageLabel(spec.pageLabel());
            if (spec.midEvent() != null) field.externalMidEventCallbackUrl(spec.midEvent());
            if ("Y".equals(spec.publish())) {
                field.publish();
            } else if ("N".equals(spec.publish())) {
                field.doNotPublish();
            } else {
                field.omitPublish();
            }
            configureComplexFields(field, eventId, spec.fieldId(), variant, complexFields);
        }
        for (StandaloneComplexSpec spec : standaloneComplexFields) {
            if (!variant.includes(spec.mask()) || !spec.eventId().equals(eventId)) continue;
            var complex = fields.complexWithoutEventField(spec.getter(), spec.complexType());
            configureComplexFields(
                    complex,
                    eventId,
                    complex.build().getRootFieldname(),
                    variant,
                    complexFields);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureComplexFields(
            Field.FieldBuilder root,
            String eventId,
            String caseFieldId,
            Variant variant,
            ComplexFieldSpec... complexFields) {
        var specs =
                Arrays.stream(complexFields)
                        .filter(
                                spec ->
                                        variant.includes(spec.mask())
                                                && spec.eventId().equals(eventId)
                                                && spec.caseFieldId().equals(caseFieldId))
                        .toList();
        if (specs.isEmpty()) return;
        FieldCollection.FieldCollectionBuilder complex =
                root.complex(SingleComplexTypes.BundleType.class);
        configureComplexFields(complex, specs);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureComplexFields(
            FieldCollection.FieldCollectionBuilder complex,
            String eventId,
            String caseFieldId,
            Variant variant,
            ComplexFieldSpec... complexFields) {
        var specs =
                Arrays.stream(complexFields)
                        .filter(
                                spec ->
                                        variant.includes(spec.mask())
                                                && spec.eventId().equals(eventId)
                                                && spec.caseFieldId().equals(caseFieldId))
                        .toList();
        configureComplexFields(complex, specs);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void configureComplexFields(
            FieldCollection.FieldCollectionBuilder complex, List<ComplexFieldSpec> specs) {
        if (specs.isEmpty()) return;
        if (specs.getFirst().rowId() != null)
            complex.eventToComplexTypeId(specs.getFirst().rowId());
        for (ComplexFieldSpec spec : specs) {
            Field.FieldBuilder field =
                    complex.field(spec.elementCode())
                            .context(context(spec.context()))
                            .fieldDisplayOrder(spec.order());
            if (spec.label() != null) field.caseEventFieldLabel(spec.label());
            if (spec.hint() != null) field.caseEventFieldHint(spec.hint());
            if (spec.condition() != null) field.showCondition(spec.condition());
            if (spec.retainHidden() != null) field.retainHiddenValue(spec.retainHidden());
            if (spec.defaultValue() != null) field.defaultValue(spec.defaultValue());
            if ("Y".equals(spec.publish())) field.publish();
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
    private static void callback(
            Event.EventBuilder<CaseData, SingleRole, CaseState> event, Webhook hook, String url) {
        if (url != null) event.externalCallbackUrl(hook, url);
    }

    private static Set<Permission> permissions(String crud) {
        EnumSet<Permission> result = EnumSet.noneOf(Permission.class);
        for (char permission : crud.toCharArray())
            result.add(Permission.valueOf(String.valueOf(permission)));
        return result;
    }

    private static void yn(String value, Runnable yes, Runnable omitted) {
        if ("Y".equals(value)) yes.run();
        else if (value == null) omitted.run();
    }
}
