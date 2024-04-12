package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class TableMarkupConstants {

    public static final String TABLE_STRING = "|--|--|\r\n";
    public static final String DOCUMENT_LINK_MARKDOWN =
        "<a href=\"/documents/%s\" target=\"_blank\">%s</a>";
    public static final String DATE_MARKUP = "|Date | %s|\r\n";

    private TableMarkupConstants() {
        // restrict instantiation
    }
}
