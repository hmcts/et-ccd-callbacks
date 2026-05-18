package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.EnumSet;

public abstract class EcmMigrationConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String ECM_POST_CONDITION_STATES = "Submitted(stateAPI=\"Submitted\"):1;"
        + "Accepted(stateAPI=\"Accepted\"):2;"
        + "Rejected(stateAPI=\"Rejected\"):3;"
        + "Transferred(stateAPI=\"Transferred\"):4;"
        + "Closed(stateAPI=\"Closed\"):5;"
        + "Vetted(stateAPI=\"Vetted\"):6;*";

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.event("migrateCase")
            .forAllStates()
            .name("Migrate Case")
            .description("Migrate Case")
            .caseEventColumn("DisplayOrder", null)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/global-search-migration/about-to-submit")
            .submittedCallbackUrl("${ET_COS_URL}/global-search-migration/submitted")
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("createEcmCase")
            .forAllStates()
            .name("Create ECM Case")
            .description("Create ECM Case from Migration")
            .displayOrder(37)
            .publishToCamunda()
            .caseEventColumn("PreConditionState(s)", null)
            .caseEventColumn("PostConditionState", ECM_POST_CONDITION_STATES)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/postDefaultValues")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        configBuilder.event("rollbackMigrateCase")
            .forStateTransition(EnumSet.allOf(EtState.class), EtState.DELETE)
            .name("Rollback Migrate Case")
            .description("Rollback Migrate Case")
            .displayOrder(37)
            .significantEvent()
            .publishToCamunda()
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/migrate/rollback/aboutToSubmit")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);
    }
}
