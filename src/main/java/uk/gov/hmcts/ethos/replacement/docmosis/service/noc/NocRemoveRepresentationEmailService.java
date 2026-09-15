package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CallbacksCollectionUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_ORG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CIT_UI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.PARTY_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveRepresentationEmailService {

    private final EmailService emailService;

    @Value("${template.nocNotification.org-admin-not-representing}")
    private String nocOrgAdminNotRepresentingTemplateId;
    @Value("${template.nocNotification.org-admin-no-rep_left}")
    private String nocOrgAdminNoRepLeftTemplateId;
    @Value("${template.nocNotification.noc-legal-rep-no-longer-assigned}")
    private String nocLegalRepNoLongerAssignedTemplateId;
    @Value("${template.nocNotification.noc-citizen-no-longer-represented}")
    private String nocCitizenNoLongerRepresentedTemplateId;
    @Value("${template.nocNotification.noc-other-party-not-represented}")
    private String nocOtherPartyNotRepresentedTemplateId;

    /**
     * Sends an email to the organisation admin when a representative is no longer representing a party.
     *
     * @param caseDetails The case details containing case data and ID
     * @param emailToSend The email address of the organisation admin
     * @param repName The name of the representative
     */
    public void sendEmailToOrgAdmin(CaseDetails caseDetails, String emailToSend, String repName, String emailType) {
        if (isNullOrEmpty(emailToSend)) {
            return;
        }
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, repName);
        String templateId = nocOrgAdminNotRepresentingTemplateId;
        if (EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT.equals(emailType)) {
            templateId = nocOrgAdminNoRepLeftTemplateId;
        }
        try {
            emailService.sendEmail(
                templateId,
                emailToSend,
                personalisation);
        } catch (Exception e) {
            log.warn(
                    WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    /**
     * Sends emails to a list of removed legal representatives.
     *
     * @param caseDetails The case details containing case data and ID
     * @param repEmailAddress List of email addresses of removed legal representatives
     */
    public void sendEmailToListOfRemovedLegalRep(CaseDetails caseDetails, List<String> repEmailAddress) {
        repEmailAddress.forEach(email -> sendEmailToRemovedLegalRep(caseDetails, email));
    }

    /**
     * Sends an email to a removed legal representative.
     *
     * @param caseDetails The case details containing case data and ID
     * @param emailToSend The email address of the removed legal representative
     */
    public void sendEmailToRemovedLegalRep(CaseDetails caseDetails, String emailToSend) {
        if (isNullOrEmpty(emailToSend)) {
            return;
        }
        try {
            Map<String, String> personalisation =
                NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
            emailService.sendEmail(
                nocLegalRepNoLongerAssignedTemplateId,
                emailToSend,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    /**
     * Sends an email to an unrepresented claimant when their legal representative is removed.
     *
     * @param caseDetails The case details containing case data and ID
     * @param orgName The name of the removed organisation
     */
    public void sendEmailToUnrepresentedClaimant(CaseDetails caseDetails, String orgName) {
        CaseData caseData = caseDetails.getCaseData();
        if (ObjectUtils.isEmpty(caseData.getClaimantType())
            || StringUtils.isBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            return;
        }
        String emailToSend = caseData.getClaimantType().getClaimantEmailAddress();
        String linkToCitUI = emailService.getCitizenCaseLink(caseDetails.getCaseId());
        sendEmailToUnrepresentedParty(caseDetails, emailToSend, orgName, linkToCitUI);
    }

    public void sendRepresentationRemovedEmailToRespondents(
        CaseDetails caseDetails,
        List<RespondentSumTypeItem> respondents,
        String orgName) {
        for (RespondentSumTypeItem respondent : respondents) {
            if (!RespondentUtils.isValidRespondent(respondent)
                    || StringUtils.isBlank(respondent.getValue().getRespondentEmail())) {
                continue;
            }
            String respondentEmailAddress = RespondentUtils.findRespondentEmailAddress(respondent);
            String linkToCitUI = emailService.getSyrCaseLink(caseDetails.getCaseId(), respondent.getId());
            sendEmailToUnrepresentedParty(caseDetails, respondentEmailAddress, orgName, linkToCitUI);
        }
    }

    private void sendEmailToUnrepresentedParty(
            CaseDetails caseDetails,
            String emailToSend,
            String orgName,
            String linkToCitUI
    ) {
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, orgName);
        personalisation.put(LINK_TO_CIT_UI, linkToCitUI);

        try {
            emailService.sendEmail(
                    nocCitizenNoLongerRepresentedTemplateId,
                    emailToSend,
                    personalisation);
        } catch (Exception e) {
            log.warn(
                    WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY,
                    caseDetails.getCaseId(),
                    e.getMessage());
        }
    }

    /**
     * Sends an email to the claimant or their legal representative as other party
     * when a respondent's representation is removed.
     *
     * @param caseDetails The case details containing case data and ID
     * @param partyName The name of the party whose representation was removed
     */
    public void sendEmailToOtherPartyClaimant(CaseDetails caseDetails, String partyName) {
        CaseData caseData = caseDetails.getCaseData();

        // check if claimant is represented
        RepresentedTypeC representativeClaimantType = caseData.getRepresentativeClaimantType();
        boolean isClaimantRepresented = representativeClaimantType != null;

        // get email address of claimant or claimant legal rep
        String emailToSend = ClaimantUtils.resolveClaimantEmailAddress(caseDetails.getCaseData());
        if (isNullOrEmpty(emailToSend)) {
            return;
        }

        // get email personalisation
        Map<String, String> personalisation = NocNotificationHelper.addCommonEmailValues(caseData);
        personalisation.put(PARTY_NAME, partyName);
        personalisation.put(LINK_TO_CIT_UI, isClaimantRepresented
            ? emailService.getExuiCaseLink(caseDetails.getCaseId())
            : emailService.getCitizenCaseLink(caseDetails.getCaseId()));

        // send email to claimant or claimant legal rep
        try {
            emailService.sendEmail(
                nocOtherPartyNotRepresentedTemplateId,
                emailToSend,
                personalisation
            );
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT,
                caseDetails.getCaseId(),
                e.getMessage()
            );
        }
    }

    /**
     * Sends notification emails to all valid respondents with a non-blank email address.
     *
     * <p>For each valid respondent, this method generates a link to the citizen UI,
     * builds the email personalisation values, and sends an email using the configured
     * NOC notification template. Respondents that are invalid or do not have an email
     * address are skipped.</p>
     *
     * <p>Assumptions:</p>
     * <ul>
     *     <li>{@code caseDetails} is not {@code null}</li>
     *     <li>{@code caseDetails.getCaseData()} is not {@code null}</li>
     *     <li>{@code caseDetails.getCaseId()} is available and valid</li>
     *     <li>{@code respondents} is not {@code null}</li>
     *     <li>{@code partyName} contains the name to be included in the email personalisation</li>
     *     <li>A respondent considered valid by {@link RespondentUtils#isValidRespondent(RespondentSumTypeItem)}
     *         has a non-null value object</li>
     * </ul>
     *
     * <p>If sending an email fails for an individual respondent, the failure is logged
     * and processing continues for the remaining respondents.</p>
     *
     * @param caseDetails the case details containing the case ID and case data used to build the email
     *                    link and common personalisation values
     * @param respondents the respondents to notify
     * @param partyName the party name to include in the email personalisation
     * @throws NullPointerException if {@code caseDetails}, its case data, or {@code respondents}
     *                              is {@code null}
     */
    public void sendEmailToOtherRespondents(CaseDetails caseDetails,
                                            List<RespondentSumTypeItem> respondents,
                                            String partyName) {
        List<RespondentSumTypeItem> otherRespondents = CallbacksCollectionUtils
                .findDifferentObjects(caseDetails.getCaseData().getRespondentCollection(), respondents);
        if (CollectionUtils.isEmpty(otherRespondents)) {
            return;
        }
        for (RespondentSumTypeItem respondent : otherRespondents) {
            if (!RespondentUtils.isValidRespondent(respondent)
                    || StringUtils.isBlank(respondent.getValue().getRespondentEmail())) {
                continue;
            }
            String linkToCitUI = emailService.getSyrCaseLink(caseDetails.getCaseId(), respondent.getId());
            try {
                Map<String, String> personalisation =
                        NocNotificationHelper.addCommonEmailValues(caseDetails.getCaseData());
                personalisation.put(PARTY_NAME, partyName);
                personalisation.put(LINK_TO_CIT_UI, linkToCitUI);

                emailService.sendEmail(
                        nocOtherPartyNotRepresentedTemplateId,
                        respondent.getValue().getRespondentEmail(),
                        personalisation
                );
            } catch (Exception e) {
                log.warn(WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT, caseDetails.getCaseId(),
                        e.getMessage());
            }
        }
    }
}
