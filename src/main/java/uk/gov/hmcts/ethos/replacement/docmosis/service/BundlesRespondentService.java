package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;

import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.formatLocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundlesRespondentService {

    /**
     * Clear interface data from caseData.
     * @param caseData contains all the case data
     */
    public void clearInputData(CaseData caseData) {
        caseData.setBundlesRespondentPrepareDocNotesShow(null);
        caseData.setBundlesRespondentAgreedDocWith(null);
        caseData.setBundlesRespondentAgreedDocWithBut(null);
        caseData.setBundlesRespondentAgreedDocWithNo(null);
    }

    /**
     * Populates select hearing field with available hearings.
     */
    public void populateSelectHearings(CaseData caseData) {
        DynamicFixedListType listType = DynamicFixedListType.from(caseData.getHearingCollection().stream()
                .map(this::createValueType)
                .collect(Collectors.toList())
        );

        caseData.setBundlesRespondentSelectHearing(listType);
    }

    private DynamicValueType createValueType(HearingTypeItem hearingTypeItem) {
        var earliestHearing = HearingsHelper.mapEarliest(hearingTypeItem);
        if (earliestHearing == null) {
            return null;
        }

        HearingType value = hearingTypeItem.getValue();
        String label = String.format("%s %s - %s - %s",
                value.getHearingNumber(),
                value.getHearingType(),
                HearingsHelper.getHearingVenue(value),
                formatLocalDate(earliestHearing.getValue().getListedDate())
        );

        return DynamicValueType.create(value.getHearingNumber(), label);
    }

}
