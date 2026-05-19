package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class NotificationConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final int updateNotificationResponseHearingDatePage;
    private final boolean hideUpdateNotificationResponseCollection;
    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;

    protected NotificationConfig(
        int updateNotificationResponseHearingDatePage,
        boolean hideUpdateNotificationResponseCollection,
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole
    ) {
        this.updateNotificationResponseHearingDatePage = updateNotificationResponseHearingDatePage;
        this.hideUpdateNotificationResponseCollection = hideUpdateNotificationResponseCollection;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.event("UPDATE_NOTIFICATION_STATE")
            .forAllStates()
            .name("Respond to a notification")
            .description("Respond to a notification")
            .displayOrder(71)
            .showCondition("caseType=\"dummy\"")
            .blankCallbackUrls()
            .grant(Permission.CRUD, EtUserRole.CITIZEN);

        hiddenRespondentNotificationEvent(
            configBuilder,
            "UPDATE_RESPONDENT_PSE_STATE",
            "Update notification state",
            "Update respondent notification state",
            "caseType=\"dummy\""
        )
            .grant(Permission.CRU, EtUserRole.DEFENDANT);

        hiddenRespondentNotificationEvent(
            configBuilder,
            "ADD_RESPONDENT_PSE_RESPONSE",
            "Add notification response",
            "Add respondent notification response",
            "caseType=\"dummy\""
        )
            .publishToCamunda()
            .grant(Permission.CRU, EtUserRole.DEFENDANT)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        hiddenRespondentNotificationEvent(
            configBuilder,
            "STORE_RESPONDENT_PSE_RESPONSE",
            "Store notification response",
            "Store respondent notification response",
            "caseType=\"Dummy\""
        )
            .grant(Permission.CRU, EtUserRole.DEFENDANT);

        hiddenRespondentNotificationEvent(
            configBuilder,
            "SUBMIT_RESPONDENT_PSE_RESPONSE",
            "Submit stored pse response",
            "Submit stored respondent notification response",
            "caseType=\"Dummy\""
        )
            .publishToCamunda()
            .grant(Permission.CRUD, EtUserRole.DEFENDANT)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        updateNotificationResponseFields(
            configBuilder.event("UPDATE_NOTIFICATION_RESPONSE")
                .forAllStates()
                .name("UPDATE_NOTIFICATION_RESPONSE")
                .description("")
                .displayOrder(9001)
                .showCondition("caseType=\"Dummy\"")
                .publishToCamunda()
                .blankCallbackUrls()
        )
            .grant(Permission.CRU, EtUserRole.CITIZEN)
            .grant(Permission.R, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        respondNotificationFields(
            configBuilder.event("respondNotification")
                .forAllStates()
                .name("Respond to a notification")
                .description("Respond to a notification")
                .showSummary()
                .showCondition("caseType=\"dummy\"")
                .publishToCamunda()
                .caseEventColumn("DisplayOrder", null)
                .aboutToStartCallbackUrl("${ET_COS_URL}/respondNotification/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/respondNotification/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/respondNotification/submitted")
        )
            .grant(
                Permission.R,
                EtUserRole.ET_ACAS_API,
                EtUserRole.CASEWORKER_EMPLOYMENT,
                EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE
            )
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> hiddenRespondentNotificationEvent(
        ConfigBuilder<T, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        String description,
        String showCondition
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .description(description)
            .showCondition(showCondition)
            .caseEventColumn("DisplayOrder", null);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> updateNotificationResponseFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field(CaseData::getSendNotificationCollection)
            .caseEventColumn("PageShowCondition", updateNotificationResponseCollectionShowCondition())
            .caseEventColumn("Publish", null)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .page(String.valueOf(updateNotificationResponseHearingDatePage))
            .field(CaseData::getNextListedDate)
            .optional()
            .caseEventColumn("ShowSummaryChangeOption", "N")
            .caseEventColumn("PageDisplayOrder", updateNotificationResponseHearingDatePage)
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn("Publish", "Y")
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .done();
    }

    private Event.EventBuilder<T, EtUserRole, EtState> respondNotificationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field(CaseData::getSelectNotificationDropdown)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .page("2")
            .field(CaseData::getRespondNotificationTitle)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageLabel", "Respond to a notification")
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationAdditionalInfo)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 4)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationUploadDocument)
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 5)
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getRespondNotificationResponseRequired)
            .mandatory()
            .showSummary()
            .showCondition(" respondNotificationCmoOrRequest=\"Case management order\" "
                               + "OR respondNotificationCmoOrRequest=\"Request\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn("PageFieldDisplayOrder", 8)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationWhoRespond)
            .mandatory()
            .showSummary()
            .showCondition("respondNotificationResponseRequired=\"Yes\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn("PageFieldDisplayOrder", 9)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getNotificationMarkdown)
            .readOnly()
            .showSummary()
            .showCondition("notificationMarkdownLabel=\"dummy\"")
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn("CallBackURLMidEvent", "${ET_COS_URL}/respondNotification/midValidateInput")
            .caseEventColumn("PageLabel", "Respond to a Notification")
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field("notificationMarkdownLabel")
            .readOnly()
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationPartyToNotify)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 21)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationCaseManagementMadeBy)
            .mandatory()
            .showSummary()
            .showCondition(" respondNotificationCmoOrRequest=\"Case management order\"")
            .caseEventColumn("RetainHiddenValue", "No")
            .caseEventColumn("PageFieldDisplayOrder", 11)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationFullName)
            .mandatory()
            .showSummary()
            .showCondition("respondNotificationCmoOrRequest=\"Case management order\" "
                               + "OR respondNotificationCmoOrRequest=\"Request\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn("PageFieldDisplayOrder", 12)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationCmoOrRequest)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 6)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field(CaseData::getRespondNotificationRequestMadeBy)
            .mandatory()
            .showSummary()
            .showCondition("respondNotificationCmoOrRequest=\"Request\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn("PageFieldDisplayOrder", 10)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .field("respondNotificationDate")
            .readOnly()
            .showCondition(" respondNotificationCmoOrRequest=\"dummy\"")
            .caseEventColumn("RetainHiddenValue", "Yes")
            .caseEventColumn("PageFieldDisplayOrder", 22)
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("Publish", null)
            .done()
            .done();
    }

    private String updateNotificationResponseCollectionShowCondition() {
        return hideUpdateNotificationResponseCollection ? "sendNotificationCollection.number=\"dummy\"" : null;
    }
}
