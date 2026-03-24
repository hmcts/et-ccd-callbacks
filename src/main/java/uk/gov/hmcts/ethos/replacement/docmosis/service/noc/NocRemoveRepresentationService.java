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
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

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
public class NocRemoveRepresentationService {

    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
    private final EmailService emailService;
    private final CaseAccessService caseAccessService;
    private final EmailNotificationService emailNotificationService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @Value("${template.nocNotification.org-admin-not-representing}")
    private String nocOrgAdminNotRepresentingTemplateId;
    @Value("${template.nocNotification.noc-legal-rep-no-longer-assigned}")
    private String nocLegalRepNoLongerAssignedTemplateId;
    @Value("${template.nocNotification.noc-citizen-no-longer-represented}")
    private String nocCitizenNoLongerRepresentedTemplateId;
    @Value("${template.nocNotification.noc-other-party-not-represented}")
    private String nocOtherPartyNotRepresentedTemplateId;

    /**
     * Revoke claimant legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke claimant legal rep
     * @param userToken the user token of the requester
     */
    public void revokeClaimantLegalRep(CaseDetails caseDetails, String userToken) {
        // create a copy of existing claimant legal rep details
        RepresentedTypeC existingClaimantRep = caseDetails.getCaseData().getRepresentativeClaimantType();
        final String orgName = existingClaimantRep.getNameOfOrganisation();
        final String orgEmailAddress = nocNotificationService.findClaimantRepOrgSuperUserEmail(existingClaimantRep);
        final String repName = existingClaimantRep.getNameOfRepresentative();
        final String repEmailAddress = existingClaimantRep.getRepresentativeEmailAddress();

        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseDetails.getCaseData());

        // send email to organisation admin
        sendNocRequestEmailToOrgAdmin(caseDetails, orgEmailAddress, repName);
        // send email to removed legal rep
        sendNocRequestEmailToRemovedLegalRep(caseDetails, repEmailAddress);
        // send email to unrepresented party, i.e. claimant
        sendClaimantNocRequestEmailToUnrepresentedParty(caseDetails, orgName);
        // send email to other party, i.e. respondents
        sendClaimantNocRequestEmailToOtherParty(caseDetails);
    }

    private void sendNocRequestEmailToOrgAdmin(
        CaseDetails caseDetails,
        String emailToSend,
        String repName
    ) {
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

    private void sendNocRequestEmailToRemovedLegalRep(CaseDetails caseDetails, String emailToSend) {
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

    private void sendClaimantNocRequestEmailToUnrepresentedParty(CaseDetails caseDetails, String orgName) {
        CaseData caseData = caseDetails.getCaseData();
        if (ObjectUtils.isEmpty(caseData.getClaimantType())
            || StringUtils.isBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            return;
        }
        String emailToSend = caseData.getClaimantType().getClaimantEmailAddress();
        String linkToCitUI = emailService.getCitizenCaseLink(caseDetails.getCaseId());

        sendNocRequestEmailToUnrepresentedParty(caseDetails, emailToSend, orgName, linkToCitUI);
    }

    private void sendNocRequestEmailToUnrepresentedParty(
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

    private void sendClaimantNocRequestEmailToOtherParty(CaseDetails caseDetails) {
        List<CaseUserAssignment> caseUserAssignments =
            caseAccessService.getCaseUserAssignmentsById(caseDetails.getCaseId());
        if (caseUserAssignments == null || caseUserAssignments.isEmpty()) {
            log.warn(WARNING_FAILED_TO_GET_CASE_ASSIGNMENTS_BY_ID,
                caseDetails.getCaseId());
            return;
        }

        emailNotificationService.getRespondentsAndRepsEmailAddresses(caseDetails.getCaseData(), caseUserAssignments)
            .forEach((email, respondentId) ->
                sendClaimantNocRequestEmailToEachRespondent(caseDetails, email, respondentId));
    }

    private void sendClaimantNocRequestEmailToEachRespondent(
        CaseDetails caseDetails,
        String emailToSend,
        String respondentId
    ) {
        try {
            Map<String, String> personalisation =
                NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
            personalisation.put(PARTY_NAME, caseDetails.getCaseData().getClaimant());
            personalisation.put(LINK_TO_CIT_UI, StringUtils.isNotBlank(respondentId)
                ? emailService.getSyrCaseLink(caseDetails.getCaseId(), respondentId)
                : emailService.getExuiCaseLink(caseDetails.getCaseId()));

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

    /**
     * Revoke respondent legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke respondent legal rep
     * @param userToken the user token of the requester
     */
    public void revokeRespondentLegalRep(CaseDetails caseDetails, String userToken) {
        // create a copy of existing claimant legal rep details
        RepresentedTypeRItem respRepToRevoke =
            nocRespondentRepresentativeService.findRepresentativeByToken(userToken, caseDetails);
        String respRepIdToRevoke = respRepToRevoke.getId();
        final String orgName = orgNameToDo;
        final String orgEmailAddress = orgEmailAddressToDo;
        final String repName = repNameToDo;
        final String repEmailAddress = repEmailAddressToDo;

        // revoke respondent legal rep
        // TODO

        // send email to organisation admin
        sendNocRequestEmailToOrgAdmin(caseDetails, orgEmailAddress, repName);
        // send email to removed legal rep
        sendNocRequestEmailToRemovedLegalRep(caseDetails, repEmailAddress);
        // send email to unrepresented party, i.e. this respondent
        sendRespondentNocRequestEmailToUnrepresentedParty(caseDetails, orgName);
        // send email to claimant representative or claimant
        sendRespondentNocRequestEmailToClaimant(caseDetails);
        // send email to other respondents
        sendRespondentNocRequestEmailToOtherParty(caseDetails);
    }

    private void sendRespondentNocRequestEmailToUnrepresentedParty(CaseDetails caseDetails, String orgName) {
        String respondentEmailAddress = emailToDo;
        String linkToCitUI = linkToDo;
        sendNocRequestEmailToUnrepresentedParty(caseDetails, orgName, linkToCitUI, respondentEmailAddress);
    }

    private void sendRespondentNocRequestEmailToClaimant(CaseDetails caseDetails) {
        RepresentedTypeC representativeClaimantType = caseDetails.getCaseData().getRepresentativeClaimantType();
        boolean isClaimantRepresented = representativeClaimantType != null;
        String emailToSend = isClaimantRepresented
            ? representativeClaimantType.getRepresentativeEmailAddress()
            : caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress();
        String partyName = partyNameToDo;
        String caseLink = isClaimantRepresented
            ? emailService.getExuiCaseLink(caseDetails.getCaseId())
            : emailService.getCitizenCaseLink(caseDetails.getCaseId());
        try {
            emailService.sendEmail(
                nocOtherPartyNotRepresentedTemplateId,
                emailToSend,
                NocNotificationHelper.buildPersonalisationWithPartyName(caseDetails, partyName, caseLink)
            );
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT,
                caseDetails.getCaseId(),
                e.getMessage()
            );
        }
    }

    private void sendRespondentNocRequestEmailToOtherParty(CaseDetails caseDetails) {
        // TODO
        // partyName
        // linkToCitUI
        log.info("sendRespondentNocRequestEmailToOtherParty is not implemented yet for caseId {}",
            caseDetails.getCaseId());
    }
}
