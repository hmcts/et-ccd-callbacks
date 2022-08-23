package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;

import java.text.MessageFormat;
import java.util.List;

@Service
@SuppressWarnings({"PMD.ConfusingTernary", "PDM.CyclomaticComplexity", "PMD.UnusedPrivateField",
    "PMD.LiteralsFirstInComparisons"})
public class ET1ServingService {
    public static final String SERVING_DOCUMENT_OTHER_TYPE = "Another type of document";
    private static final String SERVING_RECIPIENT_CLAIMANT = "Claimant";
    private static final String SERVING_RECIPIENT_RESPONDENT = "Respondent";
    private static final String ACAS_MAILTO_LINK = "mailto:ET3@acas.org.uk?subject={0}&body=Parties%20in%20claim"
            + "%3A%20{1}%20vs%20{2}%0D%0ACase%20reference%20number%3A%20{3}%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0AThe%"
            + "20tribunal%20has%20completed%20ET1%20serving%20to%20the%20respondent.%0D%0A%0D%0AThe%20documents%20we"
            + "%20sent%20are%20attached%20to%20this%20email.%0D%0A%0D%0A";
    private static final String OTHER_TYPE_DOCUMENT_NAME = "**<big>%s</big>**<br/><small>%s</small><br/>";
    private static final String CLAIMANT_ADDRESS = "**<big>Claimant</big>**<br/>%s %s%s";
    private static final String RESPONDENT_ADDRESS = "**<big>Respondent %x</big>**<br/>%s%s";

    public String generateOtherTypeDocumentName(List<DocumentTypeItem> docList) {
        StringBuilder sb = new StringBuilder();
        for (DocumentTypeItem doc : docList) {
            if (doc.getValue().getTypeOfDocument().equals(SERVING_DOCUMENT_OTHER_TYPE)) {
                sb.append(String.format(OTHER_TYPE_DOCUMENT_NAME,
                        doc.getValue().getUploadedDocument().getDocumentFilename(),
                        doc.getValue().getShortDescription()));
            }
        }
        return sb.toString();
    }

    public String generateClaimantAndRespondentAddress(CaseData caseData) {
        StringBuilder addressStr = new StringBuilder();

        ClaimantIndType claimant = caseData.getClaimantIndType();
        addressStr.append(String.format(CLAIMANT_ADDRESS, claimant.getClaimantFirstNames(),
            claimant.getClaimantLastName(),
            caseData.getClaimantType().getClaimantAddressUK().toAddressHtml()));

        int index = 1;
        for (RespondentSumTypeItem respondentItem : caseData.getRespondentCollection()) {
            addressStr.append(String.format(RESPONDENT_ADDRESS, index,
                respondentItem.getValue().getRespondentName(),
                respondentItem.getValue().getRespondentAddress().toAddressHtml()));
            index++;
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

}
