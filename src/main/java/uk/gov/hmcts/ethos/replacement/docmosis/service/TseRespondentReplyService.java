package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;

import java.util.Map;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Service
@RequiredArgsConstructor
public class TseRespondentReplyService {
    private final TornadoService tornadoService;
    private final EmailService emailService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    public void sendAcknowledgementAndClaimantEmail(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        if (YES.equals(caseData.getTseResponseCopyToOtherParty())) {
            try {
                byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, "", "TSE Reply.pdf");
                String claimantEmail = caseData.getClaimantType().getClaimantEmailAddress();
                Map<String, Object> personalisation = TseHelper.getPersonalisationForResponse(caseDetails,
                        bytes, notificationProperties.getCitizenUrl());
                emailService.sendEmail(notificationProperties.getTseRespondentResponseTemplateId(),
                        claimantEmail, personalisation);
            } catch (Exception e) {
                throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
            }
        }

        String legalRepEmail = userService.getUserDetails(userToken).getEmail();
        emailService.sendEmail(
            YES.equals(caseData.getTseResponseCopyToOtherParty())
                ? notificationProperties.getAcknowledgementRule92YesEmailTemplateId()
                : notificationProperties.getAcknowledgementRule92NoEmailTemplateId(),
            legalRepEmail,
            TseHelper.getPersonalisationForAcknowledgement(caseDetails, notificationProperties.getExuiUrl()));
    }
}
