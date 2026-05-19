package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
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

        et3ResponseDetailsFields(et3ResponseEvent(
            configBuilder,
            "et3ResponseDetails",
            "ET3 - Response Details",
            submitEt3DisplayOrder - 1
        ));

        et3ResponseEmploymentDetailsFields(et3ResponseEvent(
            configBuilder,
            "et3ResponseEmploymentDetails",
            "ET3 - Employment Details",
            submitEt3DisplayOrder - 2
        ));

        et3ResponseFields(et3ResponseEvent(
            configBuilder,
            "et3Response",
            "ET3 - Respondent Details",
            submitEt3DisplayOrder - 3
        ).caseEventColumn(PUBLISH, "Y"))
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);
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

    private Event.EventBuilder<T, EtUserRole, EtState> et3ResponseEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        int displayOrder
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(name)
            .displayOrder(displayOrder)
            .showSummary()
            .caseEventColumn("PreConditionState(s)", "Accepted")
            .aboutToStartCallbackUrl("${ET_COS_URL}/et3Response/aboutToStart")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Response/submitSection")
            .submittedCallbackUrl("${ET_COS_URL}/et3Response/sectionComplete")
            .endButtonLabel("Save ET3 as draft")
            .grant(Permission.CRUD, EtUserRole.respondentSolicitors())
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR)
            .grant(Permission.R, EtUserRole.ET_ACAS_API);
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
        return et3ResponseIntroFields(event, "et3RepresentingRespondent")
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

    private Event.EventBuilder<T, EtUserRole, EtState> et3ResponseEmploymentDetailsFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return et3ResponseIntroFields(event, "et3RepresentingRespondent")
            .page("3")
            .pageLabel("Respondent's workforce")
            .field(CaseData::getEt3ResponseEmploymentCount)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseMultipleSites)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseSiteEmploymentCount)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("4")
            .pageLabel("Are the dates of employment given by the claimant correct?")
            .field(CaseData::getEt3ResponseAreDatesCorrect)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("5")
            .pageLabel("What are the claimant's employment dates?")
            .field(CaseData::getEt3ResponseEmploymentStartDate)
            .optional()
            .showSummary()
            .caseEventColumn("PageShowCondition", "et3ResponseAreDatesCorrect=\"No\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et3Response/midEmploymentDates")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseEmploymentEndDate)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseEmploymentInformation)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("6")
            .pageLabel("Is the claimant's employment with the respondent continuing?")
            .field(CaseData::getEt3ResponseContinuingEmployment)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("7")
            .pageLabel("Is the claimant's description of their job or job title correct?")
            .field(CaseData::getEt3ResponseIsJobTitleCorrect)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseCorrectJobTitle)
            .optional()
            .showCondition("et3ResponseIsJobTitleCorrect=\"No\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("8")
            .pageLabel("Are the claimant's total weekly work hours correct?")
            .field(CaseData::getEt3ResponseClaimantWeeklyHours)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseClaimantCorrectHours)
            .optional()
            .showCondition("et3ResponseClaimantWeeklyHours=\"No\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("9")
            .pageLabel("Are the earnings details given by the claimant correct?")
            .field(CaseData::getEt3ResponseEarningDetailsCorrect)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("10")
            .pageLabel("What are the claimant's correct pay details?")
            .field("et3ResponsePayDetailsPreamble")
            .readOnly()
            .caseEventColumn("PageShowCondition", "et3ResponseEarningDetailsCorrect=\"No\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponsePayFrequency)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponsePayBeforeTax)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponsePayTakehome)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("11")
            .pageLabel("Is the information given by the claimant correct about their notice?")
            .field(CaseData::getEt3ResponseIsNoticeCorrect)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseCorrectNoticeDetails)
            .optional()
            .showCondition("et3ResponseIsNoticeCorrect=\"No\"")
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("12")
            .pageLabel("Are the details about pension and other benefits correct?")
            .field(CaseData::getEt3ResponseIsPensionCorrect)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponsePensionCorrectDetails)
            .optional()
            .showCondition("et3ResponseIsPensionCorrect=\"No\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et3ResponseFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return et3ResponseIntroFields(event, "submitEt3Respondent")
            .page("3")
            .pageLabel("Is this the correct claimant for the claim you're responding to?")
            .field(CaseData::getEt3ResponseClaimantName)
            .readOnly()
            .showCondition("et3ResponseClaimantNameLabel=\"dummy\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("et3ResponseClaimantNameLabel")
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseIsClaimantNameCorrect)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseClaimantNameCorrection)
            .mandatory()
            .showCondition("et3ResponseIsClaimantNameCorrect=\"No\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("4")
            .pageLabel("What is the respondent's name?")
            .field("et3ResponseNamePreamble")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("et3ResponseNameInset")
            .readOnly()
            .showCondition("et3ResponseNameShowInset=\"Yes\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentLegalName)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentCompanyNumber)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentEmployerType)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentPreferredTitle)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentContactName)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseNameShowInset)
            .optional()
            .showCondition("et3ResponseNamePreamble=\"dummy\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("5")
            .pageLabel("Respondent address")
            .field(CaseData::getEt3RespondentAddress)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseDXAddress)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("6")
            .pageLabel("Your information (as the representative)")
            .field("et3RepresentativeInfoFirstWords")
            .readOnly()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseContactPreference)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseContactReason)
            .mandatory()
            .showCondition("et3ResponseContactPreference=\"Post\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseContactLanguage)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponsePhone)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseAddress)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseReference)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("7")
            .pageLabel("Hearing format")
            .field("et3ResponseHearingPreamble")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseHearingRepresentative)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseHearingRespondent)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("8")
            .pageLabel("In the respondent party - are you aware of any physical, mental or learning disability or "
                           + "health conditions which requires support?")
            .field("et3ResponseHealthInsetPreamble")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentSupportNeeded)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("9")
            .pageLabel("Give details of the support you require for the parties")
            .field("et3ResponseHealthDetailsPreamble")
            .readOnly()
            .caseEventColumn("PageShowCondition", "et3ResponseRespondentSupportNeeded=\"Yes\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentSupportDetails)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt3ResponseRespondentSupportDocument)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private FieldCollection.FieldCollectionBuilder<T, EtState, Event.EventBuilder<T, EtUserRole, EtState>>
        et3ResponseIntroFields(Event.EventBuilder<T, EtUserRole, EtState> event, String respondentFieldId) {
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
            .field(respondentFieldId)
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et3Response/validateRespondent")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();
    }
}
