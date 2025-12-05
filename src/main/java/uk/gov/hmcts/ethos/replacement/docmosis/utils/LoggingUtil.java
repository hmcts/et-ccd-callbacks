package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EVENT_FIELDS_VALIDATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;

@Slf4j
public final class LoggingUtil {

    private LoggingUtil() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Logs a formatted exception message at the error level, including contextual details
     * such as a descriptive keyword, case reference number, message, class name,
     * and method name. Each parameter is validated for non-blank values; blank or null
     * inputs are replaced with an empty string in the log output.
     * <p>
     * The resulting log entry includes a multi-line, structured message identifying:
     * <ul>
     *     <li>The error description (first word)</li>
     *     <li>The associated case reference number</li>
     *     <li>The specific error message</li>
     *     <li>The class in which the exception occurred</li>
     *     <li>The method in which the exception occurred</li>
     * </ul>
     * This method is intended to provide a consistent, readable format for exception logs.
     * </p>
     *
     * @param firstWord          a short keyword describing the error (e.g., "Exception", "Failure");
     *                           may be blank or null
     * @param caseReferenceNumber the case reference number associated with the error;
     *                            may be blank or null
     * @param message            the exception or error message to log; may be blank or null
     * @param className          the name of the class where the exception occurred;
     *                           may be blank or null
     * @param methodName         the name of the method where the exception occurred;
     *                           may be blank or null
     */
    public static void logException(String firstWord,
                                    String caseReferenceNumber,
                                    String message,
                                    String className,
                                    String methodName) {
        log.error("""
                *************EXCEPTION OCCURRED*************
                ERROR DESCRIPTION: {}
                CASE REFERENCE: {}
                ERROR MESSAGE: {}
                CLASS NAME: {}
                METHOD NAME: {}
                *****************END OF EXCEPTION MESSAGE***********************""",
                isNotBlank(firstWord) ? firstWord : STRING_EMPTY,
                isNotBlank(caseReferenceNumber) ? caseReferenceNumber : STRING_EMPTY,
                isNotBlank(message) ? message : STRING_EMPTY,
                isNotBlank(className) ? className : STRING_EMPTY,
                isNotBlank(methodName) ? methodName : STRING_EMPTY);
    }

    /**
     * Logs a list of validation error messages using the application logger.
     * <p>
     * Each error message in the provided list is included in the log output,
     * prefixed with the {@code EVENT_FIELDS_VALIDATION} tag.
     * </p>
     *
     * @param errors a list of error messages to be logged; may be empty but should not be {@code null}
     */
    public static void logErrors(List<String> errors) {
        log.info(EVENT_FIELDS_VALIDATION + "{}", errors);
    }
}
