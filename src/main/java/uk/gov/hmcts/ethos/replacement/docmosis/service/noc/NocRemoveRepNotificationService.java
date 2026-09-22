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

import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveRepNotificationService {

    private final EmailService emailService;
    private final NocNotificationService nocNotificationService;

    @Value("${template.removeRepresentationNotifications.claimantRepresentative.selfRemoval.orgAdmin}")
    private String claimantRepresentativeSelfRemovalOrgAdminTemplateId;

    public void sendClaimantRepresentativeRemovalNotifications(UserDetails userDetails, CaseDetails caseDetails) {
        String orgAdminEmail = nocNotificationService.findClaimantRepOrgSuperUserEmail(caseDetails.getCaseData()
                .getRepresentativeClaimantType());
        if (NOC_REMOVE_OPTION_YOURSELF.equals(caseDetails.getCaseData().getNocRemoveOption())) {
            sendClaimantRepresentativeSelfRemovalOrgAdminNotification(caseDetails, orgAdminEmail,
                    userDetails.getName());
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
    public void sendClaimantRepresentativeSelfRemovalOrgAdminNotification(CaseDetails caseDetails,
                                                                          String orgAdminEmail,
                                                                          String representativeName) {
        if (StringUtils.isBlank(orgAdminEmail)) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION, caseDetails.getCaseId(),
                    WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
        }
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, representativeName);
        try {
            emailService.sendEmail(claimantRepresentativeSelfRemovalOrgAdminTemplateId, orgAdminEmail,
                    personalisation);
        } catch (Exception e) {
            log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION, caseDetails.getCaseId(),
                    e.getMessage());
        }
    }
}