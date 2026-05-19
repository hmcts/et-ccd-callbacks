package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.Set;

public abstract class SingleFieldEventsConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String ACTIVE_CASE_STATES = "Submitted;Vetted;Accepted;Rejected;Closed";
    private static final String BF_ACTION_STATES = "Accepted;Rejected;Submitted;Vetted";
    private static final String ACCEPTED_OR_REJECTED = "Accepted;Rejected";
    private static final String GENERATE_CORRESPONDENCE_STATES = "Accepted;Rejected;Submitted;Vetted;Closed";
    private static final String ACCEPT_REJECT_POST_CONDITION =
        "Accepted(preAcceptCase.caseAccepted=\"Yes\"):1;Rejected";
    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String RESPOND_TO_TRIBUNAL_NOT_AVAILABLE_LABEL =
        "<h3>This function is not available for this case, please click cancel to return to the main page, "
            + "you will need to submit your application outside the portal via email or post.</h3>";
    private static final String PSE_RESPONDENT_SUPPORTING_MATERIAL_LABEL =
        "Consider when providing material that:<br><ul><li>you can upload letters, photos or documents to support your "
            + "application</li><li>if you are taking a picture of a letter, place it on a flat surface and take the "
            + "picture from above</li><li>if you are uploading written documents with tracked changes, make sure that "
            + "tracked changes are turned on</li></ul>";
    private static final String PSE_RESPONDENT_COPY_PARTY_INTRO =
        "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the "
            + "other party tells the tribunal. The rules therefore require all communications with the tribunal to be "
            + "copied to the other party, apart from in exception circumstances.";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final int nocRequestDisplayOrder;
    private final String addCaseNoteDescription;
    private final int amendRespondentRepresentativeFieldDisplayOrder;
    private final int amendRespondentDetailsFieldDisplayOrder;
    private final int legalRepDocumentsDisplayOrder;
    private final int downloadDraftEt3DisplayOrder;
    private final String viewAllNotificationsName;
    private final String viewAllNotificationsDescription;

    protected SingleFieldEventsConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        int nocRequestDisplayOrder,
        String addCaseNoteDescription,
        int amendRespondentRepresentativeFieldDisplayOrder,
        int amendRespondentDetailsFieldDisplayOrder,
        int legalRepDocumentsDisplayOrder,
        int downloadDraftEt3DisplayOrder,
        String viewAllNotificationsName,
        String viewAllNotificationsDescription
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.nocRequestDisplayOrder = nocRequestDisplayOrder;
        this.addCaseNoteDescription = addCaseNoteDescription;
        this.amendRespondentRepresentativeFieldDisplayOrder = amendRespondentRepresentativeFieldDisplayOrder;
        this.amendRespondentDetailsFieldDisplayOrder = amendRespondentDetailsFieldDisplayOrder;
        this.legalRepDocumentsDisplayOrder = legalRepDocumentsDisplayOrder;
        this.downloadDraftEt3DisplayOrder = downloadDraftEt3DisplayOrder;
        this.viewAllNotificationsName = viewAllNotificationsName;
        this.viewAllNotificationsDescription = viewAllNotificationsDescription;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        regionalCaseworkerEvent(configBuilder.event("acceptRejectedCase").forState(EtState.REJECTED))
            .name("Accept Case")
            .description("Accept Case")
            .displayOrder(10)
            .showCondition("caseType =\"dummy\"")
            .caseEventColumn("PostConditionState", ACCEPT_REJECT_POST_CONDITION)
            .fields()
            .page("1")
            .pageLabel("Pre-Acceptance")
            .field(CaseData::getPreAcceptCase)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        configBuilder.event("migrateCaseLinkDetails")
            .forAllStates()
            .name("Migrate Case Link Details")
            .description("Migrate Transferred Case Link")
            .displayOrder(111)
            .showCondition("managingOffice !=\"Unassigned\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/migrateCaseLinkDetails")
            .fields()
            .page("1")
            .field(CaseData::getTransferredCaseLink)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        regionalJudgeEvent(configBuilder.event("retrieveAcasCertificate").forAllStates())
            .name("Search ACAS Certificate")
            .description("Search ACAS Certificate")
            .displayOrder(14)
            .showCondition("managingOffice !=\"Unassigned\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/acasCertificate/retrieveCertificate")
            .submittedCallbackUrl("${ET_COS_URL}/acasCertificate/confirmation")
            .fields()
            .page("1")
            .pageLabel("Retrieve ACAS Certificate")
            .field(CaseData::getAcasCertificate)
            .mandatory()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .done()
            .done();

        regionalCaseworkerEvent(configBuilder.event("preAcceptanceCase").forAllStates())
            .name("Accept/Reject Case")
            .description("Accept/Reject Case")
            .displayOrder(9)
            .showCondition("caseType !=\"Multiple\" AND preAcceptCase.caseAccepted !=\"Yes\" "
                               + "AND managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", "Accepted;Rejected;Vetted")
            .caseEventColumn("PostConditionState", ACCEPT_REJECT_POST_CONDITION)
            .publishToCamunda()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/preAcceptanceCase/aboutToSubmit")
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .fields()
            .page("1")
            .pageLabel("Pre-Acceptance")
            .field(CaseData::getPreAcceptCase)
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/postDefaultValues")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("preAcceptCase=\"dummy\"")
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", "Y")
            .done()
            .done();

        regionalCaseworkerEvent(configBuilder.event("broughtForward").forAllStates())
            .name("B/F Action")
            .description("B/F Action")
            .displayOrder(27)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", BF_ACTION_STATES)
            .publishToCamunda()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/bfActions")
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .fields()
            .page("1")
            .field(CaseData::getBfActions)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        configBuilder.event("WA_EXPIRED_BF_ACTION_TASK")
            .forAllStates()
            .name("WA_EXPIRED_BF_ACTION_TASK")
            .description("Wa Task for expired Bf Actions")
            .showCondition("caseType=\"dummy\"")
            .caseEventColumn("DisplayOrder", null)
            .publishToCamunda()
            .blankCallbackUrls()
            .fields()
            .page("1")
            .field(CaseData::getBfActions)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        regionalCaseworkerEvent(configBuilder.event("uploadDocument").forAllStates())
            .name("Upload Document")
            .description("Upload a Document")
            .displayOrder(29)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", ACTIVE_CASE_STATES)
            .aboutToStartCallbackUrl("${ET_COS_URL}/uploadDocument/aboutToStart")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/uploadDocument/aboutToSubmit")
            .fields()
            .page("1")
            .field(CaseData::getDocumentCollection)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        regionalCaseworkerEvent(generateCorrespondenceFields(configBuilder.event("generateCorrespondence")
            .forStates(EtState.ACCEPTED, EtState.REJECTED, EtState.SUBMITTED, EtState.VETTED, EtState.CLOSED)))
            .name("Letters")
            .description("Generate Letters")
            .displayOrder(30)
            .caseEventColumn("PreConditionState(s)", GENERATE_CORRESPONDENCE_STATES)
            .publishToCamunda()
            .aboutToStartCallbackUrl("${ET_COS_URL}/dynamicLetters")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/generateDocument")
            .submittedCallbackUrl("${ET_COS_URL}/generateDocumentConfirmation");

        regionalCaseworkerEvent(configBuilder.event("addDocument").forAllStates())
            .name("Add Document")
            .description("Add New Documents")
            .displayOrder(65)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", ACTIVE_CASE_STATES)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/addDocument/aboutToSubmit")
            .fields()
            .page("1")
            .field(CaseData::getAddDocumentCollection)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        configBuilder.event("nocRequest")
            .forAllStates()
            .name("NoC Request")
            .description("Notice of Change Request")
            .displayOrder(nocRequestDisplayOrder)
            .showEventNotes()
            .showSummary()
            .submittedCallbackUrl("${CCD_DEF_AAC_URL}/noc/check-noc-approval")
            .fields()
            .page("SingleFormPageWithComplex")
            .pageLabel("Change Organisation Request")
            .field(CaseData::getChangeOrganisationRequestField)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_CAA, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR)
            .authorisationCaseEventColumn(EtUserRole.CASEWORKER_CAA, "LiveFrom", "01/01/2017")
            .authorisationCaseEventColumn(EtUserRole.CASEWORKER_EMPLOYMENT_API, "LiveFrom", "01/01/2017")
            .authorisationCaseEventColumn(
                EtUserRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR,
                "LiveFrom",
                "01/01/2017"
            );

        configBuilder.event("addCaseNote")
            .forAllStates()
            .name("Add Telephone Note")
            .description(addCaseNoteDescription)
            .displayOrder(56)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/caseNotes/singles/aboutToSubmit")
            .fields()
            .page("1")
            .field(CaseData::getAddCaseNote)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done()
            .grant(Permission.CRUD, regionalCaseworkerRole, regionalJudgeRole, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE);

        configBuilder.event("draftAndSignJudgement")
            .forAllStates()
            .name("Draft and sign judgment/order")
            .description("Draft and sign judgment/order")
            .showSummary()
            .showCondition("caseType =\"dummy\"")
            .caseEventColumn("DisplayOrder", null)
            .publishToCamunda()
            .blankCallbackUrls()
            .fields()
            .page("1")
            .field(CaseData::getDraftAndSignJudgement)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done()
            .grant(Permission.CRUD, regionalJudgeRole, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .grant(Permission.R, regionalCaseworkerRole);

        regionalCaseworkerEvent(configBuilder.event("amendRespondentRepresentative").forAllStates())
            .name("Respondent Representative")
            .description("Add or Amend a Respondent Representative")
            .displayOrder(17)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", "Accepted;Closed")
            .aboutToStartCallbackUrl("${ET_COS_URL}/dynamicRespondentRepresentativeNames")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/amendRespondentRepresentative")
            .submittedCallbackUrl("${ET_COS_URL}/amendRespondentRepSubmitted")
            .fields()
            .page("1")
            .field(CaseData::getRepCollection)
            .showSummary()
            .caseEventColumn("PageDisplayOrder", amendRespondentRepresentativeFieldDisplayOrder)
            .caseEventColumn("PageFieldDisplayOrder", amendRespondentRepresentativeFieldDisplayOrder)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        regionalCaseworkerEvent(configBuilder.event("amendRespondentDetails").forAllStates())
            .name("Respondent Details")
            .description("Amend Respondent Details")
            .displayOrder(16)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", "Accepted;Closed;Rejected")
            .publishToCamunda()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/amendRespondentDetails")
            .fields()
            .page("1")
            .field(CaseData::getRespondentCollection)
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("PageDisplayOrder", amendRespondentDetailsFieldDisplayOrder)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("respondentCollection=\"dummy\"")
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageDisplayOrder", 2)
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", "Y")
            .done()
            .done();

        regionalCaseworkerEvent(configBuilder.event("amendClaimantDetails").forAllStates())
            .name("Claimant Details")
            .description("Amend Claimant Details")
            .displayOrder(13)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", "Accepted;Rejected;Closed")
            .publishToCamunda()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/amendClaimantDetails")
            .fields()
            .page("1")
            .field(CaseData::getClaimantTypeOfClaimant)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", null)
            .done()
            .field("claimant_Company ")
            .mandatory()
            .type("Text")
            .label("Company")
            .showCondition("claimant_TypeOfClaimant=\"Company\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getClaimantIndType)
            .showCondition("claimant_TypeOfClaimant=\"Individual\"")
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("2")
            .field(CaseData::getClaimantType)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("3")
            .field(CaseData::getClaimantWorkAddress)
            .showSummary()
            .caseEventColumn("PageShowCondition", "claimant_TypeOfClaimant=\"Individual\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("4")
            .field(CaseData::getCompanyPremises)
            .showSummary()
            .caseEventColumn("PageShowCondition", "claimant_TypeOfClaimant=\"Company\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("5")
            .field(CaseData::getClaimantOtherType)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("6")
            .field(CaseData::getClaimantHearingPreference)
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/midEventHearingPreferences")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        representativeContactFields(
            configBuilder.event("amendClaimantRepresentativeContact")
                .forAllStates()
                .name("Amend contact details")
                .description("Claimant - Amend Contact Details")
                .displayOrder(62)
                .showSummary()
                .caseEventColumn("PreConditionState(s)", "Submitted;Vetted;Accepted;Rejected;")
                .aboutToStartCallbackUrl("${ET_COS_URL}/et1Repped/aboutToStartAmendClaimantRepresentativeContact")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1Repped/aboutToSubmitAmendClaimantRepresentativeContact"),
            "${ET_COS_URL}/et1Repped/midEventAmendClaimantRepresentativeContact",
            CaseData::getRepresentativePhoneNumber,
            CaseData::getRepresentativeAddress,
            false
        )
            .grant(Permission.CRUD, EtUserRole.CLAIMANT_SOLICITOR, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        representativeContactFields(
            configBuilder.event("amendRespondentRepresentativeContact")
                .forAllStates()
                .name("Amend contact details")
                .description("Amend contact details")
                .displayOrder(53)
                .showSummary()
                .showCondition("caseType =\"dummy\"")
                .caseEventColumn("PreConditionState(s)", "Accepted")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Response/aboutToSubmitAmendRepresentativeContact"),
            "${ET_COS_URL}/et3Response/midEventAmendRepresentativeContact",
            CaseData::getEt3ResponsePhone,
            CaseData::getEt3ResponseAddress,
            true
        )
            .grant(Permission.CRUD, EtUserRole.respondentSolicitors())
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("legalrepDocuments")
            .forAllStates()
            .name("Case Documents")
            .description("View case documents")
            .displayOrder(legalRepDocumentsDisplayOrder)
            .showCondition("[STATE]!=\"AWAITING_SUBMISSION_TO_HMCTS\"")
            .aboutToStartCallbackUrl("${ET_COS_URL}/legalrepDocuments/aboutToStart")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/legalrepDocuments/aboutToSubmit")
            .submittedCallbackUrl("")
            .fields()
            .page("1")
            .pageLabel("Case Documents")
            .field(CaseData::getLegalRepDocumentsMarkdown)
            .optional()
            .showCondition("legalRepDocumentsMarkdownLabel=\"dummy\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getLegalRepDocumentsMarkdownLabel)
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done()
            .grant(Permission.R, EtUserRole.ET_ACAS_API)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR);

        grantRespondentSolicitors(
            pseViewNotificationsFields(
                configBuilder.event("viewAllNotifications")
                    .forAllStates()
                    .name(viewAllNotificationsName)
                    .description(viewAllNotificationsDescription)
                    .showCondition("caseType=\"dummy\"")
                    .caseEventColumn("DisplayOrder", null)
                    .aboutToStartCallbackUrl("${ET_COS_URL}/pseViewNotifications/aboutToStart")
                    .aboutToSubmitCallbackUrl("")
                    .submittedCallbackUrl("")
                    .endButtonLabel("Close and return to case details")
            ),
            Permission.CRUD
        )
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR);

        pseViewNotificationsFields(
            configBuilder.event("claimantViewAllNotifications")
                .forAllStates()
                .name("View All Notifications")
                .description("View Notification")
                .displayOrder(60)
                .aboutToStartCallbackUrl("${ET_COS_URL}/claimantViewNotification/all/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/claimantViewNotification/all/aboutToSubmit")
        )
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR);

        claimantViewNotificationFields(
            configBuilder.event("claimantViewNotification")
                .forAllStates()
                .name("View Notification")
                .description("View Notification")
                .displayOrder(59)
                .aboutToStartCallbackUrl("${ET_COS_URL}/claimantViewNotification/aboutToStart")
        )
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        grantRespondentSolicitors(
            pseRespondentViewNotificationFields(
                configBuilder.event("pseRespondentViewNotification")
                    .forAllStates()
                    .name("Judgment, Order, Notification")
                    .description("View a judgment, order or notification")
                    .showCondition("caseType=\"dummy\"")
                    .caseEventColumn("DisplayOrder", null)
                    .aboutToStartCallbackUrl("${ET_COS_URL}/pseRespondentView/aboutToStart")
                    .aboutToSubmitCallbackUrl("")
                    .submittedCallbackUrl("")
                    .endButtonLabel("Close and return to case details")
            ),
            Permission.CRUD
        )
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR);

        grantRespondentSolicitors(
            pseRespondentRespondToTribunalFields(
                configBuilder.event("pseRespondentRespondToTribunal")
                    .forAllStates()
                    .name("Respond to an Order or Request")
                    .description("Respond to an Order or Request")
                    .displayOrder(pseRespondentRespondToTribunalDisplayOrder())
                    .showSummary()
                    .showCondition("caseType=\"dummy\"")
                    .aboutToStartCallbackUrl("${ET_COS_URL}/pseRespondToTribunal/aboutToStart")
                    .aboutToSubmitCallbackUrl("${ET_COS_URL}/pseRespondToTribunal/aboutToSubmit")
                    .submittedCallbackUrl("${ET_COS_URL}/pseRespondToTribunal/submitted")
            ),
            Permission.CRUD
        )
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.D, EtUserRole.CLAIMANT_SOLICITOR);

        et1ReppedCreateCaseFields(configBuilder.event("et1ReppedCreateCase")
            .initialState(EtState.AWAITING_SUBMISSION_TO_HMCTS)
            .name("Create draft claim")
            .description("Create a new draft claim")
            .displayOrder(54)
            .significantEvent()
            .ttlIncrement("365")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1Repped/createCase/aboutToSubmit")
            .submittedCallbackUrl("${ET_COS_URL}/et1Repped/createCase/submitted")
            .endButtonLabel("Create draft claim"))
            .grant(
                Permission.CRUD,
                EtUserRole.CASEWORKER_EMPLOYMENT_API,
                EtUserRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR
            );

        configBuilder.event("createDraftEt1")
            .forState(EtState.AWAITING_SUBMISSION_TO_HMCTS)
            .name("Download draft ET1 Form")
            .description("Download draft ET1 form")
            .displayOrder(58)
            .showCondition("et1ReppedSectionOne = \"Yes\" OR et1ReppedSectionTwo = \"Yes\" "
                               + "OR et1ReppedSectionThree = \"Yes\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1Repped/createDraftEt1")
            .submittedCallbackUrl("${ET_COS_URL}/et1Repped/createDraftEt1Submitted")
            .endButtonLabel("Download draft ET1")
            .fields()
            .page("1")
            .field(CaseData::getEt1DoNotSubmitDraftMessage)
            .readOnly()
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done()
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("submitEt1Draft")
            .forStateTransition(EtState.AWAITING_SUBMISSION_TO_HMCTS, EtState.SUBMITTED)
            .name("Submit ET1 Claim")
            .description("Submit ET1 Claim")
            .displayOrder(57)
            .showCondition("et1ReppedSectionOne = \"Yes\" AND et1ReppedSectionTwo = \"Yes\" "
                               + "AND et1ReppedSectionThree = \"Yes\"")
            .publishToCamunda()
            .significantEvent()
            .ttlIncrement("36524")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1Repped/submitClaim")
            .submittedCallbackUrl("${ET_COS_URL}/et1Repped/submitted")
            .endButtonLabel("Submit ET1")
            .fields()
            .page("1")
            .field(CaseData::getSubmitEt1Preamble)
            .readOnly()
            .caseEventColumn("Publish", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getSubmitEt1Confirmation)
            .mandatory()
            .caseEventColumn("Publish", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done()
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        hearingDateFields(
            configBuilder.event("SUBMIT_CASE_DRAFT")
                .forStateTransition(EtState.AWAITING_SUBMISSION_TO_HMCTS, EtState.SUBMITTED)
                .name("Submit Draft")
                .description("Submit a draft case")
                .displayOrder(6)
                .publishToCamunda()
                .significantEvent()
                .ttlIncrement("36524")
                .aboutToStartCallbackUrl("${ET_COS_URL}/preDefaultValues")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/postDefaultValues")
                .submittedCallbackUrl("${ET_COS_URL}/et1Submission/submitted")
        )
            .grant(Permission.CRUD, EtUserRole.CREATOR, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        grantRespondentSolicitors(
            configBuilder.event("downloadDraftEt3")
                .forState(EtState.ACCEPTED)
                .name("Download draft ET3 Form")
                .description("Download draft ET3 Form")
                .displayOrder(downloadDraftEt3DisplayOrder)
                .caseEventColumn("PostConditionState", "*")
                .aboutToStartCallbackUrl("${ET_COS_URL}/et3Response/downloadDraft/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/et3Response/downloadDraft/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/et3Response/downloadDraft/submitted")
                .endButtonLabel("Download draft ET3 form")
                .fields()
                .page("1")
                .field(CaseData::getDownloadDraftEt3Label)
                .readOnly()
                .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
                .done()
                .field(CaseData::getSubmitEt3Respondent)
                .mandatory()
                .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
                .done()
                .done(),
            Permission.CRU
        )
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        regionalCaseworkerEvent(configBuilder.event("printHearing").forStates(EtState.ACCEPTED, EtState.REJECTED))
            .name("Print Hearing lists")
            .description("Print Hearing documents")
            .displayOrder(25)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PostConditionState", ACCEPT_REJECT_POST_CONDITION)
            .aboutToStartCallbackUrl("${ET_COS_URL}/initPrintHearingLists")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/generateListingsDocSingleCases")
            .submittedCallbackUrl("${ET_COS_URL}/generateListingsDocSingleCasesConfirmation")
            .endButtonLabel("Print List")
            .fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/listingSingleCases")
            .field(CaseData::getPrintHearingDetails)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("2")
            .field(CaseData::getPrintHearingCollection)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        hearingDateFields(
            configBuilder.event("issueInitialConsiderationDirectionsWA")
                .forState(EtState.ACCEPTED)
                .name("Issue IC Directions WA")
                .description("Issue IC Directions WA")
                .caseEventColumn("DisplayOrder", null)
                .caseEventColumn("PostConditionState", "*")
                .publishToCamunda()
                .aboutToStartCallbackUrl("${ET_COS_URL}/startIssueInitialConsiderationDirectionsWA")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/submitIssueInitialConsiderationDirectionsWA")
                .submittedCallbackUrl("${ET_COS_URL}/completeIssueInitialConsiderationDirectionsWA")
        )
            .grant(Permission.CRU, regionalCaseworkerRole, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.R, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        regionalCaseworkerEvent(configBuilder.event("recordDeposit").forState(EtState.ACCEPTED))
            .name("Deposit Order")
            .description("Record a Deposit")
            .displayOrder(28)
            .showCondition("managingOffice !=\"Unassigned\"")
            .aboutToStartCallbackUrl("${ET_COS_URL}/dynamicDepositOrder")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/depositValidation")
            .fields()
            .page("1")
            .field(CaseData::getDepositCollection)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        regionalCaseworkerEvent(configBuilder.event("restrictedCases").forAllStates())
            .name("Restricted Reporting")
            .description("Restricted reporting cases")
            .displayOrder(31)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", "Accepted;Rejected;Closed")
            .aboutToStartCallbackUrl("${ET_COS_URL}/dynamicRestrictedReporting")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/restrictedCases")
            .fields()
            .page("1")
            .field(CaseData::getRestrictedReporting)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        regionalCaseworkerEvent(configBuilder.event("addAmendJurisdiction").forAllStates())
            .name("Jurisdiction")
            .description("Add or Amend a Jurisdiction")
            .displayOrder(addAmendJurisdictionDisplayOrder())
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", ACCEPTED_OR_REJECTED)
            .caseEventColumn("PostConditionState", ACCEPT_REJECT_POST_CONDITION)
            .publishToCamunda()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/addAmendJurisdiction")
            .fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/jurisdictionValidation")
            .field(CaseData::getJurCodesCollection)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        regionalCaseworkerEvent(configBuilder.event("addAmendJudgment").forAllStates())
            .name("Judgment")
            .description("Add or Amend a Judgment")
            .displayOrder(26)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", ACCEPTED_OR_REJECTED)
            .caseEventColumn("PostConditionState", ACCEPT_REJECT_POST_CONDITION)
            .publishToCamunda()
            .aboutToStartCallbackUrl("${ET_COS_URL}/dynamicJudgments")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/judgementSubmitted")
            .fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/judgmentValidation")
            .field(CaseData::getJudgementCollection)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();

        documentAccessEvent(
            configBuilder,
            "adrDocuments",
            "ADR/Privileged Documents",
            "View ADR/Privileged Documents",
            adrDocumentsDisplayOrder(),
            "ADR/Privileged Documents",
            CaseData::getAdrDocumentCollection
        );

        documentAccessEvent(
            configBuilder,
            "piiDocuments",
            "PII Documents",
            "View PII Documents",
            piiDocumentsDisplayOrder(),
            "PII Documents",
            CaseData::getPiiDocumentCollection
        );

        documentAccessEvent(
            configBuilder,
            "appealDocuments",
            "Appeal Documents",
            "Appeal Documents",
            appealDocumentsDisplayOrder(),
            "Appeals Documents",
            CaseData::getAppealDocumentCollection
        );

        configBuilder.event("generateNotificationSummary")
            .forAllStates()
            .name("Generate Notification Summary")
            .description("Generate Notification Summary")
            .displayOrder(57)
            .aboutToStartCallbackUrl("${ET_COS_URL}/notificationDocument/aboutToStart")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/notificationDocument/aboutToSubmit")
            .submittedCallbackUrl("${ET_COS_URL}/notificationDocument/submitted")
            .fields()
            .page("1")
            .field(CaseData::getSelectNotificationDropdown)
            .mandatory()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done()
            .grant(
                Permission.CRUD,
                EtUserRole.CASEWORKER_EMPLOYMENT_API,
                EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE,
                regionalCaseworkerRole,
                regionalJudgeRole
            );
    }

    protected abstract int addAmendJurisdictionDisplayOrder();

    protected abstract int adrDocumentsDisplayOrder();

    protected abstract int piiDocumentsDisplayOrder();

    protected abstract int appealDocumentsDisplayOrder();

    protected abstract int pseRespondentRespondToTribunalDisplayOrder();

    protected abstract boolean pseRespondentRespondToTribunalUnavailableWarningShowsSummary();

    protected abstract String generateCorrespondenceTypeFieldId();

    protected abstract String addressLabelsSelectionPageShowCondition();

    protected abstract String addressLabelsPageShowCondition();

    protected abstract boolean includeEt1ReppedCreateCaseTriageErrorPage();

    protected Event.EventBuilder<T, EtUserRole, EtState> regionalCaseworkerEvent(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> regionalJudgeEvent(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private void documentAccessEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        String description,
        int displayOrder,
        String pageLabel,
        TypedPropertyGetter<T, ?> field
    ) {
        configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(description)
            .displayOrder(displayOrder)
            .fields()
            .page("1")
            .pageLabel(pageLabel)
            .field(field)
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE, regionalCaseworkerRole, regionalJudgeRole);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> pseViewNotificationsFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("All judgments, orders and notifications")
            .field(CaseData::getPseViewNotifications)
            .readOnly()
            .showCondition("pseViewNotificationsLabel=\"dummy\"")
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseViewNotificationsLabel)
            .readOnly()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> representativeContactFields(
        Event.EventBuilder<T, EtUserRole, EtState> event,
        String changeOptionCallbackUrl,
        TypedPropertyGetter<T, ?> phoneField,
        TypedPropertyGetter<T, ?> addressField,
        boolean retainHiddenContactDetails
    ) {
        String retainHiddenValue = retainHiddenContactDetails ? "Yes" : null;

        return event.fields()
            .pageWithCallbackUrl("1", changeOptionCallbackUrl)
            .field(CaseData::getRepresentativeContactChangeOption)
            .optional()
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .field(phoneField)
            .optional()
            .showSummary()
            .caseEventColumn("PageShowCondition", "representativeContactChangeOption=\"Amend contact details\"")
            .caseEventColumn("retainHiddenValue", retainHiddenValue)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(addressField)
            .optional()
            .showSummary()
            .caseEventColumn("retainHiddenValue", retainHiddenValue)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("3")
            .field(CaseData::getMyHmctsAddressText)
            .readOnly()
            .showSummary()
            .caseEventColumn("PageDisplayOrder", 2)
            .caseEventColumn("PageShowCondition", "representativeContactChangeOption=\"Use MyHMCTS details\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> claimantViewNotificationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/claimantViewNotification/midDetailsTable")
            .pageLabel("View a judgment, order or notification")
            .field(CaseData::getClaimantSelectNotification)
            .mandatory()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .pageLabel("View a judgment, order or notification")
            .field(CaseData::getClaimantNotificationTableMarkdown)
            .readOnly()
            .showCondition("claimantNotificationTableMarkdownLabel=\"dummy\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getClaimantNotificationTableMarkdownLabel)
            .readOnly()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> et1ReppedCreateCaseFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var fields = event.fields()
            .page("1")
            .pageLabel("Claimant's work location")
            .field("et1ReppedTriageLabel")
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt1ReppedTriageAddress)
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getEt1ReppedTriageYesNo)
            .readOnly()
            .showCondition("et1ReppedTriageAddress=\"dummy\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et1Repped/createCase/validatePostcode")
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done();

        if (includeEt1ReppedCreateCaseTriageErrorPage()) {
            fields.page("2")
                .pageLabel("Service not available yet")
                .field("et1ReppedTriageError")
                .readOnly()
                .caseEventColumn("PageShowCondition", "et1ReppedTriageYesNo=\"No\"")
                .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/et1Repped/createCase/officeError")
                .caseEventColumn(PAGE_COLUMN_NUMBER, null)
                .done();
        }

        return fields.done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> generateCorrespondenceFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field(generateCorrespondenceTypeFieldId())
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/midAddressLabels")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", null)
            .done()
            .page("2")
            .field(CaseData::getAddressLabelsSelectionType)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageShowCondition", addressLabelsSelectionPageShowCondition())
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/midAddressLabels")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .caseEventColumn("Publish", null)
            .done()
            .page("3")
            .field(CaseData::getAddressLabelCollection)
            .showSummary()
            .caseEventColumn("PageShowCondition", addressLabelsPageShowCondition())
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/midSelectedAddressLabels")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("4")
            .field(CaseData::getAddressLabelsAttributesType)
            .showSummary()
            .caseEventColumn("PageShowCondition", addressLabelsPageShowCondition())
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/midValidateAddressLabels")
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> pseRespondentRespondToTribunalFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field("respondToTribunalNotAvailableWarning")
            .readOnly()
            .type("Text")
            .label(" ")
            .showCondition("pseRespondentSelectOrderOrRequest=\"dummy\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/pseRespondToTribunal/showError")
            .caseEventColumn(
                "ShowSummaryChangeOption",
                pseRespondentRespondToTribunalUnavailableWarningShowsSummary() ? "Y" : null
            )
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .field("respondToTribunalNotAvailableWarningLabel")
            .readOnly()
            .type("Label")
            .label(RESPOND_TO_TRIBUNAL_NOT_AVAILABLE_LABEL)
            .showCondition("respondToTribunalNotAvailableWarning=\"Yes\"")
            .showSummary()
            .caseEventColumn("PageShowCondition", "respondToTribunalNotAvailableWarning=\"Yes\"")
            .caseEventColumn("PageDisplayOrder", null)
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .page("2")
            .field(CaseData::getPseRespondentSelectOrderOrRequest)
            .mandatory()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/pseRespondToTribunal/midDetailsTable")
            .caseEventColumn("PageLabel", "Select an Order or Request")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("3")
            .field(CaseData::getPseRespondentOrdReqTableMarkUp)
            .readOnly()
            .showCondition("pseRespondentRequestOrderTableLabel=\"dummy\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/pseRespondToTribunal/midValidateInput")
            .caseEventColumn("PageLabel", "Your response")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseRespondentRequestOrderTableLabel)
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseRespondentOrdReqResponseText)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseRespondentOrdReqHasSupportingMaterial)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field("pseRespondentOrdReqPage2TableLabel")
            .readOnly()
            .type("Label")
            .label(PSE_RESPONDENT_SUPPORTING_MATERIAL_LABEL)
            .showCondition("pseRespondentOrdReqHasSupportingMaterial=\"Yes\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseRespondentOrdReqUploadDocument)
            .showCondition("pseRespondentOrdReqHasSupportingMaterial=\"Yes\"")
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("4")
            .field("pseRespondentOrdReqCopyPartyIntro")
            .readOnly()
            .type("Label")
            .label(PSE_RESPONDENT_COPY_PARTY_INTRO)
            .caseEventColumn("PageLabel", "Copy this correspondence to the other party")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseRespondentOrdReqCopyToOtherParty)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseRespondentOrdReqCopyNoGiveDetails)
            .mandatory()
            .showCondition("pseRespondentOrdReqCopyToOtherParty=\"No\"")
            .showSummary()
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> pseRespondentViewNotificationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .pageWithCallbackUrl("1", "${ET_COS_URL}/pseRespondentView/midDetailsTable")
            .pageLabel("View a judgment, order or notification")
            .field(CaseData::getPseRespondentSelectJudgmentOrderNotification)
            .mandatory()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .pageLabel("View a judgment, order or notification")
            .field(CaseData::getPseRespondentOrdReqTableMarkUp)
            .readOnly()
            .showCondition("pseRespondentRequestOrderTableLabel=\"dummy\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getPseRespondentRequestOrderTableLabel)
            .readOnly()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> hearingDateFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel(" ")
            .field(CaseData::getHorizontalLine)
            .readOnly()
            .caseEventColumn("Publish", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("horizontalLine=\"dummy\"")
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("Publish", "Y")
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, 1)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> grantRespondentSolicitors(
        Event.EventBuilder<T, EtUserRole, EtState> event,
        Set<Permission> permissions
    ) {
        return event.grant(permissions, EtUserRole.respondentSolicitors());
    }
}
