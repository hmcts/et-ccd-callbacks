package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.noc.NocRespondentMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_GET_CASE_ASSIGNMENTS_BY_ID;
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
    private final CaseAccessService caseAccessService;
    private final EmailNotificationService emailNotificationService;

    @Value("${template.nocNotification.org-admin-not-representing}")
    private String nocOrgAdminNotRepresentingTemplateId;
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
    public void sendEmailToOrgAdmin(CaseDetails caseDetails, String emailToSend, String repName) {
        if (isNullOrEmpty(emailToSend)) {
            return;
        }

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, repName);

        try {
            emailService.sendEmail(
                nocOrgAdminNotRepresentingTemplateId,
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
        try {
            Map<String, String> personalisation =
                NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
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

    /**
     * Sends emails to unrepresented respondents when their legal representatives are removed.
     *
     * @param caseDetails The case details containing case data and ID
     * @param repListToRevoke List of representatives whose representation is being revoked
     * @param orgName The name of the removed organisation
     */
    public void sendEmailToUnrepresentedRespondent(
        CaseDetails caseDetails,
        List<RepresentedTypeRItem> repListToRevoke,
        String orgName
    ) {
        for (RepresentedTypeRItem representative : repListToRevoke) {
            // find respondent for this RepresentedTypeRItem
            RespondentSumTypeItem respondent = RespondentRepresentativeUtils.findRespondentByRepresentative(
                caseDetails.getCaseData(), representative);
            if (respondent == null || !RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }

            // personalize email address and link
            String respondentEmailAddress = respondent.getValue().getRespondentEmail();
            String linkToCitUI = emailService.getSyrCaseLink(caseDetails.getCaseId(), respondent.getId());

            // send email to unrepresented respondent
            sendEmailToUnrepresentedParty(caseDetails, respondentEmailAddress, orgName, linkToCitUI);
        }
    }

    private void sendEmailToUnrepresentedParty(
        CaseDetails caseDetails,
        String emailToSend,
        String orgName,
        String linkToCitUI
    ) {
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
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
        String emailToSend = isClaimantRepresented
            ? representativeClaimantType.getRepresentativeEmailAddress()
            : caseData.getClaimantType().getClaimantEmailAddress();
        if (isNullOrEmpty(emailToSend)) {
            return;
        }

        // get email personalisation
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseData);
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
     * Sends emails to other party respondents (both represented and unrepresented)
     * when a respondent's representation is removed.
     *
     * @param caseDetails The case details containing case data and ID
     * @param respondentIdInRevokeList List of respondent IDs whose representation is being revoked
     * @param partyName The name of the party whose representation was removed
     */
    public void sendEmailToOtherPartyRespondent(
        CaseDetails caseDetails,
        List<String> respondentIdInRevokeList,
        String partyName
    ) {
        List<CaseUserAssignment> caseUserAssignments =
            caseAccessService.getCaseUserAssignmentsById(caseDetails.getCaseId());
        if (caseUserAssignments == null || caseUserAssignments.isEmpty()) {
            log.warn(WARNING_FAILED_TO_GET_CASE_ASSIGNMENTS_BY_ID,
                caseDetails.getCaseId());
            return;
        }

        // send email to other respondent legal rep
        emailNotificationService.getRespondentSolicitorEmails(caseUserAssignments)
            .forEach(email -> {
                String linkToCitUI = emailService.getExuiCaseLink(caseDetails.getCaseId());
                sendEmailToEachRespondent(caseDetails, email, partyName, linkToCitUI);
            });

        // send email to other respondent with no rep and not revoke
        NocRespondentMapper.getRespondentCollectionToEmail(caseDetails.getCaseData(), respondentIdInRevokeList)
            .forEach(r -> {
                String email = NocRespondentMapper.getRespondentEmail(r.getValue());
                String linkToCitUI = emailService.getSyrCaseLink(caseDetails.getCaseId(), r.getId());
                sendEmailToEachRespondent(caseDetails, email, partyName, linkToCitUI);
            });
    }

    private void sendEmailToEachRespondent(
        CaseDetails caseDetails,
        String emailToSend,
        String partyName,
        String linkToCitUI
    ) {
        try {
            Map<String, String> personalisation =
                NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
            personalisation.put(PARTY_NAME, partyName);
            personalisation.put(LINK_TO_CIT_UI, linkToCitUI);

            emailService.sendEmail(
                nocOtherPartyNotRepresentedTemplateId,
                emailToSend,
                personalisation
            );
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT,
                caseDetails.getCaseId(),
                e.getMessage()
            );
        }
    }
}
