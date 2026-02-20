package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import ch.qos.logback.classic.Level;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class LoggingUtilsTest {

    private static final String DUMMY_EMAIL_ADDRESS = "dummy@email.address";
    private static final String ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT = "Failed to send email to claimant {}, error: {}";
    private static final String DUMMY_STRING = "Dummy string";
    private static final String EMPTY_MESSAGE = "Empty message";
    private static final String NOT_EMPTY_MESSAGE = "Not empty message";

    private static final String EXCEPTION_MESSAGE = "Exception message";
    private static final String EXPECTED_CCD_ERROR_LOGGING_MESSAGE = "Error form ccd - Exception message";
    private static final String EXPECTED_NOTIFICATION_EMAIL_ERROR_LOGGING_MESSAGE =
            "Email not found. Error message: Exception message";
    private static final String EXPECTED_NOTIFICATION_ERROR_LOGGING_MESSAGE =
            "Failed to send email to claimant dummy@email.address, error: Exception message";

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(LoggingUtils.class);
    }

    @Test
    @SneakyThrows
    void theLogCcdErrorMessageAtInfoLevel() {
        // when exception is null should not log anything and not throw exception
        assertDoesNotThrow(() -> LoggingUtils.logCcdErrorMessageAtInfoLevel(null));
        // when exception message is empty should not log anything and not throw exception
        final Exception emptyException = new Exception(StringUtils.EMPTY);
        assertDoesNotThrow(() -> LoggingUtils.logCcdErrorMessageAtInfoLevel(emptyException));
        // when exception message exists should log message
        final Exception exception = new Exception(EXCEPTION_MESSAGE);
        LoggingUtils.logCcdErrorMessageAtInfoLevel(exception);
        LoggerTestUtils.checkLog(Level.INFO, LoggerTestUtils.INTEGER_ONE, EXPECTED_CCD_ERROR_LOGGING_MESSAGE);
    }

    @Test
    @SneakyThrows
    void theLogNotificationIssue() {
        // when email is empty should log email not found error.
        final Exception exception = new Exception(EXCEPTION_MESSAGE);
        LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT, StringUtils.EMPTY, exception);
        LoggerTestUtils.checkLog(Level.INFO, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_NOTIFICATION_EMAIL_ERROR_LOGGING_MESSAGE);
        // when exception is null should not log anything and not throw exception
        assertDoesNotThrow(() -> LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT,
                DUMMY_EMAIL_ADDRESS, null));
        // when exception message is empty should not log anything and not throw exception
        final Exception emptyException = new Exception(StringUtils.EMPTY);
        assertDoesNotThrow(() -> LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT,
                DUMMY_EMAIL_ADDRESS, emptyException));
        // when logging text is empty should not log anything and not throw exception
        assertDoesNotThrow(() -> LoggingUtils.logNotificationIssue(StringUtils.EMPTY, DUMMY_EMAIL_ADDRESS, exception));
        // should log message
        LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT, DUMMY_EMAIL_ADDRESS, exception);
        LoggerTestUtils.checkLog(Level.INFO, LoggerTestUtils.INTEGER_TWO, EXPECTED_NOTIFICATION_ERROR_LOGGING_MESSAGE);
    }

    @Test
    void theResolveMessageByPresence() {
        // when object is instance of string and empty should return empty message
        assertThat(LoggingUtils.resolveMessageByPresence(StringUtils.EMPTY, EMPTY_MESSAGE, NOT_EMPTY_MESSAGE))
                .isEqualTo(EMPTY_MESSAGE);
        // when object is instance of string and not empty should return not empty message
        assertThat(LoggingUtils.resolveMessageByPresence(DUMMY_STRING, EMPTY_MESSAGE, NOT_EMPTY_MESSAGE))
                .isEqualTo(NOT_EMPTY_MESSAGE);
        // when object is a collection and empty should return empty message
        assertThat(LoggingUtils.resolveMessageByPresence(new ArrayList<>(), EMPTY_MESSAGE, NOT_EMPTY_MESSAGE))
                .isEqualTo(EMPTY_MESSAGE);
        // when object is a collection and not empty should return not empty message
        assertThat(LoggingUtils.resolveMessageByPresence(List.of(new RespondentSumTypeItem()), EMPTY_MESSAGE,
                NOT_EMPTY_MESSAGE)).isEqualTo(NOT_EMPTY_MESSAGE);
        // when object is empty should return empty message
        assertThat(LoggingUtils.resolveMessageByPresence(null, EMPTY_MESSAGE, NOT_EMPTY_MESSAGE))
                .isEqualTo(EMPTY_MESSAGE);
        // when object is not empty should return not empty message
        assertThat(LoggingUtils.resolveMessageByPresence(new CallbackRequest(), EMPTY_MESSAGE, NOT_EMPTY_MESSAGE))
                .isEqualTo(NOT_EMPTY_MESSAGE);
    }
}
