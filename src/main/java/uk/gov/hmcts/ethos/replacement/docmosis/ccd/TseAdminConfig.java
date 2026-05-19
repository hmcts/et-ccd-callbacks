package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class TseAdminConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;

    protected TseAdminConfig(EtUserRole regionalCaseworkerRole, EtUserRole regionalJudgeRole) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        closeApplicationFields(
            configBuilder.event("tseAdminCloseAnApplication")
                .forAllStates()
                .name("Close an application")
                .description("Close an application")
                .showSummary()
                .showCondition("caseType=\"dummy\"")
                .caseEventColumn("DisplayOrder", null)
                .aboutToStartCallbackUrl("${ET_COS_URL}/tseAdmin/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/tseAdmin/aboutToSubmitCloseApplication")
                .submittedCallbackUrl("${ET_COS_URL}/tseAdmin/submittedCloseApplication")
        )
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.R, EtUserRole.ET_ACAS_API, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalJudgeRole, regionalCaseworkerRole);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> closeApplicationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("Close an application")
            .field(CaseData::getTseAdminSelectApplication)
            .mandatory()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/tseAdmin/displayCloseApplicationTable")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .field(CaseData::getTseAdminCloseApplicationTable)
            .mandatory()
            .showCondition("tseAdminCloseApplicationTableLabel=\"dummy\"")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminCloseApplicationTableLabel)
            .readOnly()
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminCloseApplicationYes)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 4)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminCloseApplicationText)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 5)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }
}
