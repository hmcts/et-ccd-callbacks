package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import java.util.List;

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
    public static final String ET3_RESPONSE_STATUS_ACCEPTED = "Accepted";
    public static final String ET3_RESPONSE_STATUS_NOT_RECEIVED = "Not Received";
    public static final String ET3_RESPONSE_STATUS_REJECTED = "Rejected";
    public static final String ET3_RESPONSE_STATUS_NOT_ACCEPTED = "Not Accepted";
    public static final List<String> ET3_RESUBMIT_STATUSES = List.of(ET3_RESPONSE_STATUS_NOT_ACCEPTED,
            ET3_RESPONSE_STATUS_NOT_RECEIVED, ET3_RESPONSE_STATUS_REJECTED);

    private ET3ResponseConstants() {
        // Access through static methods
    }
}
