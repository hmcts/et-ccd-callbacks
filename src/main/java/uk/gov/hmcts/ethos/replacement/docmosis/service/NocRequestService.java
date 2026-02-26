package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY;


@Slf4j
@Service
@RequiredArgsConstructor
public class NocRequestService {

    private final EmailService emailService;

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
    }

    public void sendEmailNotification(String userToken, CaseDetails caseDetails) {
        sendEmailToOrgAdmin(caseDetails);
        sendEmailToRemovedLegalRep(caseDetails);
        sendEmailToUnrepresentedParty(caseDetails);
        sendEmailToOtherParty(caseDetails);
    }

    private void sendEmailToOrgAdmin(CaseDetails caseDetails) {
        RetrieveOrgByIdResponse resBody = getOrganisationResponse(orgId, true);
        String organisationEmail = resBody.getSuperUser().getEmail();

        Map<String, String> personalisation =
            NocNotificationHelper.buildCommonPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_NAME, "legalRepName");

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

    private void sendEmailToRemovedLegalRep(CaseDetails caseDetails) {
        Map<String, String> personalisation =
            NocNotificationHelper.buildCommonPersonalisation(caseDetails.getCaseData());
        try {
            emailService.sendEmail(
                nocLegalRepNoLongerAssignedTemplateId,
                organisationEmail,
                personalisation);
        } catch (Exception e) {
            log.warn(
                WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE,
                caseDetails.getCaseId(),
                e.getMessage());
        }
    }

    private void sendEmailToUnrepresentedParty(CaseDetails caseDetails) {
        String claimantEmailAddress = ClaimantUtils.getClaimantEmailAddress(caseDetails.getCaseData());
        if  (isNullOrEmpty(claimantEmailAddress)) {
            return;
        }

        Map<String, String> personalisation =
            NocNotificationHelper.buildCommonPersonalisation(caseDetails.getCaseData());
        personalisation.put(LEGAL_REP_ORG, "legalRepOrg");
        personalisation.put(LINK_TO_CIT_UI, "linkToCitUI");

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
        Map<String, String> personalisation =
            NocNotificationHelper.buildCommonPersonalisation(caseDetails.getCaseData());
        personalisation.put(PARTY_NAME, "party_name");
        personalisation.put(LINK_TO_CIT_UI, "linkToCitUI");
    }
}
