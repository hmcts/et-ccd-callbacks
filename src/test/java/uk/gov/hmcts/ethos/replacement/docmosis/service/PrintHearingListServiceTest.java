package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.LawOfDemeter", "PMD.UnusedPrivateMethod"})
@ExtendWith(SpringExtension.class)
class PrintHearingListServiceTest {

    @InjectMocks
    PrintHearingListService printHearingListService;

    @Mock
    VenueService venueService;

    @ParameterizedTest
    @MethodSource
    void testInitPrintHearingListsPopulatesEnglandWalesVenues(TribunalOffice tribunalOffice) {
        var venues = List.of(DynamicValueType.create("venue1", "Venue 1"),
                DynamicValueType.create("venue2", "Venue 2"));
        when(venueService.getVenues(tribunalOffice)).thenReturn(venues);
        var caseData = new CaseData();
        caseData.setManagingOffice(tribunalOffice.getOfficeName());

        printHearingListService.initPrintHearingLists(caseData);

        var listingVenue = caseData.getPrintHearingDetails().getListingVenue();
        assertEquals(venues.size(), listingVenue.getListItems().size());
        verifyVenue(venues.get(0), "venue1", "Venue 1");
        verifyVenue(venues.get(1), "venue2", "Venue 2");
        assertNull(listingVenue.getValue());
        assertNull(caseData.getPrintHearingDetails().getVenueAberdeen());
        assertNull(caseData.getPrintHearingDetails().getVenueDundee());
        assertNull(caseData.getPrintHearingDetails().getVenueEdinburgh());
        assertNull(caseData.getPrintHearingDetails().getVenueGlasgow());
    }

    private static Stream<Arguments> testInitPrintHearingListsPopulatesEnglandWalesVenues() {
        return TribunalOffice.ENGLANDWALES_OFFICES.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource
    void testInitiPrintHearingListPopulatesScotlandVenues(TribunalOffice tribunalOffice) {
        mockScotlandVenueService();
        var caseData = new CaseData();
        caseData.setManagingOffice(tribunalOffice.getOfficeName());

        printHearingListService.initPrintHearingLists(caseData);

        var aberdeen = caseData.getPrintHearingDetails().getVenueAberdeen();
        assertEquals(2, aberdeen.getListItems().size());
        verifyVenue(aberdeen.getListItems().get(0), "aberdeen1", "Aberdeen 1");
        verifyVenue(aberdeen.getListItems().get(1), "aberdeen2", "Aberdeen 2");
        assertNull(aberdeen.getValue());

        var dundee = caseData.getPrintHearingDetails().getVenueDundee();
        assertEquals(2, dundee.getListItems().size());
        verifyVenue(dundee.getListItems().get(0), "dundee1", "Dundee 1");
        verifyVenue(dundee.getListItems().get(1), "dundee2", "Dundee 2");
        assertNull(dundee.getValue());

        var edinburgh = caseData.getPrintHearingDetails().getVenueEdinburgh();
        assertEquals(2, edinburgh.getListItems().size());
        verifyVenue(edinburgh.getListItems().get(0), "edinburgh1", "Edinburgh 1");
        verifyVenue(edinburgh.getListItems().get(1), "edinburgh2", "Edinburgh 2");
        assertNull(edinburgh.getValue());

        var glasgow = caseData.getPrintHearingDetails().getVenueGlasgow();
        assertEquals(2, glasgow.getListItems().size());
        verifyVenue(glasgow.getListItems().get(0), "glasgow1", "Glasgow 1");
        verifyVenue(glasgow.getListItems().get(1), "glasgow2", "Glasgow 2");
        assertNull(glasgow.getValue());

        assertNull(caseData.getPrintHearingDetails().getListingVenue());
    }

    private static Stream<Arguments> testInitiPrintHearingListPopulatesScotlandVenues() {
        return TribunalOffice.SCOTLAND_OFFICES.stream().map(Arguments::of);
    }

    private void mockScotlandVenueService() {
        when(venueService.getVenues(TribunalOffice.ABERDEEN)).thenReturn(List.of(
                DynamicValueType.create("aberdeen1", "Aberdeen 1"),
                DynamicValueType.create("aberdeen2", "Aberdeen 2")));
        when(venueService.getVenues(TribunalOffice.DUNDEE)).thenReturn(List.of(
                DynamicValueType.create("dundee1", "Dundee 1"),
                DynamicValueType.create("dundee2", "Dundee 2")));
        when(venueService.getVenues(TribunalOffice.EDINBURGH)).thenReturn(List.of(
                DynamicValueType.create("edinburgh1", "Edinburgh 1"),
                DynamicValueType.create("edinburgh2", "Edinburgh 2")));
        when(venueService.getVenues(TribunalOffice.GLASGOW)).thenReturn(List.of(
                DynamicValueType.create("glasgow1", "Glasgow 1"),
                DynamicValueType.create("glasgow2", "Glasgow 2")));
    }

    private void verifyVenue(DynamicValueType venue, String expectedCode, String expectedLabel) {
        assertEquals(expectedCode, venue.getCode());
        assertEquals(expectedLabel, venue.getLabel());
    }
}
