package uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DynamicListHelper;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

public final class DynamicLetters {
    private DynamicLetters() {
    }

    public static void dynamicLetters(CaseData caseData, String caseTypeId) {
        List<DynamicValueType> listItems = DynamicListHelper.createDynamicHearingList(caseData);
        if (CollectionUtils.isNotEmpty(listItems)) {
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            dynamicFixedListType.setListItems(listItems);
            if (caseTypeId.equals(SCOTLAND_CASE_TYPE_ID)) {
                CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
                correspondenceScotType.setDynamicHearingNumber(dynamicFixedListType);
                caseData.setCorrespondenceScotType(correspondenceScotType);
            } else {
                CorrespondenceType correspondenceType = new CorrespondenceType();
                correspondenceType.setDynamicHearingNumber(dynamicFixedListType);
                caseData.setCorrespondenceType(correspondenceType);
            }
        }
    }

    public static void dynamicMultipleLetters(SubmitEvent submitEvent, MultipleData multipleData, String caseTypeId) {
        List<DynamicValueType> listItems = DynamicListHelper.createDynamicHearingList(submitEvent.getCaseData());
        if (CollectionUtils.isNotEmpty(listItems)) {
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            dynamicFixedListType.setListItems(listItems);
            if (caseTypeId.equals(SCOTLAND_BULK_CASE_TYPE_ID)) {
                CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
                correspondenceScotType.setDynamicHearingNumber(dynamicFixedListType);
                multipleData.setCorrespondenceScotType(correspondenceScotType);
            } else {
                CorrespondenceType correspondenceType = new CorrespondenceType();
                correspondenceType.setDynamicHearingNumber(dynamicFixedListType);
                multipleData.setCorrespondenceType(correspondenceType);
            }
        }
    }
}
