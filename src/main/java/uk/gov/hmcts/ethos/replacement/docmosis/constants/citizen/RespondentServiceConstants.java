package uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen;

public final class RespondentServiceConstants {

    // Exception Messages
    public static final String EXCEPTION_INVALID_RESPONDENT_INDEX =
            "Respondent index, %s is not valid for the case with id, %s";
    public static final String EXCEPTION_EMPTY_RESPONDENT_COLLECTION =
            "Respondent collection not found for the case with id, %s";
    public static final String EXCEPTION_RESPONDENT_NOT_EXISTS = "Respondent does not exist for case: %s";

    // Class names
    public static final String CLASS_RESPONDENT_SERVICE = "RespondentService";

    // Method names
    public static final String METHOD_REVOKE_SOLICITOR_ROLE = "revokeRespondentSolicitorRole";

    private RespondentServiceConstants() {
        // Final classes should not have a public or default constructor.
    }
}
