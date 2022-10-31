package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

/**
 * Utility class that can be used to filter document data before submission to the Tornado service.
 */
public final class TornadoDocumentFilter {

    private TornadoDocumentFilter() {
        // All access through static methods
    }

    /**
     * Filters Json string so that and backslash characters are escaped.
     * @param json Tornado document data in Json format
     * @return Json string that can be submitted to Tornado
     */
    public static String filterJson(String json) {
        return escapeBackslash(json);
    }

    private static String escapeBackslash(String value) {
        return value.replace("\\", "\\\\");
    }
}
