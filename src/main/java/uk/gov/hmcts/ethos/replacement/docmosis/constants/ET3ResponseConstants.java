package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class ET3ResponseConstants {

    public static final String ET3_ATTACHMENT = "ET3 Attachment";
    public static final String SHORT_DESCRIPTION = "Attached document submitted with a Response";
    public static final String ET3_CATEGORY_ID = "C18";
    public static final String ERROR_INVALID_USER_TOKEN = "Invalid user token";
    public static final String ERROR_INVALID_CASE_ID = "Invalid case ID";
    public static final String ERROR_CASE_ROLES_NOT_FOUND = "Case roles not found";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_USER_ID_NOT_FOUND = "User ID not found";
    public static final String ERROR_NO_REPRESENTED_RESPONDENT_FOUND = "No represented respondent found";
    public static final String ERROR_CASE_DATA_NOT_FOUND = "Case data not found";
    public static final String SYSTEM_ERROR = "A system error occurred. Please try again later!";
    // TODO: https://tools.hmcts.net/jira/browse/RET-5960
    public static final String ERROR_ORGANISATION_DETAILS_NOT_FOUND = "Organisation details not found";
    public static final String REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS = "Use MyHMCTS details";
    public static final String SECTION_COMPLETE_BODY = """
        You may want to complete the rest of the ET3 Form using the links below:\
        <br><a href="/cases/case-details/%s/trigger/et3Response/et3Response1">ET3 - Respondent Details</a>\
        <br><a href="/cases/case-details/%s/trigger/et3ResponseEmploymentDetails/et3ResponseEmploymentDetails1\
        ">ET3 - Employment Details</a>\
        <br><a href="/cases/case-details/%s/trigger/et3ResponseDetails/et3ResponseDetails1">ET3 - \
        Response Details</a>
        <br><a href="/cases/case-details/%s/trigger/downloadDraftEt3/downloadDraftEt31">Download draft ET3 Form</a>
        <br><br>%s
        """;
    public static final String SUBMIT_ET3_BUTTON = """
        <a href="/cases/case-details/%s/trigger/submitEt3/submitEt31" role="button" draggable="false" class="govuk-button" data-module="govuk-button">
          Submit ET3 Form
        </a>""";
    public static final String GENERATED_DOCUMENT_URL = "Please download the draft ET3 : ";
    public static final String ET3_COMPLETE_HEADER = "<h1>ET3 Response submitted</h1>";
    public static final String ET3_COMPLETE_BODY =
        """
                <h3>What happens next</h3>\r
                \r
                You should receive confirmation from the tribunal office to process your application within 5
                 working days. If you have not heard from them within 5 days, contact the office directly.""";

    private ET3ResponseConstants() {
        // Access through static methods
    }
}
