package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class TableMarkupConstants {

    public static final String TABLE_STRING = "|--|--|\r\n";
    public static final String RESPONSE_TABLE_HEADER = "|Response %s | |\r\n";
    public static final String SUPPORTING_MATERIAL_TABLE_HEADER = "|Supporting material | %s|\r\n";
    public static final String DOCUMENT_LINK_MARKDOWN =
        "<a href=\"/documents/%s\" target=\"_blank\">%s</a>";
    public static final String PARTY_OR_PARTIES_TO_RESPOND = "|Party or parties to respond | %s|\r\n";
    public static final String RESPONSE_DUE = "|Is a response required?| %s|\r\n";
    public static final String NAME_MARKUP = "|Name | %s|\r\n";
    public static final String RESPONSE_TITLE = "|Response | %s|\r\n";
    public static final String ADDITIONAL_INFORMATION = "|Additional information | %s|\r\n";
    public static final String RESPONSE_LIST_TITLE = "|Responses | |\r\n"
            + TABLE_STRING
            + "\r\n";
    public static final String STRING_BR = "<br>";
    public static final String DATE_MARKUP = "|Date | %s|\r\n";
    public static final String RESPONSE_FROM = "|Response from | %s|\r\n";
    public static final String RESPONSE_DATE = "|Response date | %s|\r\n";

    private TableMarkupConstants() {
        // restrict instantiation
    }
}
