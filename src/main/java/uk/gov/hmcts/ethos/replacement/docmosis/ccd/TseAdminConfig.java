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
    private final Integer recordDecisionDisplayOrder;
    private final Integer respondToApplicationDisplayOrder;
    private final boolean scotlandRecordDecisionRows;

    protected TseAdminConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        Integer recordDecisionDisplayOrder,
        Integer respondToApplicationDisplayOrder,
        boolean scotlandRecordDecisionRows
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.recordDecisionDisplayOrder = recordDecisionDisplayOrder;
        this.respondToApplicationDisplayOrder = respondToApplicationDisplayOrder;
        this.scotlandRecordDecisionRows = scotlandRecordDecisionRows;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        grantTseAdminAccess(
            recordDecisionFields(
                tseAdminEvent(
                    configBuilder,
                    "tseAdmin",
                    "Record a decision",
                    recordDecisionDisplayOrder,
                    "${ET_COS_URL}/tseAdmin/aboutToStart",
                    "${ET_COS_URL}/tseAdmin/aboutToSubmit",
                    "${ET_COS_URL}/tseAdmin/submitted"
                )
            )
        );

        grantTseAdminAccess(
            respondToApplicationFields(
                tseAdminEvent(
                    configBuilder,
                    "tseAdmReply",
                    "Respond to an application",
                    respondToApplicationDisplayOrder,
                    "${ET_COS_URL}/tseAdmin/aboutToStart",
                    "${ET_COS_URL}/tseAdmReply/aboutToSubmit",
                    "${ET_COS_URL}/tseAdmReply/submitted"
                )
            )
        );

        grantTseAdminAccess(
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
        );
    }

    private Event.EventBuilder<T, EtUserRole, EtState> tseAdminEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        Integer displayOrder,
        String aboutToStartCallbackUrl,
        String aboutToSubmitCallbackUrl,
        String submittedCallbackUrl
    ) {
        Event.EventBuilder<T, EtUserRole, EtState> event = configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(name)
            .showSummary()
            .showCondition("caseType=\"dummy\"")
            .aboutToStartCallbackUrl(aboutToStartCallbackUrl)
            .aboutToSubmitCallbackUrl(aboutToSubmitCallbackUrl)
            .submittedCallbackUrl(submittedCallbackUrl);

        if (displayOrder == null) {
            event.caseEventColumn("DisplayOrder", null);
        } else {
            event.displayOrder(displayOrder);
        }

        return event;
    }

    private Event.EventBuilder<T, EtUserRole, EtState> grantTseAdminAccess(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.R, EtUserRole.ET_ACAS_API, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalJudgeRole, regionalCaseworkerRole);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> recordDecisionFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        if (scotlandRecordDecisionRows) {
            return scotlandRecordDecisionFields(event);
        }

        return event.fields()
            .page("1")
            .pageLabel("Select an application")
            .field(CaseData::getTseAdminSelectApplication)
            .mandatory()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/tseAdmin/midDetailsTable")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .field(CaseData::getTseAdminTableMarkUp)
            .readOnly()
            .showCondition("tseAdminTableLabel=\"dummy\"")
            .caseEventColumn("PageLabel", "Record a decision")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminTableLabel)
            .readOnly()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminEnterNotificationTitle)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecision)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecisionDetails)
            .mandatory()
            .showCondition("tseAdminDecision=\"Other\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminTypeOfDecision)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminIsResponseRequired)
            .mandatory()
            .showCondition("tseAdminTypeOfDecision=\"Case management order\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminSelectPartyRespond)
            .mandatory()
            .showCondition("tseAdminTypeOfDecision=\"Case management order\" AND tseAdminIsResponseRequired=\"Yes\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminResponseRequiredYesDoc)
            .optional()
            .showCondition("tseAdminTypeOfDecision=\"Case management order\" AND tseAdminIsResponseRequired=\"Yes\"")
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminResponseRequiredNoDoc)
            .optional()
            .showCondition("tseAdminTypeOfDecision=\"Judgment\" OR tseAdminIsResponseRequired=\"No\"")
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminAdditionalInformation)
            .optional()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecisionMadeBy)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecisionMadeByFullName)
            .mandatory()
            .showCondition("tseAdminDecisionMadeBy=\"Legal officer\" OR tseAdminDecisionMadeBy=\"Judge\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminSelectPartyNotify)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> scotlandRecordDecisionFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("Select an application")
            .field(CaseData::getTseAdminSelectApplication)
            .mandatory()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/tseAdmin/midDetailsTable")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .field(CaseData::getTseAdminAdditionalInformation)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 11)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecision)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 4)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecisionDetails)
            .mandatory()
            .showCondition("tseAdminDecision=\"Other\"")
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 5)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecisionMadeBy)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 12)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminDecisionMadeByFullName)
            .mandatory()
            .showCondition("tseAdminDecisionMadeBy=\"Legal officer\" OR tseAdminDecisionMadeBy=\"Judge\"")
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 13)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminEnterNotificationTitle)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminIsResponseRequired)
            .mandatory()
            .showCondition("tseAdminTypeOfDecision=\"Case management order\"")
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 7)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminResponseRequiredNoDoc)
            .optional()
            .showCondition("tseAdminTypeOfDecision=\"Judgment\" OR tseAdminIsResponseRequired=\"No\"")
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("PageFieldDisplayOrder", 10)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminResponseRequiredYesDoc)
            .optional()
            .showCondition("tseAdminTypeOfDecision=\"Case management order\" AND tseAdminIsResponseRequired=\"Yes\"")
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("PageFieldDisplayOrder", 9)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminSelectPartyNotify)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 14)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminSelectPartyRespond)
            .mandatory()
            .showCondition("tseAdminTypeOfDecision=\"Case management order\" AND tseAdminIsResponseRequired=\"Yes\"")
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 8)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminTableMarkUp)
            .readOnly()
            .showCondition("tseAdminTableLabel=\"dummy\"")
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/tseAdmin/midValidateInput")
            .caseEventColumn("PageLabel", "Record a decision")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminTableLabel)
            .readOnly()
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdminTypeOfDecision)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 6)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> respondToApplicationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .pageLabel("Select an application")
            .field(CaseData::getTseAdminSelectApplication)
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/tseAdmReply/midDetailsTable")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .page("2")
            .field(CaseData::getTseAdmReplyTableMarkUp)
            .readOnly()
            .showCondition("tseAdmReplyTableLabel =\"dummy\"")
            .caseEventColumn("PageLabel", "Respond to an application")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/tseAdmReply/midValidateInput")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyTableLabel)
            .readOnly()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyEnterResponseTitle)
            .optional()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyAdditionalInformation)
            .optional()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyAddDocument)
            .optional()
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyIsCmoOrRequest)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyCmoMadeBy)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Case management order\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyRequestMadeBy)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Request\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyCmoEnterFullName)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Case management order\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyCmoIsResponseRequired)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Case management order\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyCmoSelectPartyRespond)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Case management order\" "
                + "AND tseAdmReplyCmoIsResponseRequired=\"Yes\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyRequestEnterFullName)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Request\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyRequestIsResponseRequired)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Request\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplyRequestSelectPartyRespond)
            .mandatory()
            .showCondition("tseAdmReplyIsCmoOrRequest=\"Request\" "
                + "AND tseAdmReplyRequestIsResponseRequired=\"Yes\"")
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .field(CaseData::getTseAdmReplySelectPartyNotify)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", null)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .done()
            .done();
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
