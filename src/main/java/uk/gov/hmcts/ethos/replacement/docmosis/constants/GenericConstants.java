package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class GenericConstants {

    public static final String EXCEPTION_CCD_REQUEST_NOT_FOUND = "CCD request not found.";
    public static final String EXCEPTION_CASE_DETAILS_NOT_FOUND = "Case details not found.";
    public static final String EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND = "Submission reference not found.";
    public static final String EXCEPTION_CASE_DATA_NOT_FOUND = "Case data not found for submission reference, %s.";
    public static final String EXCEPTION_CALLBACK_REQUEST_NOT_FOUND = "Callback request not found.";
    public static final String EXCEPTION_CLAIMANT_EMAIL_NOT_FOUND = "Could not find claimant email address.";

    public static final String EVENT_FIELDS_VALIDATION = "Event fields validation: ";

    public static final String ERROR_INVALID_CASE_DATA = "Invalid case data";
    public static final String ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT = "Failed to send email to claimant {}, error: {}";
    public static final String ERROR_FAILED_TO_SEND_EMAIL_RESPONDENT =
            "Failed to send email to respondent {}, error: {}";
    public static final String ERROR_FAILED_TO_SEND_EMAIL_ORGANISATION_ADMIN =
            "Failed to send email to organisation admin {}, error: {}";
    public static final String ERROR_FAILED_TO_SEND_EMAIL_TRIBUNAL =
            "Failed to send email to tribunal {}, error: {}";
    public static final String ERROR_EMAIL_NOT_FOUND = "Email not found. Error message: {}";

    public static final String WARNING_CLAIMANT_EMAIL_NOT_FOUND = "Claimant email not found for case {}";

    public static final String EMPTY_LOWERCASE = "empty";

    private GenericConstants() {
        // Final classes should not have a public or default constructor.
    }

}
