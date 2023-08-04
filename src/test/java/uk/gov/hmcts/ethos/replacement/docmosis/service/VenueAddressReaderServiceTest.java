package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.VenueAddressReaderException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class VenueAddressReaderServiceTest {

    private VenueAddressesService venueAddressesService;
    private VenueAddressReaderService venueAddressReaderService;

    @BeforeEach
    public void setUp() {
        venueAddressesService = mock(VenueAddressesService.class);
        venueAddressReaderService = new VenueAddressReaderService(venueAddressesService);
    }

    @Test
    void getVenueAddressForHearing_England() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Manchester1");
        venueAddress.setAddress("Test Address1");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_EnglandWales("Manchester1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, ENGLANDWALES_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address1", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_Wales() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Wales1");
        venueAddress.setAddress("Test Address2");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.WALES.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_EnglandWales("Wales1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, ENGLANDWALES_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address2", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_ScotlandGlasgow() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Glasgow1");
        venueAddress.setAddress("Test Address3");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName))
                .thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_Scotland(
                TribunalOffice.GLASGOW.getOfficeName(), "Glasgow1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address3", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_ScotlandAberdeen() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Aberdeen1");
        venueAddress.setAddress("Test Address4");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.ABERDEEN.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName))
                .thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_Scotland(
                TribunalOffice.ABERDEEN.getOfficeName(), "Aberdeen1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address4", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_ScotlandDundee() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Dundee1");
        venueAddress.setAddress("Test Address5");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.DUNDEE.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_Scotland(
                TribunalOffice.DUNDEE.getOfficeName(), "Dundee1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address5", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_ScotlandEdinburgh() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Edinburgh1");
        venueAddress.setAddress("Test Address6");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.EDINBURGH.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(
                officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_Scotland(
                TribunalOffice.EDINBURGH.getOfficeName(), "Edinburgh1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Test Address6", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_EmptyAddressValue() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Glasgow2");
        venueAddress.setAddress("");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName))
                .thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_Scotland(
                TribunalOffice.GLASGOW.getOfficeName(), "Glasgow2");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, SCOTLAND_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Glasgow2", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_HearingVenueNotInList() {
        // Arrange
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("OtherVenue");
        venueAddress.setAddress("Test Address");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName)).thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_EnglandWales("Manchester1");

        // Act
        String resultAddress = venueAddressReaderService.getVenueAddress(
                hearingType, ENGLANDWALES_CASE_TYPE_ID, officeName);

        // Assert
        assertEquals("Manchester1", resultAddress);
    }

    @Test
    void getVenueAddressForHearing_HearingVenueNotFound() {
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Glasgow3");
        venueAddress.setAddress("");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName))
                .thenReturn(venueAddressList);
        HearingType hearingType = getHearingTypeForVenue_ScotlandNotFound();

        assertThrows(IllegalArgumentException.class, () ->
                venueAddressReaderService.getVenueAddress(hearingType, SCOTLAND_CASE_TYPE_ID, officeName)
        );
    }

    @Test
    void getVenueAddressForHearing_HearingVenueEmpty() {
        VenueAddress venueAddress = new VenueAddress();
        venueAddress.setVenue("Glasgow4");
        venueAddress.setAddress("");
        List<VenueAddress> venueAddressList = List.of(venueAddress);
        String officeName = TribunalOffice.GLASGOW.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName))
                .thenReturn(venueAddressList);
        HearingType hearing = new HearingType();
        hearing.setHearingVenueScotland("");

        assertThrows(VenueAddressReaderException.class, () ->
                venueAddressReaderService.getVenueAddress(hearing, SCOTLAND_CASE_TYPE_ID, officeName)
        );
    }

    @Test
    void getVenueAddressForHearing_ThrowsAddressReaderException() {
        List<VenueAddress> venueAddressList = new ArrayList<>();
        String officeName = TribunalOffice.MANCHESTER.getOfficeName();
        when(venueAddressesService.getTribunalVenueAddresses(officeName))
                .thenReturn(venueAddressList);
        HearingType hearing = getHearingTypeForVenue_EnglandWales(officeName);

        assertThrows(VenueAddressReaderException.class, () ->
                venueAddressReaderService.getVenueAddress(hearing,
                        ENGLANDWALES_CASE_TYPE_ID, officeName)
        );
    }

    @Test
    void getVenueAddressForHearing_ThrowsArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                venueAddressReaderService.getVenueAddress(new HearingType(), "NotFound", "")
        );
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

    private HearingType getHearingTypeForVenue_Scotland(String venueOffice, String venue) {
        DynamicValueType hearingVenueValue = new DynamicValueType();
        hearingVenueValue.setLabel(venue);
        DynamicFixedListType hearingVenue = new DynamicFixedListType();
        hearingVenue.setValue(hearingVenueValue);
        HearingType hearing = new HearingType();

        final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(venueOffice);
        switch (tribunalOffice) {
            case GLASGOW -> hearing.setHearingGlasgow(hearingVenue);
            case ABERDEEN -> hearing.setHearingAberdeen(hearingVenue);
            case DUNDEE -> hearing.setHearingDundee(hearingVenue);
            case EDINBURGH -> hearing.setHearingEdinburgh(hearingVenue);
            default -> {
            }
        }

        hearing.setHearingVenueScotland(venueOffice);
        return hearing;
    }

    private HearingType getHearingTypeForVenue_ScotlandNotFound() {
        HearingType hearing = new HearingType();
        hearing.setHearingVenueScotland("NotFound");
        return hearing;
    }
}
