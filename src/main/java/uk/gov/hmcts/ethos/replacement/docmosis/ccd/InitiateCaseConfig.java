package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class InitiateCaseConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_DISPLAY_ORDER = "PageDisplayOrder";
    private static final String PAGE_FIELD_DISPLAY_ORDER = "PageFieldDisplayOrder";
    private static final String PAGE_SHOW_CONDITION = "PageShowCondition";
    private static final String RETAIN_HIDDEN_VALUE = "RetainHiddenValue";
    private static final String SHOW_SUMMARY_CHANGE_OPTION = "ShowSummaryChangeOption";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean includeAllocatedOffice;

    protected InitiateCaseConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean includeAllocatedOffice
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.includeAllocatedOffice = includeAllocatedOffice;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        initiateCaseFields(
            configBuilder.event("initiateCase")
                .initialState(EtState.SUBMITTED)
                .name("Create Case")
                .description("Create a new case")
                .displayOrder(2)
                .publishToCamunda()
                .aboutToStartCallbackUrl("${ET_COS_URL}/preDefaultValues")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/postDefaultValues")
                .submittedCallbackUrl("${ET_COS_URL}/addServiceId")
                .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT)
                .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
                .grant(Permission.R, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
                .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
                .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
        );
    }

    private Event.EventBuilder<T, EtUserRole, EtState> initiateCaseFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var fields = event.fields();
        field(fields, mandatory("receiptDate", 1, 1));
        field(fields, optional("feeGroupReference", 1, 2, "managingOffice=\"dummy\""));
        field(fields, mandatory("managingOffice", 1, 3));

        int firstPageOffset = 0;
        if (includeAllocatedOffice) {
            field(fields, mandatory("allocatedOffice", 1, 4));
            firstPageOffset = 1;
        }

        field(fields, optional("stateAPI", 1, 4 + firstPageOffset, "feeGroupReference=\"dummy\"", "No"));
        field(fields, optional("caseRefNumberCount", 1, 5 + firstPageOffset, "feeGroupReference=\"dummy\"", "Yes"));
        field(fields, optional("startCaseRefNumber", 1, 6 + firstPageOffset, "feeGroupReference=\"dummy\"", "Yes"));
        field(fields, optional("multipleRefNumber", 1, 7 + firstPageOffset, "feeGroupReference=\"dummy\"", "Yes"));
        field(fields, mandatory("caseType", 1, 8 + firstPageOffset, "feeGroupReference=\"dummy\"", "Yes"));
        field(fields, readonly("multipleReference", 1, 9 + firstPageOffset, "caseType=\"Multiple\""));
        field(fields, readonly("leadClaimant", 1, 10 + firstPageOffset, "caseType=\"Multiple\""));

        field(fields, mandatory("claimant_TypeOfClaimant", 2, 1));
        field(fields, mandatory("claimant_Company ", 2, 2, "claimant_TypeOfClaimant=\"Company\"", "Yes"));
        field(fields, complex("claimantIndType", 2, 3, "claimant_TypeOfClaimant=\"Individual\""));
        field(fields, complex("claimantType", 2, 4));
        field(fields, complex("respondentCollection", 3, 1)
            .midEvent("${ET_COS_URL}/midRespondentAddress"));
        field(fields, mandatory("claimantWorkAddressQuestion", 4, 1)
            .pageShowCondition(includeAllocatedOffice ? null : "claimant_TypeOfClaimant=\"Individual\""));
        field(fields, mandatory("claimantWorkAddressQRespondent", 4, 2, "claimantWorkAddressQuestion=\"Yes\"", "Yes"));
        field(fields, complex("claimantWorkAddress", 5, 1, "claimantWorkAddressQuestion=\"No\"")
            .pageShowCondition("claimant_TypeOfClaimant=\"Individual\" AND claimantWorkAddressQuestion=\"No\""));
        field(fields, complex("companyPremises", 6, 1)
            .pageShowCondition("claimant_TypeOfClaimant=\"Company\""));
        field(fields, complex("claimantOtherType", 7, 1));
        field(fields, mandatory("claimantRepresentedQuestion", 8, 1));
        field(fields, complex("representativeClaimantType", 8, 2, "claimantRepresentedQuestion=\"Yes\""));
        field(fields, complex("claimantHearingPreference", 9, 1)
            .midEvent("${ET_COS_URL}/midEventHearingPreferences"));

        return fields.done();
    }

    private FieldSpec mandatory(String id, int page, int pageFieldOrder) {
        return mandatory(id, page, pageFieldOrder, null, null);
    }

    private FieldSpec mandatory(String id, int page, int pageFieldOrder, String showCondition, String retainHidden) {
        return new FieldSpec(id, Display.MANDATORY, page, pageFieldOrder, showCondition, retainHidden, null, null);
    }

    private FieldSpec optional(String id, int page, int pageFieldOrder, String showCondition) {
        return optional(id, page, pageFieldOrder, showCondition, null);
    }

    private FieldSpec optional(String id, int page, int pageFieldOrder, String showCondition, String retainHidden) {
        return new FieldSpec(id, Display.OPTIONAL, page, pageFieldOrder, showCondition, retainHidden, null, null);
    }

    private FieldSpec readonly(String id, int page, int pageFieldOrder, String showCondition) {
        return new FieldSpec(id, Display.READONLY, page, pageFieldOrder, showCondition, null, null, null);
    }

    private FieldSpec complex(String id, int page, int pageFieldOrder) {
        return complex(id, page, pageFieldOrder, null);
    }

    private FieldSpec complex(String id, int page, int pageFieldOrder, String showCondition) {
        return new FieldSpec(id, Display.COMPLEX, page, pageFieldOrder, showCondition, null, null, null);
    }

    private FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> field(
        FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> fields,
        FieldSpec spec
    ) {
        var field = fields.page(String.valueOf(spec.page()))
            .field(spec.id());
        switch (spec.display()) {
            case MANDATORY -> field.mandatory();
            case OPTIONAL -> field.optional();
            case READONLY -> field.readOnly();
            case COMPLEX -> {
            }
            default -> throw new IllegalStateException("Unexpected display context: " + spec.display());
        }

        field.showSummary()
            .caseEventColumn("Publish", null)
            .caseEventColumn(PAGE_DISPLAY_ORDER, spec.page())
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, spec.pageFieldOrder())
            .caseEventColumn(SHOW_SUMMARY_CHANGE_OPTION, "Y")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1);

        if (spec.showCondition() != null) {
            field.showCondition(spec.showCondition());
        }
        if (spec.retainHidden() != null) {
            field.caseEventColumn(RETAIN_HIDDEN_VALUE, spec.retainHidden());
        }
        if (spec.pageShowCondition() != null) {
            field.caseEventColumn(PAGE_SHOW_CONDITION, spec.pageShowCondition());
        }
        if (spec.midEvent() != null) {
            field.caseEventColumn("CallBackURLMidEvent", spec.midEvent());
        }

        return field.done();
    }

    private enum Display {
        MANDATORY,
        OPTIONAL,
        READONLY,
        COMPLEX
    }

    private record FieldSpec(
        String id,
        Display display,
        int page,
        int pageFieldOrder,
        String showCondition,
        String retainHidden,
        String pageShowCondition,
        String midEvent
    ) {
        private FieldSpec pageShowCondition(String value) {
            return new FieldSpec(id, display, page, pageFieldOrder, showCondition, retainHidden, value, midEvent);
        }

        private FieldSpec midEvent(String value) {
            return new FieldSpec(
                id, display, page, pageFieldOrder, showCondition, retainHidden, pageShowCondition, value
            );
        }
    }
}
