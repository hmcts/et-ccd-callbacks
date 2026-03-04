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
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_REMOVED_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_TO_UNREPRESENTED_PARTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LEGAL_REP_ORG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CIT_UI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocNotificationHelper.buildPersonalisationWithPartyName;

@RequiredArgsConstructor
@Service("NocRequestNotificationService")
@Slf4j
public class NocRequestNotificationService {

    private final EmailService emailService;
    private final EmailNotificationService emailNotificationService;
    private final CaseAccessService caseAccessService;

    @Value("${template.nocNotification.org-admin-not-representing}")
    private String nocOrgAdminNotRepresentingTemplateId;
    @Value("${template.nocNotification.noc-legal-rep-no-longer-assigned}")
    private String nocLegalRepNoLongerAssignedTemplateId;
    @Value("${template.nocNotification.noc-citizen-no-longer-represented}")
    private String nocCitizenNoLongerRepresentedTemplateId;
    @Value("${template.nocNotification.noc-other-party-not-represented}")
    private String nocOtherPartyNotRepresentedTemplateId;

    public void sendClaimantNocRequestEmailToOrgAdmin(CaseDetails caseDetails, RepresentedTypeC representedTypeC) {
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

    public void sendClaimantNocRequestEmailToRemovedLegalRep(
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

    public void sendClaimantNocRequestEmailToUnrepresentedParty(
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

    public void sendClaimantNocRequestEmailToOtherParty(CaseDetails caseDetails) {
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
