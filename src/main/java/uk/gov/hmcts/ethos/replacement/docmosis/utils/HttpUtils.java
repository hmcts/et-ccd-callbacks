package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.CASE_USERS_RETRIEVE_API;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_HEADER_PARAM_NAME_AUTHORIZATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_HEADER_PARAM_NAME_CONTENT_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_HEADER_PARAM_NAME_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_HEADER_PARAM_VALUE_APPLICATION_JSON;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.INVALID_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.EXCEPTION_INVALID_MODIFICATION_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.ROLE_MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.ROLE_MODIFICATION_TYPE_REVOKE;

public final class HttpUtils {

    private HttpUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static HttpHeaders buildHeaders(String authToken, String serviceAuthorisation)
            throws GenericServiceException {
        if (authToken.matches(AUTHORISATION_TOKEN_REGEX)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HTTP_HEADER_PARAM_NAME_AUTHORIZATION, authToken);
            headers.add(HTTP_HEADER_PARAM_NAME_SERVICE_AUTHORIZATION, serviceAuthorisation);
            headers.add(HTTP_HEADER_PARAM_NAME_CONTENT_TYPE, HTTP_HEADER_PARAM_VALUE_APPLICATION_JSON);
            return headers;
        } else {
            throw new GenericServiceException(
                    String.format(INVALID_AUTHORISATION_TOKEN, authToken),
                    new Exception(String.format(INVALID_AUTHORISATION_TOKEN, authToken)),
                    String.format(INVALID_AUTHORISATION_TOKEN, authToken),
                    null,
                    "HttpUtils",
                    "buildHeaders");
        }
    }

    /**
     * Builds the URL used to retrieve case user access information from the CCD Data Store API.
     *
     * <p>This method formats a predefined endpoint URL by injecting the given CCD Data Store API base URL
     * and the case ID into the appropriate placeholders. The resulting URL is intended for retrieving
     * user assignments or access roles associated with the specified case.</p>
     *
     * @param ccdDataStoreApiBaseUrl the base URL of the CCD Data Store API
     * @param caseId the unique identifier of the case for which access details are being retrieved
     * @return a fully formed URL as a {@link String} to call the case user access retrieval API
     */
    public static String buildCaseAccessUrl(String ccdDataStoreApiBaseUrl,
                                            String caseId) {
        return String.format(CASE_USERS_RETRIEVE_API, ccdDataStoreApiBaseUrl, caseId);
    }

    /**
     * Returns HttpMethod by the given modification type. If modification type is Assignment then returns
     * HttpMethod POST else returns HttpMethod DELETE. Throws {@link CallbacksRuntimeException} when modification
     * type is empty or not Assignment or Revoke
     * @param caseUserRoleModificationType modification type received from client.
     * @return HttpMethod type by the given modification type
     */
    public static HttpMethod getHttpMethodByCaseUserRoleModificationType(String caseUserRoleModificationType) {
        HttpMethod httpMethod = getHttpMethodByModificationType(caseUserRoleModificationType);
        if (ObjectUtils.isEmpty(httpMethod)) {
            throw new CallbacksRuntimeException(new Exception(EXCEPTION_INVALID_MODIFICATION_TYPE));
        }
        return httpMethod;
    }

    private static HttpMethod getHttpMethodByModificationType(String modificationType) {
        return ROLE_MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType) ? HttpMethod.POST
                : ROLE_MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE
                : null;
    }

}
