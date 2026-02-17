package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class NOCConstants {

    public static final String NOC_REQUEST = "nocRequest";
    public static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";
    public static final String NOC_TYPE_REMOVAL = "Removal";
    public static final String NOC_TYPE_ADDITION = "Addition";

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
            "Failed to assign role %s, for case %s. Exception message: %s";
    public static final String EXCEPTION_INVALID_GRANT_ACCESS_PARAMETER =
            "There are missing parameters; submission reference: %s, role: %s.";
    public static final String EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL =
            "Unable to get account id by email for case %s.";
    public static final String EXCEPTION_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID =
            "Unable to find organisation by user id for case %s.";
    public static final String EXCEPTION_USER_AND_SELECTED_ORGANISATIONS_NOT_MATCH =
            "User's organisation and selected organisation does not match, for case %s.";

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
    public static final String ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_INPUTS =
            "Failed to add organisation policy for case{}. Reason: invalid inputs";
    public static final String ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND =
            "Failed to add organisation policy for case {}. Reason: representative not found";
    public static final String ERROR_UNABLE_TO_START_EVENT_TO_UPDATE_REPRESENTATIVE_AND_ORGANISATION_POLICY =
            "Unable to start update case submitted event to update representative role and organisation policy for "
                    + "case: {}";
    public static final String ERROR_UNABLE_TO_START_REMOVE_CLAIMANT_REP_AND_ORG_POLICY_INVALID_CCD_REQUEST =
            "Unable to start update case submitted event to update representative role and organisation policy for "
                    + "case: {}, Reason: invalid ccd request";
    public static final String ERROR_UNABLE_TO_START_REMOVE_CLAIMANT_REP_AND_ORG_POLICY_INVALID_PARAMETERS =
            "Unable to start update case submitted event to update representative role and organisation policy for "
                    + "case: {}, Reason: invalid parameters";
    public static final String ERROR_FAILED_TO_REMOVE_CLAIMANT_REP_AND_ORG_POLICY =
            "Failed to remove claimant representative and organisation policy for case {}. Exception: {}";

    public static final String WARNING_WHILE_VALIDATING_REPRESENTATIVE_ORGANISATION_AND_EMAIL =
            "Failed to validate representative organisation and email. Warning: {}";
    public static final String WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_CLAIMANT_FOR_RESPONDENT_REP_UPDATE =
            "Invalid case details. Unable to notify claimant for respondent representative update. Case id: {}";
    public static final String WARNING_RESPONDENT_NAME_MISSING_TO_NOTIFY_CLAIMANT_FOR_RESP_REP_UPDATE =
            "Respondent name is missing. Unable to notify claimant for respondent representative update. Case id: {}";
    public static final String WARNING_CLAIMANT_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE =
            "Claimant email not found. Unable to notify claimant for respondent representative update. Case id: {}";
    public static final String WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT =
            "Failed to send noc notification email to claimant, case id: {}, error: {}";

    public static final String WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS =
            "Representative %s is missing an email address.";
    public static final String WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Representative '%s' could not be found using %s. Case access will not be defined for this representative.";
    public static final String WARNING_MISSING_RESPONDENT_EMAIL_ADDRESS =
            "Missing respondent email address while sending Notice of Change (NoC) respondent representative removal "
                    + "notification for case {}.";
    public static final String WARNING_INVALID_RESPONDENT =
            "Invalid respondent while sending Notice of Change (NoC) respondent representative removal "
                    + "notification for case {}.";
    public static final String WARNING_INVALID_CASE_DETAILS =
            "Invalid case details while sending Notice of Change (NoC) respondent representative removal "
                    + "notification for case {}.";
    public static final String WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT =
            "Failed to send noc notification email to respondent, case id: {}, error: {}";
    public static final String WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE =
            "Invalid case details. Unable to notify organisation for respondent representative update. Case id: {}, "
                    + "NOC type: {}";
    public static final String WARNING_INVALID_REPRESENTATIVE_TO_NOTIFY_ORGANISATION_FOR_RESPONDENT_REP_UPDATE =
            "Invalid case details. Unable to notify organisation for respondent representative update. Case id: {}, "
                    + "NOC type: {}";
    public static final String WARNING_INVALID_ORGANISATION_RESPONSE_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE =
            "Cannot retrieve {} organisation by id {} [{}] {}. Unable to notify organisation for respondent "
                    + "representative update. Case id: {}";
    public static final String WARNING_INVALID_PARAMETERS_TO_NOTIFY_ORGANISATION_FOR_REP_UPDATE =
            "Invalid parameters(orgId, caseId, nocType). Unable to notify organisation for respondent "
                    + "representative update. Case id: {}";
    public static final String WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION =
            "Failed to send NOC notification email to organisation admin, case id: {}, error: {}";
    public static final String WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_TRIBUNAL_FOR_RESPONDENT_REP_UPDATE =
            "Invalid case details. Unable to notify tribunal for respondent representative update. Case id: {}, "
                    + "NOC type: {}";
    public static final String WARNING_TRIBUNAL_EMAIL_NOT_FOUND_TO_NOTIFY_FOR_RESPONDENT_REP_UPDATE =
            "Tribunal email not found. Unable to notify organisation for respondent representative update. "
                    + "Case id: {},  NOC type: {}";
    public static final String WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_TRIBUNAL =
            "Failed to send email to tribunal, case id: {}, error: {}";

    public static final String WARNING_INVALID_CASE_DETAILS_TO_NOTIFY_NEW_REPRESENTATIVE =
            "Invalid case details. Unable to notify new representative. Case id: {}";
    public static final String WARNING_INVALID_PARTY_NAME_TO_NOTIFY_NEW_REPRESENTATIVE =
            "Invalid party name. Unable to notify new representative. Case id: {}";
    public static final String WARNING_INVALID_REP_EMAIL_NOTIFY_NEW_REPRESENTATIVE =
            "Invalid representative email. Unable to notify new representative. Case id: {}";
    public static final String WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_NEW_REPRESENTATIVE =
            "Failed to send email to new representative, case id: {}, error: {}";
    public static final String WARNING_INVALID_CASE_DETAILS_CLAIMANT_NOT_NOTIFIED_OF_REMOVAL_OF_REPRESENTATIVE =
            "Invalid case details. Unable to notify claimant for removal of representative update. Case id: {}.";
    public static final String WARNING_INVALID_CLAIMANT_EMAIL_CLAIMANT_NOT_NOTIFIED_FOR_REMOVAL_OF_REPRESENTATIVE =
            "Invalid claimant email. Unable to notify claimant for removal of representative update. Case id: {}. "
                    + "Exception: {}";
    public static final String WARNING_FAILED_TO_SEND_REMOVAL_OF_REPRESENTATIVE_CLAIMANT =
            "Failed to send email to claimant for removal of representative, case id: {}, error: {}";

    public static final int MAX_NOC_ANSWERS = 10;

    private NOCConstants() {
        // Final classes should not have a public or default constructor.
    }

}
