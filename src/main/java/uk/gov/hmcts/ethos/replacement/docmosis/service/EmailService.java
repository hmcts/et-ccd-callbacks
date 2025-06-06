package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.HEARING_DOCUMENTS_PATH;

/**
 * EmailService is a class that is used for sending email via the GOV.UK Notify service.
 * For more detail, please view the documentation https://docs.notifications.service.gov.uk/java.html#send-an-email
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final NotificationClient emailClient;

    @Value("${case-details-url.exui}")
    private String exuiUrl;
    @Value("${case-details-url.citizen}")
    private String citizenUrl;
    @Value("${case-details-url.syr}")
    private String syrUrl;

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
            log.error("Failed to send email. Reference ID: {}. Reason:", referenceId, e);
        }
    }

    public String getCitizenCaseLink(String caseId) {
        return citizenUrl + caseId;
    }

    public String getExuiCaseLink(String caseId) {
        return exuiUrl + caseId;
    }

    public String getSyrCaseLink(String caseId, String respondentId) {
        return syrUrl + caseId + "/" + respondentId;
    }

    public String getClaimantRepExuiCaseNotificationsLink(String caseId) {
        return exuiUrl + caseId + "#Notifications";
    }

    public String getExuiHearingDocumentsLink(String caseId) {
        return exuiUrl + caseId + HEARING_DOCUMENTS_PATH;
    }
}
