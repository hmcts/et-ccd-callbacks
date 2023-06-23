package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.*;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplication;

@Service
@RequiredArgsConstructor
public class TseRespondentReplyService {
    private final TornadoService tornadoService;
    private final EmailService emailService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;
    private final TseService tseService;
    private final TribunalOfficesService tribunalOfficesService;
    private final RespondentTellSomethingElseService respondentTseService;
    @Value("${tse.respondent.respond.notify.claimant.template.id}")
    private String tseRespondentResponseTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92no.template.id}")
    private String acknowledgementRule92NoEmailTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92yes.template.id}")
    private String acknowledgementRule92YesEmailTemplateId;
    @Value("${tse.respondent.reply-to-tribunal.to-tribunbal}")
    private String replyToTribunalEmailToTribunalTemplateId;
    @Value("${tse.respondent.reply-to-tribunal.to-claimant}")
    private String replyToTribunalEmailToClaimantTemplateId;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    public void sendRespondingToApplicationEmails(CaseDetails caseDetails, String userToken) {
        sendEmailToClaimantForRespondingToApp(caseDetails);
        sendAcknowledgementEmailToLR(caseDetails, userToken);
        respondentTseService.sendAdminEmail(caseDetails);
    }

    private void sendEmailToClaimantForRespondingToApp(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        if (!YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            return;
        }

        try {
            byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "", "TSE Reply.pdf");
            String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
            Map<String, Object> personalisation = TseHelper.getPersonalisationForResponse(caseDetails,
                    bytes, notificationProperties.getCitizenUrl());
            emailService.sendEmail(tseRespondentResponseTemplateId,
                    claimantEmail, personalisation);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    private void sendAcknowledgementEmailToLR(CaseDetails caseDetails, String userToken) {
        String legalRepEmail = userService.getUserDetails(userToken).getEmail();
        emailService.sendEmail(
            YES.equals(caseDetails.getCaseData().getTseResponseCopyToOtherParty())
                ? acknowledgementRule92YesEmailTemplateId
                : acknowledgementRule92NoEmailTemplateId,
            legalRepEmail,
            TseHelper.getPersonalisationForAcknowledgement(caseDetails, notificationProperties.getExuiUrl()));
    }

    public void sendRespondingToTribunalEmails(CaseDetails caseDetails) {
        sendEmailToTribunal(caseDetails.getCaseData());
        sendEmailToClaimantForRespondingToTrib(caseDetails);
    }

    private void sendEmailToTribunal(CaseData caseData) {
        String email = respondentTseService.getTribunalEmail(caseData);

        if (isNullOrEmpty(email)) {
            return;
        }
        
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);
        Map<String, String> personalisation = Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                APPLICATION_TYPE, selectedApplication.getType());
        emailService.sendEmail(replyToTribunalEmailToTribunalTemplateId, email, personalisation);
    }

    private void sendEmailToClaimantForRespondingToTrib(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        if (!YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            return;
        }

        String claimantEmail = getClaimantEmailAddress(caseData);

        if (isNullOrEmpty(claimantEmail)) {
            return;
        }

        Map<String, String> personalisation = Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                LINK_TO_CITIZEN_HUB, notificationProperties.getCitizenLinkWithCaseId(caseDetails.getCaseId()));
        emailService.sendEmail(replyToTribunalEmailToClaimantTemplateId, claimantEmail, personalisation);
    }

    /**
     * Initial Application and Respond details table for when respondent responds to Tribunal's request/order.
     * @param caseData contains all the case data
     * @param authToken the caller's bearer token used to verify the caller
     */
    public void initialResReplyToTribunalTableMarkUp(CaseData caseData, String authToken) {
        GenericTseApplicationType application = getSelectedApplication(caseData);

        String applicationTable = tseService.formatApplicationDetails(application, authToken, true);
        String responses = tseService.formatApplicationResponses(application, authToken, true);

        caseData.setTseResponseTable(applicationTable + "\r\n" + responses);
    }

    /**
     * Check if the Tribunal has requested for a response from Respondent.
     *
     * @param caseData contains all the case data
     * @return a boolean value of whether the Respondent is responding to a Tribunal order/request
     */
    public boolean isRespondingToTribunal(CaseData caseData) {
        GenericTseApplicationType applicationType = getSelectedApplication(caseData);
        if (applicationType == null) {
            throw new NotFoundException("No selected application type item found.");
        }

        return YES.equals(applicationType.getRespondentResponseRequired());
    }
    private static String getClaimantEmailAddress(CaseData caseData) {
        return caseData.getClaimantType().getClaimantEmailAddress();
    }
}
