package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_USER_TOKEN;

public final class UserUtils {

    private static final String CLASS_NAME = UserUtils.class.getSimpleName();

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
        if (ObjectUtils.isEmpty(userDetails)) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isNotBlank(userDetails.getName())) {
            return StringUtils.trimToEmpty(userDetails.getName());
        }

        String firstName = StringUtils.trimToEmpty(userDetails.getFirstName());
        String lastName = StringUtils.trimToEmpty(userDetails.getLastName());

        return StringUtils.joinWith(StringUtils.SPACE, firstName, lastName).trim();
    }
}
