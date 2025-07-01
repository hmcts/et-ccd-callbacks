package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPersonalisationWithPartyName;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildRespondentPersonalisation;

/**
 * Service to support the notification of change journey with email notifications.
 */
@RequiredArgsConstructor
@Service("NocNotificationService")
@Slf4j
public class NocNotificationService {
    private final EmailService emailService;
    private final NocRespondentHelper nocRespondentHelper;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    @Value("${template.nocNotification.respondent}")
    private String respondentTemplateId;
    @Value("${template.nocNotification.claimant}")
    private String claimantTemplateId;
    @Value("${template.nocNotification.respondent-solicitor.previous}")
    private String previousRespondentSolicitorTemplateId;
    @Value("${template.nocNotification.respondent-solicitor.new}")
    private String newRespondentSolicitorTemplateId;
    @Value("${template.nocNotification.tribunal}")
    private String tribunalTemplateId;

    public void sendNotificationOfChangeEmails(CaseDetails caseDetailsPrevious,
                                               CaseDetails caseDetailsNew,
                                               ChangeOrganisationRequest changeRequest) {
        CaseData caseDataNew = caseDetailsNew.getCaseData();
        String partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        CaseData caseDataPrevious = caseDetailsPrevious.getCaseData();
        if (!isClaimantNonSystemUser(caseDataPrevious)) {
            sendClaimantEmail(caseDetailsPrevious, caseDetailsNew, partyName);
        }

        if (changeRequest.getOrganisationToRemove() != null) {
            String previousOrgId = changeRequest.getOrganisationToRemove().getOrganisationID();
            sendEmailToOldOrgAdmin(previousOrgId, caseDataPrevious);
        }

        String newOrgId = changeRequest.getOrganisationToAdd().getOrganisationID();
        sendEmailToNewOrgAdmin(newOrgId, caseDetailsNew, partyName);

        sendTribunalEmail(caseDataPrevious);

        RespondentSumType respondent =
                NocNotificationHelper.getRespondent(changeRequest, caseDataPrevious, nocRespondentHelper);
        String respondentEmail = respondent == null ? null : respondent.getRespondentEmail();

        if (isNullOrEmpty(respondentEmail)) {
            log.warn("Missing respondentEmail");
            return;
        }

        Map<String, String> personalisation = buildRespondentPersonalisation(caseDetailsPrevious, respondent);
        emailService.sendEmail(respondentTemplateId, respondentEmail, personalisation);
    }

    private void sendTribunalEmail(CaseData caseDataPrevious) {
        String tribunalEmail = caseDataPrevious.getTribunalCorrespondenceEmail();
        if (isNullOrEmpty(tribunalEmail)) {
            log.warn("missing tribunalEmail");
            return;
        }

        Map<String, String> personalisation = NocNotificationHelper.buildTribunalPersonalisation(caseDataPrevious);
        emailService.sendEmail(tribunalTemplateId, tribunalEmail, personalisation);
    }

    private void sendClaimantEmail(CaseDetails caseDetailsPrevious, CaseDetails caseDetailsNew, String partyName) {
        String email = NotificationHelper.getEmailAddressForClaimant(caseDetailsPrevious.getCaseData());
        if (isNullOrEmpty(email)) {
            log.warn("missing claimantEmail");
            return;
        }

        String citUILink = emailService.getCitizenCaseLink(caseDetailsNew.getCaseId());
        var personalisation = buildPersonalisationWithPartyName(caseDetailsPrevious, partyName, citUILink);
        emailService.sendEmail(claimantTemplateId, email, personalisation);
    }

    private void sendEmailToOldOrgAdmin(String orgId, CaseData caseDataPrevious) {
        ResponseEntity<RetrieveOrgByIdResponse> getOrgResponse = getOrganisationById(orgId);
        HttpStatusCode statusCode = getOrgResponse.getStatusCode();

        if (!HttpStatus.OK.equals(statusCode)) {
            log.error("Cannot retrieve old org by id {} [{}] {}", orgId, statusCode, getOrgResponse.getBody());
            return;
        }

        RetrieveOrgByIdResponse resBody = getOrgResponse.getBody();
        if (resBody == null) {
            return;
        }

        if (resBody.getSuperUser() == null || isNullOrEmpty(resBody.getSuperUser().getEmail())) {
            log.warn("Previous Org {} is missing org admin email", orgId);
            return;
        }

        Map<String, String> personalisation = buildPreviousRespondentSolicitorPersonalisation(caseDataPrevious);
        emailService.sendEmail(
                previousRespondentSolicitorTemplateId,
                resBody.getSuperUser().getEmail(),
                personalisation);
    }

    private void sendEmailToNewOrgAdmin(String orgId, CaseDetails caseDetailsNew, String partyName) {
        ResponseEntity<RetrieveOrgByIdResponse> getOrgResponse = getOrganisationById(orgId);
        HttpStatusCode statusCode = getOrgResponse.getStatusCode();

        if (!HttpStatus.OK.equals(statusCode)) {
            log.error("Cannot retrieve new org by id {} [{}] {}", orgId, statusCode, getOrgResponse.getBody());
            return;
        }

        RetrieveOrgByIdResponse resBody = getOrgResponse.getBody();
        if (resBody == null) {
            return;
        }

        if (resBody.getSuperUser() == null || isNullOrEmpty(resBody.getSuperUser().getEmail())) {
            log.warn("New Org {} is missing org admin email", orgId);
            return;
        }

        String citUrl = emailService.getCitizenCaseLink(caseDetailsNew.getCaseId());
        Map<String, String> personalisation = buildPersonalisationWithPartyName(caseDetailsNew, partyName, citUrl);
        emailService.sendEmail(newRespondentSolicitorTemplateId, resBody.getSuperUser().getEmail(), personalisation);
    }

    private ResponseEntity<RetrieveOrgByIdResponse> getOrganisationById(String orgId) {
        return organisationClient.getOrganisationById(
                adminUserService.getAdminUserToken(),
                authTokenGenerator.generate(),
                orgId);
    }
}