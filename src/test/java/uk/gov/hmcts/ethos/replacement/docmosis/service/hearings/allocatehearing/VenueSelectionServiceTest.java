package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class VenueSelectionServiceTest {

    @Test
    void testInitHearingCollectionNoHearings() {
        TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
        VenueService venueService = mockVenueService(tribunalOffice);
        CaseData caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);

        VenueSelectionService venueSelectionService = new VenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(1, caseData.getHearingCollection().size());
        verifyNoSelectedVenue(caseData.getHearingCollection());
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    void testInitHearingCollectionWithHearings() {
        TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
        VenueService venueService = mockVenueService(tribunalOffice);
        CaseData caseData = createCaseDataWithHearings(tribunalOffice, null);

        VenueSelectionService venueSelectionService = new VenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(3, caseData.getHearingCollection().size());
        verifyNoSelectedVenue(caseData.getHearingCollection());
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    void testInitHearingCollectionWithHearingsAndSelectedVenue() {
        TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
        VenueService venueService = mockVenueService(tribunalOffice);
        DynamicValueType selectedVenue = DynamicValueType.create("venue2", "Venue 2");
        CaseData caseData = createCaseDataWithHearings(tribunalOffice, selectedVenue);

        VenueSelectionService venueSelectionService = new VenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(3, caseData.getHearingCollection().size());
        verifySelectedVenue(caseData.getHearingCollection(), selectedVenue);
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    void testCreateVenueSelectionNoSelectedVenue() {
        TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
        VenueService venueService = mockVenueService(tribunalOffice);
        DateListedType selectedListing = createSelectedListing(null);

        VenueSelectionService venueSelectionService = new VenueSelectionService(venueService);
        DynamicFixedListType actualResult = venueSelectionService.createVenueSelection(tribunalOffice, selectedListing);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "venue", "Venue ");
    }

    @Test
    void testCreateVenueSelectionWithSelectedVenue() {
        TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
        VenueService venueService = mockVenueService(tribunalOffice);
        DynamicValueType selectedVenue = DynamicValueType.create("venue2", "Venue 2");
        DateListedType selectedListing = createSelectedListing(selectedVenue);

        VenueSelectionService venueSelectionService = new VenueSelectionService(venueService);
        DynamicFixedListType actualResult = venueSelectionService.createVenueSelection(tribunalOffice, selectedListing);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, "venue", "Venue ", selectedVenue);
    }

    private VenueService mockVenueService(TribunalOffice tribunalOffice) {
        VenueService venueService = mock(VenueService.class);
        List<DynamicValueType> venues = SelectionServiceTestUtils.createListItems("venue", "Venue ");
        when(venueService.getVenues(tribunalOffice)).thenReturn(venues);

        return venueService;
    }

    private DateListedType createSelectedListing(DynamicValueType selectedVenue) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setValue(selectedVenue);

        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingVenueDay(dynamicFixedListType);
        return dateListedType;
    }

    private CaseData createCaseDataWithHearings(TribunalOffice tribunalOffice, DynamicValueType selectedVenue) {
        CaseData caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);
        caseData.setHearingCollection(new ArrayList<>());
        HearingType hearingType = new HearingType();
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        for (int i = 0; i < 3; i++) {
            if (selectedVenue != null) {
                dynamicFixedListType.setValue(selectedVenue);
                hearingType.setHearingVenue(dynamicFixedListType);
            }
            hearingTypeItem.setValue(hearingType);
            caseData.getHearingCollection().add(hearingTypeItem);
        }

        return caseData;
    }

    private void verifyNoSelectedVenue(List<HearingTypeItem> hearings) {
        for (HearingTypeItem hearingTypeItem : hearings) {
            assertNull(hearingTypeItem.getValue().getHearingVenue().getValue());
            assertNull(hearingTypeItem.getValue().getHearingVenue().getSelectedCode());
            assertNull(hearingTypeItem.getValue().getHearingVenue().getSelectedLabel());
        }
    }

    private void verifySelectedVenue(List<HearingTypeItem> hearings, DynamicValueType selectedVenue) {
        for (HearingTypeItem hearingTypeItem : hearings) {
            assertEquals(selectedVenue.getCode(), hearingTypeItem.getValue().getHearingVenue().getValue().getCode());
            assertEquals(selectedVenue.getLabel(), hearingTypeItem.getValue().getHearingVenue().getValue().getLabel());
            assertEquals(selectedVenue.getCode(), hearingTypeItem.getValue().getHearingVenue().getSelectedCode());
            assertEquals(selectedVenue.getLabel(), hearingTypeItem.getValue().getHearingVenue().getSelectedLabel());
        }
    }

    private void verifyVenueListItems(List<HearingTypeItem> hearings) {
        for (HearingTypeItem hearing : hearings) {
            HearingType hearingType = hearing.getValue();
            DynamicFixedListType venues = hearingType.getHearingVenue();
            SelectionServiceTestUtils.verifyListItems(venues.getListItems(), "venue", "Venue ");
        }
    }
}
