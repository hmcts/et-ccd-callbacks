package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotificationsConfig {

    @Value("${uk.gov.notify.api.key}")
    private String apiKey;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(apiKey);
    }
}
