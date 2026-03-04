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

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
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
        String organisationEmail = getOrganisationEmailWithID(representedTypeC);
        if (organisationEmail == null) {
            return;
        }

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, representedTypeC.getNameOfRepresentative());

        try {
            emailService.sendEmail(
                nocOrgAdminNotRepresentingTemplateId,
                organisationEmail,
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
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        try {
            emailService.sendEmail(
                nocLegalRepNoLongerAssignedTemplateId,
                representedTypeC.getRepresentativeEmailAddress(),
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

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, representedTypeC.getNameOfOrganisation());
        personalisation.put(LINK_TO_CIT_UI, emailService.getCitizenCaseLink(caseDetails.getCaseId()));

        try {
            emailService.sendEmail(
                nocCitizenNoLongerRepresentedTemplateId,
                caseData.getClaimantType().getClaimantEmailAddress(),
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private void sendClaimantNocRequestEmailToOtherParty(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        List<CaseUserAssignment> caseUserAssignments =
            caseAccessService.getCaseUserAssignmentsById(caseDetails.getCaseId());
        if (caseUserAssignments == null || caseUserAssignments.isEmpty()) {
            log.warn("In NocNotificationService : No case user assignments found for caseId {}",
                caseDetails.getCaseId());
            return;
        }

        emailNotificationService.getRespondentsAndRepsEmailAddresses(caseData, caseUserAssignments)
            .forEach((email, respondentId) -> {
                String partyName = caseData.getClaimant();
                String caseLink = StringUtils.isNotBlank(respondentId)
                    ? emailService.getSyrCaseLink(caseDetails.getCaseId(), respondentId)
                    : emailService.getExuiCaseLink(caseDetails.getCaseId());
                emailService.sendEmail(
                    nocOtherPartyNotRepresentedTemplateId,
                    email,
                    buildPersonalisationWithPartyName(caseDetails, partyName, caseLink)
                );
            });
    }
}
