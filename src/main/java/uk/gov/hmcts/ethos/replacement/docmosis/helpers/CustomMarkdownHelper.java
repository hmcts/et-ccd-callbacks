package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;

import java.util.List;

public class CustomMarkdownHelper {
    public static final String SERVING_DOCUMENT_OTHER_TYPE = "Another type of document";
    private static final String SERVING_RECIPIENT_CLAIMANT = "Claimant";
    private static final String SERVING_RECIPIENT_RESPONDENT = "Respondent";

    private CustomMarkdownHelper() {

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

    public static String generateClaimantAndRespondentAddress(List<String> recipients, ClaimantIndType claimant,
                                                              Address claimantAddressUK,
                                                              List<RespondentSumTypeItem> respondentList) {
        StringBuilder addressStr = new StringBuilder();
        if (recipients.contains(SERVING_RECIPIENT_CLAIMANT)) {
            String claimantName = claimant.getClaimantFirstNames() + " "
                    + claimant.getClaimantLastName();
            addressStr.append("**<big>Claimant</big>**")
                    .append("<br/>" + claimantName)
                    .append(claimantAddressUK.toAddressString());
        }
        if (recipients.contains(SERVING_RECIPIENT_RESPONDENT)) {
            int index = 1;
            for (RespondentSumTypeItem respondentItem : respondentList) {
                addressStr.append("**<big>Respondent " + index + "</big>**")
                        .append("<br/>" + respondentItem.getValue().getRespondentName())
                        .append(respondentItem.getValue().getRespondentAddress().toAddressString());
                index++;
            }
        }

        return addressStr.toString();
    }

}
