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
    public static final String EXCEPTION_NEW_CASE_DETAILS_NOT_FOUND =
            "New case details are missing.";
    public static final String EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND =
            "Old case details are missing.";
    public static final String EXCEPTION_NEW_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND =
            "New case details are missing the submission reference.";
    public static final String EXCEPTION_OLD_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND =
            "Old case details are missing the submission reference.";
    public static final String EXCEPTION_OLD_AND_NEW_SUBMISSION_REFERENCES_NOT_EQUAL =
            "Old and new submission references do not match (old: %s, new: %s).";
    public static final String EXCEPTION_NEW_CASE_DATA_NOT_FOUND =
            "New case data is missing for case ID %s.";
    public static final String EXCEPTION_OLD_CASE_DATA_NOT_FOUND =
            "Old case data is missing for case ID %s.";
    public static final String EXCEPTION_OLD_RESPONDENT_COLLECTION_IS_EMPTY =
            "Old respondent collection is missing for case ID %s.";
    public static final String EXCEPTION_NEW_RESPONDENT_COLLECTION_IS_EMPTY =
            "New respondent collection is missing for case ID %s.";
    public static final String EXCEPTION_OLD_AND_NEW_RESPONDENTS_ARE_DIFFERENT =
            "Old and new respondent collections contain different respondents for case ID %s.";
    public static final String EXCEPTION_FAILED_TO_ASSIGN_ROLE =
            "Failed to assign role %s, to user with email %s, for case %s. Exception message: %s";
    public static final String EXCEPTION_INVALID_GRANT_ACCESS_PARAMETER =
            "There are missing parameters; accessToken: %s, email: %s, submission reference: %s, organisationId: %s, "
                    + "role: %s.";
    public static final String EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL =
            "Unable to get account id by email %s for case %s.";
    public static final String EXCEPTION_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID =
            "Unable to find organisation by user id %s for case %s.";
    public static final String EXCEPTION_USER_AND_SELECTED_ORGANISATIONS_NOT_MATCH =
            "User's organisation and selected organisation does not match user id %s, selected organisation id %s, for "
                    + "case %s.";

    public static final String ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name %s has more than one representative";
    public static final String ERROR_SELECTED_RESPONDENT_NOT_FOUND = "Selected respondent with name %s not found.";
    public static final String ERROR_INVALID_REPRESENTATIVE_EXISTS = "Invalid representative exists.";
    public static final String ERROR_INVALID_RESPONDENT_EXISTS = "Invalid respondent exists.";
    public static final String ERROR_UNABLE_TO_SET_ROLE = "Unable to set role {}. Case Id: {}. Error: {}";
    public static final String ERROR_SOLICITOR_ROLE_NOT_FOUND = "Solicitor role not found, case id: {}";
    public static final String ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL =
            "Unable to send notification for representative removal for case: {}. Exception: {}";
    public static final String ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES =
            "Failed to remove organisation policies for case {}. Exception: {}";
    public static final String ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES =
            "Failed to add organisation policy for case {}. Exception: {}";
    public static final String ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS =
            "Failed to add organisation policy. Reason: invalid case details";
    public static final String ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND =
            "Failed to add organisation policy for case {}. Reason: representative not found";

    public static final String WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS =
            "Representative %s is missing an email address.";
    public static final String WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Representative '%s' could not be found using %s. Case access will not be defined for this representative.";
    public static final String WARNING_MISSING_EMAIL_ADDRESS =
            "Missing email address for respondent while sending NOC notification email as revoked user for case {}.";
    public static final int MAX_NOC_ANSWERS = 10;

    private NOCConstants() {
        // Final classes should not have a public or default constructor.
    }

}
