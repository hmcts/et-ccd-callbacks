package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;

@ExtendWith(SpringExtension.class)
class HearingSelectionServiceTest {

    @Test
    void testGetHearingSelectionSortedByDateTime() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        List<DynamicValueType> actualResult = hearingSelectionService.getHearingSelectionSortedByDateTime(caseData);

        assertEquals(4, actualResult.size());
        assertEquals("id1", actualResult.get(0).getCode());
        assertEquals("1: null - null - 1 January 1970 10:00", actualResult.get(0).getLabel());
        assertEquals("id3", actualResult.get(1).getCode());
        assertEquals("2: null - null - 3 January 1970 10:00", actualResult.get(1).getLabel());
        assertEquals("id2", actualResult.get(2).getCode());
        assertEquals("3: null - null - 6 January 1970 10:00", actualResult.get(2).getLabel());
        assertEquals("id3", actualResult.get(3).getCode());
        assertEquals("4: null - null - 7 January 1970 10:00", actualResult.get(3).getLabel());
    }

    @Test
    void testGetHearingSelection() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        List<DynamicValueType> actualResult = hearingSelectionService.getHearingDetailsSelection(caseData);

        assertEquals(4, actualResult.size());
        assertEquals("id1", actualResult.get(0).getCode());
        assertEquals("Hearing 1", actualResult.get(0).getLabel());
        assertEquals("id2", actualResult.get(1).getCode());
        assertEquals("Hearing 2", actualResult.get(1).getLabel());
        assertEquals("id3", actualResult.get(2).getCode());
        assertEquals("Hearing 3", actualResult.get(2).getLabel());
    }

    @Test
    void testGetHearingSelection_withListed_displayDate() {
        CaseData caseData = new CaseData();
        List<HearingTypeItem> hearings = List.of(
            createHearing("id1", "1", List.of(
                createListingWithStatus("id1h1", "2024-01-11T10:00:00.000", HEARING_STATUS_LISTED),
                createListingWithStatus("id1h2", "2024-01-12T10:00:00.000", HEARING_STATUS_LISTED))),
            createHearing("id2", "2", List.of(
                createListingWithStatus("id2h1", "2024-02-11T10:00:00.000", HEARING_STATUS_LISTED),
                createListingWithStatus("id2h2", "2024-02-10T10:00:00.000", HEARING_STATUS_LISTED))),
            createHearing("id3", "3", List.of(
                createListingWithStatus("id3h1", "2024-03-11T10:00:00.000", HEARING_STATUS_POSTPONED),
                createListingWithStatus("id3h2", "2024-03-12T10:00:00.000", HEARING_STATUS_LISTED))),
            createHearing("id4", "4", List.of(
                createListingWithStatus("id4h1", "2024-04-11T10:00:00.000", HEARING_STATUS_POSTPONED),
                createListingWithStatus("id4h2", "2024-04-12T10:00:00.000", HEARING_STATUS_POSTPONED)))
        );
        caseData.setHearingCollection(hearings);

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        List<DynamicValueType> actualResult = hearingSelectionService.getHearingDetailsSelection(caseData);

        assertEquals(4, actualResult.size());
        assertEquals("id1", actualResult.get(0).getCode());
        assertEquals("Hearing 1, 11 January 2024 10:00", actualResult.get(0).getLabel());
        assertEquals("id2", actualResult.get(1).getCode());
        assertEquals("Hearing 2, 11 February 2024 10:00", actualResult.get(1).getLabel());
        assertEquals("id3", actualResult.get(2).getCode());
        assertEquals("Hearing 3, 12 March 2024 10:00", actualResult.get(2).getLabel());
        assertEquals("id4", actualResult.get(3).getCode());
        assertEquals("Hearing 4", actualResult.get(3).getLabel());
    }

    @Test
    void testGetSelectedHearing() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        HearingType selectedHearing = hearingSelectionService.getSelectedHearing(caseData,
            new DynamicFixedListType("id1"));
        assertEquals("1", selectedHearing.getHearingNumber());
        selectedHearing = hearingSelectionService.getSelectedHearing(caseData, new DynamicFixedListType("id2"));
        assertEquals("2", selectedHearing.getHearingNumber());
        selectedHearing = hearingSelectionService.getSelectedHearing(caseData, new DynamicFixedListType("id3"));
        assertEquals("3", selectedHearing.getHearingNumber());
    }

    @Test
    void testGetSelectedHearingNotFound() {
        CaseData caseData = createCaseData();
        HearingSelectionService hearingSelectionService = new HearingSelectionService();

        assertThrows(IllegalStateException.class, () ->
                hearingSelectionService.getSelectedHearing(caseData, new DynamicFixedListType("id5"))
        );
    }

    @Test
    void getSelectedListing() {
        CaseData caseData = createCaseData();
        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        caseData.setAllocateHearingHearing(new DynamicFixedListType("id1"));
        DateListedType selectedListing = hearingSelectionService.getSelectedListing(caseData);
        assertEquals("1970-01-01T10:00:00.000", selectedListing.getListedDate());
        caseData.setAllocateHearingHearing(new DynamicFixedListType("id2"));
        selectedListing = hearingSelectionService.getSelectedListing(caseData);
        assertEquals("1970-01-06T10:00:00.000", selectedListing.getListedDate());
        caseData.setAllocateHearingHearing(new DynamicFixedListType("id3"));
        selectedListing = hearingSelectionService.getSelectedListing(caseData);
        assertEquals("1970-01-07T10:00:00.000", selectedListing.getListedDate());
    }

    @Test
    void testGetListingsNotFound() {
        CaseData caseData = createCaseData();
        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        caseData.setAllocateHearingHearing(new DynamicFixedListType("id5"));

        assertThrows(IllegalStateException.class, () ->
                hearingSelectionService.getSelectedListing(caseData)
        );
    }

    private CaseData createCaseData() {
        CaseData caseData = new CaseData();

        List<HearingTypeItem> hearings = List.of(
                createHearing("id1", "1", List.of(createListing("id2", "1970-01-06T10:00:00.000"))),
                createHearing("id2", "2", List.of(createListing("id1", "1970-01-01T10:00:00.000"))),
                createHearing("id3", "3", List.of(createListing("id3", "1970-01-07T10:00:00.000"))),
                createHearing("id4", "3", List.of(createListing("id3", "1970-01-03T10:00:00.000")))
        );

        caseData.setHearingCollection(hearings);

        return caseData;
    }

    private HearingTypeItem createHearing(String id, String hearingNumber, List<DateListedTypeItem> listings) {
        HearingType hearing = new HearingType();
        hearing.setHearingNumber(hearingNumber);
        hearing.setHearingDateCollection(listings);
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId(id);
        hearingTypeItem.setValue(hearing);
        return hearingTypeItem;
    }

    private DateListedTypeItem createListingWithStatus(String id, String listedDate, String status) {
        DateListedTypeItem dateListedTypeItem = createListing(id, listedDate);
        dateListedTypeItem.getValue().setHearingStatus(status);
        return dateListedTypeItem;
    }

    private DateListedTypeItem createListing(String id, String listedDate) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(id);
        dateListedTypeItem.setValue(dateListedType);
        return dateListedTypeItem;
    }
}
