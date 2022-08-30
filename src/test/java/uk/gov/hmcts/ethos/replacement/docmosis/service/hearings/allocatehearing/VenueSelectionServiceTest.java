package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.junit.Test;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VenueSelectionServiceTest {

    @Test
    public void testInitHearingCollectionNoHearings() {
        var tribunalOffice = TribunalOffice.ABERDEEN;
        var venueService = mockVenueService(tribunalOffice);
        var caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);

        var venueSelectionService = new VenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(1, caseData.getHearingCollection().size());
        verifyNoSelectedVenue(caseData.getHearingCollection());
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    public void testInitHearingCollectionWithHearings() {
        var tribunalOffice = TribunalOffice.ABERDEEN;
        var venueService = mockVenueService(tribunalOffice);
        var caseData = createCaseDataWithHearings(tribunalOffice, null);

        var venueSelectionService = new VenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(3, caseData.getHearingCollection().size());
        verifyNoSelectedVenue(caseData.getHearingCollection());
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    public void testInitHearingCollectionWithHearingsAndSelectedVenue() {
        var tribunalOffice = TribunalOffice.ABERDEEN;
        var venueService = mockVenueService(tribunalOffice);
        var selectedVenue = DynamicValueType.create("venue2", "Venue 2");
        var caseData = createCaseDataWithHearings(tribunalOffice, selectedVenue);

        var venueSelectionService = new VenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(3, caseData.getHearingCollection().size());
        verifySelectedVenue(caseData.getHearingCollection(), selectedVenue);
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    public void testCreateVenueSelectionNoSelectedVenue() {
        var tribunalOffice = TribunalOffice.ABERDEEN;
        var venueService = mockVenueService(tribunalOffice);
        var selectedListing = createSelectedListing(null);

        var venueSelectionService = new VenueSelectionService(venueService);
        var actualResult = venueSelectionService.createVenueSelection(tribunalOffice, selectedListing);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "venue", "Venue ");
    }

    @Test
    public void testCreateVenueSelectionWithSelectedVenue() {
        var tribunalOffice = TribunalOffice.ABERDEEN;
        var venueService = mockVenueService(tribunalOffice);
        var selectedVenue = DynamicValueType.create("venue2", "Venue 2");
        var selectedListing = createSelectedListing(selectedVenue);

        var venueSelectionService = new VenueSelectionService(venueService);
        var actualResult = venueSelectionService.createVenueSelection(tribunalOffice, selectedListing);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, "venue", "Venue ", selectedVenue);
    }

    private VenueService mockVenueService(TribunalOffice tribunalOffice) {
        var venueService = mock(VenueService.class);
        var venues = SelectionServiceTestUtils.createListItems("venue", "Venue ");
        when(venueService.getVenues(tribunalOffice)).thenReturn(venues);

        return venueService;
    }

    private DateListedType createSelectedListing(DynamicValueType selectedVenue) {
        var dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setValue(selectedVenue);

        var dateListedType = new DateListedType();
        dateListedType.setHearingVenueDay(dynamicFixedListType);
        return dateListedType;
    }

    private CaseData createCaseDataWithHearings(TribunalOffice tribunalOffice, DynamicValueType selectedVenue) {
        var caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);
        caseData.setHearingCollection(new ArrayList<>());
        for (var i = 0; i < 3; i++) {
            var hearingType = new HearingType();
            if (selectedVenue != null) {
                var dynamicFixedListType = new DynamicFixedListType();
                dynamicFixedListType.setValue(selectedVenue);
                hearingType.setHearingVenue(dynamicFixedListType);
            }
            var hearingTypeItem = new HearingTypeItem();
            hearingTypeItem.setValue(hearingType);
            caseData.getHearingCollection().add(hearingTypeItem);
        }

        return caseData;
    }

    private void verifyNoSelectedVenue(List<HearingTypeItem> hearings) {
        for (var hearingTypeItem : hearings) {
            assertNull(hearingTypeItem.getValue().getHearingVenue().getValue());
            assertNull(hearingTypeItem.getValue().getHearingVenue().getSelectedCode());
            assertNull(hearingTypeItem.getValue().getHearingVenue().getSelectedLabel());
        }
    }

    private void verifySelectedVenue(List<HearingTypeItem> hearings, DynamicValueType selectedVenue) {
        for (var hearingTypeItem : hearings) {
            assertEquals(selectedVenue.getCode(), hearingTypeItem.getValue().getHearingVenue().getValue().getCode());
            assertEquals(selectedVenue.getLabel(), hearingTypeItem.getValue().getHearingVenue().getValue().getLabel());
            assertEquals(selectedVenue.getCode(), hearingTypeItem.getValue().getHearingVenue().getSelectedCode());
            assertEquals(selectedVenue.getLabel(), hearingTypeItem.getValue().getHearingVenue().getSelectedLabel());
        }
    }

    private void verifyVenueListItems(List<HearingTypeItem> hearings) {
        for (var hearing : hearings) {
            var hearingType = hearing.getValue();
            var venues = hearingType.getHearingVenue();
            SelectionServiceTestUtils.verifyListItems(venues.getListItems(), "venue", "Venue ");
        }
    }
}
