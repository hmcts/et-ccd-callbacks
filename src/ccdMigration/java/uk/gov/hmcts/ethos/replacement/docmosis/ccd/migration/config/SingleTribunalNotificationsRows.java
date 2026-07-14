package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import uk.gov.hmcts.et.common.model.ccd.SingleRole;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.ComplexFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.GrantSpec;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
    "checkstyle:LineLength",
    "checkstyle:RightCurlyAlone",
    "PMD.AvoidDuplicateLiterals"
})
final class SingleTribunalNotificationsRows {
    private static final int CFTLIB_ENGLAND_WALES = 1;
    private static final int CFTLIB_SCOTLAND = 2;
    private static final int CFTLIB = 3;
    private static final int PROD_ENGLAND_WALES = 4;
    private static final int ENGLAND_WALES = 5;
    private static final int PROD_SCOTLAND = 8;
    private static final int SCOTLAND = 10;
    private static final int PROD = 12;
    private static final int ALL = 15;

    private static final String LOCALHOST = "http://localhost:8081";
    private static final String PRODUCTION =
            "http://et-cos-prod.service.core-compute-prod.internal";

    private static final String SEND_NOTIFICATION = "sendNotification";
    private static final String RESPOND_NOTIFICATION = "respondNotification";
    private static final String VIEW_ALL_NOTIFICATIONS = "viewAllNotifications";
    private static final String GENERATE_NOTIFICATION_SUMMARY = "generateNotificationSummary";

    static final EventSpec[] EVENTS = notificationEvents();
    static final EventFieldSpec[] EVENT_FIELDS = eventFields();
    static final ComplexFieldSpec[] COMPLEX_FIELDS = complexFields();
    static final GrantSpec[] EVENT_GRANTS = eventGrants();

    private SingleTribunalNotificationsRows() {}

    private static EventSpec[] notificationEvents() {
        return new EventSpec[] {
            notificationEvent(CFTLIB, SEND_NOTIFICATION, "Send a notification", LOCALHOST),
            notificationEvent(PROD, SEND_NOTIFICATION, "Send a notification", PRODUCTION),
            notificationEvent(CFTLIB, RESPOND_NOTIFICATION, "Respond to a notification", LOCALHOST),
            notificationEvent(PROD, RESPOND_NOTIFICATION, "Respond to a notification", PRODUCTION),
            viewNotifications(
                    CFTLIB_ENGLAND_WALES,
                    "Judgment, Order, Notification",
                    "View all judgments, orders and notifications",
                    LOCALHOST),
            viewNotifications(
                    PROD_ENGLAND_WALES,
                    "Judgment, Order, Notification",
                    "View all judgments, orders and notifications",
                    PRODUCTION),
            viewNotifications(
                    CFTLIB_SCOTLAND,
                    "Judgement, Order, Notification",
                    "View all judgement, orders and notifications",
                    LOCALHOST),
            viewNotifications(
                    PROD_SCOTLAND,
                    "Judgement, Order, Notification",
                    "View all judgement, orders and notifications",
                    PRODUCTION),
            summaryEvent(CFTLIB, LOCALHOST),
            summaryEvent(PROD, PRODUCTION)
        };
    }

    private static EventSpec notificationEvent(
            int mask, String eventId, String description, String base) {
        return new EventSpec(
                mask,
                eventId,
                description,
                description,
                null,
                "*",
                "*",
                "caseType=\"dummy\"",
                "Y",
                "N",
                "Y",
                false,
                null,
                base + "/" + eventId + "/aboutToStart",
                base + "/" + eventId + "/aboutToSubmit",
                base + "/" + eventId + "/submitted");
    }

    private static EventSpec viewNotifications(
            int mask, String name, String description, String base) {
        return new EventSpec(
                mask,
                VIEW_ALL_NOTIFICATIONS,
                name,
                description,
                null,
                "*",
                "*",
                "caseType=\"dummy\"",
                "N",
                "N",
                null,
                false,
                null,
                base + "/pseViewNotifications/aboutToStart",
                null,
                null,
                "Close and return to case details");
    }

    private static EventSpec summaryEvent(int mask, String base) {
        return new EventSpec(
                mask,
                GENERATE_NOTIFICATION_SUMMARY,
                "Generate Notification Summary",
                "Generate Notification Summary",
                57,
                "*",
                "*",
                null,
                "N",
                "N",
                null,
                false,
                null,
                base + "/notificationDocument/aboutToStart",
                base + "/notificationDocument/aboutToSubmit",
                base + "/notificationDocument/submitted");
    }

    private static EventFieldSpec[] eventFields() {
        List<EventFieldSpec> rows = new ArrayList<>();
        addSendNotificationFields(rows);
        addRespondNotificationFields(rows);
        rows.add(
                field(
                        ALL,
                        VIEW_ALL_NOTIFICATIONS,
                        "pseViewNotifications",
                        "READONLY",
                        1,
                        1,
                        null,
                        "pseViewNotificationsLabel=\"dummy\"",
                        "No",
                        "All judgments, orders and notifications",
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        VIEW_ALL_NOTIFICATIONS,
                        "pseViewNotificationsLabel",
                        "READONLY",
                        1,
                        2,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        GENERATE_NOTIFICATION_SUMMARY,
                        "selectNotificationDropdown",
                        "MANDATORY",
                        1,
                        1,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        return rows.toArray(EventFieldSpec[]::new);
    }

    private static void addSendNotificationFields(List<EventFieldSpec> rows) {
        rows.add(
                field(
                        ENGLAND_WALES,
                        SEND_NOTIFICATION,
                        "sendNotificationInfo",
                        "READONLY",
                        1,
                        1,
                        null,
                        null,
                        null,
                        "Send a notification",
                        null,
                        null));
        rows.add(
                field(
                        CFTLIB_SCOTLAND,
                        SEND_NOTIFICATION,
                        "sendNotificationInfo",
                        "READONLY",
                        1,
                        1,
                        null,
                        null,
                        null,
                        "Send a notification",
                        LOCALHOST + "/sendNotification/midValidateInput",
                        null));
        rows.add(
                field(
                        PROD_SCOTLAND,
                        SEND_NOTIFICATION,
                        "sendNotificationInfo",
                        "READONLY",
                        1,
                        1,
                        null,
                        null,
                        null,
                        "Send a notification",
                        PRODUCTION + "/sendNotification/midValidateInput",
                        null));
        rows.add(sendField("sendNotificationTitle", "MANDATORY", 2, null, null, null));
        rows.add(sendField("sendNotificationLetter", "MANDATORY", 3, null, null, null));
        rows.add(
                sendField(
                        "sendNotificationUploadDocument",
                        "COMPLEX",
                        4,
                        "sendNotificationLetter=\"Yes\"",
                        "No",
                        null));
        rows.add(sendField("sendNotificationSubject", "MANDATORY", 5, null, null, null));
        rows.add(
                sendField(
                        "sendNotificationSelectHearing",
                        "MANDATORY",
                        6,
                        "sendNotificationSubject CONTAINS \"Hearing\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationCaseManagement",
                        "MANDATORY",
                        7,
                        "sendNotificationSubject CONTAINS \"Case management orders / requests\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationResponseTribunal",
                        "MANDATORY",
                        8,
                        "sendNotificationSubject CONTAINS \"Case management orders / requests\" OR"
                                + " sendNotificationSubject CONTAINS \"Employer Contract Claim\"",
                        "No",
                        "N"));
        rows.add(
                sendField(
                        "sendNotificationSelectParties",
                        "MANDATORY",
                        9,
                        "sendNotificationSubject CONTAINS \"Case management orders / requests\" OR"
                            + " sendNotificationSubject CONTAINS \"Employer Contract Claim\" AND"
                            + " sendNotificationResponseTribunal=\"Yes - view document for"
                            + " details\"",
                        "No",
                        "N"));
        rows.add(
                sendField(
                        "sendNotificationWhoCaseOrder",
                        "MANDATORY",
                        10,
                        "sendNotificationSubject CONTAINS \"Case management orders / requests\" AND"
                                + " sendNotificationCaseManagement=\"Case management order\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationFullName",
                        "MANDATORY",
                        11,
                        "sendNotificationSubject CONTAINS \"Case management orders / requests\" AND"
                                + " sendNotificationCaseManagement!=\"\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationRequestMadeBy",
                        "MANDATORY",
                        11,
                        "sendNotificationSubject CONTAINS \"Case management orders / requests\" AND"
                                + " sendNotificationCaseManagement=\"Request\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationWhoMadeJudgement",
                        "MANDATORY",
                        12,
                        " sendNotificationSubject CONTAINS \"Judgment\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationFullName2",
                        "MANDATORY",
                        13,
                        "sendNotificationSubject CONTAINS \"Judgment\" AND"
                                + " sendNotificationWhoMadeJudgement!=\"\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationDecision",
                        "MANDATORY",
                        14,
                        "sendNotificationSubject CONTAINS \"Judgment\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationDetails",
                        "MANDATORY",
                        15,
                        "sendNotificationDecision=\"Other\" AND sendNotificationSubject CONTAINS"
                                + " \"Judgment\"",
                        "No",
                        null));
        rows.add(
                sendField(
                        "sendNotificationEccQuestion",
                        "MANDATORY",
                        16,
                        "sendNotificationSubject CONTAINS \"Employer Contract Claim\"",
                        "No",
                        null));
        rows.add(sendField("sendNotificationAdditionalInfo", "OPTIONAL", 17, null, null, null));
        rows.add(sendField("sendNotificationNotify", "MANDATORY", 18, null, null, null));
    }

    private static EventFieldSpec sendField(
            String fieldId,
            String context,
            int order,
            String condition,
            String retainHidden,
            String publish) {
        return field(
                ALL,
                SEND_NOTIFICATION,
                fieldId,
                context,
                1,
                order,
                "Y",
                condition,
                retainHidden,
                null,
                null,
                publish);
    }

    private static void addRespondNotificationFields(List<EventFieldSpec> rows) {
        rows.add(
                field(
                        ALL,
                        RESPOND_NOTIFICATION,
                        "selectNotificationDropdown",
                        "MANDATORY",
                        1,
                        2,
                        "Y",
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        CFTLIB,
                        RESPOND_NOTIFICATION,
                        "notificationMarkdown",
                        "READONLY",
                        2,
                        1,
                        "Y",
                        "notificationMarkdownLabel=\"dummy\"",
                        "No",
                        "Respond to a Notification",
                        LOCALHOST + "/respondNotification/midValidateInput",
                        null));
        rows.add(
                field(
                        PROD,
                        RESPOND_NOTIFICATION,
                        "notificationMarkdown",
                        "READONLY",
                        2,
                        1,
                        "Y",
                        "notificationMarkdownLabel=\"dummy\"",
                        "No",
                        "Respond to a Notification",
                        PRODUCTION + "/respondNotification/midValidateInput",
                        null));
        rows.add(
                field(
                        ALL,
                        RESPOND_NOTIFICATION,
                        "notificationMarkdownLabel",
                        "READONLY",
                        2,
                        2,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                respondField(
                        "respondNotificationTitle",
                        "MANDATORY",
                        3,
                        null,
                        null,
                        "Respond to a notification"));
        rows.add(
                respondField("respondNotificationAdditionalInfo", "OPTIONAL", 4, null, null, null));
        rows.add(respondField("respondNotificationUploadDocument", "COMPLEX", 5, null, null, null));
        rows.add(respondField("respondNotificationCmoOrRequest", "MANDATORY", 6, null, null, null));
        rows.add(
                respondField(
                        "respondNotificationResponseRequired",
                        "MANDATORY",
                        8,
                        " respondNotificationCmoOrRequest=\"Case management order\" OR"
                                + " respondNotificationCmoOrRequest=\"Request\"",
                        "Yes",
                        null));
        rows.add(
                respondField(
                        "respondNotificationWhoRespond",
                        "MANDATORY",
                        9,
                        "respondNotificationResponseRequired=\"Yes\"",
                        "Yes",
                        null));
        rows.add(
                respondField(
                        "respondNotificationRequestMadeBy",
                        "MANDATORY",
                        10,
                        "respondNotificationCmoOrRequest=\"Request\"",
                        "Yes",
                        null));
        rows.add(
                respondField(
                        "respondNotificationCaseManagementMadeBy",
                        "MANDATORY",
                        11,
                        " respondNotificationCmoOrRequest=\"Case management order\"",
                        "No",
                        null));
        rows.add(
                respondField(
                        "respondNotificationFullName",
                        "MANDATORY",
                        12,
                        "respondNotificationCmoOrRequest=\"Case management order\" OR"
                                + " respondNotificationCmoOrRequest=\"Request\"",
                        "Yes",
                        null));
        rows.add(
                respondField(
                        "respondNotificationPartyToNotify", "MANDATORY", 21, null, null, null));
        rows.add(
                field(
                        ALL,
                        RESPOND_NOTIFICATION,
                        "respondNotificationDate",
                        "READONLY",
                        2,
                        22,
                        null,
                        " respondNotificationCmoOrRequest=\"dummy\"",
                        "Yes",
                        null,
                        null,
                        null));
    }

    private static EventFieldSpec respondField(
            String fieldId,
            String context,
            int order,
            String condition,
            String retainHidden,
            String pageLabel) {
        return field(
                ALL,
                RESPOND_NOTIFICATION,
                fieldId,
                context,
                2,
                order,
                "Y",
                condition,
                retainHidden,
                pageLabel,
                null,
                null);
    }

    private static ComplexFieldSpec[] complexFields() {
        return new ComplexFieldSpec[] {
            element(
                    ALL,
                    "respondNotificationUploadDocument",
                    RESPOND_NOTIFICATION,
                    "respondNotificationUploadDocument",
                    "shortDescription",
                    "OPTIONAL",
                    2,
                    "Short description",
                    "No"),
            element(
                    ALL,
                    "respondNotificationUploadDocument",
                    RESPOND_NOTIFICATION,
                    "respondNotificationUploadDocument",
                    "uploadedDocument",
                    "MANDATORY",
                    1,
                    "Document",
                    "No"),
            element(
                    PROD,
                    "sendNotificationUploadDocument",
                    SEND_NOTIFICATION,
                    "sendNotificationUploadDocument",
                    "uploadedDocument",
                    "MANDATORY",
                    1,
                    null,
                    null),
            element(
                    PROD,
                    "sendNotificationUploadDocument",
                    SEND_NOTIFICATION,
                    "sendNotificationUploadDocument",
                    "shortDescription",
                    "MANDATORY",
                    2,
                    "Short description of document",
                    null)
        };
    }

    private static GrantSpec[] eventGrants() {
        List<GrantSpec> rows = new ArrayList<>();
        addSendNotificationGrants(rows);
        addRespondNotificationGrants(rows);
        addViewNotificationGrants(rows);
        addSummaryGrants(rows);
        return rows.toArray(GrantSpec[]::new);
    }

    private static void addSendNotificationGrants(List<GrantSpec> rows) {
        rows.add(new GrantSpec(ALL, SEND_NOTIFICATION, SingleRole.ET_ACAS_API, "R"));
        rows.add(
                new GrantSpec(
                        ALL, SEND_NOTIFICATION, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        rows.add(
                new GrantSpec(
                        ALL, SEND_NOTIFICATION, SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        SEND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
                        "CRU"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        SEND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                        "CRU"));
        rows.add(
                new GrantSpec(
                        ALL, SEND_NOTIFICATION, SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "R"));
        rows.add(new GrantSpec(SCOTLAND, SEND_NOTIFICATION, SingleRole.CASEWORKER_EMPLOYMENT, "R"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        SEND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
                        "CRU"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        SEND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
                        "CRU"));
    }

    private static void addRespondNotificationGrants(List<GrantSpec> rows) {
        rows.add(new GrantSpec(ALL, RESPOND_NOTIFICATION, SingleRole.ET_ACAS_API, "R"));
        rows.add(new GrantSpec(ALL, RESPOND_NOTIFICATION, SingleRole.CASEWORKER_EMPLOYMENT, "R"));
        rows.add(
                new GrantSpec(
                        ALL, RESPOND_NOTIFICATION, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        rows.add(
                new GrantSpec(
                        ALL, RESPOND_NOTIFICATION, SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        RESPOND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
                        "CRU"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        RESPOND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                        "CRU"));
        rows.add(
                new GrantSpec(
                        ALL,
                        RESPOND_NOTIFICATION,
                        SingleRole.CASEWORKER_WA_TASK_CONFIGURATION,
                        "CRUD"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        RESPOND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
                        "CRU"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        RESPOND_NOTIFICATION,
                        SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
                        "CRU"));
    }

    private static void addViewNotificationGrants(List<GrantSpec> rows) {
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORA, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORB, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORC, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORD, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORE, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORF, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORG, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORH, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORI, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.SOLICITORJ, "CRUD"));
        rows.add(
                new GrantSpec(
                        ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        rows.add(new GrantSpec(ALL, VIEW_ALL_NOTIFICATIONS, SingleRole.CLAIMANTSOLICITOR, "D"));
    }

    private static void addSummaryGrants(List<GrantSpec> rows) {
        rows.add(
                new GrantSpec(
                        ALL,
                        GENERATE_NOTIFICATION_SUMMARY,
                        SingleRole.CASEWORKER_EMPLOYMENT_API,
                        "CRUD"));
        rows.add(
                new GrantSpec(
                        ALL,
                        GENERATE_NOTIFICATION_SUMMARY,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE,
                        "CRUD"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        GENERATE_NOTIFICATION_SUMMARY,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
                        "CRUD"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        GENERATE_NOTIFICATION_SUMMARY,
                        SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                        "CRUD"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        GENERATE_NOTIFICATION_SUMMARY,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
                        "CRUD"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        GENERATE_NOTIFICATION_SUMMARY,
                        SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
                        "CRUD"));
    }

    private static EventFieldSpec field(
            int mask,
            String eventId,
            String fieldId,
            String context,
            int page,
            int order,
            String showSummary,
            String condition,
            String retainHidden,
            String pageLabel,
            String midEvent,
            String publish) {
        return new EventFieldSpec(
                mask,
                eventId,
                fieldId,
                context,
                Integer.toString(page),
                page,
                order,
                false,
                showSummary,
                condition,
                retainHidden,
                null,
                pageLabel,
                midEvent,
                publish);
    }

    private static ComplexFieldSpec element(
            int mask,
            String rowId,
            String eventId,
            String caseFieldId,
            String elementCode,
            String context,
            int order,
            String label,
            String retainHidden) {
        return new ComplexFieldSpec(
                mask,
                rowId,
                eventId,
                caseFieldId,
                elementCode,
                context,
                order,
                label,
                null,
                null,
                retainHidden,
                null,
                null);
    }
}
