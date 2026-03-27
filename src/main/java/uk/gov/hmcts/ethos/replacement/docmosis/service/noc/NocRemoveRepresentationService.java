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
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
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

    private static final String ORG_NAME = "orgName";
    private static final String ORG_EMAIL_ADDRESS = "orgEmailAddress";
    private static final String REP_NAME = "repName";
    private static final String REP_EMAIL_ADDRESS = "repEmailAddress";

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
        // get existing rep and organisation details for sending emails
        Map<String, String> claimantRepDetails = getClaimantRepDetails(caseDetails);

        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseDetails.getCaseData());

        // send email to organisation admin
        sendNocRequestEmailToOrgAdmin(
            caseDetails, claimantRepDetails.get(ORG_EMAIL_ADDRESS), claimantRepDetails.get(REP_NAME));
        // send email to removed legal rep
        sendNocRequestEmailToRemovedLegalRep(caseDetails, claimantRepDetails.get(REP_EMAIL_ADDRESS));
        // send email to unrepresented party, i.e. claimant
        sendClaimantNocRequestEmailToUnrepresentedParty(caseDetails, claimantRepDetails.get(ORG_NAME));
        // send email to other party, i.e. respondents
        sendClaimantNocRequestEmailToOtherParty(caseDetails);
    }

    private Map<String, String> getClaimantRepDetails(CaseDetails caseDetails) {
        RepresentedTypeC existingClaimantRep = caseDetails.getCaseData().getRepresentativeClaimantType();
        return Map.of(
            ORG_NAME, existingClaimantRep.getNameOfOrganisation(),
            ORG_EMAIL_ADDRESS, nocNotificationService.findClaimantRepOrgSuperUserEmail(existingClaimantRep),
            REP_NAME, existingClaimantRep.getNameOfRepresentative(),
            REP_EMAIL_ADDRESS, existingClaimantRep.getRepresentativeEmailAddress()
        );
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
     * Mid-event to check if more than 1 representative from the organisation.
     * @param caseDetails the case details of the case to revoke respondent legal rep
     * @param userToken the user token of the requester
     * @return return Yes if more than 1 representative from the organisation, else return No
     */
    public String isMoreThanOneRespondent(CaseDetails caseDetails, String userToken) {
        List<RepresentedTypeRItem> currentRepList =
            nocRespondentRepresentativeService.findRepresentativesByToken(userToken, caseDetails);
        if (CollectionUtils.isEmpty(currentRepList)) {
            return NO;
        }

        String orgId = getOrganisationID(currentRepList);
        if (isNullOrEmpty(orgId)) {
            return NO;
        }

        List<RepresentedTypeRItem> orgRepList =
            RespondentRepresentativeUtils.findRepresentativesByOrganisationId(
                caseDetails.getCaseData(),
                orgId
            );
        return orgRepList.size() > currentRepList.size()
            ? YES
            : NO;
    }

    private String getOrganisationID(List<RepresentedTypeRItem> currentRepList) {
        if (CollectionUtils.isEmpty(currentRepList)) {
            return null;
        }
        RepresentedTypeR currentRep = currentRepList.getFirst().getValue();
        if (currentRep.getRespondentOrganisation() == null
            || currentRep.getRespondentOrganisation().getOrganisationID() == null) {
            return null;
        }
        return currentRep.getRespondentOrganisation().getOrganisationID();
    }

    /**
     * Revoke respondent legal rep and send email notifications to related parties.
     * @param caseDetails the case details of the case to revoke respondent legal rep
     * @param userToken the user token of the requester
     */
    public void revokeRespondentLegalRep(CaseDetails caseDetails, String userToken) {
        // create a copy of existing respondent legal rep details
        List<RepresentedTypeRItem> currentRepList =
            nocRespondentRepresentativeService.findRepresentativesByToken(userToken, caseDetails);
        // TODO: if empty, return
        RepresentedTypeR currentRep = currentRepList.getFirst().getValue();
        String orgId = currentRep.getRespondentOrganisation().getOrganisationID(); // TODO

        List<RepresentedTypeRItem> orgRepList =
            RespondentRepresentativeUtils.findRepresentativesByOrganisationId(
                caseDetails.getCaseData(),
                orgId
            );

        // get existing rep and organisation details for sending emails
        Map<String, String> respondentRepDetails = getRespondentRepDetails(orgRepList.getFirst().getValue());

        // revoke respondent legal rep
        nocRespondentRepresentativeService.revokeAndRemoveRepresentativesByOrganisation(
            caseDetails,
            orgId
        );

        // send email to organisation admin
        sendNocRequestEmailToOrgAdmin(
            caseDetails, respondentRepDetails.get(ORG_EMAIL_ADDRESS), respondentRepDetails.get(REP_NAME));
        // send email to removed legal rep
        sendNocRequestEmailToRemovedLegalRep(caseDetails, respondentRepDetails.get(REP_EMAIL_ADDRESS));
        // send email to unrepresented party, i.e. this respondent
        sendRespondentNocRequestEmailToUnrepresentedParty(caseDetails, respondentRepDetails.get(ORG_NAME));
        // send email to claimant
        sendRespondentNocRequestEmailToClaimant(caseDetails);
        // send email to other respondent
        // TODO
    }

    private Map<String, String> getRespondentRepDetails(RepresentedTypeR representedTypeR) {
        return Map.of(
            ORG_NAME, representedTypeR.getNameOfOrganisation(),
            ORG_EMAIL_ADDRESS, "orgEmailAddress", // TODO
            REP_NAME, representedTypeR.getNameOfRepresentative(),
            REP_EMAIL_ADDRESS, representedTypeR.getRepresentativeEmailAddress()
        );
    }

    private void sendRespondentNocRequestEmailToUnrepresentedParty(CaseDetails caseDetails, String orgName) {
        String respondentEmailAddress = "emailToDo"; // TODO
        String linkToCitUI = "linkToDo"; // TODO
        sendNocRequestEmailToUnrepresentedParty(caseDetails, orgName, linkToCitUI, respondentEmailAddress);
    }

    private void sendRespondentNocRequestEmailToClaimant(CaseDetails caseDetails) {
        RepresentedTypeC representativeClaimantType = caseDetails.getCaseData().getRepresentativeClaimantType();
        boolean isClaimantRepresented = representativeClaimantType != null;
        String emailToSend = isClaimantRepresented
            ? representativeClaimantType.getRepresentativeEmailAddress()
            : caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress();
        String partyName = "partyNameToDo"; // TODO
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
}
