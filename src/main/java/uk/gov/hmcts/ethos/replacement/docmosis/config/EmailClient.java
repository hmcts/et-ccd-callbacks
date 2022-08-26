package uk.gov.hmcts.ethos.replacement.docmosis.config;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.service.notify.NotificationClient;

@Component
public class EmailClient extends NotificationClient {
    @Value("${referral.template.id}")
    @NotEmpty
    public String referralTemplateId;

    @Autowired
    public EmailClient(@Value("${uk.gov.notify.api.key}") String apiKey) {
        super(apiKey);
    }
}