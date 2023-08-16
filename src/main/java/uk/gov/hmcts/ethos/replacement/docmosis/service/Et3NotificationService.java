package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;

/**
 * Service to support ET3 Notification journey.
 */
@Slf4j
@RequiredArgsConstructor
@Service("et3NotificationService")
public class Et3NotificationService {
    public static final String EMAIL_ADDRESS = "emailAddress";
    private final EmailService emailService;
    @Value("${template.et3Notification.myhmcts}")
    private String et3MyHmctsTemplateId;
    @Value("${template.et3Notification.citizen}")
    private String et3CitizenTemplateId;

    /**
     * Sends notification emails to the claimant and respondent (or their reps if applicable).
     */
    public void sendNotifications(CaseDetails caseDetails) {
        String caseId = caseDetails.getCaseId();
        caseDetails.getCaseData().getRespondentCollection()
            .forEach(obj -> {
                Map<String, String> respondent = NotificationHelper.buildMapForRespondent(caseDetails, obj.getValue());
                respondent.put(LINK_TO_EXUI, emailService.getExuiCaseLink(caseId));
                String respondentEmail = respondent.get(EMAIL_ADDRESS);
                if (isNullOrEmpty(respondentEmail)) {
                    return;
                }
                emailService.sendEmail(et3MyHmctsTemplateId, respondentEmail, respondent);
            });

        Map<String, String> claimant = NotificationHelper.buildMapForClaimant(caseDetails);
        claimant.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseId));
        String claimantEmail = claimant.get(EMAIL_ADDRESS);
        if (isNullOrEmpty(claimantEmail)) {
            return;
        }

        emailService.sendEmail(et3CitizenTemplateId, claimantEmail, claimant);
    }
}