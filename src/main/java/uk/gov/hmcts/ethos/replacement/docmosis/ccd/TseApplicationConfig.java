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

    private void claimantApplicationPage(
        FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>> fields,
        ApplicationPage applicationPage
    ) {
        int page = applicationPage.index() + 1;
        fields.page(String.valueOf(page))
            .field("claimantTseGuidanceLabel" + applicationPage.index())
            .readOnly()
            .caseEventColumn(PAGE_SHOW_CONDITION, "claimantTseSelectApplication=\"" + applicationPage.label() + "\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/claimantTSE/validateGiveDetails")
            .caseEventColumn(PAGE_LABEL, applicationPage.label())
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

    private record ApplicationPage(int index, String label) {
    }
}
