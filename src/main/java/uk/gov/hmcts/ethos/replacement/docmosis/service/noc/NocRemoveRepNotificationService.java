package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;

import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_NOT_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_LEAD;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_EMAIL_TEMPLATE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;

@Slf4j
@Service
public class NocRemoveRepNotificationService {

    private final EmailService emailService;
    private final NocNotificationService nocNotificationService;
    private final String claimantSharedRepRemovalOrgAdminTemplate;
    private final String claimantNonSharedRepRemovalOrgAdminTemplate;
    private final String claimantRepRemovalRepresentativeTemplateId;

    public NocRemoveRepNotificationService(
            EmailService emailService,
            NocNotificationService nocNotificationService,
            @Value("${template.representation.remove.claimant.sharedRepOrgAdmin}")
            String claimantSharedRepRemovalOrgAdminTemplate,
            @Value("${template.representation.remove.claimant.nonSharedRepOrgAdmin}")
            String claimantNonSharedRepRemovalOrgAdminTemplate,
            @Value("${template.representation.remove.claimant.representative}")
            String claimantRepRemovalRepresentativeTemplateId) {
        this.emailService = emailService;
        this.nocNotificationService = nocNotificationService;
        this.claimantSharedRepRemovalOrgAdminTemplate = claimantSharedRepRemovalOrgAdminTemplate;
        this.claimantNonSharedRepRemovalOrgAdminTemplate = claimantNonSharedRepRemovalOrgAdminTemplate;
        this.claimantRepRemovalRepresentativeTemplateId = claimantRepRemovalRepresentativeTemplateId;
    }

    public void sendClaimantRepresentativeRemovalNotifications(UserDetails userDetails, CaseDetails caseDetails) {
        if (NOC_REPRESENTATIVE_TYPE_SHARED.equals(caseDetails.getCaseData().getNocRepresentativeType())
                && NOC_CASE_SHARING_STATUS_SHARED.equals(caseDetails.getCaseData().getNocCaseSharingStatus())) {
            String orgAdminEmail = nocNotificationService.findClaimantRepOrgSuperUserEmail(caseDetails.getCaseData()
                    .getRepresentativeClaimantType());
            // We don't send email to claimant
            sendClaimantRepRemovalOrgAdminNotification(caseDetails, orgAdminEmail,
                    UserUtils.resolveUserDisplayName(userDetails), claimantSharedRepRemovalOrgAdminTemplate);
        }
        if (NOC_REPRESENTATIVE_TYPE_LEAD.equals(caseDetails.getCaseData().getNocRepresentativeType())
                && NOC_CASE_SHARING_STATUS_NOT_SHARED.equals(caseDetails.getCaseData().getNocCaseSharingStatus())) {
            String orgAdminEmail = nocNotificationService.findClaimantRepOrgSuperUserEmail(caseDetails.getCaseData()
                    .getRepresentativeClaimantType());
            // We don't send email to claimant
            sendClaimantRepRemovalOrgAdminNotification(caseDetails, orgAdminEmail,
                    UserUtils.resolveUserDisplayName(userDetails), claimantNonSharedRepRemovalOrgAdminTemplate);
        }
        if (NOC_REPRESENTATIVE_TYPE_SHARED.equals(caseDetails.getCaseData().getNocRepresentativeType())
                && NOC_CASE_SHARING_STATUS_SHARED.equals(caseDetails.getCaseData().getNocCaseSharingStatus())
                || NOC_REPRESENTATIVE_TYPE_LEAD.equals(caseDetails.getCaseData().getNocRepresentativeType())
                && NOC_CASE_SHARING_STATUS_NOT_SHARED.equals(caseDetails.getCaseData().getNocCaseSharingStatus())) {
            sendClaimantRepRemovalRepresentativeNotification(caseDetails, userDetails.getEmail());
        }
    }

    /**
     * Sends a notification email to the claimant representative's organisation administrator
     * when a claimant representative is removed from a case.
     *
     * <p>Assumes that the appropriate notification template has already been selected
     * based on the representative's role (lead or shared representative) and whether
     * the case is shared with other organisations.</p>
     *
     * <p>The email includes common case details and the removed representative's name.
     * If the organisation administrator's email address or template ID is missing,
     * or if sending the email fails, a warning is logged without throwing an exception.</p>
     *
     * @param caseDetails the case details used to populate the email notification
     * @param orgAdminEmail the email address of the organisation administrator
     * @param representativeName the name of the removed claimant representative
     * @param templateId the notification template ID selected based on the removal scenario
     */
    public void sendClaimantRepRemovalOrgAdminNotification(CaseDetails caseDetails,
                                                           String orgAdminEmail,
                                                           String representativeName,
                                                           String templateId) {
        if (StringUtils.isBlank(orgAdminEmail)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN, caseDetails.getCaseId(),
                    WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
            return;
        }
        if (StringUtils.isBlank(templateId)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN, caseDetails.getCaseId(),
                    WARNING_EMAIL_TEMPLATE_NOT_FOUND);
            return;
        }
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, representativeName);
        try {
            emailService.sendEmail(templateId, orgAdminEmail, personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    /**
     * Sends a notification email to a claimant representative when they are removed from a case.
     *
     * <p>Assumes that the representative's email address is valid and that the notification
     * template ID has been configured correctly.</p>
     *
     * <p>The email includes common case details using the configured claimant representative
     * removal notification template. If sending the email fails, a warning is logged
     * without propagating the exception.</p>
     *
     * @param caseDetails the case details used to populate the email notification
     * @param representativeEmail the email address of the removed claimant representative
     */
    public void sendClaimantRepRemovalRepresentativeNotification(CaseDetails caseDetails,
                                                                 String representativeEmail) {
        if (StringUtils.isBlank(representativeEmail)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE, caseDetails.getCaseId(),
                    WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND);
            return;
        }
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        try {
            emailService.sendEmail(claimantRepRemovalRepresentativeTemplateId, representativeEmail, personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }
}
