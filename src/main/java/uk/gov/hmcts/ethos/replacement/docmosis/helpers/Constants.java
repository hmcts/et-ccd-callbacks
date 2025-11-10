package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

public final class Constants {

    public static final String ET1_DOC_TYPE = "ET1";
    public static final String ET1_ATTACHMENT_DOC_TYPE = "ET1 Attachment";
    public static final String ET1_VETTING_DOC_TYPE = "ET1 Vetting";
    public static final String ET3_DOC_TYPE = "ET3";
    public static final String ET3_PROCESSING_DOC_TYPE = "ET3 Processing";
    public static final String COMPANY = "Company";
    public static final String ACAS_DOC_TYPE = "ACAS Certificate";
    public static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s%s";
    public static final String BEFORE_LABEL_ET1 = "<br><a target=\"_blank\" href=\"%s\">ET1 form"
        + " (opens in new tab)</a>";
    public static final String BEFORE_LABEL_ET1_IC = "<br><a target=\"_blank\" href=\"%s\">ET1</a>";
    public static final String BEFORE_LABEL_ET1_VETTING_IC = "<br><a target=\"_blank\" href=\"%s\">ET1 Vetting</a>";
    public static final String BEFORE_LABEL_ET3_IC = "<br><a target=\"_blank\" href=\"%s\">ET3</a>";
    public static final String BEFORE_LABEL_ET3_PROCESSING_IC = "<br><a target=\"_blank\" "
        + "href=\"%s\">ET3 Processing</a>";
    public static final String BEFORE_LABEL_REFERRALS_IC = "<br><a target=\"_blank\" "
            + "href=\"%s\">Referrals</a>";
    public static final String REFERRALS_PAGE_FRAGMENT_ID = "#Referrals";
    public static final String TO_HELP_YOU_COMPLETE_IC_EVENT_LABEL = "To help you complete this (opens in new tab)"
            + "%s%s%s%s%s";
    public static final String BEFORE_LABEL_ACAS =
            "<br><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    public static final String BEFORE_LABEL_ET1_ATTACHMENT =
            "<br><a target=\"_blank\" href=\"%s\">%s (opens in new tab)</a>";
    public static final String BEFORE_LABEL_ACAS_OPEN_TAB =
            "<br><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
                    + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
    public static final String CASE_DETAILS_URL_PARTIAL = "/cases/case-details/";
    public static final String CLAIMANT_DETAILS_PERSON = "<hr><h3>Claimant</h3>"
            + "<pre>First name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Last name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre>";
    public static final String CLAIMANT_DETAILS_COMPANY = "<hr><h3>Claimant</h3>"
            + "<pre>Company name &#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre>";
    public static final String CLAIMANT_AND_RESPONDENT_ADDRESSES = "<hr><h2>Listing details<hr><h3>Claimant</h3>"
            + "<pre>Contact address &#09&#09 %s</pre>"
            + "<br><pre>Work address &#09&#09&#09 %s</pre><hr>"
            + "<h3>Respondent</h3>"
            + "<pre>Contact address &#09&#09 %s</pre><hr>";
    public static final String CLAIMANT_AND_RESPONDENT_ADDRESSES_WITHOUT_WORK_ADDRESS =
            "<hr><h2>Listing details<hr><h3>Claimant</h3>"
                    + "<pre>Contact address &#09&#09 %s</pre>"
                    + "<hr><h3>Respondent</h3>"
                    + "<pre>Contact address &#09&#09 %s</pre><hr>";

    public static final String RESPONDENT_DETAILS = "<h3>Respondent %s</h3>"
            + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    public static final String RESPONDENT_ACAS_DETAILS = "<hr><h3>Respondent %o</h3>"
            + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><h3>Acas certificate</h3>";
    public static final String BR_WITH_TAB = "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 ";
    public static final String TRIBUNAL_OFFICE_LOCATION = "<hr><h3>Tribunal location</h3>"
            + "<pre>Tribunal &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Office &#09&#09&#09&#09&#09 %s</pre><hr>";
    public static final String TRIBUNAL_LOCATION_LABEL = "**<big>%s regional office</big>**";
    public static final String TRACK_ALLOCATION_HTML = "|||\r\n|--|--|\r\n|Track allocation|%s|\r\n";
    public static final String JUR_CODE_HTML = "<hr><h3>Jurisdiction Codes</h3>"
            + "<a target=\"_blank\" href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">"
            + "View all jurisdiction codes and descriptors (opens in new tab)</a><hr>"
            + "<h3>Codes already added</h3>%s<hr>";
    public static final String CASE_NAME_AND_DESCRIPTION_HTML = "<h4>%s</h4>%s";
    public static final String ERROR_EXISTING_JUR_CODE = "Jurisdiction code %s already exists.";
    public static final String ERROR_SELECTED_JUR_CODE = "Jurisdiction code %s is selected more than once.";

    public static final String TRIBUNAL_ENGLAND = "England & Wales";
    public static final String TRIBUNAL_SCOTLAND = "Scotland";
    public static final String ACAS_CERT_LIST_DISPLAY = "Certificate number %s has been provided.<br><br><br>";
    public static final String NO_ACAS_CERT_DISPLAY = "No certificate has been provided.<br><br><br>";
    public static final int FIVE_ACAS_DOC_TYPE_ITEMS_COUNT = 5;
    public static final int ONE_RESPONDENT_COUNT = 1;
    public static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";
    public static final String RECORD_DECISION = "Record decision";
    public static final String REPLY_TO_APPLICATION = "Reply to application";
    public static final String TSE_ADMIN_CORRESPONDENCE = "Tse admin correspondence";
    public static final String RESPONDENT_CORRESPONDENCE = "Respondent correspondence";
    public static final String UNEXPECTED_VALUE = "Unexpected value: ";
    public static final String MONTH_STRING_DATE_FORMAT = "dd MMM yyyy";
    public static final String EMPTY_STRING = "";
    public static final String DIGITAL_CASE_FILE = "Digital case file";
    public static final String DOC_OPENS_IN_NEW_TAB_MARK_UP =
        "<a target=\"_blank\" href=\"%s\">%s (opens in new tab)</a><br>";
    public static final String EXCEPTION_CASE_REFERENCE_NOT_FOUND = "Case reference not found";
    public static final String EXCEPTION_CASE_DATA_NOT_FOUND = "Case data not found";
    public static final String EXCEPTION_UPDATE_RESPONDENT_REPRESENTATIVE_REQUEST_EMPTY =
            "Update respondent representative request is empty";
    public static final String NOT_AVAILABLE_FOR_VIDEO_HEARINGS = "\nNot available for video hearings\n";

    private Constants() {
        // Utility classes should not have a public or default constructor.
    }
}
