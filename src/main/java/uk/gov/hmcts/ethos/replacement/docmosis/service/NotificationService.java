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
@Service("NotificationService")
public class NotificationService {
    public static final String EMAIL_ADDRESS = "emailAddress";
    private final EmailService emailService;
    @Value("${notification.template.id}")
    private String templateId;

    /**
     * Sends notification emails to the claimant and respondent (or their reps if available).
     * @param caseData object that holds case data.
     */
    public void sendNotifications(CaseData caseData) {
        Map<String, String> claimant = NotificationHelper.buildMapForClaimant(caseData);

        caseData.getRespondentCollection()
            .forEach(o -> {
                Map<String, String> respondent = NotificationHelper.buildMapForRespondent(caseData, o.getValue());
                if (isNullOrEmpty(respondent.get(EMAIL_ADDRESS))) {
                    return;
                }
                emailService.sendEmail(templateId, respondent.get(EMAIL_ADDRESS), respondent);
            });

        if (isNullOrEmpty(claimant.get(EMAIL_ADDRESS))) {
            return;
        }

        emailService.sendEmail(templateId, claimant.get(EMAIL_ADDRESS), claimant);
    }
}