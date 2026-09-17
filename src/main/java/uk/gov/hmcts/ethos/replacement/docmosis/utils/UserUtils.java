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

    public static boolean isLeadClaimantRepresentative(UserDetails userDetails,
                                                       RepresentedTypeC claimantRepresentative) {
        return hasRequiredUserDetails(userDetails)
                && ClaimantRepresentativeUtils.hasRequiredClaimantRepresentativeDetails(claimantRepresentative)
                && Strings.CI.equals(userDetails.getEmail(), claimantRepresentative.getRepresentativeEmailAddress());
    }
}
