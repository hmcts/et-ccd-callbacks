package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Map;
import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_ORGANISATION_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_FAILED_TO_SEND_EMAIL_TRIBUNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_MISSING_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantRepresentedByMyHmctsOrganisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildNoCPersonalisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPersonalisationWithPartyName;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation;

/**
 * Service to support the notification of change journey with email notifications.
 */
@RequiredArgsConstructor
@Service("nocNotificationService")
@Slf4j
public class NocNotificationService {
    private final EmailService emailService;
    private final NocRespondentHelper nocRespondentHelper;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final EmailNotificationService emailNotificationService;
    private final CaseAccessService caseAccessService;

    @Value("${template.nocNotification.respondent}")
    private String respondentTemplateId;
    @Value("${template.nocNotification.claimant}")
    private String claimantTemplateId;
    @Value("${template.nocNotification.claimantRepAssigned}")
    private String claimantRepAssignedTemplateId;
    @Value("${template.nocNotification.respondent-solicitor.previous}")
    private String previousRespondentSolicitorTemplateId;
    @Value("${template.nocNotification.respondent-solicitor.new}")
    private String newRespondentSolicitorTemplateId;
    @Value("${template.nocNotification.tribunal}")
    private String tribunalTemplateId;

    public void sendRespondentRepresentationRemovalNotifications(CaseDetails oldCaseDetails,
                                                                 CaseDetails newCaseDetails,
                                                                 List<RepresentedTypeRItem> revokedRepresentatives) {
        if (CollectionUtils.isEmpty(revokedRepresentatives)) {
            return;
        }
        for (RepresentedTypeRItem revokedRepresentative : revokedRepresentatives) {
            if (!RespondentRepresentativeUtils.isValidRepresentative(revokedRepresentative)) {
                continue;
            }
            CaseData oldCaseData = oldCaseDetails.getCaseData();
            RespondentSumTypeItem respondent = RespondentRepresentativeUtils.findRespondentByRepresentative(oldCaseData,
                    revokedRepresentative);
            if (!RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }
            assert respondent != null;

            // Sending notification e-mail to claimant
            if (!isClaimantNonSystemUser(oldCaseData)) {
                sendClaimantEmail(oldCaseDetails, newCaseDetails, respondent.getValue().getRespondentName());
            }

            // sending notification email to organisation admin of the representative
            if (ObjectUtils.isNotEmpty(revokedRepresentative.getValue().getRespondentOrganisation())
                    && StringUtils.isNotBlank(revokedRepresentative.getValue().getRespondentOrganisation()
                    .getOrganisationID())) {
                sendEmailToOldOrgAdmin(revokedRepresentative.getValue().getRespondentOrganisation().getOrganisationID(),
                        oldCaseData);
            }

            // sending notification e-mail to tribunal
            if (StringUtils.isNotBlank(oldCaseData.getTribunalCorrespondenceEmail())) {
                sendTribunalEmail(oldCaseData);
            }

            // sending notification e-mail to respondent
            if (StringUtils.isBlank(respondent.getValue().getRespondentEmail())) {
                log.warn(WARNING_MISSING_EMAIL_ADDRESS, oldCaseDetails.getCaseId());
                continue;
            }
            Map<String, String> personalisation = buildNoCPersonalisation(oldCaseDetails,
                    respondent.getValue().getRespondentName());
            try {
                emailService.sendEmail(respondentTemplateId, respondent.getValue().getRespondentEmail(),
                        personalisation);
            } catch (Exception e) {
                LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_RESPONDENT,
                        respondent.getValue().getRespondentEmail(), e);
            }
        }
    }

    public void sendNotificationOfChangeEmails(CaseDetails caseDetailsPrevious,
                             CaseDetails caseDetailsNew,
                             ChangeOrganisationRequest changeRequest) {
        DynamicFixedListType caseRoleId = changeRequest.getCaseRoleId();
        CaseData caseDataNew = caseDetailsNew.getCaseData();
        CaseData caseDataPrevious = caseDetailsPrevious.getCaseData();
        String partyName;
        String newRepEmailAddress = null;

        if (caseRoleId.getValue().getCode().equals(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())) {
            // send claimant noc change email
            partyName = caseDataPrevious.getClaimant();
            if (caseDataNew.getRepresentativeClaimantType() != null) {
                newRepEmailAddress = caseDataNew.getRepresentativeClaimantType().getRepresentativeEmailAddress();
                handleClaimantNocEmails(caseDetailsNew, partyName);
            }
        } else {
            // send respondent noc change email
            handleRespondentNocEmails(caseDetailsPrevious, caseDetailsNew, changeRequest);
            // todo: to get the new respondent solicitor email address
            partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        }

        // send organisation noc change email
        handleOrganisationNocEmails(caseDataPrevious, caseDetailsNew, changeRequest, partyName, newRepEmailAddress);

        // send tribunal noc change email
        sendTribunalEmail(caseDataPrevious);
    }

    private void handleClaimantNocEmails(CaseDetails caseDetailsNew, String partyName) {
        CaseData caseDataNew = caseDetailsNew.getCaseData();

        List<CaseUserAssignment> caseUserAssignments =
                caseAccessService.getCaseUserAssignmentsById(caseDetailsNew.getCaseId());

        // send respondents or respondent solicitors the claimant noc change email
        emailNotificationService.getRespondentsAndRepsEmailAddresses(caseDataNew, caseUserAssignments)
                .forEach((email, respondentId) -> {
                    String caseLink = StringUtils.isNotBlank(respondentId)
                            ? emailService.getSyrCaseLink(caseDetailsNew.getCaseId(), respondentId)
                            : emailService.getExuiCaseLink(caseDetailsNew.getCaseId());
                    emailService.sendEmail(claimantTemplateId, email,
                            buildPersonalisationWithPartyName(caseDetailsNew, partyName, caseLink));
                });

        // send claimant noc change email
        String claimantEmail = ClaimantUtils.getClaimantEmailAddress(caseDataNew);
        if (isNullOrEmpty(claimantEmail)) {
            log.warn("missing claimantEmail");
            return;
        }

        Map<String, String> personalisation = buildNoCPersonalisation(caseDetailsNew, partyName);
        personalisation.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseDetailsNew.getCaseId()));
        emailService.sendEmail(claimantRepAssignedTemplateId, claimantEmail, personalisation);
    }

    private void handleRespondentNocEmails(CaseDetails caseDetailsPrevious,
                                          CaseDetails caseDetailsNew,
                                          ChangeOrganisationRequest changeRequest) {

        CaseData caseDataNew = caseDetailsNew.getCaseData();
        CaseData caseDataPrevious = caseDetailsPrevious.getCaseData();
        String partyName = NocNotificationHelper.getRespondentNameForNewSolicitor(changeRequest, caseDataNew);
        // send claimant or claimant solicitor noc change email
        if (!isClaimantNonSystemUser(caseDataPrevious)
                || isClaimantRepresentedByMyHmctsOrganisation(caseDataPrevious)) {
            sendClaimantEmail(caseDetailsPrevious, caseDetailsNew, partyName);
        }

        // send respondent noc change email
        RespondentSumType respondent =
                NocNotificationHelper.getRespondent(changeRequest, caseDataPrevious, nocRespondentHelper);
        String respondentEmail = respondent == null ? null : respondent.getRespondentEmail();

        if (isNullOrEmpty(respondentEmail)) {
            log.warn("Missing respondentEmail");
            return;
        }

        Map<String, String> personalisation = buildNoCPersonalisation(caseDetailsPrevious,
                respondent.getRespondentName());
        emailService.sendEmail(respondentTemplateId, respondentEmail, personalisation);
    }

    private void handleOrganisationNocEmails(CaseData caseDataPrevious,
                                            CaseDetails caseDetailsNew,
                                            ChangeOrganisationRequest changeRequest,
                                            String partyName,
                                            String newRepEmailAddress) {

        if (changeRequest.getOrganisationToRemove() != null) {
            String previousOrgId = changeRequest.getOrganisationToRemove().getOrganisationID();
            sendEmailToOldOrgAdmin(previousOrgId, caseDataPrevious);
        }

        if (changeRequest.getOrganisationToAdd() != null) {
            String newOrgId = changeRequest.getOrganisationToAdd().getOrganisationID();
            sendEmailToNewOrgAdmin(newOrgId, caseDetailsNew, partyName);
        }

        // send email to the new legal representative
        if (StringUtils.isNotBlank(newRepEmailAddress)) {
            Map<String, String> personalisation = buildPersonalisationWithPartyName(caseDetailsNew, partyName,
                    emailService.getExuiCaseLink(caseDetailsNew.getCaseId()));
            emailService.sendEmail(newRespondentSolicitorTemplateId, newRepEmailAddress, personalisation);
        }
    }

    private void sendTribunalEmail(CaseData caseData) {
        String tribunalEmail = caseData.getTribunalCorrespondenceEmail();
        if (isNullOrEmpty(tribunalEmail)) {
            log.warn("missing tribunalEmail");
            return;
        }

        Map<String, String> personalisation = NocNotificationHelper.buildTribunalPersonalisation(caseData);
        try {
            emailService.sendEmail(tribunalTemplateId, tribunalEmail, personalisation);
        } catch (Exception e) {
            LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_TRIBUNAL, tribunalEmail, e);
        }
    }

    public void sendClaimantEmail(CaseDetails caseDetailsPrevious, CaseDetails caseDetailsNew, String partyName) {
        if (ObjectUtils.isEmpty(caseDetailsPrevious) || ObjectUtils.isEmpty(caseDetailsNew)) {
            log.error(EXCEPTION_CASE_DETAILS_NOT_FOUND);
            return;
        }
        RepresentedTypeC claimantRep = caseDetailsPrevious.getCaseData().getRepresentativeClaimantType();
        String email = ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetailsPrevious);
        if (isNullOrEmpty(email)) {
            log.warn(WARNING_CLAIMANT_EMAIL_NOT_FOUND, caseDetailsPrevious.getCaseId());
            return;
        }
        String citUILink = claimantRep != null
                ? emailService.getExuiCaseLink(caseDetailsNew.getCaseId())
                : emailService.getCitizenCaseLink(caseDetailsNew.getCaseId());
        var personalisation = buildPersonalisationWithPartyName(caseDetailsPrevious, partyName, citUILink);
        try {
            emailService.sendEmail(claimantTemplateId, email, personalisation);
        } catch (Exception e) {
            LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_CLAIMANT, email, e);
        }
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
        try {
            emailService.sendEmail(
                    previousRespondentSolicitorTemplateId,
                    resBody.getSuperUser().getEmail(),
                    personalisation);
        } catch (Exception e) {
            LoggingUtils.logNotificationIssue(ERROR_FAILED_TO_SEND_EMAIL_ORGANISATION_ADMIN, orgId, e);
        }
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