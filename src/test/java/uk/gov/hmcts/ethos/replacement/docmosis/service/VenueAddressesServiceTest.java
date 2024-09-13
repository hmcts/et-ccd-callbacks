package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.config.VenueAddressesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { VenueAddressesService.class })
@EnableConfigurationProperties({ VenueAddressesConfiguration.class })
class VenueAddressesServiceTest {

    @Autowired
    VenueAddressesService venueAddressesService;

    private static final Object[][] TEST_CASES = {
        { TribunalOffice.BRISTOL.getOfficeName(), "Barnstaple" },
        { TribunalOffice.MANCHESTER.getOfficeName(),
            "Barrow Magistrates Court, Abbey Road, Barrow in Furness, Cumbria, LA14 5QX" },
        { TribunalOffice.WALES.getOfficeName(), "Abergele" },
        { TribunalOffice.GLASGOW.getOfficeName(),
            "Campbeltown HC, Sheriff Court House, Castle Hill, Campbeltown, PA28 6AN" },
        { TribunalOffice.ABERDEEN.getOfficeName(), "Ground Floor, AB1, 48 Huntly Street, Aberdeen, AB10 1SH" },
        { TribunalOffice.DUNDEE.getOfficeName(),
            "Ground Floor, Block C, Caledonian House, Greenmarket, Dundee, DD1 4QG" },
        { TribunalOffice.EDINBURGH.getOfficeName(), "54-56 Melville Street, Edinburgh, EH3 7HF" }
    };

    @ParameterizedTest
    @MethodSource("testCasesProvider")
    void testGetsCorrectVenueAddressList(String officeName, String expectedAddress) {
        List<VenueAddress> venueAddressList = venueAddressesService.getTribunalVenueAddresses(officeName);
        assertEquals(expectedAddress, venueAddressList.get(0).getAddress());
    }

    private static Stream<Arguments> testCasesProvider() {
        return Stream.of(TEST_CASES).map(Arguments::of);
    }
}