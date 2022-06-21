package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.config.notification.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationClient notificationClient;

    @Value("${notifications.emailTemplateId}")
    private String emailTemplateId;

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param targetEmail - recepient target email id
     * @param parameters - map of strings to add this to the template
     * @param reference - reference string for email template
     * @return response from notification api
     */

    public SendEmailResponse sendEmail(String targetEmail, Map<String, String> parameters) {
        SendEmailResponse sendEmailResponse;
        try {
            sendEmailResponse = notificationClient.sendEmail(emailTemplateId, targetEmail, parameters, null);
        } catch (NotificationClientException ne) {
            log.error("Error while trying to sending notification to client", ne);
            throw new NotificationException(ne);
        }
        return sendEmailResponse;
    }

}
