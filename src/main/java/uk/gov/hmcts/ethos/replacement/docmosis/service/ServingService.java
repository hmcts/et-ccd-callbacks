package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.addressIsEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantRepresentedByMyHmctsOrganisation;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ADDRESS_NOT_ENTERED;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServingService {
    public static final String SERVING_DOCUMENT_OTHER_TYPE = "Another type of document";
    private static final String ACAS_MAILTO_LINK = "mailto:et3@acas.org.uk?subject={0}&body=Parties%20in%20claim"
            + "%3A%20{1}%20vs%20{2}%0D%0ACase%20reference%20number%3A%20{3}%0D%0A%0D%0ADear%20Acas%2C%0D%0A%0D%0AThe%"
            + "20tribunal%20has%20completed%20{4}%20to%20the%20{5}.%0D%0A%0D%0AThe%20documents%20we"
            + "%20sent%20are%20attached%20to%20this%20email.%0D%0A%0D%0A";
    private static final String OTHER_TYPE_DOCUMENT_NAME = "**<big>%s</big>**<br/><small><a target=\"_blank\" "
        + "href=\"%s\">%s</a></small><br/>";
    private static final String RESPONDENT_ADDRESS = "**<big>Respondent %x</big>**<br/>%s%s";
    private static final String ET3_NOTIFICATION = "ET3%20notifications";
    private static final String ET1_SERVING = "ET1%20serving";
    private static final String ET3_RELEVANT_PARTIES = "relevant%20parties";
    private static final String ET1_RESPONDENT = "respondent";
    private static final String EMAIL_ADDRESS = "emailAddress";

    private static final String DOC_TYPE_ACK_OF_CLAIM = "Acknowledgement of claim";
    private static final String DOC_TYPE_NOTICE_OF_CLAIM = "Notice of a claim";
    private static final String DOC_TYPE_NOTICE_OF_HEARING = "Notice of Hearing";
    private static final String DOC_TYPE_OTHER = "Other ";

    private static final String SERVING_DOC_1_1 = "1.1";
    private static final String SERVING_DOC_2_6 = "2.6";
    private static final String SERVING_DOC_2_7 = "2.7";
    private static final String SERVING_DOC_2_8 = "2.8";
    private static final String SERVING_DOC_7_7 = "7.7";
    private static final String SERVING_DOC_7_8 = "7.8";
    private static final String SERVING_DOC_7_8A = "7.8a";

    private static final List<String> NOTICE_OF_CASE_MANAGEMENT_DISCUSSION =
        List.of(SERVING_DOC_7_7, SERVING_DOC_7_8, SERVING_DOC_7_8A);

    private static final String TYPE_OF_DOCUMENT_ERROR_MESSAGE =
        "You have only uploaded a notice of hearing. Please also upload the relevant service letter.";

    @Value("${template.et1Serving.claimant}")
    private String claimantTemplateId;

    @Value("${template.et1Serving.claimantRep}")
    private String claimantRepTemplateId;

    private final EmailService emailService;
    private final EmailNotificationService emailNotificationService;
    private final CaseAccessService caseAccessService;

    /**
     * Check if only 7.7, 7.8 or 7.8a is uploaded, display an error message
     * @param docList ServingDocumentCollection
     * @return error message if any
     */
    public List<String> checkTypeOfDocumentError(List<DocumentTypeItem> docList) {
        List<String> errors = new ArrayList<>();

        if (CollectionUtils.isEmpty(docList)) {
            return errors;
        }

        boolean onlyNoticeUploaded = docList.stream().map(item -> item.getValue().getTypeOfDocument())
            .allMatch(NOTICE_OF_CASE_MANAGEMENT_DISCUSSION::contains);
        if (onlyNoticeUploaded) {
            errors.add(TYPE_OF_DOCUMENT_ERROR_MESSAGE);
        }

        return errors;
    }

    public String generateOtherTypeDocumentLink(List<DocumentTypeItem> docList) {
        String documentLinks = "";
        if (isNotEmpty(docList)) {
            documentLinks = docList
                .stream()
                .filter(d -> SERVING_DOCUMENT_OTHER_TYPE.equals(d.getValue().getTypeOfDocument()))
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

    public String generateRespondentAddressList(CaseData caseData) {
        StringBuilder addressStr = new StringBuilder();

        int index = 1;
        for (RespondentSumTypeItem respondentItem : caseData.getRespondentCollection()) {
            RespondentSumType respondentSumType = respondentItem.getValue();
            addressStr.append(String.format(RESPONDENT_ADDRESS, index,
                respondentSumType.getRespondentName(),
                addressIsEmpty(respondentSumType.getRespondentAddress())
                        ? "<br>" + ADDRESS_NOT_ENTERED + "<br>"
                        : respondentSumType.getRespondentAddress().toAddressHtml()));
            index++;
        }

        return addressStr.toString();
    }

    /**
     * Generates the email link to the Acas. The claimants and the list of respondent's names are put through a
     * URL encoder to ensure that special characters are handled correctly when the mailto is clicked.
     * @param caseData object that holds the case data.
     * @param isET3 boolean value to indicate if the notification is for ET1 Serving or ET3 Notification.
     * @return markdown email link containing the claimant's name, respondent's names and case number.
     */
    public String generateEmailLinkToAcas(CaseData caseData, boolean isET3) {
        StringBuilder respondentList = new StringBuilder();
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        respondentCollection.forEach(respondent -> {
            if (!respondentList.isEmpty()) {
                respondentList.append("%2C%20");
            }
            respondentList.append(encode(respondent.getValue().getRespondentName(), UTF_8));
        });

        String caseNumber = caseData.getEthosCaseReference();
        String claimantName = encode(caseData.getClaimant(), UTF_8);

        return MessageFormat.format(ACAS_MAILTO_LINK, caseNumber,
                formatNameForEmail(claimantName),
                formatNameForEmail(respondentList.toString()),
                caseNumber,
                isET3 ? ET3_NOTIFICATION : ET1_SERVING,
                isET3 ? ET3_RELEVANT_PARTIES : ET1_RESPONDENT);
    }

    private String formatNameForEmail(String name) {
        return isNullOrEmpty(name)
                ? ""
                : name.replaceAll("\\s+", "%20")
                        .replace("+", "%20")
                        .replace("\\+", "%20");
    }

    /**
     * Sends notifications to the relevant parties that their case has been updated.
     * @param caseDetails object that holds the case data.
     */
    public void sendNotifications(CaseDetails caseDetails) {
        Map<String, String> personalisation;

        List<CaseUserAssignment> caseUserAssignments =
                caseAccessService.getCaseUserAssignmentsById(caseDetails.getCaseId());
        if (isClaimantRepresentedByMyHmctsOrganisation(caseDetails.getCaseData())) {
            personalisation = NotificationHelper.buildMapForClaimantRepresentative(caseDetails.getCaseData());
            personalisation.put(LINK_TO_EXUI, emailService.getExuiCaseLink(caseDetails.getCaseId()));

            emailNotificationService.getCaseClaimantSolicitorEmails(caseUserAssignments).stream()
                    .filter(email -> email != null && !email.isEmpty())
                    .forEach(email -> emailService.sendEmail(
                            claimantRepTemplateId,
                            email,
                            personalisation));
        } else if (!isClaimantNonSystemUser(caseDetails.getCaseData())) {
            personalisation = NotificationHelper.buildMapForClaimant(caseDetails);
            personalisation.put(LINK_TO_CITIZEN_HUB, emailService.getCitizenCaseLink(caseDetails.getCaseId()));
            if (isNullOrEmpty(personalisation.get(EMAIL_ADDRESS))) {
                return;
            }
            emailService.sendEmail(claimantTemplateId, personalisation.get(EMAIL_ADDRESS), personalisation);
        }
    }

    /**
     * Copy servingDocumentCollection to documentCollection.
     * @param caseData object that holds the case data.
     */
    public void addServingDocToDocumentCollection(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getServingDocumentCollection())) {
            log.warn("No serving documents found");
            return;
        }

        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }

        caseData.getServingDocumentCollection()
            .forEach(d -> caseData.getDocumentCollection().add(
                DocumentHelper.createDocumentTypeItem(
                    d.getValue().getUploadedDocument(),
                    updateServingTypeOfDocument(d.getValue().getTypeOfDocument()))
            ));
    }

    private String updateServingTypeOfDocument(String typeOfDocument) {
        return switch (typeOfDocument) {
            case SERVING_DOC_1_1 -> DOC_TYPE_ACK_OF_CLAIM;
            case SERVING_DOC_2_6, SERVING_DOC_2_7, SERVING_DOC_2_8 -> DOC_TYPE_NOTICE_OF_CLAIM;
            case SERVING_DOC_7_7, SERVING_DOC_7_8, SERVING_DOC_7_8A ->  DOC_TYPE_NOTICE_OF_HEARING;
            default ->  DOC_TYPE_OTHER;
        };
    }
}
