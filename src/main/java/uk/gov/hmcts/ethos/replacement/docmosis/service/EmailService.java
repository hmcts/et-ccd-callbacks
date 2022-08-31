package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EmailClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service("emailService")
public class EmailService {

    private final EmailClient emailClient;

    public EmailService(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    public void sendEmail(String templateId, String emailAddress, Map<String, String> personalisation) {

        var referenceId = UUID.randomUUID().toString();

        try {
            emailClient.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                referenceId
            );
            log.info("Sending email success. Reference ID: {}", referenceId);

        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason:", referenceId, e);
        }
    }
} 