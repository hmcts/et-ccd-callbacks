package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ListedHearingData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;

@Service
public class HearingSelectionService {

    private static final String HEARING_FORMAT = "%s: %s - %s - %s";

    /**
     * Returns hearing list sorted by datetime only.
     */
    public List<DynamicValueType> getHearingSelectionSortedByDateTime(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return null;
        }

        List<DynamicValueType> values = new ArrayList<>();
        int index = 1;

        List<ListedHearingData> hearingList = caseData.getHearingCollection().stream()
            .flatMap(hearing -> generateHearingList(hearing).stream())
            .toList();

        List<ListedHearingData> sortedHearingList = hearingList.stream()
                .sorted(Comparator.comparing((ListedHearingData h) -> LocalDateTime.parse(h.getListedDate())))
                .toList();

        for (ListedHearingData listing : sortedHearingList) {
            String code = listing.getListedId();

            DynamicFixedListType hearingVenue = listing.getHearingVenue();

            String venue = hearingVenue == null ? listing.getHearingVenueScotland() :
                    hearingVenue.getValue().getLabel();

            String date = UtilHelper.formatLocalDateTime(listing.getListedDate());
            String label = String.format(
                    HEARING_FORMAT,
                    index,
                    listing.getHearingType(),
                    venue,
                    date);
            values.add(DynamicValueType.create(code, label));
            index++;
        }

        return values;
    }

    private static List<ListedHearingData> generateHearingList(HearingTypeItem hearing) {
        HearingType hearingValue = hearing.getValue();
        return hearingValue.getHearingDateCollection().stream().map(h ->
                new ListedHearingData(
                    hearingValue.getHearingType(),
                    hearingValue.getHearingVenue(),
                    hearingValue.getHearingVenueScotland(),
                    h.getId(),
                    h.getValue().getListedDate()
                )
            ).toList();
    }

    public List<DynamicValueType> getHearingDetailsSelection(CaseData caseData) {
        List<DynamicValueType> values = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearing : caseData.getHearingCollection()) {
                String code = hearing.getId();
                String label = getHearingListWithDateWhenListed(hearing);
                values.add(DynamicValueType.create(code, label));
            }
        }
        return values;
    }

    private static String getHearingListWithDateWhenListed(HearingTypeItem hearing) {
        for (DateListedTypeItem listing : hearing.getValue().getHearingDateCollection()) {
            if (HEARING_STATUS_LISTED.equals(listing.getValue().getHearingStatus())) {
                String date = UtilHelper.formatLocalDateTime(listing.getValue().getListedDate());
                return String.format("Hearing %s, %s", hearing.getValue().getHearingNumber(), date);
            }
        }
        return String.format("Hearing %s", hearing.getValue().getHearingNumber());
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
        return getSelectedListingWithList(caseData, dynamicFixedListType);
    }

    public DateListedType getSelectedListingWithList(CaseData caseData, DynamicFixedListType dynamicFixedListType) {
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
