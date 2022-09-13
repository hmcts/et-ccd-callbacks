package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3NotificationHelper;

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
    @Value("${et3notification.template.id}")
    private String templateId;

    /**
     * Sends notification emails to the claimant and respondent (or their reps if available).
     */
    public void sendNotifications(CaseData caseData) {
        Map<String, String> claimant = Et3NotificationHelper.buildMapForClaimant(caseData);

        emailService.sendEmail(templateId, claimant.get(EMAIL_ADDRESS), claimant);

        caseData.getRespondentCollection()
            .forEach(o -> {
                Map<String, String> respondent = Et3NotificationHelper.buildMapForRespondent(caseData, o.getValue());
                if (isNullOrEmpty(respondent.get(EMAIL_ADDRESS))) {
                    return;
                }
                emailService.sendEmail(templateId, respondent.get(EMAIL_ADDRESS), respondent);
            });
    }
}
