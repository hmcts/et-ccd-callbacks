package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.util.ArrayList;
import java.util.List;

@Service
public class HearingSelectionService {

    public List<DynamicValueType> getHearingSelection(CaseData caseData) {
        var values = new ArrayList<DynamicValueType>();

        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (var hearing : caseData.getHearingCollection()) {
                for (var listing : hearing.getValue().getHearingDateCollection()) {
                    var code = listing.getId();

                    var date = UtilHelper.formatLocalDateTime(listing.getValue().getListedDate());
                    var label = String.format("Hearing %s, %s", hearing.getValue().getHearingNumber(), date);
                    values.add(DynamicValueType.create(code, label));
                }
            }
        }

        return values;
    }

    public HearingType getSelectedHearing(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
        var id = dynamicFixedListType.getValue().getCode();
        for (var hearing : caseData.getHearingCollection()) {
            for (var listing : hearing.getValue().getHearingDateCollection()) {
                if (listing.getId().equals(id)) {
                    return hearing.getValue();
                }
            }
        }

        throw new IllegalStateException(String.format("Selected hearing %s not found in case %s",
                dynamicFixedListType.getValue().getLabel(), caseData.getEthosCaseReference()));
    }

    public DateListedType getSelectedListing(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
        var id = dynamicFixedListType.getValue().getCode();
        for (var hearing : caseData.getHearingCollection()) {
            for (var listing : hearing.getValue().getHearingDateCollection()) {
                if (listing.getId().equals(id)) {
                    return listing.getValue();
                }
            }
        }

        throw new IllegalStateException(String.format("Selected listing %s not found in case %s",
                dynamicFixedListType.getValue().getLabel(), caseData.getEthosCaseReference()));
    }

}
