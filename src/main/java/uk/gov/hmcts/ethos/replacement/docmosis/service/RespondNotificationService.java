package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.createLinkForUploadedDocument;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports"})
public class RespondNotificationService {

    private final EmailService emailService;
    private final SendNotificationService sendNotificationService;

    @Value("${url.exui.case-details}")
    private String exuiUrl;
    @Value("${url.citizen.case-details}")
    private String citizenUrl;
    @Value("${sendNotification.template.id}")
    private String responseTemplateId;
    @Value("${respondNotification.noResponseTemplate.id}")
    private String noResponseTemplateId;

    private static final String RESPONSE_DETAILS = "|  | |\r\n"
        + "| --- | --- |\r\n"
        + "| RESPONSE %1$S | |\r\n"
        + "| Response from | %2$s |\r\n"
        + "| Response date | %3$s |\r\n"
        + " %4$s\r\n"
        + "| What's your response to the tribunal? | %5$s\r\n"
        + "| Do you want to copy correspondence to the other party to satisfy the Rules of Procedure? | %6$s |\r\n";

    public void populateSendNotificationSelection(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(getSendNotificationSelection(caseData));
        caseData.setSelectNotificationDropdown(dynamicFixedListType);
    }

    /**
     * Creates the responseNotification and adds it to a list of responses stored on the notification.
     * @param caseData caseData contains the notification details
     * @param sendNotificationType the notificationType where the data will be stored
     */
    public void createResponseNotification(CaseData caseData, SendNotificationType sendNotificationType) {

        if (sendNotificationType.getRespondNotificationTypeCollection() == null) {
            sendNotificationType.setRespondNotificationTypeCollection(new ArrayList<>());
        }

        RespondNotificationType respondNotificationType = new RespondNotificationType();
        respondNotificationType.setRespondNotificationDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        respondNotificationType.setRespondNotificationTitle(caseData.getRespondNotificationTitle());
        respondNotificationType.setRespondNotificationAdditionalInfo(caseData.getRespondNotificationAdditionalInfo());
        respondNotificationType.setRespondNotificationUploadDocument(caseData.getRespondNotificationUploadDocument());
        respondNotificationType.setRespondNotificationCmoOrRequest(caseData.getRespondNotificationCmoOrRequest());
        respondNotificationType.setRespondNotificationResponseRequired(
            caseData.getRespondNotificationResponseRequired());
        respondNotificationType.setRespondNotificationWhoRespond(caseData.getRespondNotificationWhoRespond());
        respondNotificationType.setRespondNotificationCaseManagementMadeBy(
            caseData.getRespondNotificationCaseManagementMadeBy());
        respondNotificationType.setRespondNotificationFullName(caseData.getRespondNotificationFullName());
        respondNotificationType.setRespondNotificationPartyToNotify(caseData.getRespondNotificationPartyToNotify());

        GenericTypeItem<RespondNotificationType> respondNotificationTypeGenericTypeItem = new GenericTypeItem<>();
        respondNotificationTypeGenericTypeItem.setId(String.valueOf(randomUUID()));
        respondNotificationTypeGenericTypeItem.setValue(respondNotificationType);
        sendNotificationType.getRespondNotificationTypeCollection().add(respondNotificationTypeGenericTypeItem);
    }

    /**
     * Clears the responseNotification fields, so the page can be reused.
     * @param caseData caseData to be cleared
     */
    public void clearResponseNotificationFields(CaseData caseData) {
        caseData.setRespondNotificationTitle(null);
        caseData.setRespondNotificationAdditionalInfo(null);
        caseData.setRespondNotificationUploadDocument(null);
        caseData.setRespondNotificationCmoOrRequest(null);
        caseData.setRespondNotificationResponseRequired(null);
        caseData.setRespondNotificationWhoRespond(null);
        caseData.setRespondNotificationCaseManagementMadeBy(null);
        caseData.setRespondNotificationFullName(null);
        caseData.setRespondNotificationPartyToNotify(null);
    }

    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData) {
        return sendNotificationService.getSendNotificationSelection(caseData, (sendNotificationType) -> {
            String notificationTitle = sendNotificationType.getValue().getSendNotificationTitle();
            String notificationSubject = sendNotificationType.getValue().getSendNotificationSubject().toString();
            return String.format("%s - %s", notificationTitle, notificationSubject);
        });
    }

    private String getRespondNotificationSingleDocumentMarkdown(UploadedDocumentType uploadedDocumentType) {
        String document = "| Supporting material | |";
        if (uploadedDocumentType != null) {
            document = String.format("| Supporting material | %s", createLinkForUploadedDocument(uploadedDocumentType));
        }
        return document;
    }

    private String getRespondNotificationDocumentsMarkdown(RespondNotificationType respondNotificationType) {
        if (respondNotificationType.getRespondNotificationUploadDocument() == null) {
            return "| Supporting material | |";
        }
        List<String> documents = respondNotificationType.getRespondNotificationUploadDocument().stream()
            .map(documentTypeItem ->
                getRespondNotificationSingleDocumentMarkdown(documentTypeItem.getValue().getUploadedDocument()))
            .collect(Collectors.toList());
        return String.join("\r\n", documents);
    }

    /**
     * Gets markdown containing the details of the notification responses.
     * @param sendNotificationType notification
     * @return markdown of responses
     */
    public String getRespondNotificationMarkdown(SendNotificationType sendNotificationType) {
        var respondNotificationTypeCollection = sendNotificationType.getRespondNotificationTypeCollection();
        if (respondNotificationTypeCollection == null) {
            return "";
        }
        List<String> respondNotificationMarkdownList =
            respondNotificationTypeCollection.stream().map(respondNotificationType -> {
                Integer index =
                    respondNotificationTypeCollection.indexOf(respondNotificationType) + 1;
                RespondNotificationType value = respondNotificationType.getValue();
                String responseToTribunal = String.format("%s - %s", value.getRespondNotificationTitle(),
                    value.getRespondNotificationAdditionalInfo());
                return String.format(
                        RESPONSE_DETAILS,
                        index,
                        TRIBUNAL,
                        value.getRespondNotificationDate(),
                        getRespondNotificationDocumentsMarkdown(value),
                        responseToTribunal,
                        YES
                    );
                }
            ).collect(Collectors.toList());
        return String.join("\r\n", respondNotificationMarkdownList);
    }

    public String getNotificationMarkDown(CaseData caseData) {
        Optional<SendNotificationTypeItem> sendNotification = sendNotificationService.getSendNotification(caseData);
        if (sendNotification.isEmpty()) {
            return "";
        }
        SendNotificationType sendNotificationType = sendNotification.get().getValue();
        return String.join("\r\n",
            sendNotificationService.getSendNotificationMarkDown(sendNotificationType),
                getRespondNotificationMarkdown(sendNotificationType));
    }

    private Map<String, String> buildPersonalisation(CaseDetails caseDetails, String envUrl,
                                                     String sendNotificationTitle) {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
            "caseNumber", caseData.getEthosCaseReference(),
            "environmentUrl", envUrl + caseDetails.getCaseId(),
            "claimant", caseData.getClaimant(),
            "respondents", getRespondentNames(caseData),
            "notificationTitle", sendNotificationTitle
        );
    }

    private void sendRespondentEmail(CaseData caseData, Map<String, String> emailData, RespondentSumType respondent,
                                     String templateId) {
        String respondentEmail = NotificationHelper.getEmailAddressForRespondent(caseData, respondent);
        if (isNullOrEmpty(respondentEmail)) {
            return;
        }
        emailService.sendEmail(templateId, respondentEmail, emailData);
    }

    /**
     * Sends notification emails for the claimant and/or respondent(s) based on the radio list from the
     * respond to a notification event.
     * @param caseDetails - caseDetails
     * @param sendNotificationType - the notification containing the details of the response
     */
    public void sendNotifyEmails(CaseDetails caseDetails, SendNotificationType sendNotificationType) {
        CaseData caseData = caseDetails.getCaseData();
        String templateId;
        if (NO.equals(caseData.getRespondNotificationResponseRequired())) {
            templateId = noResponseTemplateId;
        } else {
            templateId = responseTemplateId;
        }

        String sendNotificationTitle = sendNotificationType.getSendNotificationTitle();
        if (!RESPONDENT_ONLY.equals(caseData.getRespondNotificationPartyToNotify())) {
            emailService.sendEmail(templateId, caseData.getClaimantType().getClaimantEmailAddress(),
                buildPersonalisation(caseDetails, citizenUrl, sendNotificationTitle));
        }

        if (!CLAIMANT_ONLY.equals(caseData.getRespondNotificationPartyToNotify())) {
            Map<String, String> personalisation = buildPersonalisation(caseDetails, exuiUrl, sendNotificationTitle);
            List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
            respondents.forEach(obj -> sendRespondentEmail(caseData, personalisation, obj.getValue(), templateId));
        }
    }

    /**
     * Handles the 'about to submit' event including.
     * * Creating the ResponseNotification and persisting in caseData
     * * sending the notification emails
     * * clearing the response fields so page is ready for the next use
     * @param caseDetails - caseDetails
     */
    public void handleAboutToSubmit(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        Optional<SendNotificationTypeItem> sendNotificationTypeItemOptional =
            sendNotificationService.getSendNotification(caseData);
        if (sendNotificationTypeItemOptional.isEmpty()) {
            log.warn(caseDetails.getCaseId() + " failed to create and send notification due to missing notification "
                + "details");
            return;
        }
        SendNotificationType sendNotificationType = sendNotificationTypeItemOptional.get().getValue();
        createResponseNotification(caseData, sendNotificationType);
        sendNotifyEmails(caseDetails, sendNotificationType);
        clearResponseNotificationFields(caseData);
    }
}
