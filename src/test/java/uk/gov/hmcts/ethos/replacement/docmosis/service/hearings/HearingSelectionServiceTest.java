package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings;

import org.junit.jupiter.api.Test;
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

public class HearingSelectionServiceTest {

    @Test
    public void testGetHearingSelectionSortedByDateTime() {
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
    public void testGetHearingSelection() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        List<DynamicValueType> actualResult = hearingSelectionService.getHearingSelection(caseData);

        assertEquals(4, actualResult.size());
        assertEquals("id1", actualResult.get(0).getCode());
        assertEquals("Hearing 1", actualResult.get(0).getLabel());
        assertEquals("id2", actualResult.get(1).getCode());
        assertEquals("Hearing 2", actualResult.get(1).getLabel());
        assertEquals("id3", actualResult.get(2).getCode());
        assertEquals("Hearing 3", actualResult.get(2).getLabel());
    }

    @Test
    public void testGetSelectedHearing() {
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
    public void testGetSelectedHearingNotFound() {
        CaseData caseData = createCaseData();
        HearingSelectionService hearingSelectionService = new HearingSelectionService();

        assertThrows(IllegalStateException.class, () ->
                hearingSelectionService.getSelectedHearing(caseData, new DynamicFixedListType("id5"))
        );
    }

    @Test
    public void getSelectedListing() {
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
    public void testGetListingsNotFound() {
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

    private DateListedTypeItem createListing(String id, String listedDate) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(id);
        dateListedTypeItem.setValue(dateListedType);
        return dateListedTypeItem;
    }
}
