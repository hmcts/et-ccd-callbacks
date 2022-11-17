package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Service to support ET3 Notification journey.
 */
@Slf4j
@RequiredArgsConstructor
@Service("et3NotificationService")
public class Et3NotificationService {
    public static final String EMAIL_ADDRESS = "emailAddress";
    private final EmailService emailService;
    @Value("${et3Notification.template.myhmcts.id}")
    private String et3MyHmctsTemplateId;
    @Value("${et3Notification.template.citizen.id}")
    private String et3CitizenTemplateId;

    /**
     * Sends notification emails to the claimant and respondent (or their reps if applicable).
     */
    public void sendNotifications(CaseData caseData) {
        Map<String, String> claimantPersonalisation = NotificationHelper.buildMapForClaimant(caseData);

        caseData.getRespondentCollection()
            .forEach(obj -> {
                Map<String, String> respondent = NotificationHelper.buildMapForRespondent(caseData, obj.getValue());
                String respondentEmail = respondent.get(EMAIL_ADDRESS);
                if (isNullOrEmpty(respondentEmail)) {
                    return;
                }
                emailService.sendEmail(et3MyHmctsTemplateId, respondentEmail, respondent);
            });

        String claimantEmail = claimantPersonalisation.get(EMAIL_ADDRESS);
        if (isNullOrEmpty(claimantEmail)) {
            return;
        }

        emailService.sendEmail(et3CitizenTemplateId, claimantEmail, claimantPersonalisation);
    }
}