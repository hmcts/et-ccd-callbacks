package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;

import java.util.List;

public class ServingHelper {
    public static final String SERVING_DOCUMENT_OTHER_TYPE = "Another type of document";
    private static final String SERVING_RECIPIENT_CLAIMANT = "Claimant";
    private static final String SERVING_RECIPIENT_RESPONDENT = "Respondent";

    private ServingHelper() {
    }

    public static String generateOtherTypeDocumentName(List<DocumentTypeItem> docList) {
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

    public static String generateClaimantAndRespondentAddress(CaseData caseData) {
        List<String> recipients = caseData.getServingDocumentRecipient();
        StringBuilder addressStr = new StringBuilder();
        if (recipients.contains(SERVING_RECIPIENT_CLAIMANT)) {
            ClaimantIndType claimant = caseData.getClaimantIndType();
            String claimantName = claimant.getClaimantFirstNames() + " "
                    + claimant.getClaimantLastName();
            addressStr.append("**<big>Claimant</big>**")
                    .append("<br/>" + claimantName)
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

    public static String generateEmailLinkToAcas(CaseData caseData) {
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
        String mailToLink = "mailto:ET3@acas.org.uk?subject="
                + caseNumber
                + "&body=Parties%20in%20claim%3A%20"
                + claimantName.replaceAll("\\s+", "%20")
                + "%20vs%20"
                + respondentList
                + "%0D%0ACase%20reference%20number%3A%20"
                + caseNumber
                + "%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0A"
                + "The%20tribunal%20has%20completed%20ET1%20serving%20to%20the%20respondent.%0D%0A%0D%0A"
                + "The%20documents%20we%20sent%20are%20attached%20to%20this%20email.%0D%0A%0D%0A";

        return mailToLink;
    }

}
