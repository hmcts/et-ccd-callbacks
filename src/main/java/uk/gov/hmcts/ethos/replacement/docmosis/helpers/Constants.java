package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

 public class Constants {
     private Constants() {

     }
    public static final String ET1_DOC_TYPE = "ET1";
    public static final String ET1_ATTACHMENT_DOC_TYPE = "ET1 Attachment";
    public static final String COMPANY = "Company";
    public static final String ACAS_DOC_TYPE = "ACAS Certificate";
    public static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s%s";
    public static final String BEFORE_LABEL_ET1 =
            "<br><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    public static final String BEFORE_LABEL_ACAS =
            "<br><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    public static final String BEFORE_LABEL_ET1_ATTACHMENT =
            "<br><a target=\"_blank\" href=\"%s\">%s (opens in new tab)</a>";
    public static final String BEFORE_LABEL_ACAS_OPEN_TAB =
            "<br><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
                    + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
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
}
