package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocCcdService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRequestService {

    private final EmailService emailService;
    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
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

    private static final String LEGAL_REP_NAME = "legalRepName";
    private static final String LEGAL_REP_ORG = "legalRepOrg";
    private static final String LINK_TO_CIT_UI = "linkToCitUI";
    private static final String PARTY_NAME = "party_name";

    public void revokeClaimantLegalRep(String userToken, CaseDetails caseDetails) {
        // create a copy of existing claimant legal rep details
        RepresentedTypeC repCopy = getRepTrueCopy(caseDetails);

        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseDetails.getCaseData());

        // send email to organisation admin
        sendEmailToOrgAdmin(caseDetails, repCopy);
        // send email to removed legal rep
        sendEmailToRemovedLegalRep(caseDetails, repCopy);
        // send email to unrepresented party, i.e. claimant
        sendEmailToUnrepresentedParty(caseDetails, repCopy);
        // send email to other party, i.e. respondents
        sendEmailToOtherParty(caseDetails);
    }

    private static RepresentedTypeC getRepTrueCopy(CaseDetails caseDetails) {
        RepresentedTypeC existingClaimantRep = caseDetails.getCaseData().getRepresentativeClaimantType();
        return RepresentedTypeC.builder()
            .representativeId(existingClaimantRep.getRepresentativeId())
            .nameOfRepresentative(existingClaimantRep.getNameOfRepresentative())
            .nameOfOrganisation(existingClaimantRep.getNameOfOrganisation())
            .representativeEmailAddress(existingClaimantRep.getRepresentativeEmailAddress())
            .organisationId(existingClaimantRep.getOrganisationId())
            .myHmctsOrganisation(Organisation.builder()
                .organisationID(existingClaimantRep.getMyHmctsOrganisation().getOrganisationID())
                .organisationName(existingClaimantRep.getMyHmctsOrganisation().getOrganisationName())
                .build())
            .build();
    }

    private void sendEmailToOrgAdmin(CaseDetails caseDetails, RepresentedTypeC repCopy) {
        RetrieveOrgByIdResponse resBody =
            nocNotificationService.getOrganisationResponse(repCopy.getOrganisationId(), false);
        if (ObjectUtils.isEmpty(resBody)) {
            return;
        }

        String organisationEmail = resBody.getSuperUser().getEmail();

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, repCopy.getNameOfRepresentative());

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

    private void sendEmailToRemovedLegalRep(CaseDetails caseDetails, RepresentedTypeC repCopy) {
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        try {
            emailService.sendEmail(
                nocLegalRepNoLongerAssignedTemplateId,
                repCopy.getRepresentativeEmailAddress(),
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private void sendEmailToUnrepresentedParty(CaseDetails caseDetails, RepresentedTypeC repCopy) {
        String claimantEmailAddress = ClaimantUtils.getClaimantEmailAddress(caseDetails.getCaseData());
        if  (isNullOrEmpty(claimantEmailAddress)) {
            return;
        }

        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, repCopy.getNameOfOrganisation());
        personalisation.put(LINK_TO_CIT_UI, emailService.getCitizenCaseLink(caseDetails.getCaseId()));

        try {
            emailService.sendEmail(
                nocCitizenNoLongerRepresentedTemplateId,
                claimantEmailAddress,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private void sendEmailToOtherParty(CaseDetails caseDetails) {
        List<CaseUserAssignment> caseUserAssignments =
            caseAccessService.getCaseUserAssignmentsById(caseDetails.getCaseId());
        emailNotificationService
            .getRespondentsAndRepsEmailAddresses(caseDetails.getCaseData(), caseUserAssignments)
            .forEach((email, respondentId) -> {
                sendRespondentEmail(caseDetails, email, respondentId);
            });
    }

    private void sendRespondentEmail(CaseDetails caseDetails, String email, String respondentId) {
        Map<String, String> personalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseDetails.getCaseData());
        personalisation.put(PARTY_NAME, CLAIMANT_TITLE);
        String caseLink = StringUtils.isNotBlank(respondentId)
            ? emailService.getSyrCaseLink(caseDetails.getCaseId(), respondentId)
            : emailService.getExuiCaseLink(caseDetails.getCaseId());
        personalisation.put(LINK_TO_CIT_UI, caseLink);
        emailService.sendEmail(
            nocOtherPartyNotRepresentedTemplateId,
            email,
            personalisation);
    }
}
