package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.VenueAddressReaderException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.GLASGOW_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

public class VenueAddressReaderServiceTest {

    private VenueAddressesService venueAddressesService;
    private VenueAddressReaderService venueAddressReaderService;

    @Before
    public void setUp() {
        venueAddressesService = mock(VenueAddressesService.class);
        venueAddressReaderService = new VenueAddressReaderService(venueAddressesService);
    }

    @Test
    public void getVenueAddressForHearing_England() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Manchester1");
        venueAddress.setAddress("Test Address1");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        var officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_EnglandWales("Manchester1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(hearingType, ENGLANDWALES_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address1", resultAddress);
    }

    @Test
    public void getVenueAddressForHearing_Wales() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Wales1");
        venueAddress.setAddress("Test Address2");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        var officeName = TribunalOffice.WALES.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_EnglandWales("Wales1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(hearingType, ENGLANDWALES_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address2", resultAddress);
    }

    @Test
    public void getVenueAddressForHearing_Scotland() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Glasgow1");
        venueAddress.setAddress("Test Address3");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        var officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_ScotlandGlasgow("Glasgow1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address3", resultAddress);
    }

    @Test
    public void getVenueAddressForHearing_EmptyAddressValue() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Glasgow2");
        venueAddress.setAddress("");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        var officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_ScotlandGlasgow("Glasgow2");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Glasgow2", resultAddress);
    }

    @Test
    public void getVenueAddressForHearing_HearingVenueNotInList() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("OtherVenue");
        venueAddress.setAddress("Test Address");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        var officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_EnglandWales("Manchester1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(hearingType, ENGLANDWALES_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Manchester1", resultAddress);
    }

    @Test
    public void getVenueAddressForHearing_HearingVenueNotFound() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Glasgow3");
        venueAddress.setAddress("");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        var officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_ScotlandNotFound();

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertNull(resultAddress);
    }

    @Test(expected = VenueAddressReaderException.class)
    public void getVenueAddressForHearing_ThrowsAddressReaderException() {
        // Arrange
        List<VenueAddress> venueAddressList = new ArrayList<>();
        var officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearing = getHearingTypeForVenue_EnglandWales(officeName);

        // Act
        venueAddressReaderService.getVenueAddress(hearing, ENGLANDWALES_CASE_TYPE_ID, officeName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getVenueAddressForHearing_ThrowsArgumentException() {
        // Arrange
        HearingType hearing = new HearingType();

        // Act
        venueAddressReaderService.getVenueAddress(hearing, "NotFound", "");
    }

    private HearingType getHearingTypeForVenue_EnglandWales(String venue) {
        DynamicValueType hearingVenueValue = new DynamicValueType();
        hearingVenueValue.setLabel(venue);
        DynamicFixedListType hearingVenue = new DynamicFixedListType();
        hearingVenue.setValue(hearingVenueValue);
        HearingType hearing = new HearingType();
        hearing.setHearingVenue(hearingVenue);
        return hearing;
    }

    private HearingType getHearingTypeForVenue_ScotlandGlasgow(String venue) {
        DynamicValueType hearingVenueValue = new DynamicValueType();
        hearingVenueValue.setLabel(venue);
        DynamicFixedListType hearingVenue = new DynamicFixedListType();
        hearingVenue.setValue(hearingVenueValue);
        HearingType hearing = new HearingType();
        hearing.setHearingGlasgow(hearingVenue);
        hearing.setHearingVenueScotland(GLASGOW_OFFICE);
        return hearing;
    }

    private HearingType getHearingTypeForVenue_ScotlandNotFound() {
        HearingType hearing = new HearingType();
        hearing.setHearingVenueScotland("NotFound");
        return hearing;
    }
}