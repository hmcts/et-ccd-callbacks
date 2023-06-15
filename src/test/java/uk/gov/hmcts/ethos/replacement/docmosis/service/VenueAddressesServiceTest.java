package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.config.VenueAddressesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
@SpringBootTest(classes = { VenueAddressesService.class })
@EnableConfigurationProperties({ VenueAddressesConfiguration.class })
public class VenueAddressesServiceTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    VenueAddressesService venueAddressesService;

    private static final Object[][] TEST_CASES = new Object[][] {
            { TribunalOffice.BRISTOL.getOfficeName(), "Barnstaple" },
            { TribunalOffice.MANCHESTER.getOfficeName(), "Barrow Magistrates Court, "
                    + "Abbey Road, Barrow in Furness, Cumbria, LA14 5QX" },
            { TribunalOffice.WALES.getOfficeName(), "Abergele" },
            { TribunalOffice.GLASGOW.getOfficeName(), "Campbeltown HC, Sheriff Court House, "
                    + "Castle Hill, Campbeltown, PA28 6AN" },
            { TribunalOffice.ABERDEEN.getOfficeName(), "Ground Floor,"
                    + " AB1, 48 Huntly Street, Aberdeen, AB10 1SH" },
            { TribunalOffice.DUNDEE.getOfficeName(), "Ground Floor, "
                    + "Block C, Caledonian House, Greenmarket, Dundee, DD1 4QG" },
            { TribunalOffice.EDINBURGH.getOfficeName(), "54-56 Melville Street, Edinburgh, EH3 7HF" },
    };

    private final String managingOffice;
    private final String expectedAddress;

    public VenueAddressesServiceTest(String officeName, String expectedAddress) {
        this.managingOffice = officeName;
        this.expectedAddress = expectedAddress;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(TEST_CASES);
    }

    @Test
    public void testGetsCorrectVenueAddressList() {
        List<VenueAddress> venueAddressList = venueAddressesService.getTribunalVenueAddresses(managingOffice);
        assertEquals(expectedAddress, venueAddressList.get(0).getAddress());
    }
}