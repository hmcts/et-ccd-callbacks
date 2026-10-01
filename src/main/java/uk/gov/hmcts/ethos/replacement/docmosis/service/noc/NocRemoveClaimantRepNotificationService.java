package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_INVALID_REMOVE_OPTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_RESPONDENT_EMAIL_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_ORG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CIT_UI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.PARTY_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveClaimantRepNotificationService {

    private final EmailService emailService;
    private final NocNotificationService nocNotificationService;
    private final OrganisationService organisationService;
    private final RespondentService respondentService;

    @Value("${template.removeRepresentationNotifications.claimantRepresentative.selfRemoval.orgAdmin}")
    private String claimantRepresentativeSelfRemovalOrgAdminTemplateId;
    @Value("${template.removeRepresentationNotifications.claimantRepresentative.orgRemoval.orgAdmin}")
    private String claimantRepresentativeOrganisationRemovalOrgAdminTemplateId;
    @Value("${template.removeRepresentationNotifications.claimantRepresentative.representative}")
    private String claimantRepresentativeRemovalRepTemplateId;
    @Value("${template.removeRepresentationNotifications.claimantRepresentative.orgRemoval.claimant}")
    private String claimantRepresentativeOrganisationRemovalClaimantTemplateId;
    @Value("${template.removeRepresentationNotifications.claimantRepresentative.orgRemoval.respondent}")
    private String claimantRepresentativeOrganisationRemovalRespondentTemplateId;

    public void sendClaimantRepresentativeRemovalNotifications(UserDetails userDetails, CaseDetails caseDetails) {
        if (!CaseDataUtils.hasValidNocRemoveOption(caseDetails)) {
            LoggingUtils.logNocWarningIfValid(WARNING_INVALID_REMOVE_OPTION, caseDetails.getCaseId(),
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
            sendClaimantRepresentativeRemovalRespondentNotifications(caseDetails);
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
            LoggingUtils.logNocWarningIfValid(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN,
                    caseDetails.getCaseId(), WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
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
            LoggingUtils.logNocWarningIfValid(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION_ADMIN,
                    caseDetails.getCaseId(), e.getMessage());
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
            LoggingUtils.logNocWarningIfValid(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE,
                    caseDetails.getCaseId(), e.getMessage());
        }
    }

    public void sendClaimantRepresentativeRemovalClaimantNotification(UserDetails userDetails,
                                                                      CaseDetails caseDetails) {
        String claimantEmailAddress = ClaimantUtils.getClaimantEmailAddressWithoutException(caseDetails.getCaseData());
        if (StringUtils.isBlank(claimantEmailAddress)) {
            LoggingUtils.logNocWarningIfValid(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT,
                    caseDetails.getCaseId(), WARNING_CLAIMANT_EMAIL_NOT_FOUND);
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
            LoggingUtils.logNocWarningIfValid(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT,
                    caseDetails.getCaseId(), e.getMessage());
        }
    }

    /**
     * Sends claimant representative removal notification emails to all valid respondents
     * associated with the given case.
     * <p>
     * The method first checks whether the case contains respondents and then retrieves
     * the valid respondents from the case data. If no valid respondents are available,
     * no notifications are sent.
     * </p>
     *
     * <p>
     * For each valid respondent, the method resolves the email address to use. The
     * respondent's representative email is preferred where available; otherwise, the
     * respondent's own email address is used as a fallback.
     * </p>
     *
     * <p>
     * If no suitable email address can be resolved for a respondent, a warning is logged
     * and processing continues with the next respondent. Each notification is processed
     * independently so that a failure for one respondent does not prevent notifications
     * from being sent to the remaining respondents.
     * </p>
     *
     * <p><strong>Assumptions:</strong></p>
     * <ul>
     *     <li>The supplied {@code caseDetails} contains non-null case data.</li>
     *     <li>Only respondents considered valid by {@link RespondentUtils#getValidRespondents(CaseData)}
     *         are eligible to receive a notification.</li>
     *     <li>A representative's email address takes precedence over the respondent's
     *         own email address when resolving the notification recipient.</li>
     *     <li>The resolved claimant name is used as the {@code PARTY_NAME}
     *         personalisation value.</li>
     *     <li>A respondent-specific case link is generated and added as the
     *         {@code LINK_TO_CIT_UI} personalisation value.</li>
     *     <li>If an email address cannot be resolved, the notification is skipped for
     *         that respondent and a warning is logged.</li>
     *     <li>Any exception encountered while processing a respondent is caught and
     *         logged, allowing processing to continue for the remaining respondents.</li>
     * </ul>
     *
     * @param caseDetails the case details containing the case data, case identifier,
     *                    respondents, and information required to build the notification
     */
    public void sendClaimantRepresentativeRemovalRespondentNotifications(CaseDetails caseDetails) {
        if (!RespondentUtils.hasRespondents(caseDetails.getCaseData())) {
            return;
        }
        List<RespondentSumTypeItem> respondents = RespondentUtils.getValidRespondents(caseDetails.getCaseData());
        if (CollectionUtils.isEmpty(respondents)) {
            return;
        }
        String claimantName = ClaimantUtils.resolveClaimantName(caseDetails.getCaseData());
        for (RespondentSumTypeItem respondent : respondents) {
            try {
                String emailAddressToSend = respondentService
                        .resolveRepresentativeOrRespondentEmail(caseDetails.getCaseData(), respondent);
                if (StringUtils.isBlank(emailAddressToSend)) {
                    LoggingUtils.logNocWarningIfValid(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT,
                            caseDetails.getCaseId(), WARNING_RESPONDENT_EMAIL_NOT_FOUND);
                    continue;
                }
                Map<String, String> personalisation = NocNotificationHelper
                        .addCommonEmailValues(caseDetails.getCaseData());
                String linkToSyrUI = emailService.getSyrCaseLink(caseDetails.getCaseId(), respondent.getId());
                personalisation.put(PARTY_NAME, claimantName);
                personalisation.put(LINK_TO_CIT_UI, linkToSyrUI);
                emailService.sendEmail(
                        claimantRepresentativeOrganisationRemovalRespondentTemplateId,
                        emailAddressToSend,
                        personalisation
                );
            } catch (Exception e) {
                LoggingUtils.logNocWarningIfValid(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT,
                        caseDetails.getCaseId(), e.getMessage());
            }
        }
    }
}