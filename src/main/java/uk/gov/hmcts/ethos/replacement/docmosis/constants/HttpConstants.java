package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class HttpConstants {

    // HTTP Status Codes
    public static final String HTTP_STATUS_200_OK = "200";
    public static final String HTTP_STATUS_400_BAD_REQUEST = "400";
    public static final String HTTP_STATUS_401_UNAUTHORIZED = "401";
    public static final String HTTP_STATUS_403_FORBIDDEN = "403";
    public static final String HTTP_STATUS_404_NOT_FOUND = "404";
    public static final String HTTP_STATUS_500_INTERNAL_SERVER_ERROR = "500";
    public static final String HTTP_STATUS_503_SERVICE_UNAVAILABLE = "503";

    // HTTP Status Descriptions
    public static final String ACCESSED_SUCCESSFULLY = "Accessed successfully";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";
    public static final String NOT_FOUND = "Not Found";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String SERVICE_UNAVAILABLE = "Service Unavailable";

    // Regex Patterns
    public static final String AUTHORISATION_TOKEN_REGEX = "[a-zA-Z0-9._\\s\\S]+$";

    // HTTP Header Parameters
    public static final String HTTP_HEADER_PARAM_NAME_AUTHORIZATION = "Authorization";
    public static final String HTTP_HEADER_PARAM_NAME_SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String HTTP_HEADER_PARAM_NAME_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_PARAM_VALUE_APPLICATION_JSON = "application/json";

    // Exception Messages
    public static final String INVALID_AUTHORISATION_TOKEN = "Authorisation token, %s not valid";

    // HTTP Api URIs
    public static final String URI_CCD_DATA_STORE_SEARCH_API = "/case-users/search";
    public static final String CASE_USERS_RETRIEVE_API = "%s/case-users?case_ids=%s";
    public static final String CASE_USERS_API_URL = "/case-users";

    private HttpConstants() {
        // Final classes should not have a public or default constructor.
    }

}
