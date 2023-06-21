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

import java.util.Map;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getSelectedApplication;

@Service
@RequiredArgsConstructor
public class TseRespondentReplyService {
    private final TornadoService tornadoService;
    private final EmailService emailService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;
    private final TseService tseService;
    @Value("${tse.respondent.respond.notify.claimant.template.id}")
    private String tseRespondentResponseTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92no.template.id}")
    private String acknowledgementRule92NoEmailTemplateId;
    @Value("${tse.respondent.respond.acknowledgement.rule92yes.template.id}")
    private String acknowledgementRule92YesEmailTemplateId;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    public void sendAcknowledgementAndClaimantEmail(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        if (YES.equals(caseData.getTseResponseCopyToOtherParty())) {
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

        String legalRepEmail = userService.getUserDetails(userToken).getEmail();
        emailService.sendEmail(
            YES.equals(caseData.getTseResponseCopyToOtherParty())
                ? acknowledgementRule92YesEmailTemplateId
                : acknowledgementRule92NoEmailTemplateId,
            legalRepEmail,
            TseHelper.getPersonalisationForAcknowledgement(caseDetails, notificationProperties.getExuiUrl()));
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

        return NO.equals(applicationType.getRespondentResponseRequired());
    }

}
