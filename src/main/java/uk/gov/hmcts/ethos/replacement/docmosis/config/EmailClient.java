package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;

/**
 * Email client for sending GOV.UK Notify emails.
 * Migrated from et-message-handler.
 */
@Component
public class EmailClient extends NotificationClient {

    @Autowired
    public EmailClient(@Value("${uk.gov.notify.api.key:test-key}") String apiKey) {
        super(apiKey);
    }
}
