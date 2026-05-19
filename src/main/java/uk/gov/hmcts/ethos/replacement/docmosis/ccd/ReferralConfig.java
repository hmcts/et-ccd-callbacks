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
    private final int createReferralDisplayOrder;
    private final String createReferralDescription;
    private final int createReferralNextListedDateDisplayOrder;
    private final int createReferralReferentEmailDisplayOrder;
    private final int updateReferralDisplayOrder;
    private final Object updateReferralSelectPageColumnNumber;
    private final boolean retainHiddenUpdateReferralSubjectSpecify;
    private final int replyToReferralDisplayOrder;
    private final int closeReferralDisplayOrder;

    protected ReferralConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        int createReferralDisplayOrder,
        String createReferralDescription,
        int createReferralNextListedDateDisplayOrder,
        int createReferralReferentEmailDisplayOrder,
        int updateReferralDisplayOrder,
        Object updateReferralSelectPageColumnNumber,
        boolean retainHiddenUpdateReferralSubjectSpecify,
        int replyToReferralDisplayOrder,
        int closeReferralDisplayOrder
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.createReferralDisplayOrder = createReferralDisplayOrder;
        this.createReferralDescription = createReferralDescription;
        this.createReferralNextListedDateDisplayOrder = createReferralNextListedDateDisplayOrder;
        this.createReferralReferentEmailDisplayOrder = createReferralReferentEmailDisplayOrder;
        this.updateReferralDisplayOrder = updateReferralDisplayOrder;
        this.updateReferralSelectPageColumnNumber = updateReferralSelectPageColumnNumber;
        this.retainHiddenUpdateReferralSubjectSpecify = retainHiddenUpdateReferralSubjectSpecify;
        this.replyToReferralDisplayOrder = replyToReferralDisplayOrder;
        this.closeReferralDisplayOrder = closeReferralDisplayOrder;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        grantReferralAccess(createReferralFields(
            configBuilder.event("createReferral")
                .forAllStates()
                .name("Create Referral")
                .description(createReferralDescription)
                .displayOrder(createReferralDisplayOrder)
                .showSummary()
                .showCondition("caseType =\"dummy\"")
                .publishToCamunda()
                .aboutToStartCallbackUrl("${ET_COS_URL}/createReferral/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/createReferral/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/createReferral/completeCreateReferral")
        ));

        grantReferralAccess(updateReferralFields(
            configBuilder.event("updateReferral")
                .forAllStates()
                .name("Update Referral")
                .description("Update Referral")
                .displayOrder(updateReferralDisplayOrder)
                .showSummary()
                .showCondition("caseType =\"dummy\"")
                .publishToCamunda()
                .aboutToStartCallbackUrl("${ET_COS_URL}/updateReferral/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/updateReferral/aboutToSubmit")
        ));

        grantReferralAccess(replyToReferralFields(
            configBuilder.event("replyToReferral")
                .forAllStates()
                .name("Reply to Referral")
                .description("Refer to admin, legal officer or judge")
                .displayOrder(replyToReferralDisplayOrder)
                .showSummary()
                .showCondition("caseType =\"dummy\"")
                .publishToCamunda()
                .aboutToStartCallbackUrl("${ET_COS_URL}/replyReferral/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/replyReferral/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/replyReferral/completeReplyToReferral")
        ));

        grantReferralAccess(closeReferralFields(
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
        ));
    }

    private Event.EventBuilder<T, EtUserRole, EtState> grantReferralAccess(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> createReferralFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("Refer to admin, legal officer or judge")
            .field(CaseData::getReferralHearingDetails)
            .readOnly()
            .showSummary()
            .showCondition("referralHearingDetailsLabel=\"dummy\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/createReferral/validateReferentEmail")
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralHearingDetailsLabel)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferCaseTo)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferentEmail)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", createReferralReferentEmailDisplayOrder)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getIsUrgent)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 5)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralSubject)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 6)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralSubjectSpecify)
            .mandatory()
            .showSummary()
            .showCondition("referralSubject =\"Other\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 7)
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralDetails)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 8)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralDocument)
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 9)
            .done()
            .field(CaseData::getReferralInstruction)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 10)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralCollection)
            .showCondition("referralHearingDetailsLabel=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 11)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("referralHearingDetailsLabel=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", createReferralNextListedDateDisplayOrder)
            .caseEventColumn(PUBLISH, "Y")
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> replyToReferralFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("Refer to admin, legal officer or judge")
            .field(CaseData::getSelectReferral)
            .mandatory()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/replyReferral/initHearingAndReferralDetails")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getIsJudge)
            .readOnly()
            .showCondition("selectReferral=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralCollection)
            .showCondition("selectReferral=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("selectReferral=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .caseEventColumn(PUBLISH, "Y")
            .done()
            .page("2")
            .pageLabel("Refer to admin, legal officer or judge")
            .field(CaseData::getReplyToReferralDcfLink)
            .readOnly()
            .showCondition("replyToReferralDcfLinkLabel=\"dummy\"")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/replyReferral/validateReplyToEmail")
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReplyToReferralDcfLinkLabel)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getHearingAndReferralDetails)
            .readOnly()
            .showCondition("hearingAndReferralDetailsLabel=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getHearingAndReferralDetailsLabel)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getDirectionTo)
            .mandatory()
            .showSummary()
            .showCondition("isJudge=\"True\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReplyTo)
            .mandatory()
            .showSummary()
            .showCondition("isJudge=\"False\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReplyToEmailAddress)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getIsUrgentReply)
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getDirectionDetails)
            .mandatory()
            .showSummary()
            .showCondition("isJudge=\"True\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReplyDetails)
            .mandatory()
            .showSummary()
            .showCondition("isJudge=\"False\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReplyDocument)
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .done()
            .field(CaseData::getReplyGeneralNotes)
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> updateReferralFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var updateReferralSubjectSpecifyField = event.fields()
            .page("1")
            .pageLabel("Update Referral")
            .field(CaseData::getSelectReferral)
            .mandatory()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/updateReferral/initHearingAndReferralDetails")
            .caseEventColumn(PAGE_COLUMN_NUMBER, updateReferralSelectPageColumnNumber)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralCollection)
            .showSummary()
            .showCondition("selectReferral=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .done()
            .field(CaseData::getNextListedDate)
            .optional()
            .showCondition("selectReferral=\"dummy\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 4)
            .caseEventColumn(PUBLISH, "Y")
            .done()
            .page("2")
            .pageLabel("Refer to admin, legal officer or judge")
            .field(CaseData::getReferralHearingDetails)
            .readOnly()
            .showCondition("referralHearingDetailsLabel=\"dummy\"")
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getReferralHearingDetailsLabel)
            .readOnly()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getUpdateReferCaseTo)
            .mandatory()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getUpdateReferentEmail)
            .optional()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 4)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getUpdateIsUrgent)
            .mandatory()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 5)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getUpdateReferralSubject)
            .mandatory()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 6)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getUpdateReferralDetails)
            .mandatory()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 7)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field(CaseData::getUpdateReferralDocument)
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 8)
            .done()
            .field(CaseData::getUpdateReferralSubjectSpecify)
            .mandatory()
            .showCondition("updateReferralSubject =\"Other\"")
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 9)
            .caseEventColumn(PUBLISH, null);

        if (retainHiddenUpdateReferralSubjectSpecify) {
            updateReferralSubjectSpecifyField.caseEventColumn("RetainHiddenValue", "Yes");
        }

        return updateReferralSubjectSpecifyField
            .done()
            .field(CaseData::getUpdateReferralInstruction)
            .optional()
            .caseEventColumn(PAGE_LABEL, null)
            .caseEventColumn("PageFieldDisplayOrder", 10)
            .caseEventColumn(PUBLISH, null)
            .done()
            .done();
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
