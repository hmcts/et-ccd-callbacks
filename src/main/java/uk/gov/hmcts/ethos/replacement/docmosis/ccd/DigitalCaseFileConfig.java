package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;

public abstract class DigitalCaseFileConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final TypedPropertyGetter<T, String> uploadOrRemoveDcf;
    private final TypedPropertyGetter<T, DigitalCaseFileType> digitalCaseFile;
    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;

    protected DigitalCaseFileConfig(
        TypedPropertyGetter<T, String> uploadOrRemoveDcf,
        TypedPropertyGetter<T, DigitalCaseFileType> digitalCaseFile,
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole
    ) {
        this.uploadOrRemoveDcf = uploadOrRemoveDcf;
        this.digitalCaseFile = digitalCaseFile;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        digitalCaseFileEvent(configBuilder, "createDcf", "Create, Upload or Remove DCF", 29)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/dcf/asyncAboutToSubmit")
            .fields()
            .page("1")
            .field(uploadOrRemoveDcf)
            .mandatory()
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .done()
            .field(digitalCaseFile)
            .showCondition("uploadOrRemoveDcf=\"Upload\"")
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .done()
            .done()
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole)
            .grant(Permission.CRU, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        digitalCaseFileEvent(configBuilder, "asyncStitchingComplete", "Stitching bundle complete", 9000)
            .showCondition("caseType=\"dummy\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/dcf/asyncCompleteAboutToSubmit")
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole)
            .grant(Permission.CRU, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> digitalCaseFileEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        int displayOrder
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(name)
            .displayOrder(displayOrder);
    }
}
