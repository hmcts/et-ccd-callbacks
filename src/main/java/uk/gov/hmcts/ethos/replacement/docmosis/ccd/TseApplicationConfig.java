package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class TseApplicationConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_DISPLAY_ORDER = "PageDisplayOrder";
    private static final String PAGE_FIELD_DISPLAY_ORDER = "PageFieldDisplayOrder";
    private static final String PAGE_LABEL = "PageLabel";
    private static final String PAGE_SHOW_CONDITION = "PageShowCondition";
    private static final String RETAIN_HIDDEN_VALUE = "RetainHiddenValue";
    private static final String SHOW_SUMMARY_CHANGE_OPTION = "ShowSummaryChangeOption";

    private static final ApplicationPage[] CLAIMANT_APPLICATION_PAGES = {
        new ApplicationPage(1, "Amend claim"),
        new ApplicationPage(2, "Change personal details"),
        new ApplicationPage(3, "Consider decision afresh"),
        new ApplicationPage(4, "Contact the tribunal"),
        new ApplicationPage(5, "Order a witness to attend to give evidence"),
        new ApplicationPage(6, "Order other party"),
        new ApplicationPage(7, "Postpone a hearing"),
        new ApplicationPage(8, "Reconsider judgment"),
        new ApplicationPage(9, "Respondent not complied"),
        new ApplicationPage(10, "Restrict publicity"),
        new ApplicationPage(11, "Strike out all or part of the response"),
        new ApplicationPage(12, "Vary or revoke an order"),
        new ApplicationPage(13, "Withdraw all or part of claim")
    };

    private static final ApplicationPage[] RESPONDENT_APPLICATION_PAGES = {
        new ApplicationPage(1, "Amend response"),
        new ApplicationPage(2, "Change personal details"),
        new ApplicationPage(3, "Claimant not complied"),
        new ApplicationPage(4, "Consider a decision afresh"),
        new ApplicationPage(5, "Contact the tribunal"),
        new ApplicationPage(6, "Order other party"),
        new ApplicationPage(7, "Order a witness to attend to give evidence"),
        new ApplicationPage(8, "Postpone a hearing"),
        new ApplicationPage(9, "Reconsider judgement", "Reconsider judgment"),
        new ApplicationPage(10, "Restrict publicity"),
        new ApplicationPage(11, "Strike out all or part of a claim"),
        new ApplicationPage(12, "Vary or revoke an order")
    };

    private final Integer respondentTseDisplayOrder;
    private final boolean showRespondentNotAvailableWarningSummary;

    protected TseApplicationConfig(
        Integer respondentTseDisplayOrder,
        boolean showRespondentNotAvailableWarningSummary
    ) {
        this.respondentTseDisplayOrder = respondentTseDisplayOrder;
        this.showRespondentNotAvailableWarningSummary = showRespondentNotAvailableWarningSummary;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        claimantTseFields(
            configBuilder.event("claimantTSE")
                .forAllStates()
                .name("Make an Application")
                .description("Claimant Tell Something Else")
                .showSummary()
                .showCondition("caseType=\"dummy\"")
                .caseEventColumn("DisplayOrder", null)
                .caseEventColumn("PreConditionState(s)", "Submitted;Vetted;Rejected;Accepted;Closed")
                .caseEventColumn("PostConditionState", "*")
                .caseEventColumn("Publish", "Y")
                .aboutToStartCallbackUrl("${ET_COS_URL}/claimantTSE/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/claimantTSE/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/claimantTSE/completeApplication")
        )
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        Event.EventBuilder<T, EtUserRole, EtState> respondentTse = respondentTseFields(
            configBuilder.event("respondentTSE")
                .forAllStates()
                .name("Contact the tribunal")
                .description("Respondent Tell Something Else")
                .showSummary()
                .showCondition("caseType=\"dummy\"")
                .caseEventColumn("PreConditionState(s)", "*")
                .caseEventColumn("PostConditionState", "*")
                .caseEventColumn("Publish", "Y")
                .aboutToStartCallbackUrl("${ET_COS_URL}/respondentTSE/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/respondentTSE/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/respondentTSE/completeApplication")
        )
            .grant(Permission.CRUD, EtUserRole.respondentSolicitors())
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR);

        applyDisplayOrder(respondentTse, respondentTseDisplayOrder);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> claimantTseFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var fields = event.fields();
        fields.page("1")
            .field("genericTseApplicationCollection")
            .showCondition("claimantTseSelectApplication=\"dummy\"")
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn(SHOW_SUMMARY_CHANGE_OPTION, "N")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("claimantTseSelectApplication")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, "Select an application")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();

        for (ApplicationPage applicationPage : CLAIMANT_APPLICATION_PAGES) {
            claimantApplicationPage(fields, applicationPage);
        }

        claimantRule92Fields(fields);
        return fields.done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> respondentTseFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var fields = event.fields();
        fields.page("1")
            .field("resTseNotAvailableWarning")
            .readOnly()
            .showCondition("resTseSelectApplication=\"dummy\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/respondentTSE/showError")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(
                SHOW_SUMMARY_CHANGE_OPTION,
                showRespondentNotAvailableWarningSummary ? "Y" : null
            )
            .done()
            .field("resTseNotAvailableWarningLabel")
            .readOnly()
            .showCondition("resTseNotAvailableWarning=\"Yes\"")
            .caseEventColumn(PAGE_SHOW_CONDITION, "resTseNotAvailableWarning=\"Yes\"")
            .caseEventColumn(SHOW_SUMMARY_CHANGE_OPTION, "Y")
            .caseEventColumn(PAGE_DISPLAY_ORDER, null)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("2")
            .field("genericTseApplicationCollection")
            .showCondition("resTseSelectApplication=\"dummy\"")
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn(SHOW_SUMMARY_CHANGE_OPTION, "N")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field("nextListedDate")
            .optional()
            .showCondition("resTseSelectApplication=\"dummy\"")
            .caseEventColumn(SHOW_SUMMARY_CHANGE_OPTION, "N")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", "Y")
            .done()
            .field("resTseSelectApplication")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, "Select an application")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();

        for (ApplicationPage applicationPage : RESPONDENT_APPLICATION_PAGES) {
            respondentApplicationPage(fields, applicationPage);
        }

        respondentRule92Fields(fields);
        return fields.done();
    }

    private void claimantApplicationPage(
        FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> fields,
        ApplicationPage applicationPage
    ) {
        int page = applicationPage.index() + 1;
        fields.page(String.valueOf(page))
            .field("claimantTseGuidanceLabel" + applicationPage.index())
            .readOnly()
            .caseEventColumn(
                PAGE_SHOW_CONDITION,
                "claimantTseSelectApplication=\"" + applicationPage.pageLabel() + "\""
            )
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/claimantTSE/validateGiveDetails")
            .caseEventColumn(PAGE_LABEL, applicationPage.pageLabel())
            .caseEventColumn(PAGE_DISPLAY_ORDER, page)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("claimantTseDocument" + applicationPage.index())
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_DISPLAY_ORDER, page)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("claimantTseTextBox" + applicationPage.index())
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_DISPLAY_ORDER, page)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 3)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();
    }

    private void claimantRule92Fields(
        FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> fields
    ) {
        fields.page("15")
            .field("claimantTseRule92TextArea")
            .readOnly()
            .caseEventColumn(SHOW_SUMMARY_CHANGE_OPTION, "N")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PAGE_LABEL, "Copy this correspondence to the other party")
            .caseEventColumn(
                PAGE_SHOW_CONDITION,
                "claimantTseSelectApplication != \"Order a witness to attend to give evidence\""
            )
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .done()
            .field("claimantTseRespNotAvailable")
            .readOnly()
            .showCondition("claimantTseSelectApplication=\"dummy\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .done()
            .field("claimantTseRule92TextWhenRespOffline")
            .readOnly()
            .showCondition("claimantTseRespNotAvailable=\"Yes\"")
            .caseEventColumn(SHOW_SUMMARY_CHANGE_OPTION, "N")
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .done()
            .field("claimantTseRule92")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 3)
            .done()
            .field("claimantTseRule92AnsNoGiveDetails")
            .mandatory()
            .showSummary()
            .showCondition("claimantTseRule92=\"No\"")
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "Yes")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 4)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();
    }

    private void respondentApplicationPage(
        FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> fields,
        ApplicationPage applicationPage
    ) {
        int page = applicationPage.index() + 2;
        fields.page(String.valueOf(page))
            .field("resTseGuidanceLabel" + applicationPage.index())
            .readOnly()
            .caseEventColumn(
                PAGE_SHOW_CONDITION,
                "resTseSelectApplication=\"" + applicationPage.showConditionLabel() + "\""
            )
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/respondentTSE/validateGiveDetails")
            .caseEventColumn(PAGE_LABEL, applicationPage.pageLabel())
            .caseEventColumn(PAGE_DISPLAY_ORDER, page)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("resTseDocument" + applicationPage.index())
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_DISPLAY_ORDER, page)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("resTseTextBox" + applicationPage.index())
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_DISPLAY_ORDER, page)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 3)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();
    }

    private void respondentRule92Fields(
        FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> fields
    ) {
        fields.page("15")
            .field("resTseHorizontalLine")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, "Copy this correspondence to the other party")
            .caseEventColumn(
                PAGE_SHOW_CONDITION,
                "resTseSelectApplication != \"Order a witness to attend to give evidence\""
            )
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("resTseCopyThisCorrespondenceText")
            .readOnly()
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("resTseCopyToOtherPartyYesOrNo")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 3)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("resTseCopyToOtherPartyTextArea")
            .mandatory()
            .showSummary()
            .showCondition("resTseCopyToOtherPartyYesOrNo=\"No\"")
            .caseEventColumn(PAGE_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 4)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();
    }

    private void applyDisplayOrder(Event.EventBuilder<T, EtUserRole, EtState> event, Integer displayOrder) {
        if (displayOrder == null) {
            event.caseEventColumn("DisplayOrder", null);
        } else {
            event.displayOrder(displayOrder);
        }
    }

    private record ApplicationPage(int index, String showConditionLabel, String pageLabel) {
        private ApplicationPage(int index, String label) {
            this(index, label, label);
        }
    }
}
