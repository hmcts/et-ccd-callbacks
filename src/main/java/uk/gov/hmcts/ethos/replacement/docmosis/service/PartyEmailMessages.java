package uk.gov.hmcts.ethos.replacement.docmosis.service;

/**
 * Party-specific user-facing messages for email update and case-access flows.
 */
public record PartyEmailMessages(
        String emailUnchangedError,
        String idamUserNotFoundError,
        String idamUserAmbiguousError,
        String idamUserNotCitizenError,
        String idamUserLookupError,
        String accessLookupError,
        String accessRevokeError,
        String accessGrantError,
        String emailUpdateAfterReassignError,
        String emailUpdateAfterGrantError,
        String emailUpdateError
) {

    public static PartyEmailMessages claimant() {
        return new PartyEmailMessages(
                "Enter an email address that is different from the current claimant email address.",
                "No user account was found for the new email address. The claimant must register an "
                        + "account before the email address can be updated.",
                "More than one user account was found for the new email address. Check the email address "
                        + "with the claimant before trying again.",
                "The new email address is linked to an account that is not a citizen account. "
                        + "Enter a different email address.",
                "The new email address could not be checked against user accounts. Try again later.",
                "The claimant's current case access could not be checked. Try again later.",
                "Case access could not be removed from the previous claimant email address. "
                        + "The claimant email address was not updated. Enter the email address again.",
                "Case access could not be given to the new claimant email address. "
                        + "The claimant email address was not updated. Enter the email address again.",
                "Case access was moved to the new email address, but the claimant email address could not "
                        + "be updated. Enter the email address again.",
                "Case access was given to the new email address, but the claimant email address could not "
                        + "be updated. Enter the email address again.",
                "The claimant email address could not be updated. Enter the email address again."
        );
    }

    public static PartyEmailMessages respondent() {
        return new PartyEmailMessages(
                "Enter an email address that is different from the current respondent email address.",
                "No user account was found for the new email address. The respondent must register an "
                        + "account before the email address can be updated.",
                "More than one user account was found for the new email address. Check the email address "
                        + "with the respondent before trying again.",
                "The new email address is linked to an account that is not a citizen account. "
                        + "Enter a different email address.",
                "The new email address could not be checked against user accounts. Try again later.",
                "The respondent's current case access could not be checked. Try again later.",
                "Case access could not be removed from the previous respondent email address. "
                        + "The respondent email address was not updated. Enter the email address again.",
                "Case access could not be given to the new respondent email address. "
                        + "The respondent email address was not updated. Enter the email address again.",
                "Case access was moved to the new email address, but the respondent email address could not "
                        + "be updated. Enter the email address again.",
                "Case access was given to the new email address, but the respondent email address could not "
                        + "be updated. Enter the email address again.",
                "The respondent email address could not be updated. Enter the email address again."
        );
    }
}
