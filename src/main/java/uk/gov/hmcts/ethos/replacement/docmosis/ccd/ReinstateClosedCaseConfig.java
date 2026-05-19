package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class ReinstateClosedCaseConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String POST_CONDITION_STATES =
        "Accepted(preAcceptCase.caseAccepted=\"Yes\" AND positionType!=\"Case closed\"):1;"
            + "Rejected(preAcceptCase.caseAccepted=\"No\" AND positionType!=\"Case closed\"):2;"
            + "Closed";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final int displayOrder;

    protected ReinstateClosedCaseConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        int displayOrder
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.displayOrder = displayOrder;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        reinstateFields(
            configBuilder.event("reinstateClosedCase")
                .forState(EtState.CLOSED)
                .name("Reinstate Case")
                .description("Reinstate Case")
                .displayOrder(displayOrder)
                .caseEventColumn("PostConditionState", POST_CONDITION_STATES)
                .caseEventColumn("SignificantEvent", "${ET_ENV_RETENTION_SIGNIFICANT_EVENT}")
                .caseEventColumn("TTLIncrement", "${ET_ENV_REINSTATE_CLOSED_CASE_TTL_INCREMENT}")
        )
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> reinstateFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field("positionType")
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/reinstateClosedCaseMidEventValidation")
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field("preAcceptCase")
            .showSummary()
            .showCondition("positionType !=\"Case closed\"")
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .done();
    }
}
