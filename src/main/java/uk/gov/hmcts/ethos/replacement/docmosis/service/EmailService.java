package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.EmailServiceException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

/**
 * EmailService is a class that is used for sending email via the GOV.UK Notify service.
 * For more detail, please view the documentation https://docs.notifications.service.gov.uk/java.html#send-an-email
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final NotificationClient emailClient;

    @Value("${url.exui.case-details}")
    private String exuiUrl;
    @Value("${url.citizen.case-details}")
    private String citizenUrl;

    /**
     * Sends email to an email address using a specified email template.
     * @param templateId The template that of the email being sent.
     * @param emailAddress The email address of the recipient.
     * @param personalisation A map of values that contains the personalised information.
     */
    public void sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation) {

        String referenceId = UUID.randomUUID().toString();
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
            throw new EmailServiceException("Failed to send email", e);
        }
    }

    public String getCitizenCaseLink(String caseId) {
        return citizenUrl + caseId;
    }

    protected String getExuiCaseLink(String caseId) {
        return exuiUrl + caseId;
    }
}
