package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings;

import org.junit.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HearingSelectionServiceTest {

    @Test
    public void testGetHearingSelection() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        List<DynamicValueType> actualResult = hearingSelectionService.getHearingSelection(caseData);

        assertEquals(3, actualResult.size());
        assertEquals("id1", actualResult.get(0).getCode());
        assertEquals("Hearing 1, 1 January 1970 10:00", actualResult.get(0).getLabel());
        assertEquals("id2", actualResult.get(1).getCode());
        assertEquals("Hearing 1, 2 January 1970 10:00", actualResult.get(1).getLabel());
        assertEquals("id3", actualResult.get(2).getCode());
        assertEquals("Hearing 2, 3 January 1970 10:00", actualResult.get(2).getLabel());
    }

    @Test
    public void testGetSelectedHearing() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        HearingType selectedHearing = hearingSelectionService.getSelectedHearing(caseData,
            new DynamicFixedListType("id1"));
        assertEquals("1", selectedHearing.getHearingNumber());
        selectedHearing = hearingSelectionService.getSelectedHearing(caseData, new DynamicFixedListType("id2"));
        assertEquals("1", selectedHearing.getHearingNumber());
        selectedHearing = hearingSelectionService.getSelectedHearing(caseData, new DynamicFixedListType("id3"));
        assertEquals("2", selectedHearing.getHearingNumber());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSelectedHearingNotFound() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        hearingSelectionService.getSelectedHearing(caseData, new DynamicFixedListType("id4"));

        fail("No hearing should be found");
    }

    @Test
    public void getSelectedListing() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        DateListedType selectedListing = hearingSelectionService.getSelectedListing(caseData,
            new DynamicFixedListType("id1"));
        assertEquals("1970-01-01T10:00:00.000", selectedListing.getListedDate());
        selectedListing = hearingSelectionService.getSelectedListing(caseData, new DynamicFixedListType("id2"));
        assertEquals("1970-01-02T10:00:00.000", selectedListing.getListedDate());
        selectedListing = hearingSelectionService.getSelectedListing(caseData, new DynamicFixedListType("id3"));
        assertEquals("1970-01-03T10:00:00.000", selectedListing.getListedDate());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSelectedListingNotFound() {
        CaseData caseData = createCaseData();

        HearingSelectionService hearingSelectionService = new HearingSelectionService();
        hearingSelectionService.getSelectedListing(caseData, new DynamicFixedListType("id4"));
        fail("No listing should be found");
    }

    private CaseData createCaseData() {
        CaseData caseData = new CaseData();

        List<HearingTypeItem> hearings = List.of(
                createHearing("1", List.of(createListing("id1", "1970-01-01T10:00:00.000"),
                        createListing("id2", "1970-01-02T10:00:00.000"))),
                createHearing("2", List.of(createListing("id3", "1970-01-03T10:00:00.000")))
        );

        caseData.setHearingCollection(hearings);

        return caseData;
    }

    private HearingTypeItem createHearing(String hearingNumber, List<DateListedTypeItem> listings) {
        HearingType hearing = new HearingType();
        hearing.setHearingNumber(hearingNumber);
        hearing.setHearingDateCollection(listings);
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
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
