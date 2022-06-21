package uk.gov.hmcts.ethos.replacement.docmosis.config.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotificationsConfiguration {

    @Bean
    public NotificationsProperties notificationsProperties() {
        return new NotificationsProperties();
    }

    @Bean
    public NotificationClient notificationClient(NotificationsProperties notificationsProperties) {
        return new NotificationClient(notificationsProperties.getGovNotifyApiKey());
    }

}