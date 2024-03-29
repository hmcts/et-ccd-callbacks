package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

/**
 * Instantiate NotificationClient bean for using the GOV.UK Notify client to send emails.
 */
@Configuration
public class NotificationsConfig {

    @Value("${gov-notify-api-key}")
    private String apiKey;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(apiKey);
    }
}
