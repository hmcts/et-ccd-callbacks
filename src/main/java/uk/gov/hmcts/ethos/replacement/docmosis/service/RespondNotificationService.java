package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.util.List;
import java.util.Optional;

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

    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData) {
        return sendNotificationService.getSendNotificationSelection(caseData, (sendNotificationType) -> {
            String notificationTitle = sendNotificationType.getValue().getSendNotificationTitle();
            String notificationSubject = sendNotificationType.getValue().getSendNotificationSubject().toString();
            return String.format("%s - %s", notificationTitle, notificationSubject);
        });
    }

    public String getNotificationMarkDown(CaseData caseData) {
        Optional<SendNotificationTypeItem> sendNotification = sendNotificationService.getSendNotification(caseData);
        if (sendNotification.isEmpty()) {
            return "";
        }
        return sendNotificationService.getSendNotificationMarkDown(sendNotification.get().getValue());


    }
}
