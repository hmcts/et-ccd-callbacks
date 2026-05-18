package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class SingleFieldEventsConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String ACTIVE_CASE_STATES = "Submitted;Vetted;Accepted;Rejected;Closed";
    private static final String BF_ACTION_STATES = "Accepted;Rejected;Submitted;Vetted";

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
            .caseEventColumn("PostConditionState", "Accepted(preAcceptCase.caseAccepted=\"Yes\"):1;Rejected")
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
    }

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
}
