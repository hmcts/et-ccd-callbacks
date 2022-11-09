package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.junit.Test;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScotlandVenueSelectionServiceTest {

    @Test
    public void testInitHearingCollectionNoHearings() {
        var venueService = mockVenueService();
        var caseData = new CaseData();

        var venueSelectionService = new ScotlandVenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(1, caseData.getHearingCollection().size());
        verifyNoSelectedVenue(caseData.getHearingCollection());
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    public void testInitHearingCollectionWithHearings() {
        var venueService = mockVenueService();
        var caseData = createCaseDataWithHearings();

        var venueSelectionService = new ScotlandVenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(3, caseData.getHearingCollection().size());
        verifyNoSelectedVenue(caseData.getHearingCollection());
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    public void testInitHearingCollectionWithHearingsAndSelectedVenue() {
        var venueService = mockVenueService();
        var selectedAberdeenVenue = createWithSelectedIndex(TribunalOffice.ABERDEEN, 1);
        var selectedDundeeVenue = createWithSelectedIndex(TribunalOffice.DUNDEE, 2);
        var selectedEdinburghVenue = createWithSelectedIndex(TribunalOffice.EDINBURGH, 3);
        var selectedGlasgow = createWithSelectedIndex(TribunalOffice.GLASGOW, 3);
        var caseData = createCaseDataWithSelectedHearingsAndVenue(selectedAberdeenVenue, selectedDundeeVenue,
                selectedEdinburghVenue, selectedGlasgow);

        var venueSelectionService = new ScotlandVenueSelectionService(venueService);
        venueSelectionService.initHearingCollection(caseData);

        assertEquals(3, caseData.getHearingCollection().size());
        verifySelectedVenues(caseData.getHearingCollection(), selectedAberdeenVenue, selectedDundeeVenue,
                selectedEdinburghVenue, selectedGlasgow);
        verifyVenueListItems(caseData.getHearingCollection());
    }

    @Test
    public void testCreateVenueSelectionNoSelectedVenue() {
        var venueService = mockVenueService();

        for (TribunalOffice tribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            var selectedListing = createSelectedListing(tribunalOffice, null);
            var venueSelectionService = new ScotlandVenueSelectionService(venueService);
            var actualResult = venueSelectionService.createVenueSelection(tribunalOffice, selectedListing);

            SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, tribunalOffice.getOfficeName(),
                    tribunalOffice.getOfficeName() + " ");
        }
    }

    @Test
    public void testCreateVenueSelectionWithSelectedVenue() {
        var venueService = mockVenueService();

        for (TribunalOffice tribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            var selectedVenue = createWithSelectedIndex(tribunalOffice, 2);
            var selectedListing = createSelectedListing(tribunalOffice, DynamicFixedListType.of(selectedVenue));
            var venueSelectionService = new ScotlandVenueSelectionService(venueService);
            var actualResult = venueSelectionService.createVenueSelection(tribunalOffice, selectedListing);

            SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, tribunalOffice.getOfficeName(),
                    tribunalOffice.getOfficeName() + " ", selectedVenue);
        }
    }

    private DateListedType createSelectedListing(TribunalOffice tribunalOffice, DynamicFixedListType selectedValue) {
        var dateListedType = new DateListedType();
        switch (tribunalOffice) {
            case ABERDEEN:
                dateListedType.setHearingAberdeen(selectedValue);
                break;
            case DUNDEE:
                dateListedType.setHearingDundee(selectedValue);
                break;
            case EDINBURGH:
                dateListedType.setHearingEdinburgh(selectedValue);
                break;
            case GLASGOW:
                dateListedType.setHearingGlasgow(selectedValue);
                break;
            default:
                throw new IllegalArgumentException("Unexpected Tribunal Office " + tribunalOffice.getOfficeName());
        }

        return dateListedType;
    }

    private DynamicValueType createWithSelectedIndex(TribunalOffice tribunalOffice, int index) {
        return DynamicValueType.create(tribunalOffice.getOfficeName() + index,
                tribunalOffice.getOfficeName() + " " + index);
    }

    private VenueService mockVenueService() {
        var venueService = mock(VenueService.class);

        for (TribunalOffice tribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            var venues = SelectionServiceTestUtils.createListItems(tribunalOffice.getOfficeName(),
                    tribunalOffice.getOfficeName() + " ");
            when(venueService.getVenues(tribunalOffice)).thenReturn(venues);
        }

        return venueService;
    }

    private CaseData createCaseDataWithHearings() {
        var caseData = new CaseData();
        caseData.setHearingCollection(new ArrayList<>());
        for (var i = 0; i < 3; i++) {
            var hearingType = new HearingType();
            var hearingTypeItem = new HearingTypeItem();
            hearingTypeItem.setValue(hearingType);
            caseData.getHearingCollection().add(hearingTypeItem);
        }

        return caseData;
    }

    private CaseData createCaseDataWithSelectedHearingsAndVenue(DynamicValueType selectedAberdeen,
                                                                DynamicValueType selectedDundee,
                                                                DynamicValueType selectedEdinburgh,
                                                                DynamicValueType selectedGlasgow) {
        var caseData = new CaseData();
        caseData.setHearingCollection(new ArrayList<>());
        for (var i = 0; i < 3; i++) {
            var hearingType = new HearingType();

            hearingType.setHearingAberdeen(DynamicFixedListType.of(selectedAberdeen));
            hearingType.setHearingDundee(DynamicFixedListType.of(selectedDundee));
            hearingType.setHearingEdinburgh(DynamicFixedListType.of(selectedEdinburgh));
            hearingType.setHearingGlasgow(DynamicFixedListType.of(selectedGlasgow));

            var hearingTypeItem = new HearingTypeItem();
            hearingTypeItem.setValue(hearingType);
            caseData.getHearingCollection().add(hearingTypeItem);
        }

        return caseData;
    }

    private void verifyNoSelectedVenue(List<HearingTypeItem> hearings) {
        for (var hearingTypeItem : hearings) {
            assertNull(hearingTypeItem.getValue().getHearingAberdeen().getValue());
            assertNull(hearingTypeItem.getValue().getHearingAberdeen().getSelectedCode());
            assertNull(hearingTypeItem.getValue().getHearingAberdeen().getSelectedLabel());

            assertNull(hearingTypeItem.getValue().getHearingDundee().getValue());
            assertNull(hearingTypeItem.getValue().getHearingDundee().getSelectedCode());
            assertNull(hearingTypeItem.getValue().getHearingDundee().getSelectedLabel());

            assertNull(hearingTypeItem.getValue().getHearingEdinburgh().getValue());
            assertNull(hearingTypeItem.getValue().getHearingEdinburgh().getSelectedCode());
            assertNull(hearingTypeItem.getValue().getHearingEdinburgh().getSelectedLabel());

            assertNull(hearingTypeItem.getValue().getHearingGlasgow().getValue());
            assertNull(hearingTypeItem.getValue().getHearingGlasgow().getSelectedCode());
            assertNull(hearingTypeItem.getValue().getHearingGlasgow().getSelectedLabel());
        }
    }

    private void verifySelectedVenues(List<HearingTypeItem> hearings, DynamicValueType selectedAberdeen,
                                      DynamicValueType selectedDundee, DynamicValueType selectedEdinburgh,
                                      DynamicValueType selectedGlasgow) {
        for (var hearingTypeItem : hearings) {
            verifySelectedVenue(hearingTypeItem.getValue().getHearingAberdeen(), selectedAberdeen);
            verifySelectedVenue(hearingTypeItem.getValue().getHearingDundee(), selectedDundee);
            verifySelectedVenue(hearingTypeItem.getValue().getHearingEdinburgh(), selectedEdinburgh);
            verifySelectedVenue(hearingTypeItem.getValue().getHearingGlasgow(), selectedGlasgow);
        }

    }

    private void verifySelectedVenue(DynamicFixedListType hearing, DynamicValueType selectedVenue) {
        assertEquals(selectedVenue.getCode(), hearing.getValue().getCode());
        assertEquals(selectedVenue.getLabel(), hearing.getValue().getLabel());
        assertEquals(selectedVenue.getCode(), hearing.getSelectedCode());
        assertEquals(selectedVenue.getLabel(), hearing.getSelectedLabel());
    }

    private void verifyVenueListItems(List<HearingTypeItem> hearings) {
        for (var hearing : hearings) {
            var hearingType = hearing.getValue();
            SelectionServiceTestUtils.verifyListItems(hearingType.getHearingAberdeen().getListItems(),
                    TribunalOffice.ABERDEEN.getOfficeName(), TribunalOffice.ABERDEEN.getOfficeName() + " ");
            SelectionServiceTestUtils.verifyListItems(hearingType.getHearingDundee().getListItems(),
                    TribunalOffice.DUNDEE.getOfficeName(), TribunalOffice.DUNDEE.getOfficeName() + " ");
            SelectionServiceTestUtils.verifyListItems(hearingType.getHearingEdinburgh().getListItems(),
                    TribunalOffice.EDINBURGH.getOfficeName(), TribunalOffice.EDINBURGH.getOfficeName() + " ");
            SelectionServiceTestUtils.verifyListItems(hearingType.getHearingGlasgow().getListItems(),
                    TribunalOffice.GLASGOW.getOfficeName(), TribunalOffice.GLASGOW.getOfficeName() + " ");
        }
    }
}
