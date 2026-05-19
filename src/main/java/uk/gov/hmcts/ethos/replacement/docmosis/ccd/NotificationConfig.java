package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class NotificationConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String PAGE_FIELD_DISPLAY_ORDER = "PageFieldDisplayOrder";
    private static final String PAGE_COLUMN_NUMBER = "PageColumnNumber";
    private static final String RETAIN_HIDDEN_VALUE = "RetainHiddenValue";
    private static final String PUBLISH = "Publish";

    private final int updateNotificationResponseHearingDatePage;
    private final boolean hideUpdateNotificationResponseCollection;
    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean sendNotificationInfoMidEvent;
    private final boolean grantSendNotificationReadToCaseworker;

    protected NotificationConfig(
        int updateNotificationResponseHearingDatePage,
        boolean hideUpdateNotificationResponseCollection,
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean sendNotificationInfoMidEvent,
        boolean grantSendNotificationReadToCaseworker
    ) {
        this.updateNotificationResponseHearingDatePage = updateNotificationResponseHearingDatePage;
        this.hideUpdateNotificationResponseCollection = hideUpdateNotificationResponseCollection;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.sendNotificationInfoMidEvent = sendNotificationInfoMidEvent;
        this.grantSendNotificationReadToCaseworker = grantSendNotificationReadToCaseworker;
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

        Event.EventBuilder<T, EtUserRole, EtState> sendNotification = sendNotificationFields(
            configBuilder.event("sendNotification")
                .forAllStates()
                .name("Send a notification")
                .description("Send a notification")
                .showSummary()
                .showCondition("caseType=\"dummy\"")
                .caseEventColumn("DisplayOrder", null)
                .caseEventColumn(PUBLISH, "Y")
                .aboutToStartCallbackUrl("${ET_COS_URL}/sendNotification/aboutToStart")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/sendNotification/aboutToSubmit")
                .submittedCallbackUrl("${ET_COS_URL}/sendNotification/submitted")
        )
            .grant(Permission.R, EtUserRole.ET_ACAS_API, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.R, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION);

        if (grantSendNotificationReadToCaseworker) {
            sendNotification.grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT);
        }

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

    private Event.EventBuilder<T, EtUserRole, EtState> sendNotificationFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var sendNotificationInfo = event.fields()
            .page("1")
            .field("sendNotificationInfo")
            .readOnly()
            .caseEventColumn("PageLabel", "Send a notification")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 1)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null);

        if (sendNotificationInfoMidEvent) {
            sendNotificationInfo.caseEventColumn(
                "CallBackURLMidEvent",
                "${ET_COS_URL}/sendNotification/midValidateInput"
            );
        }

        return sendNotificationInfo
            .done()
            .field("sendNotificationLetter")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 3)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationUploadDocument")
            .showCondition("sendNotificationLetter=\"Yes\"")
            .showSummary()
            .caseEventColumn("DisplayContext", "COMPLEX")
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 4)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationSubject")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 5)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationSelectHearing")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Hearing\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 6)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationCaseManagement")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Case management orders / requests\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 7)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationResponseTribunal")
            .mandatory()
            .showCondition(
                "sendNotificationSubject CONTAINS \"Case management orders / requests\" "
                    + "OR sendNotificationSubject CONTAINS \"Employer Contract Claim\""
            )
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 8)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, "N")
            .done()
            .field("sendNotificationWhoCaseOrder")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Case management orders / requests\" "
                               + "AND sendNotificationCaseManagement=\"Case management order\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 10)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationFullName")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Case management orders / requests\" "
                               + "AND sendNotificationCaseManagement!=\"\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 11)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationSelectParties")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Case management orders / requests\" "
                               + "OR sendNotificationSubject CONTAINS \"Employer Contract Claim\" "
                               + "AND sendNotificationResponseTribunal=\"Yes - view document for details\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 9)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, "N")
            .done()
            .field("sendNotificationTitle")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 2)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationWhoMadeJudgement")
            .mandatory()
            .showCondition(" sendNotificationSubject CONTAINS \"Judgment\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 12)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationFullName2")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Judgment\" AND sendNotificationWhoMadeJudgement!=\"\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 13)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationDecision")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Judgment\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 14)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationEccQuestion")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Employer Contract Claim\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 16)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationNotify")
            .mandatory()
            .showSummary()
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 18)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationAdditionalInfo")
            .optional()
            .showSummary()
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 17)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationDetails")
            .mandatory()
            .showCondition("sendNotificationDecision=\"Other\" AND sendNotificationSubject CONTAINS \"Judgment\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 15)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .field("sendNotificationRequestMadeBy")
            .mandatory()
            .showCondition("sendNotificationSubject CONTAINS \"Case management orders / requests\" "
                               + "AND sendNotificationCaseManagement=\"Request\"")
            .showSummary()
            .caseEventColumn(RETAIN_HIDDEN_VALUE, "No")
            .caseEventColumn(PAGE_FIELD_DISPLAY_ORDER, 11)
            .caseEventColumn(PAGE_COLUMN_NUMBER, null)
            .caseEventColumn(PUBLISH, null)
            .done()
            .done();
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
