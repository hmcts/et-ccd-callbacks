package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class ApplicationStateConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final int updateApplicationStateDisplayOrder;
    private final boolean includeAdminDecisionState;

    protected ApplicationStateConfig(int updateApplicationStateDisplayOrder, boolean includeAdminDecisionState) {
        this.updateApplicationStateDisplayOrder = updateApplicationStateDisplayOrder;
        this.includeAdminDecisionState = includeAdminDecisionState;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.event("UPDATE_APPLICATION_STATE")
            .forAllStates()
            .name("View an application")
            .description("View an application")
            .displayOrder(updateApplicationStateDisplayOrder)
            .showCondition("caseType=\"dummy\"")
            .blankCallbackUrls()
            .grant(Permission.CRUD, EtUserRole.CITIZEN);

        if (includeAdminDecisionState) {
            configBuilder.event("UPDATE_ADMIN_DECISION_STATE")
                .forAllStates()
                .name("UPDATE_ADMIN_DECISION_STATE")
                .description("")
                .displayOrder(9002)
                .showCondition("caseType=\"Dummy\"")
                .blankCallbackUrls()
                .grant(Permission.CRU, EtUserRole.CITIZEN);
        }
    }
}
