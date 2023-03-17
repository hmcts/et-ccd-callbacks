package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

@Service
public class HearingSelectionService {

    public List<DynamicValueType> getHearingSelection(CaseData caseData) {
        List<DynamicValueType> values = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                //for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
                    String code = hearing.getId();

                    //String date = UtilHelper.formatLocalDateTime(listing.getValue().getListedDate());
                    String label = String.format("Hearing %s", hearing.getValue().getHearingNumber());
                    values.add(DynamicValueType.create(code, label));
                }
            }
        //}

        return values;
    }

    public HearingType getSelectedHearing(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
        String id = dynamicFixedListType.getValue().getCode();
        for (HearingTypeItem hearing : caseData.getHearingCollection()) {
           // for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
                if (hearing.getId().equals(id)) {
                    return hearing.getValue();
                }
           // }
        }

        throw new IllegalStateException(String.format("Selected hearing %s not found in case %s",
                dynamicFixedListType.getValue().getLabel(), caseData.getEthosCaseReference()));
    }

    public List<DateListedTypeItem> getListings(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
        String id = dynamicFixedListType.getValue().getCode();
        for (HearingTypeItem hearing : caseData.getHearingCollection()) {
            if (hearing.getId().equals(id)) {
                return hearing.getValue().getHearingDateCollection();
            }
        }
        throw new IllegalStateException(String.format("Listings %s not found for selected hearing in case %s",
                dynamicFixedListType.getValue().getLabel(), caseData.getEthosCaseReference()));
    }
}
