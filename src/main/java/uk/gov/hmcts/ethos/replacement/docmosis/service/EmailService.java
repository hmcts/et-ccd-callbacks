package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final NotificationClient emailClient;

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