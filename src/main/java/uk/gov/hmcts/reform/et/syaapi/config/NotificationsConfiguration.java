package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;

/**
 * Initialize configurations for the notification controller.
 */
@Configuration
public class NotificationsConfiguration {

    /**
     * Creates an entirely new {@link NotificationsProperties} object.
     * @return the new properties object
     */
    @Bean
    public NotificationsProperties notificationsProperties() {
        return new NotificationsProperties();
    }
}
