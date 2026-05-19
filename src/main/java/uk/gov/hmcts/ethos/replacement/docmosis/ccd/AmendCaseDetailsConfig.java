package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.COMPLEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.MANDATORY;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.OPTIONAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.AmendCaseDetailsConfig.FieldKind.READ_ONLY;

public abstract class AmendCaseDetailsConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_DISPLAY_ORDER = "PageDisplayOrder";
    private static final String PAGE_FIELD_DISPLAY_ORDER = "PageFieldDisplayOrder";
    private static final String RETAIN_HIDDEN_VALUE = "RetainHiddenValue";
    private static final String MULTIPLE_CASE_CONDITION = "caseType=\"Multiple\" AND multipleFlag=\"No\"";
    private static final String TTL_CONDITION =
        "additionalCaseInfo.additional_live_appeal=\"Yes\" OR additionalCaseInfo.interventionRequired=\"Yes\"";
    private static final String SINGLE_CASE_MULTIPLE_VALIDATION =
        "${ET_COS_URL}/singleCaseMultipleMidEventValidation";
    private static final String INITIALISE_AMEND_CASE_DETAILS = "${ET_COS_URL}/initialiseAmendCaseDetails";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean amendCaseDetailsShowsSummary;
    private final boolean amendCaseDetailsPublishes;
    private final boolean amendCaseDetailsHasSubmittedCallback;
    private final List<FieldSpec> amendCaseDetailsFields;
    private final List<FieldSpec> amendCaseDetailsClosedFields;

    protected AmendCaseDetailsConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean amendCaseDetailsShowsSummary,
        boolean amendCaseDetailsPublishes,
        boolean amendCaseDetailsHasSubmittedCallback,
        List<FieldSpec> amendCaseDetailsFields,
        List<FieldSpec> amendCaseDetailsClosedFields
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.amendCaseDetailsShowsSummary = amendCaseDetailsShowsSummary;
        this.amendCaseDetailsPublishes = amendCaseDetailsPublishes;
        this.amendCaseDetailsHasSubmittedCallback = amendCaseDetailsHasSubmittedCallback;
        this.amendCaseDetailsFields = amendCaseDetailsFields;
        this.amendCaseDetailsClosedFields = amendCaseDetailsClosedFields;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configureEvent(
            configBuilder,
            "amendCaseDetails",
            "Accepted;Rejected;Submitted;Vetted;Transferred",
            "*",
            amendCaseDetailsShowsSummary,
            amendCaseDetailsPublishes,
            amendCaseDetailsHasSubmittedCallback,
            amendCaseDetailsFields
        );
        configureEvent(
            configBuilder,
            "amendCaseDetailsClosed",
            "Closed",
            "Closed",
            false,
            false,
            false,
            amendCaseDetailsClosedFields
        );
    }

    private void configureEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String preConditionStates,
        String postConditionState,
        boolean showSummary,
        boolean publish,
        boolean submittedCallback,
        List<FieldSpec> fieldSpecs
    ) {
        Event.EventBuilder<T, EtUserRole, EtState> event = withFields(
            configBuilder.event(eventId)
                .forAllStates()
                .name("Case Details")
                .description("Amend Case Details")
                .displayOrder("amendCaseDetails".equals(eventId) ? 11 : 12)
                .showCondition("managingOffice !=\"Unassigned\"")
                .caseEventColumn("PreConditionState(s)", preConditionStates)
                .caseEventColumn("PostConditionState", postConditionState)
                .aboutToStartCallbackUrl(INITIALISE_AMEND_CASE_DETAILS)
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/amendCaseDetails"),
            fieldSpecs
        );

        if (showSummary) {
            event.showSummary();
        }
        if (publish) {
            event.caseEventColumn("Publish", "Y");
        }
        if (submittedCallback) {
            event.submittedCallbackUrl("");
        }

        grantRegionalCaseworkerAccess(event);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> withFields(
        Event.EventBuilder<T, EtUserRole, EtState> event,
        List<FieldSpec> fieldSpecs
    ) {
        var fields = event.fields();
        for (FieldSpec fieldSpec : fieldSpecs) {
            addField(fields, fieldSpec);
        }
        return fields.done();
    }

    private void addField(
        FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> fields,
        FieldSpec spec
    ) {
        Field.FieldBuilder<?, EtState, T, Event.EventBuilder<T, EtUserRole, EtState>> field =
            fields.page(String.valueOf(spec.page())).field(spec.id());
        applyContext(field, spec.kind());
        field.showSummary()
            .showCondition(spec.showCondition())
            .caseEventColumn(PAGE_DISPLAY_ORDER, spec.pageDisplayOrder())
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, spec.pageFieldDisplayOrder())
            .caseEventColumn(PAGE_COLUMN_NUMBER, spec.pageColumnNumber());

        if (spec.callbackUrl() != null) {
            field.caseEventColumn("CallBackURLMidEvent", spec.callbackUrl());
        }
        if (spec.retainHiddenValue() != null) {
            field.caseEventColumn(RETAIN_HIDDEN_VALUE, spec.retainHiddenValue());
        }

        field.done();
    }

    private void applyContext(
        Field.FieldBuilder<?, EtState, T, Event.EventBuilder<T, EtUserRole, EtState>> field,
        FieldKind kind
    ) {
        switch (kind) {
            case MANDATORY -> field.mandatory();
            case OPTIONAL -> field.optional();
            case READ_ONLY -> field.readOnly();
            case COMPLEX -> field.caseEventColumn("DisplayContext", "COMPLEX");
            default -> throw new IllegalArgumentException("Unsupported field kind " + kind);
        }
    }

    private Event.EventBuilder<T, EtUserRole, EtState> grantRegionalCaseworkerAccess(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    protected static FieldSpec field(
        String id,
        FieldKind kind,
        int page,
        int pageDisplayOrder,
        int pageFieldDisplayOrder
    ) {
        return new FieldSpec(id, kind, page, pageDisplayOrder, pageFieldDisplayOrder, null, null, null, 1);
    }

    protected static FieldSpec ttl(int page, int pageDisplayOrder, int pageFieldDisplayOrder) {
        return field("TTL", COMPLEX, page, pageDisplayOrder, pageFieldDisplayOrder).show(TTL_CONDITION);
    }

    protected static FieldSpec receiptDate(int page, int pageDisplayOrder) {
        return field("receiptDate", MANDATORY, page, pageDisplayOrder, 1).callback(SINGLE_CASE_MULTIPLE_VALIDATION);
    }

    protected static List<FieldSpec> multipleFields(
        int page,
        int pageDisplayOrder,
        int multipleFlagOrder,
        String caseTypeCondition
    ) {
        return List.of(
            field("multipleFlag", READ_ONLY, page, pageDisplayOrder, multipleFlagOrder)
                .show("feeGroupReference=\"dummy\""),
            field("caseType", MANDATORY, page, pageDisplayOrder, multipleFlagOrder + 1)
                .show(caseTypeCondition)
                .retainHidden(),
            field("multipleReference", MANDATORY, page, pageDisplayOrder, multipleFlagOrder + 2)
                .show(MULTIPLE_CASE_CONDITION)
                .retainHidden(),
            field("leadClaimant", MANDATORY, page, pageDisplayOrder, multipleFlagOrder + 3)
                .show(MULTIPLE_CASE_CONDITION)
                .retainHidden(),
            field("subMultipleName  ", OPTIONAL, page, pageDisplayOrder, multipleFlagOrder + 4)
                .show(MULTIPLE_CASE_CONDITION)
                .retainHidden()
        );
    }

    protected static List<FieldSpec> scotlandFileLocations(int page, int pageDisplayOrder, int pageFieldDisplayOrder) {
        return List.of(
            fileLocation("Glasgow", page, pageDisplayOrder, pageFieldDisplayOrder),
            fileLocation("Aberdeen", page, pageDisplayOrder, pageFieldDisplayOrder),
            fileLocation("Dundee", page, pageDisplayOrder, pageFieldDisplayOrder),
            fileLocation("Edinburgh", page, pageDisplayOrder, pageFieldDisplayOrder)
        );
    }

    private static FieldSpec fileLocation(
        String office,
        int page,
        int pageDisplayOrder,
        int pageFieldDisplayOrder
    ) {
        return field("fileLocation" + office, OPTIONAL, page, pageDisplayOrder, pageFieldDisplayOrder)
            .show("managingOffice=\"" + office + "\"")
            .retainHidden();
    }

    protected enum FieldKind {
        MANDATORY,
        OPTIONAL,
        READ_ONLY,
        COMPLEX
    }

    protected record FieldSpec(
        String id,
        FieldKind kind,
        int page,
        int pageDisplayOrder,
        int pageFieldDisplayOrder,
        String showCondition,
        String retainHiddenValue,
        String callbackUrl,
        Integer pageColumnNumber
    ) {
        FieldSpec show(String value) {
            return new FieldSpec(
                id, kind, page, pageDisplayOrder, pageFieldDisplayOrder, value,
                retainHiddenValue, callbackUrl, pageColumnNumber
            );
        }

        FieldSpec retainHidden() {
            return new FieldSpec(
                id, kind, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
                "Yes", callbackUrl, pageColumnNumber
            );
        }

        FieldSpec callback(String value) {
            return new FieldSpec(
                id, kind, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
                retainHiddenValue, value, pageColumnNumber
            );
        }

        FieldSpec withPageFieldDisplayOrder(int value) {
            return new FieldSpec(
                id, kind, page, pageDisplayOrder, value, showCondition,
                retainHiddenValue, callbackUrl, pageColumnNumber
            );
        }

        FieldSpec noPageColumn() {
            return new FieldSpec(
                id, kind, page, pageDisplayOrder, pageFieldDisplayOrder, showCondition,
                retainHiddenValue, callbackUrl, null
            );
        }
    }
}
