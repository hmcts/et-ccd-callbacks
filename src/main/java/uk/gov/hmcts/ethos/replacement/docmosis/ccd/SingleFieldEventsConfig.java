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
    private static final String ACCEPT_REJECT_POST_CONDITION =
        "Accepted(preAcceptCase.caseAccepted=\"Yes\"):1;Rejected";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final int nocRequestDisplayOrder;
    private final String addCaseNoteDescription;
    private final int amendRespondentRepresentativeFieldDisplayOrder;
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .done();

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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", null)
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
            .caseEventColumn("PageColumnNumber", null)
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .done();

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
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getLegalRepDocumentsMarkdownLabel)
            .readOnly()
            .caseEventColumn("PageColumnNumber", null)
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
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .done()
            .grant(Permission.CRU, EtUserRole.CLAIMANT_SOLICITOR)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

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
                .caseEventColumn("PageColumnNumber", 1)
                .done()
                .field(CaseData::getSubmitEt3Respondent)
                .mandatory()
                .caseEventColumn("PageColumnNumber", 1)
                .done()
                .done(),
            Permission.CRU
        )
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_API);

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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", null)
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
            .caseEventColumn("PageColumnNumber", 1)
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
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getPseViewNotificationsLabel)
            .readOnly()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> grantRespondentSolicitors(
        Event.EventBuilder<T, EtUserRole, EtState> event,
        Set<Permission> permissions
    ) {
        return event.grant(
            permissions,
            EtUserRole.SOLICITOR_A,
            EtUserRole.SOLICITOR_B,
            EtUserRole.SOLICITOR_C,
            EtUserRole.SOLICITOR_D,
            EtUserRole.SOLICITOR_E,
            EtUserRole.SOLICITOR_F,
            EtUserRole.SOLICITOR_G,
            EtUserRole.SOLICITOR_H,
            EtUserRole.SOLICITOR_I,
            EtUserRole.SOLICITOR_J
        );
    }
}
