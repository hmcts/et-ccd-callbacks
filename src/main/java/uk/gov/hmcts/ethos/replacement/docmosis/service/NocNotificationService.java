package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Service to support the notification of change journey with email notifications.
 */
@RequiredArgsConstructor
@Service("NocNotificationService")
@Slf4j
@SuppressWarnings({"PMD.CognitiveComplexity", "PMD.LawOfDemeter"})
public class NocNotificationService {
    private final EmailService emailService;
    private final NocRespondentHelper nocRespondentHelper;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
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

        if (changeRequest.getOrganisationToRemove() != null) {
            String previousOrgId = changeRequest.getOrganisationToRemove().getOrganisationID();
            sendEmailToOldOrgAdmin(previousOrgId, caseDataPrevious);
        }

        String newOrgId = changeRequest.getOrganisationToAdd().getOrganisationID();
        sendEmailToNewOrgAdmin(newOrgId, caseDetailsNew, partyName);

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

    private void sendEmailToOldOrgAdmin(String orgId, CaseData caseDataPrevious) {
        ResponseEntity<RetrieveOrgByIdResponse> getOrgResponse = getOrganisationById(orgId);
        if (HttpStatus.OK.equals(getOrgResponse.getStatusCode())) {
            Object resBody = getOrgResponse.getBody();
            if (resBody != null) {
                String oldOrgAdminEmail = ((RetrieveOrgByIdResponse) resBody).getSuperUser().getEmail();
                if (isNullOrEmpty(oldOrgAdminEmail)) {
                    log.warn("Previous Org " + orgId + " is missing org admin email");
                } else {
                    emailService.sendEmail(
                            previousRespondentSolicitorTemplateId,
                            oldOrgAdminEmail,
                            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDataPrevious)
                    );
                }
            }

        } else {
            log.error("Cannot retrieve old org by id " + orgId
                    + " [" + getOrgResponse.getStatusCode() + "] " + getOrgResponse.getBody());
        }
    }

    private void sendEmailToNewOrgAdmin(String orgId, CaseDetails caseDetailsNew, String partyName) {
        ResponseEntity<RetrieveOrgByIdResponse> getOrgResponse = getOrganisationById(orgId);
        if (HttpStatus.OK.equals(getOrgResponse.getStatusCode())) {
            Object resBody = getOrgResponse.getBody();
            if (resBody != null) {
                String newOrgAdminEmail = ((RetrieveOrgByIdResponse) resBody).getSuperUser().getEmail();
                if (isNullOrEmpty(newOrgAdminEmail)) {
                    log.warn("New Org " + orgId + " is missing org admin email");
                } else {
                    emailService.sendEmail(
                            newRespondentSolicitorTemplateId,
                            newOrgAdminEmail,
                            NocNotificationHelper.buildPersonalisationWithPartyName(caseDetailsNew, partyName)
                    );
                }
            }
        } else {
            log.error("Cannot retrieve new org by id " + orgId
                    + " [" + getOrgResponse.getStatusCode() + "] " + getOrgResponse.getBody());
        }
    }

    private ResponseEntity<RetrieveOrgByIdResponse> getOrganisationById(String orgId) {
        return organisationClient.getOrganisationById(
                adminUserService.getAdminUserToken(),
                authTokenGenerator.generate(),
                orgId);
    }
}