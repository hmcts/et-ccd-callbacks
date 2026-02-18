package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EVENT_FIELDS_VALIDATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;

@Slf4j
public final class LoggingUtils {

    private LoggingUtils() {
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

    /**
     * Logs a CCD-related exception message at INFO level.
     * <p>
     * This method is intentionally non-intrusive: if the supplied exception is
     * {@code null}, empty, or contains a blank message, no log entry is written.
     * Only the exception message is logged, not the full stack trace.
     *
     * @param exception the exception originating from CCD whose message
     *                  should be logged at INFO level
     */
    public static void logCcdErrorMessageAtInfoLevel(Exception exception) {
        if (ObjectUtils.isEmpty(exception) || StringUtils.isBlank(exception.getMessage())) {
            return;
        }
        log.info("Error form ccd - {}", exception.getMessage());
    }

    /**
     * Logs a notification-related issue at INFO level with contextual details.
     * <p>
     * If the provided email address is blank, an informational log entry is written
     * indicating that the email could not be found. The method then proceeds to log
     * the main message if all required inputs are present.
     * <p>
     * The main log entry is written only if the exception, its message, and the
     * logging text are all non-null and non-blank. Only the exception message is
     * logged; the stack trace is not included.
     *
     * @param loggingText the SLF4J message template used for logging
     * @param email the email address associated with the notification
     * @param exception the exception whose message should be logged
     */
    public static void logNotificationIssue(String loggingText, String email, Exception exception) {
        if (StringUtils.isBlank(email)) {
            log.info(ERROR_EMAIL_NOT_FOUND, exception.getMessage());
            return;
        }
        if (ObjectUtils.isEmpty(exception)
                || StringUtils.isBlank(exception.getMessage())
                || StringUtils.isBlank(loggingText)) {
            return;
        }
        log.info(loggingText, email, exception.getMessage());
    }

    /**
     * Resolves and returns one of two messages based on whether the provided object
     * is considered empty.
     *
     * <p>The object is evaluated as follows:
     * <ul>
     *     <li>If the object is a {@link String}, it is considered empty if it is
     *         {@code null}, empty, or contains only whitespace
     *         (evaluated using {@link StringUtils#isBlank(CharSequence)}).</li>
     *     <li>If the object is a {@link Collection}, it is considered empty if it is
     *         {@code null} or contains no elements
     *         (evaluated using {@link CollectionUtils#isEmpty(Collection)}).</li>
     *     <li>For all other object types, emptiness is determined using
     *         {@link ObjectUtils#isEmpty(Object)}.</li>
     * </ul>
     *
     * @param <T>              the type of the object to evaluate
     * @param object           the object whose presence or emptiness is evaluated
     * @param emptyMessage     the message to return if the object is considered empty
     * @param notEmptyMessage  the message to return if the object is not empty
     * @return {@code emptyMessage} if the object is considered empty;
     *         otherwise {@code notEmptyMessage}
     */
    public static <T> String resolveMessageByPresence(T object,
                                                      String emptyMessage,
                                                      String notEmptyMessage) {
        if (object == null) {
            return emptyMessage;
        }
        boolean isEmpty = switch (object) {
            case String str -> StringUtils.isBlank(str);
            case Collection<?> collection -> CollectionUtils.isEmpty(collection);
            default -> ObjectUtils.isEmpty(object);
        };
        return isEmpty ? emptyMessage : notEmptyMessage;
    }
}
