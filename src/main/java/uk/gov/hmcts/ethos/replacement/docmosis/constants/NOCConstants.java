package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class NOCConstants {

    public static final String NOC_REQUEST = "nocRequest";
    public static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";
    public static final String INVALID_NOTICE_OF_CHANGE_ANSWER_INDEX = "Invalid notice of change answer index, %s "
            + "entered for the case with id: %s";
    public static final String EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND =
            "Notice of change answer not found for the respondent with name, %s for the case with id, %s";
    public static final String EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND =
            "Respondent solicitor type not found for case with id, %s and respondent organisation policy index, %s";
    public static final String EXCEPTION_CASE_USER_ROLES_NOT_FOUND = "Case user roles not found for caseId: %s";

    // Event names
    public static final String EVENT_NAME_REMOVE_OWN_REPRESENTATIVE = "REMOVE_OWN_REPRESENTATIVE";

    private NOCConstants() {
        // Final classes should not have a public or default constructor.
    }

}
