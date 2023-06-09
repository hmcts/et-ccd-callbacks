package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import java.util.ArrayList;
import java.util.List;

@Service
public class HearingSelectionService {

    public List<DynamicValueType> getHearingSelection(CaseData caseData) {
        List<DynamicValueType> values = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                String code = hearing.getId();
                String label = String.format("Hearing %s", hearing.getValue().getHearingNumber());
                values.add(DynamicValueType.create(code, label));
            }
        }
        return values;
    }

    public List<DynamicValueType> getHearingSelectionAllocateHearing(CaseData caseData) {
        List<DynamicValueType> values = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
                    String code = listing.getId();
                    String date = UtilHelper.formatLocalDateTime(listing.getValue().getListedDate());
                    String label = String.format("Hearing %s, %s", hearing.getValue().getHearingNumber(), date);
                    values.add(DynamicValueType.create(code, label));
                }
            }
        }
        return values;
    }

    public HearingType getSelectedHearingAllocateHearing(CaseData caseData) {
        String id = caseData.getAllocateHearingHearing().getValue().getCode();
        for (HearingTypeItem hearing : caseData.getHearingCollection()) {
            for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
                if (listing.getId().equals(id)) {
                    return hearing.getValue();
                }
            }
        }
        throw new IllegalStateException(String.format("Selected hearing %s not found in case %s",
            caseData.getAllocateHearingHearing().getValue().getLabel(), caseData.getEthosCaseReference()));
    }

    public HearingType getSelectedHearing(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
        String id = dynamicFixedListType.getValue().getCode();
        for (HearingTypeItem hearing : caseData.getHearingCollection()) {
            if (hearing.getId().equals(id)) {
                return hearing.getValue();
            }
        }
        throw new IllegalStateException(String.format("Selected hearing %s not found in case %s",
                dynamicFixedListType.getValue().getLabel(), caseData.getEthosCaseReference()));
    }

    public List<DateListedTypeItem> getDateListedItemsFromSelectedHearing(CaseData caseData,
                                                                         DynamicFixedListType dynamicFixedListType) {
        return getSelectedHearing(caseData, dynamicFixedListType).getHearingDateCollection();
    }

    public DateListedType getSelectedListing(CaseData caseData) {
        DynamicFixedListType dynamicFixedListType = caseData.getAllocateHearingHearing();
        String id = dynamicFixedListType.getValue().getCode();
        for (HearingTypeItem hearing : caseData.getHearingCollection()) {
            for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
                if (listing.getId().equals(id)) {
                    return listing.getValue();
                }
            }
        }
        throw new IllegalStateException(String.format("Selected listing %s not found in case %s",
                dynamicFixedListType.getValue().getLabel(), caseData.getEthosCaseReference()));
    }
}
