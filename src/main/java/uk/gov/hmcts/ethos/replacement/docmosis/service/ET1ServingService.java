package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ET1ServingService {
    public static final String SERVING_DOCUMENT_OTHER_TYPE = "Another type of document";
    private static final String SERVING_RECIPIENT_CLAIMANT = "Claimant";
    private static final String SERVING_RECIPIENT_RESPONDENT = "Respondent";
    private static final String ACAS_MAILTO_LINK = "mailto:ET3@acas.org.uk?subject={0}&body=Parties%20in%20claim" +
            "%3A%20{1}%20vs%20{2}%0D%0ACase%20reference%20number%3A%20{3}%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0AThe%" +
            "20tribunal%20has%20completed%20ET1%20serving%20to%20the%20respondent.%0D%0A%0D%0AThe%20documents%20we" +
            "%20sent%20are%20attached%20to%20this%20email.%0D%0A%0D%0A";
    private static final String AFTER_SUBMITTED_HTML = "<h1>Documents submitted</h1>We have notified " +
            "the following parties:<br/>%s%s";

    private final NotificationService notificationService;

    public String generateOtherTypeDocumentName(List<DocumentTypeItem> docList) {
        StringBuilder sb = new StringBuilder();
        for (DocumentTypeItem doc : docList) {
            if (doc.getValue().getTypeOfDocument().equals(SERVING_DOCUMENT_OTHER_TYPE)) {
                sb.append("**<big>");
                sb.append(doc.getValue().getUploadedDocument().getDocumentFilename());
                sb.append("</big>**<br/>");
                sb.append("<small>");
                sb.append(doc.getValue().getShortDescription());
                sb.append("</small><br/>");
            }
        }
        return sb.toString();
    }

    public String generateClaimantAndRespondentAddress(CaseData caseData) {
        List<String> recipients = caseData.getServingDocumentRecipient();
        StringBuilder addressStr = new StringBuilder();
        if (recipients.contains(SERVING_RECIPIENT_CLAIMANT)) {
            ClaimantIndType claimant = caseData.getClaimantIndType();
            addressStr.append("**<big>Claimant</big>**")
                    .append("<br/>" + claimant.getClaimantFirstNames() + " " + claimant.getClaimantLastName())
                    .append(caseData.getClaimantType().getClaimantAddressUK().toAddressHtml());
        }
        if (recipients.contains(SERVING_RECIPIENT_RESPONDENT)) {
            int index = 1;
            for (RespondentSumTypeItem respondentItem : caseData.getRespondentCollection()) {
                addressStr.append("**<big>Respondent " + index + "</big>**")
                        .append("<br/>" + respondentItem.getValue().getRespondentName())
                        .append(respondentItem.getValue().getRespondentAddress().toAddressHtml());
                index++;
            }
        }

        return addressStr.toString();
    }

    public String generateEmailLinkToAcas(CaseData caseData) {
        StringBuilder respondentList = new StringBuilder();
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        for (RespondentSumTypeItem respondent : respondentCollection) {
            if (!respondentList.toString().isEmpty()) {
                respondentList.append("%2C%20");
            }
            respondentList.append(respondent.getValue().getRespondentName().replaceAll("\\s+", "%20"));
        }

        String caseNumber = caseData.getEthosCaseReference();
        String claimantName = caseData.getClaimantIndType().getClaimantFirstNames() + " "
                + caseData.getClaimantIndType().getClaimantLastName();

        return MessageFormat.format(ACAS_MAILTO_LINK, caseNumber,
                claimantName.replaceAll("\\s+", "%20"),
                respondentList, caseNumber);
    }

    public void sendSubmittedEmailToRespondentAndClaimant(CaseData caseData) {
        if (caseData.getClaimantType().getClaimantEmailAddress() != null) {
            Map<String, String> emailParam = Map.of(
                    "title", caseData.getClaimantIndType().getClaimantTitle(),
                    "last_name", caseData.getClaimantIndType().getClaimantLastName(),
                    "case_reference", caseData.getEthosCaseReference() != null ?
                            caseData.getEthosCaseReference() : "",
                    "message_link", "loginLink");
            notificationService.sendEmail(caseData.getClaimantType().getClaimantEmailAddress(), emailParam);
        }

        for (RespondentSumTypeItem respondent : caseData.getRespondentCollection()) {
            if (respondent.getValue().getRespondentEmail() != null) {
                Map<String, String> emailParam = Map.of(
                        "title", "",
                        "last_name", respondent.getValue().getRespondentName(),
                        "case_reference", caseData.getEthosCaseReference() != null ?
                                caseData.getEthosCaseReference() : "",
                        "message_link", "loginLink");
                notificationService.sendEmail(respondent.getValue().getRespondentEmail(), emailParam);
            }
        }
    }

    public String generateAfterSubmittedHtml(CaseData caseData) {
        StringBuilder respondentNames = new StringBuilder();
        for (RespondentSumTypeItem respondent : caseData.getRespondentCollection()) {
            respondentNames.append(", " + respondent.getValue().getRespondentName());
        }
        return String.format(AFTER_SUBMITTED_HTML, caseData.getClaimantIndType().getClaimantLastName() + " " +
                caseData.getClaimantIndType().getClaimantFirstNames(),
                respondentNames);
    }

}
