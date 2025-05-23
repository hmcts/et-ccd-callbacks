package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.createLinkForUploadedDocument;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isRepresentedClaimantWithMyHmctsCase;

@Service
@RequiredArgsConstructor
@Slf4j
public class RespondNotificationService {
    private static final String UPLOAD_DOCUMENT_IS_REQUIRED = "Upload document is required";
    private static final String SUPPORTING_MATERIAL = "| Supporting material | |";
    private static final String RESPONSE_DETAILS = """
        |  | |\r
        | --- | --- |\r
        | Response %1$S | |\r
        | Response from | %2$s |\r
        | Response date | %3$s |\r
         %4$s\r
        | What's your response to the tribunal? | %5$s\r
        | Do you want to copy correspondence to the other party to satisfy the Rules of Procedure? | %6$s |\r
        """;

    private final EmailService emailService;
    private final SendNotificationService sendNotificationService;

    @Value("${template.sendNotification}")
    private String responseTemplateId;
    @Value("${template.respondNotification.noResponse}")
    private String noResponseTemplateId;

    public void populateSendNotificationSelection(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(getSendNotificationSelection(caseData));
        caseData.setSelectNotificationDropdown(dynamicFixedListType);
    }

    /**
     * Creates the respondNotification and adds it to a list of responses stored on the notification.
     * @param caseData caseData contains the notification details
     * @param sendNotificationType the notificationType where the data will be stored
     */
    private void createRespondNotification(CaseData caseData, SendNotificationType sendNotificationType) {

        if (sendNotificationType.getRespondNotificationTypeCollection() == null) {
            sendNotificationType.setRespondNotificationTypeCollection(new ArrayList<>());
        }

        RespondNotificationType respondNotificationType = new RespondNotificationType();
        respondNotificationType.setRespondNotificationDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        respondNotificationType.setRespondNotificationTitle(caseData.getRespondNotificationTitle());
        respondNotificationType.setRespondNotificationAdditionalInfo(caseData.getRespondNotificationAdditionalInfo());
        respondNotificationType.setRespondNotificationUploadDocument(caseData.getRespondNotificationUploadDocument());
        respondNotificationType.setRespondNotificationCmoOrRequest(caseData.getRespondNotificationCmoOrRequest());
        String responseRequired = caseData.getRespondNotificationResponseRequired();
        respondNotificationType.setRespondNotificationResponseRequired(responseRequired);
        String whoRespond = caseData.getRespondNotificationWhoRespond();
        respondNotificationType.setRespondNotificationWhoRespond(whoRespond);
        respondNotificationType.setRespondNotificationCaseManagementMadeBy(
            caseData.getRespondNotificationCaseManagementMadeBy());
        respondNotificationType.setRespondNotificationRequestMadeBy(caseData.getRespondNotificationRequestMadeBy());
        respondNotificationType.setRespondNotificationFullName(caseData.getRespondNotificationFullName());
        respondNotificationType.setRespondNotificationPartyToNotify(caseData.getRespondNotificationPartyToNotify());

        if (YES.equals(responseRequired) && whoRespond != null && !RESPONDENT_ONLY.equals(whoRespond)) {
            respondNotificationType.setIsClaimantResponseDue(YES);
            respondNotificationType.setState(NOT_STARTED_YET);
            sendNotificationType.setNotificationState(NOT_STARTED_YET);
        } else {
            respondNotificationType.setState(NOT_VIEWED_YET);
            sendNotificationType.setNotificationState(NOT_VIEWED_YET);
        }

        GenericTypeItem<RespondNotificationType> respondNotificationTypeGenericTypeItem = new GenericTypeItem<>();
        respondNotificationTypeGenericTypeItem.setId(String.valueOf(randomUUID()));
        respondNotificationTypeGenericTypeItem.setValue(respondNotificationType);
        sendNotificationType.getRespondNotificationTypeCollection().add(respondNotificationTypeGenericTypeItem);
    }

    /**
     * Clears the respondNotification fields, so the page can be reused.
     * @param caseData caseData to be cleared
     */
    private void clearRespondNotificationFields(CaseData caseData) {
        caseData.setRespondNotificationTitle(null);
        caseData.setRespondNotificationAdditionalInfo(null);
        caseData.setRespondNotificationUploadDocument(null);
        caseData.setRespondNotificationCmoOrRequest(null);
        caseData.setRespondNotificationResponseRequired(null);
        caseData.setRespondNotificationWhoRespond(null);
        caseData.setRespondNotificationCaseManagementMadeBy(null);
        caseData.setRespondNotificationRequestMadeBy(null);
        caseData.setRespondNotificationFullName(null);
        caseData.setRespondNotificationPartyToNotify(null);
    }

    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData) {
        return sendNotificationService.getSendNotificationSelection(caseData, sendNotificationType -> {
            String notificationTitle = sendNotificationType.getValue().getSendNotificationTitle();
            String notificationSubject = sendNotificationType.getValue().getSendNotificationSubject().toString();
            return String.format("%s - %s", notificationTitle, notificationSubject);
        });
    }

    private String getRespondNotificationSingleDocumentMarkdown(UploadedDocumentType uploadedDocumentType) {
        String document = SUPPORTING_MATERIAL;
        if (uploadedDocumentType != null) {
            document = String.format("| Supporting material | %s", createLinkForUploadedDocument(uploadedDocumentType));
        }
        return document;
    }

    private String getRespondNotificationDocumentsMarkdown(RespondNotificationType respondNotificationType) {
        if (respondNotificationType.getRespondNotificationUploadDocument() == null) {
            return SUPPORTING_MATERIAL;
        }
        List<String> documents = respondNotificationType.getRespondNotificationUploadDocument().stream()
            .map(documentTypeItem ->
                getRespondNotificationSingleDocumentMarkdown(documentTypeItem.getValue().getUploadedDocument()))
            .toList();
        return String.join("\r\n", documents);
    }

    /**
     * Gets markdown containing the details of the notification responses.
     * @param sendNotificationType notification
     * @return markdown of responses
     */
    private String getRespondNotificationMarkdown(SendNotificationType sendNotificationType) {
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
            ).toList();
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
            "environmentUrl", envUrl,
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
     * "respond to a notification" event.
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

        String claimantEmail = isRepresentedClaimantWithMyHmctsCase(caseData)
                ? caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress()
                : caseData.getClaimantType().getClaimantEmailAddress();
        String claimantUrl = isRepresentedClaimantWithMyHmctsCase(caseData)
                ? emailService.getExuiCaseLink(caseDetails.getCaseId())
                : emailService.getCitizenCaseLink(caseDetails.getCaseId());

        String sendNotificationTitle = sendNotificationType.getSendNotificationTitle();
        String caseId = caseDetails.getCaseId();
        if (!RESPONDENT_ONLY.equals(caseData.getRespondNotificationPartyToNotify()) && !isNullOrEmpty(claimantEmail)
            && !isClaimantNonSystemUser(caseData)) {
            emailService.sendEmail(
                templateId,
                claimantEmail,
                buildPersonalisation(caseDetails, claimantUrl, sendNotificationTitle)
            );
        }

        if (!CLAIMANT_ONLY.equals(caseData.getRespondNotificationPartyToNotify())) {
            Map<String, String> personalisation = buildPersonalisation(caseDetails,
                    emailService.getExuiCaseLink(caseId), sendNotificationTitle);
            List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
            respondents.forEach(obj -> sendRespondentEmail(caseData, personalisation, obj.getValue(), templateId));
        }
    }

    public List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (YES.equals(caseData.getRespondNotificationResponseRequired())
            && CollectionUtils.isEmpty(caseData.getRespondNotificationUploadDocument())) {
            errors.add(UPLOAD_DOCUMENT_IS_REQUIRED);
        }
        return errors;
    }

    /**
     * Handles the 'about to submit' event including.
     * * Creating the RespondNotification and persisting in caseData
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
        createRespondNotification(caseData, sendNotificationType);
        sendNotifyEmails(caseDetails, sendNotificationType);
        clearRespondNotificationFields(caseData);
    }
}
