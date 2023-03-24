package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class TableMarkupConstants {

    public static final String TABLE_STRING = "|--|--|\r\n";
    public static final String RESPONSE_TABLE_HEADER = "|Response %s | |\r\n";
    public static final String SUPPORTING_MATERIAL_TABLE_HEADER = "|Supporting material | %s|\r\n";
    public static final String ORDER_APP_HEARING_MARKUP = "|Hearing | %s|\r\n";
    public static final String ORDER_APP_DOC_MARKUP = "|Description | %s|\r\n"
            + "|Document | <a href=\"/documents/%s\" target=\"_blank\">%s</a>|\r\n";
    public static final String PARTY_OR_PARTIES_TO_RESPOND = "|Party or parties to respond | %s|\r\n";
    public static final String RESPONSE_DUE = "|Response due | %s|\r\n";
    public static final String NAME_MARKUP = "|Name | %s|\r\n";
    public static final String RESPONSE_TITLE = "|Response | %s|\r\n";
    public static final String ADDITIONAL_INFORMATION = "|Additional information | %s|\r\n";
    public static final String RESPONSE_LIST_TITLE = "|Responses | |\r\n"
            + TABLE_STRING
            + "\r\n";
    public static final String APP_DETAILS_DETAILS = "|Details of the application | %s|\r\n";
    public static final String DOCUMENT = "|Document | %s|\r\n";
    public static final String STRING_BR = "<br>";
    public static final String CLOSE_APP_DECISION_DETAILS_OTHER = "|Decision details | %s|\r\n";
    public static final String DECISION_NOTIFICATION_TITLE = "|Notification | %s|\r\n";
    public static final String DATE_MARKUP = "|Date | %s|\r\n";
    public static final String RESPONSE_FROM = "|Response from | %s|\r\n";
    public static final String RESPONSE_DATE = "|Response date | %s|\r\n";

    private TableMarkupConstants() {
        // restrict instantiation
    }
}
