package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;

@Component
public class EmailClient extends NotificationClient {

    @Autowired
    public EmailClient() {
        super("apiKey");
    }
}