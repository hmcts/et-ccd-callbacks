package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;

import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_NOT_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_LEAD;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REMOVED_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;

@Slf4j
@Service
public class NocRemoveRepNotificationService {

    private final EmailService emailService;
    private final NocNotificationService nocNotificationService;
    private final String claimantNonSharedLeadRepRemovalOrgAdminTemplate;
    private final String claimantLeadRepRemovalRepresentativeTemplateId;

    public NocRemoveRepNotificationService(
            EmailService emailService,
            NocNotificationService nocNotificationService,
            @Value("${template.representation.remove.claimant.nonSharedLeadRepOrgAdmin}")
            String claimantNonSharedLeadRepRemovalOrgAdminTemplate,
            @Value("${template.representation.remove.claimant.nonSharedLeadRep}")
            String claimantLeadRepRemovalRepresentativeTemplateId) {
        this.emailService = emailService;
        this.nocNotificationService = nocNotificationService;
        this.claimantNonSharedLeadRepRemovalOrgAdminTemplate = claimantNonSharedLeadRepRemovalOrgAdminTemplate;
        this.claimantLeadRepRemovalRepresentativeTemplateId = claimantLeadRepRemovalRepresentativeTemplateId;
    }

    /**
     * Sends removal notifications to the claimant representative and their organisation
     * administrator when the lead representative is removed from a non-shared case.
     *
     * <p>Assumes that removing the lead representative from a non-shared case results
     * in the removal of the entire claimant organisation from the case.</p>
     *
     * <p>If the removed representative's details are missing, a warning is logged
     * and no notifications are sent. Notifications are sent only when the removed
     * representative is identified as the lead representative and the case is not shared.
     * The organisation administrator is notified using the removed representative's
     * name, and the removed representative receives a separate notification.
     * No notification is sent to the claimant.</p>
     *
     * <p>Any failures during email delivery are handled and logged by the respective
     * notification methods without propagating the exceptions.</p>
     *
     * @param userDetails the user details of the removed claimant representative
     * @param caseDetails the case details used to determine notification eligibility
     *                    and populate the email notifications
     * @param removedRepresentative the removed claimant representative's details,
     *                              used to identify the organisation administrator
     */
    public void sendClaimantRepresentativeRemovalNotifications(UserDetails userDetails,
                                                               CaseDetails caseDetails,
                                                               RepresentedTypeC removedRepresentative) {
        if (ObjectUtils.isEmpty(removedRepresentative)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL, caseDetails.getCaseId(),
                    WARNING_REMOVED_REPRESENTATIVE_NOT_FOUND);
            return;
        }
        if (NOC_REPRESENTATIVE_TYPE_LEAD.equals(caseDetails.getCaseData().getNocRepresentativeType())
                && NOC_CASE_SHARING_STATUS_NOT_SHARED.equals(caseDetails.getCaseData().getNocCaseSharingStatus())) {
            String orgAdminEmail = nocNotificationService.findClaimantRepOrgSuperUserEmail(removedRepresentative);
            // We don't send email to claimant
            sendClaimantNonSharedLeadRepRemovalOrgAdminNotification(caseDetails, orgAdminEmail,
                    UserUtils.resolveUserDisplayName(userDetails));
            sendClaimantNonSharedLeadRepRemovalRepresentativeNotification(caseDetails, userDetails.getEmail());
        }
    }

    /**
     * Sends a notification email to the claimant representative's organisation administrator
     * when the claimant's representation is removed from a non-shared case.
     *
     * <p>Assumes that the removal of the lead representative results in the removal
     * of the entire claimant organisation from the case, and that the notification
     * template ID has been configured correctly.</p>
     *
     * <p>The email includes common case details and the removed representative's name.
     * If the organisation administrator's email address is missing, or if sending
     * the email fails, a warning is logged without throwing an exception.</p>
     *
     * @param caseDetails the case details used to populate the email notification
     * @param orgAdminEmail the email address of the claimant organisation administrator
     * @param representativeName the name of the removed claimant representative
     */
    public void sendClaimantNonSharedLeadRepRemovalOrgAdminNotification(CaseDetails caseDetails,
                                                                        String orgAdminEmail,
                                                                        String representativeName) {
        if (StringUtils.isBlank(orgAdminEmail)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN, caseDetails.getCaseId(),
                    WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
            return;
        }
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, representativeName);
        try {
            emailService.sendEmail(claimantNonSharedLeadRepRemovalOrgAdminTemplate, orgAdminEmail, personalisation);
        } catch (Exception ex) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN, caseDetails.getCaseId(),
                    ex.getMessage());
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
    public void sendClaimantNonSharedLeadRepRemovalRepresentativeNotification(CaseDetails caseDetails,
                                                                              String representativeEmail) {
        if (StringUtils.isBlank(representativeEmail)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE, caseDetails.getCaseId(),
                    WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND);
            return;
        }
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        try {
            emailService.sendEmail(claimantLeadRepRemovalRepresentativeTemplateId, representativeEmail,
                    personalisation);
        } catch (Exception ex) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE, caseDetails.getCaseId(),
                    ex.getMessage());
        }
    }
}
