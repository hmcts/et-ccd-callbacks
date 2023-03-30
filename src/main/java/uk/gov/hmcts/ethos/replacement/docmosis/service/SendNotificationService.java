
package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.createLinkForUploadedDocument;

@Service("sendNotificationService")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports"})
public class SendNotificationService {
    private final HearingSelectionService hearingSelectionService;
    private final EmailService emailService;
    @Value("${url.exui.case-details}")
    private String exuiUrl;
    @Value("${url.citizen.case-details}")
    private String citizenUrl;
    @Value("${sendNotification.template.id}")
    private String templateId;

    private static final String BLANK_DOCUMENT_MARKDOWN = "| Document | | \r\n| Description | |";

    private static final String POPULATED_DOCUMENT_MARKDOWN = "| Document |%s|\r\n| Description |%s|";

    private static final String NOTIFICATION_DETAILS = "|  | |\r\n"
             + "| --- | --- |\r\n"
             + "| Subject | %1$s |\r\n"
             + "| Notification | %2$s |\r\n"
             + "| Hearing | %3$s |\r\n"
             + "| Date Sent | %4$s |\r\n"
             + "| Sent By | %5$s  |\r\n"
             + "| Case management order or request | %6$s |\r\n"
             + "| Response due | %7$s |\r\n"
             + "| Party or parties to respond | %8$s |\r\n"
             + "| Additional Information | %9$s |\r\n"
             + " %10$s\r\n"
             + "| Case management order made by | %11$s |\r\n"
             + "| Name | %12$s |\r\n"
             + "| Sent to | %8$s |\r\n";

    public void populateHearingSelection(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(hearingSelectionService.getHearingSelection(caseData, "%s: %s - %s - %s"));
        caseData.setSendNotificationSelectHearing(dynamicFixedListType);
    }

    public void createSendNotification(CaseData caseData) {

        if (caseData.getSendNotificationCollection() == null) {
            caseData.setSendNotificationCollection(new ArrayList<>());
        }
        SendNotificationType sendNotificationType = new SendNotificationType();
        sendNotificationType.setNumber(String.valueOf(getNextNotificationNumber(caseData)));
        sendNotificationType.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        sendNotificationType.setSendNotificationTitle(caseData.getSendNotificationTitle());
        sendNotificationType.setSendNotificationLetter(caseData.getSendNotificationLetter());
        sendNotificationType.setSendNotificationUploadDocument(caseData.getSendNotificationUploadDocument());
        sendNotificationType.setSendNotificationSubject(caseData.getSendNotificationSubject());
        sendNotificationType.setSendNotificationAdditionalInfo(caseData.getSendNotificationAdditionalInfo());
        sendNotificationType.setSendNotificationNotify(caseData.getSendNotificationNotify());
        sendNotificationType.setSendNotificationSelectHearing(caseData.getSendNotificationSelectHearing());
        sendNotificationType.setSendNotificationCaseManagement(caseData.getSendNotificationCaseManagement());
        sendNotificationType.setSendNotificationResponseTribunal(caseData.getSendNotificationResponseTribunal());
        sendNotificationType.setSendNotificationWhoCaseOrder(caseData.getSendNotificationWhoCaseOrder());
        sendNotificationType.setSendNotificationSelectParties(caseData.getSendNotificationSelectParties());
        sendNotificationType.setSendNotificationFullName(caseData.getSendNotificationFullName());
        sendNotificationType.setSendNotificationFullName2(caseData.getSendNotificationFullName2());
        sendNotificationType.setSendNotificationDecision(caseData.getSendNotificationDecision());
        sendNotificationType.setSendNotificationDetails(caseData.getSendNotificationDetails());
        sendNotificationType.setSendNotificationRequestMadeBy(caseData.getSendNotificationRequestMadeBy());
        sendNotificationType.setSendNotificationEccQuestion(caseData.getSendNotificationEccQuestion());
        sendNotificationType.setSendNotificationWhoMadeJudgement(caseData.getSendNotificationWhoMadeJudgement());
        if (RESPONDENT_ONLY.equals(sendNotificationType.getSendNotificationSelectParties())) {
            sendNotificationType.setNotificationState(NOT_VIEWED_YET);
        } else {
            sendNotificationType.setNotificationState(NOT_STARTED_YET);
        }

        sendNotificationType.setSendNotificationSentBy(TRIBUNAL);
        sendNotificationType.setSendNotificationSubjectString(
                String.join(", ", caseData.getSendNotificationSubject())
        );
        sendNotificationType.setSendNotificationResponsesCount("0");
        sendNotificationType.setSendNotificationResponseTribunalTable(
                NO.equals(caseData.getSendNotificationResponseTribunal()) ? NO : YES
        );

        SendNotificationTypeItem sendNotificationTypeItem = new SendNotificationTypeItem();
        sendNotificationTypeItem.setId(UUID.randomUUID().toString());
        sendNotificationTypeItem.setValue(sendNotificationType);
        caseData.getSendNotificationCollection().add(sendNotificationTypeItem);

    }

    private static int getNextNotificationNumber(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return 1;
        }
        return caseData.getSendNotificationCollection().size() + 1;
    }

    public void clearSendNotificationFields(CaseData caseData) {
        caseData.setSendNotificationTitle(null);
        caseData.setSendNotificationLetter(null);
        caseData.setSendNotificationUploadDocument(null);
        caseData.setSendNotificationSubject(null);
        caseData.setSendNotificationAdditionalInfo(null);
        caseData.setSendNotificationNotify(null);
        caseData.setSendNotificationSelectHearing(null);
        caseData.setSendNotificationCaseManagement(null);
        caseData.setSendNotificationResponseTribunal(null);
        caseData.setSendNotificationWhoCaseOrder(null);
        caseData.setSendNotificationSelectParties(null);
        caseData.setSendNotificationFullName(null);
        caseData.setSendNotificationFullName2(null);
        caseData.setSendNotificationDecision(null);
        caseData.setSendNotificationDetails(null);
        caseData.setSendNotificationRequestMadeBy(null);
        caseData.setSendNotificationEccQuestion(null);
        caseData.setSendNotificationWhoCaseOrder(null);
    }

    /**
     * Gets a list of notifications in the selected format.
     * @param caseData data to get notifications from
     * @param format lambda contains details for the formatting
     * @return A list of notifications
     */
    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData,
                                                               Function<SendNotificationTypeItem, String> format) {
        List<SendNotificationTypeItem> sendNotificationTypeItemList = caseData.getSendNotificationCollection();
        if (CollectionUtils.isEmpty(sendNotificationTypeItemList)) {
            return new ArrayList<>();
        }
        return sendNotificationTypeItemList.stream()
            .map(sendNotificationType -> DynamicValueType.create(sendNotificationType.getId(),
                format.apply(sendNotificationType)))
            .collect(Collectors.toList());
    }

    /**
     * Sends notification emails for the claimant and/or respondent(s) based on the radio list from the
     * sendNotification event.
     *
     * @param caseDetails Details of the case
     */
    public void sendNotifyEmails(CaseDetails caseDetails) {

        CaseData caseData = caseDetails.getCaseData();

        if (!RESPONDENT_ONLY.equals(caseData.getSendNotificationNotify())) {
            emailService.sendEmail(templateId, caseData.getClaimantType().getClaimantEmailAddress(),
                buildPersonalisation(caseDetails, citizenUrl));
        }

        if (!CLAIMANT_ONLY.equals(caseData.getSendNotificationNotify())) {
            Map<String, String> personalisation = buildPersonalisation(caseDetails, exuiUrl);
            List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
            respondents.forEach(obj -> sendRespondentEmail(caseData, personalisation, obj.getValue()));
        }
    }

    public Optional<SendNotificationTypeItem> getSendNotification(CaseData caseData) {
        return caseData.getSendNotificationCollection()
                .stream()
                .filter(s -> s.getId().equals(caseData.getSelectNotificationDropdown().getSelectedCode()))
                .findFirst();
    }

    private String getSendNotificationSingleDocumentMarkdown(DocumentType uploadedDocumentType) {
        String document = BLANK_DOCUMENT_MARKDOWN;
        if (uploadedDocumentType != null) {
            document = String.format(POPULATED_DOCUMENT_MARKDOWN,
                createLinkForUploadedDocument(uploadedDocumentType.getUploadedDocument()),
                uploadedDocumentType.getShortDescription());
        }
        return document;
    }

    private String getSendNotificationDocumentsMarkdown(SendNotificationType sendNotification) {
        if (sendNotification.getSendNotificationUploadDocument() == null) {
            return BLANK_DOCUMENT_MARKDOWN;
        }
        List<String> documents = sendNotification.getSendNotificationUploadDocument().stream()
                .map(documentTypeItem ->
                        getSendNotificationSingleDocumentMarkdown(documentTypeItem.getValue()))
                .collect(Collectors.toList());
        return String.join("\r\n", documents);
    }

    public String getSendNotificationMarkDown(SendNotificationType sendNotification) {

        Optional<DynamicFixedListType> sendNotificationSelectHearing
                = Optional.ofNullable(sendNotification.getSendNotificationSelectHearing());

        return String.format(NOTIFICATION_DETAILS, Strings.nullToEmpty(sendNotification.getSendNotificationTitle()),
                Strings.nullToEmpty(String.join(", ", sendNotification.getSendNotificationSubject())),
                sendNotificationSelectHearing.isPresent() ? sendNotificationSelectHearing.get().getSelectedLabel() : "",
                sendNotification.getDate(),
                TRIBUNAL,
                Strings.nullToEmpty(sendNotification.getSendNotificationCaseManagement()),
                Strings.nullToEmpty(sendNotification.getSendNotificationResponseTribunal()),
                Strings.nullToEmpty(sendNotification.getSendNotificationNotify()),
                Strings.nullToEmpty(sendNotification.getSendNotificationAdditionalInfo()),
                getSendNotificationDocumentsMarkdown(sendNotification),
                Strings.nullToEmpty(sendNotification.getSendNotificationWhoCaseOrder()),
                Strings.nullToEmpty(sendNotification.getSendNotificationFullName()));

    }

    private void sendRespondentEmail(CaseData caseData, Map<String, String> emailData, RespondentSumType respondent) {
        String respondentEmail = NotificationHelper.getEmailAddressForRespondent(caseData, respondent);
        if (isNullOrEmpty(respondentEmail)) {
            return;
        }
        emailService.sendEmail(templateId, respondentEmail, emailData);
    }

    private Map<String, String> buildPersonalisation(CaseDetails caseDetails, String envUrl) {
        return Map.of(
                CASE_NUMBER, caseDetails.getCaseData().getEthosCaseReference(),
            "environmentUrl", envUrl + caseDetails.getCaseId()
        );
    }
}
