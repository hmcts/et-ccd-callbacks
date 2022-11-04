package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.ConfusingTernary", "PDM.CyclomaticComplexity", "PMD.UnusedPrivateField",
    "PMD.LiteralsFirstInComparisons"})
public class ServingService {
    public static final String SERVING_DOCUMENT_OTHER_TYPE = "Another type of document";
    private static final String SERVING_RECIPIENT_CLAIMANT = "Claimant";
    private static final String SERVING_RECIPIENT_RESPONDENT = "Respondent";
    private static final String ACAS_MAILTO_LINK = "mailto:ET3@acas.org.uk?subject={0}&body=Parties%20in%20claim"
            + "%3A%20{1}%20vs%20{2}%0D%0ACase%20reference%20number%3A%20{3}%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0AThe%"
            + "20tribunal%20has%20completed%20{4}%20to%20the%20{5}.%0D%0A%0D%0AThe%20documents%20we"
            + "%20sent%20are%20attached%20to%20this%20email.%0D%0A%0D%0A";
    private static final String OTHER_TYPE_DOCUMENT_NAME = "**<big>%s</big>**<br/><small><a target=\"_blank\" "
        + "href=\"%s\">%s</a></small><br/>";
    private static final String CLAIMANT_ADDRESS = "**<big>Claimant</big>**<br/>%s %s%s";
    private static final String RESPONDENT_ADDRESS = "**<big>Respondent %x</big>**<br/>%s%s";
    private static final String ET3_NOTIFICATION = "ET3%20notifications";
    private static final String ET1_SERVING = "ET1%20serving";
    private static final String ET3_RELEVANT_PARTIES = "relevant%20parties";
    private static final String ET1_RESPONDENT = "respondent";
    public static final String EMAIL_ADDRESS = "emailAddress";

    @Value("${et1Serving.template.id}")
    private String templateId;

    private final EmailService emailService;

    public String generateOtherTypeDocumentLink(List<DocumentTypeItem> docList) {
        String documentLinks = "";
        if (CollectionUtils.isNotEmpty(docList)) {
            documentLinks = docList
                .stream()
                .filter(d -> d.getValue().getTypeOfDocument().equals(SERVING_DOCUMENT_OTHER_TYPE))
                .map(this::createDocLinkBinary)
                .collect(Collectors.joining());
        }

        return documentLinks;
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        String documentName = documentTypeItem.getValue().getUploadedDocument().getDocumentFilename();
        return String.format(OTHER_TYPE_DOCUMENT_NAME, documentName,
            documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/")), documentName);
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

    public String generateEmailLinkToAcas(CaseData caseData, boolean isET3) {
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
                respondentList, caseNumber, isET3 ? ET3_NOTIFICATION : ET1_SERVING,
                isET3 ? ET3_RELEVANT_PARTIES : ET1_RESPONDENT);
    }

    /**
     * Sends notifications to the relevant parties that their case has been updated.
     * @param caseData object that holds the case data.
     */
    public void sendNotifications(CaseData caseData) {
        Map<String, String> claimant = NotificationHelper.buildMapForClaimant(caseData);

        caseData.getRespondentCollection()
            .forEach(o -> {
                Map<String, String> respondent = NotificationHelper.buildMapForRespondent(caseData, o.getValue());
                if (isNullOrEmpty(respondent.get(EMAIL_ADDRESS))) {
                    return;
                }
                emailService.sendEmail(templateId, respondent.get(EMAIL_ADDRESS), respondent);
            });

        if (isNullOrEmpty(claimant.get(EMAIL_ADDRESS))) {
            return;
        }

        emailService.sendEmail(templateId, claimant.get(EMAIL_ADDRESS), claimant);
    }

}
