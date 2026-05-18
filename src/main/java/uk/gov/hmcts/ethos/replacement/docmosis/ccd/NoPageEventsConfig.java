package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class NoPageEventsConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String ACTIVE_CASE_STATES = "Submitted;Vetted;Accepted;Rejected;Closed";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final int reconfigureWaTasksDisplayOrder;

    protected NoPageEventsConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        int reconfigureWaTasksDisplayOrder
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.reconfigureWaTasksDisplayOrder = reconfigureWaTasksDisplayOrder;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.event("generateEt1Documents")
            .forAllStates()
            .name("generateEt1Documents")
            .description("Generate ET1 docs in the background")
            .displayOrder(58)
            .showCondition("caseType = \"dummy\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/et1Repped/generateDocuments")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("WA_REVIEW_RULE21_REFERRAL")
            .forAllStates()
            .name("WA_REVIEW_RULE21_REFERRAL")
            .description("")
            .showCondition("caseType=\"dummy\"")
            .caseEventColumn("DisplayOrder", null)
            .publishToCamunda()
            .blankCallbackUrls()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        configBuilder.event("RECONFIGURE_WA_TASKS")
            .forAllStates()
            .name("Reconfigure WA tasks")
            .description("Reconfigure WA tasks")
            .displayOrder(reconfigureWaTasksDisplayOrder)
            .showSummary()
            .showCondition("caseType=\"dummy\"")
            .publishToCamunda()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        configBuilder.event("REMOVE_OWN_REP_AS_RESPONDENT")
            .forAllStates()
            .name("REMOVE_OWN_REP_AS_RESPONDENT")
            .description("enables respondent citizen to remove his/her own representative via et-syr-frontend project")
            .displayOrder(6)
            .showCondition("caseType =\"dummy\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/respondentRepresentative/removeOwnRepresentative")
            .grant(Permission.CRUD, EtUserRole.DEFENDANT, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("REMOVE_OWN_REP_AS_CLAIMANT")
            .forAllStates()
            .name("REMOVE_OWN_REP_AS_CLAIMANT")
            .description("enables claimant citizen to remove his/her own representative via et-sya-frontend project")
            .displayOrder(6)
            .showCondition("caseType =\"dummy\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/claimantRepresentative/removeOwnRepresentative")
            .grant(Permission.CRUD, EtUserRole.CREATOR, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("refreshSharedUsers")
            .forAllStates()
            .name("Refresh Shared Users")
            .description("Refresh Shared Users")
            .displayOrder(72)
            .showCondition("managingOffice !=\"Unassigned\"")
            .caseEventColumn("PreConditionState(s)", ACTIVE_CASE_STATES)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/refreshSharedUsers/aboutToSubmit")
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole,
                EtUserRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("claimantTransferredCaseAccess")
            .forAllStates()
            .name("Assign Claimant Access to Case")
            .description("Give the claimant access back to the case after it's been transferred")
            .caseEventColumn("DisplayOrder", null)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/caseAccess/claimant/transferredCase")
            .grant(Permission.CRUD, regionalCaseworkerRole, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }
}
