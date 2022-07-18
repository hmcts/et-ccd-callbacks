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
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
@SpringBootTest(classes = {
        TribunalOfficesService.class,
})
@EnableConfigurationProperties({CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class})
public class TribunalOfficeServiceTest {

    @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    TribunalOfficesService tribunalOfficesService;

    private static final Object[][] TEST_CASES = new Object[][] {
            { TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA" },
            { TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA" },
            { TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA" },
            { TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT" },
            { TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT" },
            { TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT" },
            { TribunalOffice.ABERDEEN.getOfficeName(), "AB10 1SH" },
            { TribunalOffice.ABERDEEN.getOfficeName(), "AB10 1SH" },
            { TribunalOffice.ABERDEEN.getOfficeName(), "AB10 1SH" },
            { TribunalOffice.DUNDEE.getOfficeName(), "DD1 4QB" },
            { TribunalOffice.DUNDEE.getOfficeName(), "DD1 4QB" },
            { TribunalOffice.DUNDEE.getOfficeName(), "DD1 4QB" },
            { TribunalOffice.EDINBURGH.getOfficeName(), "EH3 7HF" },
            { TribunalOffice.EDINBURGH.getOfficeName(), "EH3 7HF" },
            { TribunalOffice.EDINBURGH.getOfficeName(), "EH3 7HF" }
    };

    private final String managingOffice;
    private final String expectedPostcode;

    public TribunalOfficeServiceTest(String officeName, String expectedPostcode) {
        this.managingOffice = officeName;
        this.expectedPostcode = expectedPostcode;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(TEST_CASES);
    }

    @Test
    public void testGetsCorrectTribunalContactDetails() {
        ContactDetails contactDetails = tribunalOfficesService.getTribunalContactDetails(managingOffice);
        assertEquals(expectedPostcode, contactDetails.getPostcode());
    }
}