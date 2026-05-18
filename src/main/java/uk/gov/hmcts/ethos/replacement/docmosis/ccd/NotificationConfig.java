package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class NotificationConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final int updateNotificationResponseHearingDatePage;
    private final boolean hideUpdateNotificationResponseCollection;

    protected NotificationConfig(
        int updateNotificationResponseHearingDatePage,
        boolean hideUpdateNotificationResponseCollection
    ) {
        this.updateNotificationResponseHearingDatePage = updateNotificationResponseHearingDatePage;
        this.hideUpdateNotificationResponseCollection = hideUpdateNotificationResponseCollection;
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

    private String updateNotificationResponseCollectionShowCondition() {
        return hideUpdateNotificationResponseCollection ? "sendNotificationCollection.number=\"dummy\"" : null;
    }
}
