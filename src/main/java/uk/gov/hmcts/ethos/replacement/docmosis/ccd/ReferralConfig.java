package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class ReferralConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String PAGE_LABEL = "PageLabel";
    private static final String PUBLISH = "Publish";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final int closeReferralDisplayOrder;

    protected ReferralConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        int closeReferralDisplayOrder
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.closeReferralDisplayOrder = closeReferralDisplayOrder;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        closeReferralFields(
            configBuilder.event("closeReferral")
                .forAllStates()
                .name("Close Referral")
                .description("Close referral")
                .displayOrder(closeReferralDisplayOrder)
                .showSummary()
                .showCondition("caseType =\"dummy\"")
                .publishToCamunda()
                .aboutToStartCallbackUrl("${ET_COS_URL}/closeReferral/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/closeReferral/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/closeReferral/completeCloseReferral")
        )
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> closeReferralFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("Close referral")
            .field(CaseData::getSelectReferral)
            .mandatory()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/closeReferral/initHearingAndReferralDetails")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .page("2")
            .pageLabel("Close referral")
            .field(CaseData::getCloseReferralHearingDetails)
            .readOnly()
            .showCondition("closeReferralHearingDetailsLabel=\"dummy\"")
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getCloseReferralHearingDetailsLabel)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getConfirmCloseReferral)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getCloseReferralGeneralNotes)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .done();
    }
}
