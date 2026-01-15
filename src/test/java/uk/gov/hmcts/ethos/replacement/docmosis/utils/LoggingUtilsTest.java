package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class LoggingUtilsTest {

    private static final String EXCEPTION_MESSAGE = "Exception message";
    private static final String EXPECTED_LOGGING_MESSAGE = "Error form ccd - Exception message";

    @Test
    @SneakyThrows
    void theLogCCDException() {
        // when exception is null should not log anything and not throw exception
        assertDoesNotThrow(() -> LoggingUtils.logCCDException(null));
        // when exception message is empty should not log anything and not throw exception
        final Exception emptyException = new Exception(StringUtils.EMPTY);
        assertDoesNotThrow(() -> LoggingUtils.logCCDException(emptyException));
        // when exception message exists should log message
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingUtils.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        final Exception exception = new Exception(EXCEPTION_MESSAGE);
        LoggingUtils.logCCDException(exception);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_LOGGING_MESSAGE);
    }
}
