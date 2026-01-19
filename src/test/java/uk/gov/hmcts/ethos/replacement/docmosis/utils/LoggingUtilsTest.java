package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class LoggingUtilsTest {

    private static final String DUMMY_EMAIL_ADDRESS = "dummy@email.address";
    private static final String ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT = "Failed to send email to claimant {}, error: {}";

    private static final String EXCEPTION_MESSAGE = "Exception message";
    private static final String EXPECTED_CCD_ERROR_LOGGING_MESSAGE = "Error form ccd - Exception message";
    private static final String EXPECTED_NOTIFICATION_EMAIL_ERROR_LOGGING_MESSAGE =
            "Email not found. Error message: Exception message";
    private static final String EXPECTED_NOTIFICATION_ERROR_LOGGING_MESSAGE =
            "Failed to send email to claimant dummy@email.address, error: Exception message";

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingUtils.class);
        appender.start();
        logger.addAppender(appender);
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
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_CCD_ERROR_LOGGING_MESSAGE);
    }

    @Test
    @SneakyThrows
    void theLogNotificationIssue() {
        // when email is empty should log email not found error.
        final Exception exception = new Exception(EXCEPTION_MESSAGE);
        LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT, StringUtils.EMPTY, exception);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_NOTIFICATION_EMAIL_ERROR_LOGGING_MESSAGE);
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
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(EXPECTED_NOTIFICATION_ERROR_LOGGING_MESSAGE);
    }
}
