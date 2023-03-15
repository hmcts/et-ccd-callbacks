package uk.gov.hmcts.ethos.replacement.docmosis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

@Service
@RequiredArgsConstructor
@Slf4j
public class RespondNotificationService {

    private final SendNotificationService sendNotificationService;

    public void populateSendNotificationSelection(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(getSendNotificationSelection(caseData));
        caseData.setSelectNotificationDropdown(dynamicFixedListType);
    }

    public void createResponseNotification(CaseData caseData) {
        Optional<SendNotificationTypeItem> sendNotificationTypeItemOptional = sendNotificationService.getSendNotification(caseData);
        if(sendNotificationTypeItemOptional.isEmpty()) {
            return;
        }
        SendNotificationType sendNotificationType = sendNotificationTypeItemOptional.get().getValue();

        if (sendNotificationType.getRespondNotificationTypeCollection() == null) {
            sendNotificationType.setRespondNotificationTypeCollection(new ArrayList<>());
        }

        RespondNotificationType respondNotificationType = new RespondNotificationType();
        respondNotificationType.setRespondNotificationTitle(caseData.getRespondNotificationTitle());
        respondNotificationType.setRespondNotificationAdditionalInfo(caseData.getRespondNotificationAdditionalInfo());
        respondNotificationType.setRespondNotificationUploadDocument(caseData.getRespondNotificationUploadDocument());
        respondNotificationType.setRespondNotificationResponseRequired(
            caseData.getRespondNotificationResponseRequired());
        respondNotificationType.setRespondNotificationWhoRespond(caseData.getRespondNotificationWhoRespond());
        respondNotificationType.setRespondNotificationCaseManagementMadeBy(
            caseData.getRespondNotificationCaseManagementMadeBy());
        respondNotificationType.setRespondNotificationFullName(caseData.getRespondNotificationFullName());
        respondNotificationType.setRespondNotificationPartyToNotify(caseData.getRespondNotificationPartyToNotify());

        sendNotificationType.getRespondNotificationTypeCollection().add(respondNotificationType);
    }

    public void clearResponseNotificationFields(CaseData caseData) {
        caseData.setRespondNotificationTitle(null);
        caseData.setRespondNotificationAdditionalInfo(null);
        caseData.setRespondNotificationUploadDocument(null);
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

    public List<DynamicValueType> getRespondNotificationSelection(SendNotificationType sendNotificationType) {

    }

    public String getNotificationMarkDown(CaseData caseData) {
        Optional<SendNotificationTypeItem> sendNotification = sendNotificationService.getSendNotification(caseData);
        if (sendNotification.isEmpty()) {
            return "";
        }
        return sendNotificationService.getSendNotificationMarkDown(sendNotification.get().getValue());
    }
}
