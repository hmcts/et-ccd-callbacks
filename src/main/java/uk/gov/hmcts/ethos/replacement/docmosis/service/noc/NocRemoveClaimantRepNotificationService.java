package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;

import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_REMOVE_OPTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_ORG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CIT_UI;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveClaimantRepNotificationService {

    private final EmailService emailService;
    private final NocNotificationService nocNotificationService;
    private final OrganisationService organisationService;

    @Value("${template.removeRepresentationNotifications.claimantRepresentative.selfRemoval.orgAdmin}")
    private String claimantRepresentativeSelfRemovalOrgAdminTemplateId;
    @Value("${template.removeRepresentationNotifications.claimantRepresentative.orgRemoval.orgAdmin}")
    private String claimantRepresentativeOrganisationRemovalOrgAdminTemplateId;
    @Value("${template.removeRepresentationNotifications.claimantRepresentative.representative}")
    private String claimantRepresentativeRemovalRepTemplateId;
    @Value("${template.removeRepresentationNotifications.claimantRepresentative.orgRemoval.claimant}")
    private String claimantRepresentativeOrganisationRemovalClaimantTemplateId;

    public void sendClaimantRepresentativeRemovalNotifications(UserDetails userDetails, CaseDetails caseDetails) {
        if (!CaseDataUtils.hasValidNocRemoveOption(caseDetails)) {
            log.warn(WARNING_INVALID_REMOVE_OPTION, caseDetails.getCaseId(),
                    caseDetails.getCaseData().getNocRemoveOption());
            return;
        }
        String orgAdminEmail = nocNotificationService.findClaimantRepOrgSuperUserEmail(caseDetails.getCaseData()
                .getRepresentativeClaimantType());
        sendClaimantRepresentativeRemovalOrgAdminNotification(caseDetails, orgAdminEmail,
                UserUtils.resolveUserDisplayName(userDetails));
        sendClaimantRepresentativeRemovalRepNotification(caseDetails, userDetails.getEmail());
        if (NOC_REMOVE_OPTION_ORGANISATION.equals(caseDetails.getCaseData().getNocRemoveOption())) {
            sendClaimantRepresentativeRemovalClaimantNotification(userDetails, caseDetails);
        }
    }

    /**
     * Sends a notification email to the claimant representative's organisation administrator
     * when the representative removes themselves from the case.
     * <p>
     * The email is personalised using common case details and the representative's name.
     * If the organisation administrator email address is blank, or if an error occurs while
     * sending the email, a warning is logged.
     * </p>
     *
     * @param caseDetails        the case details used to populate the email personalisation
     * @param orgAdminEmail      the email address of the organisation administrator
     * @param representativeName the name of the claimant representative being removed
     */
    public void sendClaimantRepresentativeRemovalOrgAdminNotification(CaseDetails caseDetails,
                                                                      String orgAdminEmail,
                                                                      String representativeName) {
        if (StringUtils.isBlank(orgAdminEmail)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN, caseDetails.getCaseId(),
                    WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
            return;
        }

        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, representativeName);
        String templateId = NOC_REMOVE_OPTION_YOURSELF.equals(caseDetails.getCaseData().getNocRemoveOption())
                ? claimantRepresentativeSelfRemovalOrgAdminTemplateId
                : claimantRepresentativeOrganisationRemovalOrgAdminTemplateId;
        try {
            emailService.sendEmail(templateId, orgAdminEmail, personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    /**
     * Sends a notification email to the claimant representative when they remove
     * themselves or their organisations from the case.
     * <p>
     * The email is populated with common case-related personalisation values.
     * If an error occurs while sending the notification, a warning is logged
     * against the case ID.
     * </p>
     *
     * @param caseDetails the case details used to populate the email personalisation
     * @param representativeEmail the email address of the claimant representative
     */
    public void sendClaimantRepresentativeRemovalRepNotification(CaseDetails caseDetails,
                                                                 String representativeEmail) {
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        try {
            emailService.sendEmail(claimantRepresentativeRemovalRepTemplateId, representativeEmail,
                    personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    public void sendClaimantRepresentativeRemovalClaimantNotification(UserDetails userDetails,
                                                                      CaseDetails caseDetails) {
        String claimantEmailAddress = ClaimantUtils.getClaimantEmailAddressWithoutException(caseDetails.getCaseData());
        if (StringUtils.isBlank(claimantEmailAddress)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT, caseDetails.getCaseId(),
                    WARNING_CLAIMANT_EMAIL_NOT_FOUND);
            return;
        }
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, organisationService.resolveClaimantRepresentativeOrganisationName(
                caseDetails.getCaseData().getRepresentativeClaimantType(), userDetails));
        personalisation.put(LINK_TO_CIT_UI, emailService.getCitizenCaseLink(caseDetails.getCaseId()));
        try {
            emailService.sendEmail(claimantRepresentativeOrganisationRemovalClaimantTemplateId, claimantEmailAddress,
                    personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT, caseDetails.getCaseId(), e.getMessage());
        }
    }

}