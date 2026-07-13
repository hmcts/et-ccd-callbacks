package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

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
}
