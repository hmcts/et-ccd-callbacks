package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

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

    protected SingleFieldEventsConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        int nocRequestDisplayOrder,
        String addCaseNoteDescription
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.nocRequestDisplayOrder = nocRequestDisplayOrder;
        this.addCaseNoteDescription = addCaseNoteDescription;
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

    private Event.EventBuilder<T, EtUserRole, EtState> regionalCaseworkerEvent(
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
}
