package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public final class HttpConstants {

    public static final String HTTP_CODE_TWO_HUNDRED = "200";
    public static final String HTTP_CODE_FOUR_HUNDRED = "400";
    public static final String HTTP_CODE_FOUR_ZERO_ONE = "401";
    public static final String HTTP_CODE_FOUR_ZERO_THREE = "403";
    public static final String HTTP_CODE_FOUR_ZERO_FOUR = "404";
    public static final String HTTP_CODE_FIVE_HUNDRED = "500";
    public static final String HTTP_CODE_FIVE_ZERO_ONE = "501";
    public static final String HTTP_CODE_FIVE_ZERO_THREE = "503";

    public static final String HTTP_MESSAGE_TWO_HUNDRED = "OK";
    public static final String HTTP_MESSAGE_FOUR_HUNDRED = "Bad Request";
    public static final String HTTP_MESSAGE_FOUR_ZERO_ONE = "Unauthorized";
    public static final String HTTP_MESSAGE_FOUR_ZERO_THREE = "Forbidden";
    public static final String HTTP_MESSAGE_FOUR_ZERO_FOUR = "Not Found";
    public static final String HTTP_MESSAGE_FIVE_HUNDRED = "Internal Server Error";
    public static final String HTTP_MESSAGE_FIVE_ZERO_ONE = "Not Implemented";
    public static final String HTTP_MESSAGE_FIVE_ZERO_THREE = "Service Unavailable";

    private HttpConstants() {
        // Access through static methods
    }

}
