package uk.gov.hmcts.ethos.replacement.docmosis.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RespondNotificationService {

    private final SendNotificationService sendNotificationService;

    public void populateSendNotificationSelection(CaseData caseData){
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(getSendNotificationSelection(caseData));
        caseData.setSelectNotificationDropdown(dynamicFixedListType);

    }

    public List<DynamicValueType> getSendNotificationSelection(CaseData caseData) {
        return sendNotificationService.getSendNotificationSelection(caseData);

    }
}
