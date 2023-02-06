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

    public List<DynamicValueType> getHearingSelection(CaseData caseData, String format) {
        List<DynamicValueType> values = new ArrayList<>();
        int index = 1;

        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
                    String code = listing.getId();

                    String date = UtilHelper.formatLocalDateTime(listing.getValue().getListedDate());
                    String label = String.format(format,
                            index,
                            hearing.getValue().getHearingType(),
                            hearing.getValue().getHearingVenue().getValue().getLabel(),
                            date);
                    values.add(DynamicValueType.create(code, label));
                    index++;
                }
            }
        }
        return values;
    }

    public List<DynamicValueType> getHearingSelection(CaseData caseData) {
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

    public HearingType getSelectedHearing(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
        String id = dynamicFixedListType.getValue().getCode();
        for (HearingTypeItem hearing : caseData.getHearingCollection()) {
            for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
                if (listing.getId().equals(id)) {
                    return hearing.getValue();
                }
            }
        }

        throw new IllegalStateException(String.format("Selected hearing %s not found in case %s",
                dynamicFixedListType.getValue().getLabel(), caseData.getEthosCaseReference()));
    }

    public DateListedType getSelectedListing(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
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
