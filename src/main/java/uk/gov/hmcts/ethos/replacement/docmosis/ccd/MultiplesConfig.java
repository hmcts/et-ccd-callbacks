package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.EnumSet;

public abstract class MultiplesConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;

    protected MultiplesConfig(EtUserRole regionalCaseworkerRole, EtUserRole regionalJudgeRole) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.event("createLeadCase")
            .forStateTransition(EtState.ACCEPTED, EnumSet.allOf(EtState.class))
            .name("Create Lead Case")
            .description("Create Lead Case")
            .caseEventColumn("DisplayOrder", null)
            .caseEventColumn("EventEnablingCondition", "")
            .blankCallbackUrls()
            .grant(Permission.CRUD, regionalCaseworkerRole);

        configBuilder.event("sendNotificationMultiple")
            .forStateTransition(EtState.ACCEPTED, EnumSet.allOf(EtState.class))
            .name("Notification from Multiple")
            .description("Notification from Multiple")
            .showSummary()
            .showCondition("state=\"dummy\"")
            .caseEventColumn("DisplayOrder", null)
            .aboutToStartCallbackUrl("")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/sendNotification/aboutToSubmit")
            .submittedCallbackUrl("")
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("addLegalRepToMultiple")
            .forAllStates()
            .name("Add me to Multiple")
            .description("Add me to Multiple")
            .showCondition("caseType=\"Multiple\"")
            .caseEventColumn("DisplayOrder", null)
            .aboutToStartCallbackUrl("${ET_COS_URL}/multiples/addLegalRepToMultiple/aboutToStart")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/multiples/addLegalRepToMultiple/aboutToSubmit")
            .submittedCallbackUrl("${ET_COS_URL}/multiples/addLegalRepToMultiple/completed")
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR);
    }
}
