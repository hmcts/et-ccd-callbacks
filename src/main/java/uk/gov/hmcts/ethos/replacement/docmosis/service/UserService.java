package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_NOT_FOUND;

@Slf4j
@Service("userService")
@RequiredArgsConstructor
public class UserService {

    private final AdminUserService adminUserService;
    private final UserIdamService userIdamService;

    private static final String CLASS_NAME = UserService.class.getName();

    /**
     * Fetches user details from IDAM and validates that the details are present
     * and the user's UID is non-blank.
     *
     * @param userToken the authentication token used to retrieve the user details
     * @param submissionReference the submission reference included in validation
     *                            exceptions for error reporting
     * @return the retrieved user details with a non-blank UID; never {@code null}
     * @throws GenericServiceException if the user details are missing or the UID
     *                                 is {@code null}, empty, or contains only whitespace
     */
    public UserDetails fetchAndValidateUserDetails(String userToken, String submissionReference)
            throws GenericServiceException {
        final String methodName = "getUserDetails";
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        if (ObjectUtils.isEmpty(userDetails)) {
            throw new GenericServiceException(ERROR_USER_NOT_FOUND,
                    new Exception(ERROR_USER_NOT_FOUND),
                    ERROR_USER_NOT_FOUND,
                    submissionReference,
                    CLASS_NAME,
                    methodName);
        }
        if (StringUtils.isBlank(userDetails.getUid())) {
            throw new GenericServiceException(ERROR_USER_ID_NOT_FOUND,
                    new Exception(ERROR_USER_ID_NOT_FOUND),
                    ERROR_USER_ID_NOT_FOUND,
                    submissionReference,
                    CLASS_NAME,
                    methodName);
        }
        return userDetails;
    }

    /**
     * Retrieves the user details associated with the given authentication token.
     *
     * <p>Returns {@code null} if no user details are found or if the retrieved
     * user's UID is null, empty, or contains only whitespace.</p>
     *
     * @param userToken the authentication token used to retrieve the user details
     * @return the user details if found and the UID is non-blank; otherwise {@code null}
     */
    public UserDetails findUserDetailsByToken(String userToken) {
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        return ObjectUtils.isEmpty(userDetails) || StringUtils.isBlank(userDetails.getUid()) ? null : userDetails;
    }

    /**
     * Finds user details in IDAM for the given IDAM ID.
     *
     * @param idamId the IDAM ID of the user whose details are requested
     * @return the user details, or {@code null} if the service returns no details
     *         or the returned user's UID is {@code null}, empty, or whitespace only
     */
    public UserDetails findUserDetailsByIdamId(String idamId) {
        UserDetails userDetails = userIdamService.getUserDetailsById(adminUserService.getAdminUserToken(), idamId);
        if (ObjectUtils.isEmpty(userDetails) || StringUtils.isBlank(userDetails.getUid())) {
            return null;
        }
        return userDetails;
    }
}
