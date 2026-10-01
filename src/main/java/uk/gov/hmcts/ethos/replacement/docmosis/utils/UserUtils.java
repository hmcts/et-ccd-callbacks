package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_USER_TOKEN;

public final class UserUtils {

    private static final String CLASS_NAME = RespondentUtils.class.getSimpleName();

    private UserUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static void validateToken(String token, String submissionReference) throws GenericServiceException {
        String methodName = "validateToken";
        if (StringUtils.isBlank(token)) {
            throw new GenericServiceException(ERROR_INVALID_USER_TOKEN,
                    new Exception(ERROR_INVALID_USER_TOKEN),
                    ERROR_INVALID_USER_TOKEN,
                    submissionReference,
                    CLASS_NAME,
                    methodName + " - userToken is blank");
        }
    }

    /**
     * Checks whether the required user details are present.
     *
     * <p>A user is considered to have the required details when the user object exists
     * and both the user ID and email address are non-blank.</p>
     *
     * @param userDetails the user details to check
     * @return {@code true} if the required user details are present;
     *         {@code false} otherwise
     */
    public static boolean hasRequiredUserDetails(UserDetails userDetails) {
        return !ObjectUtils.isEmpty(userDetails)
                && !StringUtils.isBlank(userDetails.getUid())
                && !StringUtils.isBlank(userDetails.getEmail());
    }

    /**
     * Determines whether the given user is the lead claimant representative.
     * <p>
     * The user is considered the lead claimant representative when both the user details
     * and claimant representative details contain the required information, and the user's
     * email address matches the claimant representative's email address, ignoring case.
     * </p>
     *
     * @param userDetails the user details to validate and compare
     * @param claimantRepresentative the claimant representative details to validate and compare
     * @return {@code true} if the user is the lead claimant representative; otherwise {@code false}
     */
    public static boolean isLeadClaimantRepresentative(UserDetails userDetails,
                                                       RepresentedTypeC claimantRepresentative) {
        return hasRequiredUserDetails(userDetails)
                && ClaimantRepresentativeUtils.hasRequiredClaimantRepresentativeDetails(claimantRepresentative)
                && Strings.CI.equals(userDetails.getEmail(), claimantRepresentative.getRepresentativeEmailAddress());
    }

    /**
     * Resolves a display name for the given user details.
     * <p>
     * If {@link UserDetails#getName()} contains a non-blank value, it is returned.
     * Otherwise, the display name is constructed from the user's first and last names,
     * ignoring any blank values.
     * </p>
     *
     * @param userDetails the user details used to resolve the display name
     * @return the resolved display name, or an empty string if no name information is available
     */
    public static String resolveUserDisplayName(UserDetails userDetails) {
        if (StringUtils.isNotBlank(userDetails.getName())) {
            return userDetails.getName();
        }

        String firstName = StringUtils.trimToEmpty(userDetails.getFirstName());
        String lastName = StringUtils.trimToEmpty(userDetails.getLastName());

        return StringUtils.joinWith(StringUtils.SPACE, firstName, lastName).trim();
    }
}
