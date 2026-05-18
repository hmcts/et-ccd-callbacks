package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class Et3Config<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean grantSubmitToApi;

    protected Et3Config(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean grantSubmitToApi
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.grantSubmitToApi = grantSubmitToApi;
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
}
