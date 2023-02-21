
package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotificationService {

    private final HearingSelectionService hearingSelectionService;

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


    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData) {
        List<DynamicValueType> values = new ArrayList<>();
        List<SendNotificationTypeItem> sendNotificationTypeItemList = caseData.getSendNotificationCollection();
        if (CollectionUtils.isEmpty(sendNotificationTypeItemList)) {
            return values;
        }
        for (SendNotificationTypeItem sendNotificationType : sendNotificationTypeItemList) {
            String notificationId = sendNotificationType.getId();
            String notificationTitle = sendNotificationType.getValue().getSendNotificationTitle();
            String notificationSubject = sendNotificationType.getValue().getSendNotificationSubject().toString();
            String label = String.format("%s - %s", notificationTitle, notificationSubject);
            values.add(DynamicValueType.create(notificationId, label));
        }
        return values;

    }
}
