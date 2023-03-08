
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
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.SendNotificationTypeData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

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

    public SendNotificationType getSendNotification(CaseData caseData) {
        Optional<SendNotificationTypeItem> sendNotificationTypeItemOptional = caseData.getSendNotificationCollection().stream()
                .filter(s -> s.getId().equals(caseData.getSelectNotificationDropdown().getSelectedCode()))
                .findFirst();

        if (sendNotificationTypeItemOptional.isEmpty()){
            //TODO error handling
            return null;
        }

        SendNotificationType sendNotificationType = sendNotificationTypeItemOptional.get().getValue();
        return sendNotificationType;
    }

    public String getSendNotificationMarkDown(SendNotificationType sendNotification) {
        StringBuilder markdownBuilder = new StringBuilder();
        markdownBuilder.append("| | |\r\n");
        markdownBuilder.append("| --- | --- |\r\n");
        markdownBuilder.append("| Subject |" + Strings.nullToEmpty(sendNotification.getSendNotificationTitle()) + "|\r\n");
        markdownBuilder.append("| Notification |" + sendNotification.getSendNotificationSubject() + "|\r\n");
        markdownBuilder.append("| Hearing |" + sendNotification.getSendNotificationSelectHearing().getSelectedLabel() + "|\r\n");
        markdownBuilder.append("| Date Sent |" + "01 Jan 1970" + "|\r\n");
        markdownBuilder.append("| Sent By |" + "TEST PERSON" + "|\r\n");
        markdownBuilder.append("| Case management order request |" + Strings.nullToEmpty(sendNotification.getSendNotificationCaseManagement()) + "|\r\n");
        markdownBuilder.append("| Response due |" + "01 Jan 1970" + "|\r\n");
        markdownBuilder.append("| Party or parties to respond |" + Strings.nullToEmpty(sendNotification.getSendNotificationSelectParties()) + "|\r\n");
        markdownBuilder.append("| Additional Infomation |" + Strings.nullToEmpty(sendNotification.getSendNotificationAdditionalInfo()) + "|\r\n");
        markdownBuilder.append("| Desciption |" + Strings.nullToEmpty(sendNotification.getSendNotificationTitle()) + "|\r\n");
        markdownBuilder.append("| Document |" + getSendNotificationUploadDocument(sendNotification) + "|\r\n");
        markdownBuilder.append("| Case management order made by |" + Strings.nullToEmpty(sendNotification.getSendNotificationRequestMadeBy()) + "|\r\n");
        markdownBuilder.append("| Name |" + Strings.nullToEmpty(sendNotification.getSendNotificationFullName()) + "|\r\n");
        markdownBuilder.append("| Sent to |" + Strings.nullToEmpty(sendNotification.getSendNotificationSelectParties()) + "|\r\n");
        return markdownBuilder.toString();
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
