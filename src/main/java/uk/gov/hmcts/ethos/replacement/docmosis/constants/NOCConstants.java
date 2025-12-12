package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class NOCConstants {

    public static final String NOC_REQUEST = "nocRequest";
    public static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";
    public static final String EXCEPTION_RESPONDENT_NOT_FOUND = "Respondent not found for case ID %s.";
    public static final String EXCEPTION_RESPONDENT_ID_NOT_FOUND =
            "Respondent ID not found for case ID %s.";
    public static final String EXCEPTION_RESPONDENT_DETAILS_NOT_EXIST =
            "Respondent details could not be found for respondent ID %s in case %s.";
    public static final String EXCEPTION_RESPONDENT_NAME_NOT_EXISTS =
            "Respondent name could not be found for respondent ID %s in case %s.";
    public static final String EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "Representative not found for case ID %s.";
    public static final String EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND =
            "Representative ID not found for case ID %s.";
    public static final String EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXIST =
            "Representative details not found for representative ID %s in case %s.";
    public static final String EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND =
            "Organisation not found for representative %s.";

    public static final String ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name %s has more than one representative";
    public static final String ERROR_SELECTED_RESPONDENT_NOT_FOUND =
            "Selected respondent with name %s not found.";
    public static final String ERROR_INVALID_REPRESENTATIVE_EXISTS =
            "Invalid representative exists.";
    public static final String ERROR_INVALID_RESPONDENT_EXISTS =
            "Invalid respondent exists.";

    public static final String WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS =
            "Representative %s is missing an email address.";
    public static final String WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Representative '%s' could not be found using %s.";

    private NOCConstants() {
        // Final classes should not have a public or default constructor.
    }

}
