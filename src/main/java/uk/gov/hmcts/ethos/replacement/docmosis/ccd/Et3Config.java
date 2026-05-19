package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class Et3Config<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PUBLISH = "Publish";
    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_LABEL = "PageLabel";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean grantSubmitToApi;
    private final boolean grantEt3NotificationToAcas;
    private final int submitEt3DisplayOrder;
    private final String submitEt3ShowCondition;
    private final boolean includeSubmitEt3ReadonlyDocuments;

    protected Et3Config(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean grantSubmitToApi,
        boolean grantEt3NotificationToAcas,
        int submitEt3DisplayOrder,
        String submitEt3ShowCondition,
        boolean includeSubmitEt3ReadonlyDocuments
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.grantSubmitToApi = grantSubmitToApi;
        this.grantEt3NotificationToAcas = grantEt3NotificationToAcas;
        this.submitEt3DisplayOrder = submitEt3DisplayOrder;
        this.submitEt3ShowCondition = submitEt3ShowCondition;
        this.includeSubmitEt3ReadonlyDocuments = includeSubmitEt3ReadonlyDocuments;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        Event.EventBuilder<T, EtUserRole, EtState> submitEt3Form = et3Event(
            configBuilder,
            "SUBMIT_ET3_FORM",
            "Submit ET3 Form"
        )
            .publishToCamunda()
            .grant(Permission.CRUD, EtUserRole.DEFENDANT, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .grant(
                Permission.R,
                EtUserRole.CASEWORKER_EMPLOYMENT,
                regionalCaseworkerRole,
                EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE,
                regionalJudgeRole
            );

        if (grantSubmitToApi) {
            submitEt3Form.grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
        }

        et3Event(configBuilder, "UPDATE_ET3_FORM", "Update ET3 Form")
            .grant(Permission.CRUD, EtUserRole.CITIZEN);

        Event.EventBuilder<T, EtUserRole, EtState> et3Notification = et3NotificationFields(
            configBuilder.event("et3Notification")
                .forAllStates()
                .name("ET3 notification")
                .description("ET3 notification")
                .displayOrder(19)
                .caseEventColumn("PreConditionState(s)", "Accepted")
                .aboutToStartCallbackUrl("${ET_COS_URL}/et3Notification/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Notification/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/et3Notification/submitted")
        )
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        if (grantEt3NotificationToAcas) {
            et3Notification.grant(Permission.R, EtUserRole.ET_ACAS_API);
        }

        Event.EventBuilder<T, EtUserRole, EtState> submitEt3 = submitEt3Fields(
            configBuilder.event("submitEt3")
                .forAllStates()
                .name("Submit ET3 Form")
                .description("Submit ET3 Form")
                .displayOrder(submitEt3DisplayOrder)
                .showSummary()
                .publishToCamunda()
                .caseEventColumn("PreConditionState(s)", "Accepted")
                .aboutToStartCallbackUrl("${ET_COS_URL}/et3Response/startSubmitEt3")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Response/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/et3Response/processingComplete")
                .endButtonLabel("Submit ET3 to Tribunal")
        )
            .grant(Permission.CRUD, EtUserRole.respondentSolicitors())
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR)
            .grant(
                Permission.R,
                EtUserRole.ET_ACAS_API,
                EtUserRole.CASEWORKER_EMPLOYMENT,
                regionalCaseworkerRole,
                EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE,
                regionalJudgeRole
            )
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        if (submitEt3ShowCondition != null) {
            submitEt3.showCondition(submitEt3ShowCondition);
        }

        et3ResponseDetailsFields(
            configBuilder.event("et3ResponseDetails")
                .forAllStates()
                .name("ET3 - Response Details")
                .description("ET3 - Response Details")
                .displayOrder(submitEt3DisplayOrder - 1)
                .showSummary()
                .caseEventColumn("PreConditionState(s)", "Accepted")
                .aboutToStartCallbackUrl("${ET_COS_URL}/et3Response/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Response/submitSection")
                .submittedCallbackUrl("${ET_COS_URL}/et3Response/sectionComplete")
                .endButtonLabel("Save ET3 as draft")
        )
            .grant(Permission.CRUD, EtUserRole.respondentSolicitors())
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR)
            .grant(Permission.R, EtUserRole.ET_ACAS_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et3Event(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name
    ) {
        return configBuilder.event(eventId)
            .forStateTransition(EtState.ACCEPTED, EtState.ACCEPTED)
            .name(name)
            .description(name)
            .caseEventColumn("DisplayOrder", null);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et3NotificationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/et3Notification/midUploadDocuments")
            .pageLabel("Upload documents")
            .field(CaseData::getEt3NotificationDocCollection)
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("2")
            .pageLabel("Who are you sending this document to?")
            .field(CaseData::getHorizontalLine4)
            .readOnly()
            .showSummary()
            .caseEventColumn("PageShowCondition", "et3OtherTypeDocumentName != \"\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3OtherTypeDocumentName)
            .mandatory()
            .showCondition("horizontalLine4=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3OtherTypeDocumentNameLabel)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3SelectAllThatApply)
            .readOnly()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3NotificationDocRecipient)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("3")
            .pageLabel("Email Acas")
            .field(CaseData::getEt3EmailDocsToAcasTitle)
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3EmailLinkToAcas)
            .mandatory()
            .showCondition("et3EmailDocsToAcasTitle=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3EmailDocsToAcasLink)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getEt3EmailDocsToAcasInstructions)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> submitEt3Fields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var fields = event.fields()
            .page("1")
            .field(CaseData::getSubmitEt3Respondent)
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et3Response/reloadSubmitData")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("submitEt3Respondent=\"dummy\"")
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn(PUBLISH, "Y")
            .done()
            .page("2")
            .field(CaseData::getConfirmEt3Submit)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done();

        if (includeSubmitEt3ReadonlyDocuments) {
            fields
                .field(CaseData::getEt3ResponseEmployerClaimDocument)
                .readOnly()
                .showSummary()
                .showCondition("confirmEt3Submit CONTAINS \"dummy\"")
                .caseEventColumn(PAGE_COLUMN_NUMBER, null)
                .caseEventColumn(PUBLISH, null)
                .done()
                .field(CaseData::getEt3ResponseContestClaimDocument)
                .readOnly()
                .showSummary()
                .showCondition("confirmEt3Submit CONTAINS \"dummy\"")
                .caseEventColumn(PAGE_COLUMN_NUMBER, null)
                .caseEventColumn(PUBLISH, null)
                .done()
                .field(CaseData::getEt3ResponseRespondentSupportDocument)
                .readOnly()
                .showSummary()
                .showCondition("confirmEt3Submit CONTAINS \"dummy\"")
                .caseEventColumn(PAGE_COLUMN_NUMBER, null)
                .caseEventColumn(PUBLISH, null)
                .done();
        }

        return fields.done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et3ResponseDetailsFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("ET3 - Response to Employment tribunal claim (ET1)")
            .field(CaseData::getEt3ResponseShowInset)
            .optional()
            .showCondition("et3StartPagePreamble=\"dummy\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("et3StartPagePreamble")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("et3StartPageInset")
            .readOnly()
            .showCondition("et3ResponseShowInset=\"Yes\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("et3StartPageMainBody")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .field(CaseData::getEt3RepresentingRespondent)
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et3Response/validateRespondent")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("3")
            .pageLabel("Do you agree with the details given by the claimant about early conciliation with Acas?")
            .field(CaseData::getEt3ResponseAcasAgree)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseAcasAgreeReason)
            .optional()
            .showCondition("et3ResponseAcasAgree=\"No\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("4")
            .pageLabel("Does the respondent contest the claim?")
            .field(CaseData::getEt3ResponseRespondentContestClaim)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("5")
            .pageLabel("Explain why the respondent contests the claim")
            .field("et3ResponseContestClaimPreamble")
            .readOnly()
            .caseEventColumn("PageShowCondition", "et3ResponseRespondentContestClaim=\"Yes\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseContestClaimDocument)
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseContestClaimDetails)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("6")
            .pageLabel("Does the respondent wish to make an Employer's Contract Claim?")
            .field(CaseData::getEt3ResponseEmployerClaim)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("7")
            .pageLabel("Provide the background and details of your Employer's Contract Claim")
            .field("et3ResponseEmployerClaimDetailsPreamble")
            .readOnly()
            .caseEventColumn("PageShowCondition", "et3ResponseEmployerClaim=\"Yes\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseEmployerClaimDetails)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseEmployerClaimDocument)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }
}
