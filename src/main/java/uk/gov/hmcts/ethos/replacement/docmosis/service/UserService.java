package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_NOT_FOUND;

@Slf4j
@Service("userService")
@RequiredArgsConstructor
public class UserService {

    private final AuthTokenGenerator authTokenGenerator;
    private final UserIdamService userIdamService;

    private static final String CLASS_NAME = UserService.class.getName();

    public UserDetails getValidatedUserDetails(String userToken, String submissionReference)
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
     * Finds user details in IDAM for the given IDAM ID.
     *
     * @param idamId the IDAM ID of the user whose details are requested
     * @return the user details, or {@code null} if the service returns no details
     *         or the returned user's UID is {@code null}, empty, or whitespace only
     */
    public UserDetails findUserDetailsByIdamId(String idamId) {
        UserDetails userDetails = userIdamService.getUserDetailsById(authTokenGenerator.generate(), idamId);
        if (ObjectUtils.isEmpty(userDetails) || StringUtils.isBlank(userDetails.getUid())) {
            return null;
        }
        return userDetails;
    }
}
