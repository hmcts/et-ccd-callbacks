package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;

/**
 * Service to support the notification of change journey with email notifications.
 */
@Slf4j
@RequiredArgsConstructor
@Service("NocNotificationService")
public class NocNotificationService {
    private final EmailService emailService;
    private final RespondentRepresentativeService respondentRepresentativeService;
    @Value("${nocNotification.template.respondent.id}")
    private String respondentTemplateId;
    @Value("${nocNotification.template.claimant.id}")
    private String claimantTemplateId;
    @Value("${nocNotification.template.previousrespondentsolicitor.id}")
    private String previousRespondentSolicitorTemplateId;
    @Value("${nocNotification.template.newrespondentsolicitor.id}")
    private String newRespondentSolicitorTemplateId;
    @Value("${nocNotification.template.tribunal.id}")
    private String tribunalTemplateId;

    public void sendNotificationOfChangeEmails(CallbackRequest callbackRequest, CaseData caseData) {
        emailService.sendEmail(
            claimantTemplateId,
            caseData.getClaimantType().getClaimantEmailAddress(),
            NocNotificationHelper.buildClaimantPersonalisation(caseData)
        );
        
        String oldSolicitorEmail = NocNotificationHelper.getOldSolicitorEmail(callbackRequest);
        emailService.sendEmail(
            previousRespondentSolicitorTemplateId,
            oldSolicitorEmail,
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseData)
        );

        String newSolicitorEmail = NocNotificationHelper.getNewSolicitorEmail(callbackRequest);
        emailService.sendEmail(
            newRespondentSolicitorTemplateId,
            newSolicitorEmail,
            NocNotificationHelper.buildNewRespondentSolicitorPersonalisation(caseData)
        );

        emailService.sendEmail(
            tribunalTemplateId,
            caseData.getTribunalCorrespondenceEmail(),
            NocNotificationHelper.buildTribunalPersonalisation(caseData)
        );

        RespondentSumType respondent = NocNotificationHelper.getRespondent(callbackRequest, caseData, respondentRepresentativeService);
        emailService.sendEmail(
            respondentTemplateId,
            respondent.getRespondentEmail(),
            NocNotificationHelper.buildRespondentPersonalisation(caseData, respondent)
        );
    }
}