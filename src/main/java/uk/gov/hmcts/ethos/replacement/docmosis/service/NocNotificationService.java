package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Service to support the notification of change journey with email notifications.
 */
@RequiredArgsConstructor
@Service("NocNotificationService")
@Slf4j
public class NocNotificationService {
    private final EmailService emailService;
    private final NocRespondentHelper nocRespondentHelper;
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

    public void sendNotificationOfChangeEmails(CaseDetails caseDetailsPrevious, CaseDetails caseDetailsNew,
                                               ChangeOrganisationRequest changeRequest) {
        CaseData caseDataNew = caseDetailsNew.getCaseData();
        String partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        CaseData caseDataPrevious = caseDetailsPrevious.getCaseData();
        String claimantEmail = NotificationHelper.buildMapForClaimant(caseDataPrevious, "").get("emailAddress");
        if (isNullOrEmpty(claimantEmail)) {
            log.warn("missing claimantEmail");
        } else {
            emailService.sendEmail(
                claimantTemplateId,
                claimantEmail,
                NocNotificationHelper.buildPersonalisationWithPartyName(caseDetailsPrevious, partyName)
            );
        }
        String oldSolicitorEmail = NocNotificationHelper
                .getOldSolicitorEmailForRepUpdate(caseDataPrevious, changeRequest);
        if (isNullOrEmpty(oldSolicitorEmail)) {
            log.warn("missing oldSolicitorEmail");
        } else {
            emailService.sendEmail(
                    previousRespondentSolicitorTemplateId,
                    oldSolicitorEmail,
                    NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDataPrevious)
            );
        }
        String newSolicitorEmail = NocNotificationHelper.getNewSolicitorEmailForRepUpdate(caseDataNew, changeRequest);
        if (isNullOrEmpty(newSolicitorEmail)) {
            log.warn("missing newSolicitorEmail");
        } else {
            emailService.sendEmail(
                newRespondentSolicitorTemplateId,
                newSolicitorEmail,
                NocNotificationHelper.buildPersonalisationWithPartyName(caseDetailsNew, partyName)
            );
        }

        String tribunalEmail = caseDataPrevious.getTribunalCorrespondenceEmail();
        if (isNullOrEmpty(tribunalEmail)) {
            log.warn("missing tribunalEmail");
        } else {
            emailService.sendEmail(
                tribunalTemplateId,
                caseDataPrevious.getTribunalCorrespondenceEmail(),
                NocNotificationHelper.buildTribunalPersonalisation(caseDataPrevious)
            );
        }

        RespondentSumType respondent =
                NocNotificationHelper.getRespondent(changeRequest, caseDataPrevious, nocRespondentHelper);
        String respondentEmail = respondent == null ? null : respondent.getRespondentEmail();
        if (isNullOrEmpty(respondentEmail)) {
            log.warn("Missing respondentEmail");
        } else {
            emailService.sendEmail(
                respondentTemplateId,
                respondent.getRespondentEmail(),
                NocNotificationHelper.buildRespondentPersonalisation(caseDataPrevious, respondent));
        }
    }
}