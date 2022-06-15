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
                    .append(caseData.getClaimantType().getClaimantAddressUK().toAddressString());
        }
        if (recipients.contains(SERVING_RECIPIENT_RESPONDENT)) {
            int index = 1;
            for (RespondentSumTypeItem respondentItem : caseData.getRespondentCollection()) {
                addressStr.append("**<big>Respondent " + index + "</big>**")
                        .append("<br/>" + respondentItem.getValue().getRespondentName())
                        .append(respondentItem.getValue().getRespondentAddress().toAddressString());
                index++;
            }
        }

        return addressStr.toString();
    }

}
