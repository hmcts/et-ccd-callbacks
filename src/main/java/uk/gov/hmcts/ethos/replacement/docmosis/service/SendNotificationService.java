
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
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.SendNotificationTypeData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PseHelper.getSendNotificationUploadDocument;

@Service("sendNotificationService")
@RequiredArgsConstructor
@Slf4j
public class SendNotificationService {

    private final HearingSelectionService hearingSelectionService;
    private final EmailService emailService;
    @Value("${url.exui.case-details}")
    private String exuiUrl;
    @Value("${url.citizen.case-details}")
    private String citizenUrl;
    @Value("${sendNotification.template.id}")
    private String templateId;


    private final String RESPONSE_DETAILS = "|  | |\r\n"
             + "| --- | --- |\r\n"
             + "| Subject | %1$s |\r\n"
             + "| Notification | %2$s |\r\n"
             + "| Hearing | %3$s |\r\n"
             + "| Date Sent | %4$s |\r\n"
             + "| Sent By | %5$s  |\r\n"
             + "| Case management order request | %6$s |\r\n"
             + "| Response due | %7$s |\r\n"
             + "| Party or parties to respond | %8$s |\r\n"
             + "| Additional Information | %9$s |\r\n"
             + "| Description | %1$s |\r\n"
             + " %10$s\r\n"
             + "| Case management order made by | %11$s |\r\n"
             + "| Name | %12$s |\r\n"
             + "| Sent to | %8$s |\r\n";
    private final String TRIBUNAL = "TRIBUNAL";


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
        sendNotificationType.setSendNotificationDetails(caseData.getSendNotificationDetails());
        sendNotificationType.setSendNotificationRequestMadeBy(caseData.getSendNotificationRequestMadeBy());

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
        caseData.setSendNotificationDetails(null);
        caseData.setSendNotificationRequestMadeBy(null);
    }

    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData,
                                                               Function<SendNotificationTypeItem, String> format) {
        List<DynamicValueType> values = new ArrayList<>();
        List<SendNotificationTypeItem> sendNotificationTypeItemList = caseData.getSendNotificationCollection();
        if (CollectionUtils.isEmpty(sendNotificationTypeItemList)) {
            return values;
        }
        for (SendNotificationTypeItem sendNotificationType : sendNotificationTypeItemList) {
            String notificationId = sendNotificationType.getId();
            String label = format.apply(sendNotificationType);
            values.add(DynamicValueType.create(notificationId, label));
        }
        return values;
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

    private String getSendNotificationSingleDocumentMarkdown(DocumentTypeItem documentTypeItem){
        String document = "| Document | N/A |";
        if (documentTypeItem != null) {
            Matcher matcher = Helper.getDocumentMatcher(
                    documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl());
            String documentLink = matcher.replaceFirst("");
            String documentName = documentTypeItem.getValue().getUploadedDocument().getDocumentFilename();
            document = String.format("| Document | <a href=\"/documents/%s\" target=\"_blank\">%s</a>| ", documentLink,
                    documentName);
        }
        return document;
    }

    private String getSendNotificationDocumentsMarkdown(SendNotificationType sendNotification) {
        if(sendNotification.getSendNotificationUploadDocument() == null) {
            return "";
        }
        List<String> documents = sendNotification.getSendNotificationUploadDocument().stream()
                .map(documentTypeItem -> getSendNotificationSingleDocumentMarkdown(documentTypeItem))
                .collect(Collectors.toList());
        return String.join("\r\n", documents);

    }

    public String getSendNotificationMarkDown(SendNotificationType sendNotification) {

        Optional<DynamicFixedListType> sendNotificationSelectHearing
                = Optional.ofNullable(sendNotification.getSendNotificationSelectHearing());

        return String.format(RESPONSE_DETAILS, Strings.nullToEmpty(sendNotification.getSendNotificationTitle()),
                Strings.nullToEmpty(sendNotification.getSendNotificationSubject().toString()),
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
            "caseNumber", caseDetails.getCaseData().getEthosCaseReference(),
            "environmentUrl", envUrl + caseDetails.getCaseId()
        );
    }
}
