package uk.gov.hmcts.ethos.replacement.docmosis.test.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public final class LoggerTestUtils {

    public static final int INTEGER_ZERO = NumberUtils.INTEGER_ZERO;
    public static final int INTEGER_ONE = NumberUtils.INTEGER_ONE;
    public static final int INTEGER_TWO = 2;
    public static final int INTEGER_THREE = 3;
    public static final int INTEGER_FOUR = 4;
    public static final int INTEGER_FIVE = 5;
    public static final int INTEGER_SIX = 6;
    public static final int INTEGER_SEVEN = 7;
    public static final int INTEGER_EIGHT = 8;
    public static final int INTEGER_NINE = 9;
    public static final int INTEGER_TEN = 10;
    public static final int INTEGER_ELEVEN = 11;

    private static ListAppender<ILoggingEvent> appender;

    private LoggerTestUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static void initializeLogger(Class<?> clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    public static void checkLog(Level loggingLevel, int numberOfMessages, String message) {
        if (numberOfMessages == 0) {
            assertThat(appender.list)
                    .filteredOn(e -> e.getLevel().equals(loggingLevel))
                    .hasSize(numberOfMessages);
            return;
        }
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel().equals(loggingLevel))
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(numberOfMessages)
                .contains(message);
    }
}
