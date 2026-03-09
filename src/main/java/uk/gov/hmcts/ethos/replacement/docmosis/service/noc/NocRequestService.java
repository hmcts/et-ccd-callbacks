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
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_ORG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CIT_UI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPersonalisationWithPartyName;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRequestService {

    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
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
     * Revoke claimant legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke claimant legal rep
     * @param userToken the user token of the requester
     */
    public void revokeClaimantLegalRep(CaseDetails caseDetails, String userToken) {
        // create a copy of existing claimant legal rep details
        RepresentedTypeC repCopy = getRepTrueCopy(caseDetails);

        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseDetails.getCaseData());

        // send email to organisation admin
        sendClaimantNocRequestEmailToOrgAdmin(caseDetails, repCopy);
        // send email to removed legal rep
        sendClaimantNocRequestEmailToRemovedLegalRep(caseDetails, repCopy);
        // send email to unrepresented party, i.e. claimant
        sendClaimantNocRequestEmailToUnrepresentedParty(caseDetails, repCopy);
        // send email to other party, i.e. respondents
        sendClaimantNocRequestEmailToOtherParty(caseDetails);
    }

    private static RepresentedTypeC getRepTrueCopy(CaseDetails caseDetails) {
        RepresentedTypeC existingClaimantRep = caseDetails.getCaseData().getRepresentativeClaimantType();
        return RepresentedTypeC.builder()
            .representativeId(existingClaimantRep.getRepresentativeId())
            .nameOfRepresentative(existingClaimantRep.getNameOfRepresentative())
            .nameOfOrganisation(existingClaimantRep.getNameOfOrganisation())
            .representativeEmailAddress(existingClaimantRep.getRepresentativeEmailAddress())
            .organisationId(existingClaimantRep.getOrganisationId())
            .myHmctsOrganisation(existingClaimantRep.getMyHmctsOrganisation() == null
                ? null
                : Organisation.builder()
                    .organisationID(existingClaimantRep.getMyHmctsOrganisation().getOrganisationID())
                    .organisationName(existingClaimantRep.getMyHmctsOrganisation().getOrganisationName())
                    .build())
            .build();
    }

    private void sendClaimantNocRequestEmailToOrgAdmin(CaseDetails caseDetails, RepresentedTypeC representedTypeC) {
        String emailToSend =
            nocNotificationService.findClaimantRepOrgSuperUserEmail(representedTypeC);
        if (isNullOrEmpty(emailToSend)) {
            return;
        }

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, representedTypeC.getNameOfRepresentative());
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

    private void sendClaimantNocRequestEmailToRemovedLegalRep(
        CaseDetails caseDetails,
        RepresentedTypeC representedTypeC
    ) {
        String emailToSend = representedTypeC.getRepresentativeEmailAddress();
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        try {
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

    private void sendClaimantNocRequestEmailToUnrepresentedParty(
        CaseDetails caseDetails,
        RepresentedTypeC representedTypeC
    ) {
        CaseData caseData = caseDetails.getCaseData();
        if (ObjectUtils.isEmpty(caseData.getClaimantType())
            || StringUtils.isBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            return;
        }

        String emailToSend = caseData.getClaimantType().getClaimantEmailAddress();
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, representedTypeC.getNameOfOrganisation());
        personalisation.put(LINK_TO_CIT_UI, emailService.getCitizenCaseLink(caseDetails.getCaseId()));
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
            log.warn("In NocNotificationService : No case user assignments found for caseId {}",
                caseDetails.getCaseId());
            return;
        }

        emailNotificationService.getRespondentsAndRepsEmailAddresses(caseDetails.getCaseData(), caseUserAssignments)
            .forEach((email, respondentId) ->
                sendClaimantNocRequestEmailToEachRespondent(caseDetails, email, respondentId));
    }

    private void sendClaimantNocRequestEmailToEachRespondent(
        CaseDetails caseDetails,
        String email,
        String respondentId
    ) {
        String partyName = caseDetails.getCaseData().getClaimant();
        String caseLink = StringUtils.isNotBlank(respondentId)
            ? emailService.getSyrCaseLink(caseDetails.getCaseId(), respondentId)
            : emailService.getExuiCaseLink(caseDetails.getCaseId());
        try {
            emailService.sendEmail(
                nocOtherPartyNotRepresentedTemplateId,
                email,
                buildPersonalisationWithPartyName(caseDetails, partyName, caseLink)
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
        // revoke respondent legal rep
        // TODO

        // send email to organisation admin
        sendRespondentNocRequestEmailToOrgAdmin(caseDetails);
        // send email to removed legal rep
        sendRespondentNocRequestEmailToRemovedLegalRep(caseDetails);
        // send email to unrepresented party, i.e. this respondent
        sendRespondentNocRequestEmailToUnrepresentedParty(caseDetails);
        // send email to claimant representative or claimant
        sendRespondentNocRequestEmailToClaimant(caseDetails);
        // send email to other respondents
        sendRespondentNocRequestEmailToOtherParty(caseDetails);
    }

    private void sendRespondentNocRequestEmailToOrgAdmin(CaseDetails caseDetails) {
        // TODO
        log.info("sendRespondentNocRequestEmailToOrgAdmin is not implemented yet for caseId {}",
            caseDetails.getCaseId());
    }

    private void sendRespondentNocRequestEmailToRemovedLegalRep(CaseDetails caseDetails) {
        // TODO
        log.info("sendRespondentNocRequestEmailToRemovedLegalRep is not implemented yet for caseId {}",
            caseDetails.getCaseId());
    }

    private void sendRespondentNocRequestEmailToUnrepresentedParty(CaseDetails caseDetails) {
        // TODO
        log.info("sendRespondentNocRequestEmailToUnrepresentedParty is not implemented yet for caseId {}",
            caseDetails.getCaseId());
    }

    private void sendRespondentNocRequestEmailToClaimant(CaseDetails caseDetails) {
        RepresentedTypeC representativeClaimantType = caseDetails.getCaseData().getRepresentativeClaimantType();
        boolean isClaimantRepresented = representativeClaimantType != null;
        String emailToSend = isClaimantRepresented
            ? representativeClaimantType.getRepresentativeEmailAddress()
            : caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress();
        String partyName = "partyName"; // TODO
        String caseLink = isClaimantRepresented
            ? emailService.getExuiCaseLink(caseDetails.getCaseId())
            : emailService.getCitizenCaseLink(caseDetails.getCaseId());
        try {
            emailService.sendEmail(
                nocOtherPartyNotRepresentedTemplateId,
                emailToSend,
                buildPersonalisationWithPartyName(caseDetails, partyName, caseLink)
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
        log.info("sendRespondentNocRequestEmailToOtherParty is not implemented yet for caseId {}",
            caseDetails.getCaseId());
    }
}
