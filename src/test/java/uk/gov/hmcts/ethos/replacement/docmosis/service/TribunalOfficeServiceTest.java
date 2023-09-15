package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
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
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.UNASSIGNED_OFFICE;

@SuppressWarnings({"PMD.SingularField"})
@RunWith(Parameterized.class)
@SpringBootTest(classes = { TribunalOfficesService.class })
@EnableConfigurationProperties({ CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class })
public class TribunalOfficeServiceTest {
    @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    TribunalOfficesService tribunalOfficesService;

    private CaseDetails caseDetails;
    private final String managingOffice;
    private final String expectedPostcode;
    private final String epimmsId;
    private final String region;
    private static final String CASE_ID = "1655312312192821";

    private static final Object[][] TEST_CASES = {
            {TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA", "301017", "4"},
            {TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA", "301017", "4"},
            {TribunalOffice.MANCHESTER.getOfficeName(), "M3 2JA", "301017", "4"},
            {TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT", "366559", "11"},
            {TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT", "366559", "11"},
            {TribunalOffice.GLASGOW.getOfficeName(), "G2 8GT", "366559", "11"},
            {TribunalOffice.ABERDEEN.getOfficeName(), "AB10 1SH", "219164", "11"},
            {TribunalOffice.ABERDEEN.getOfficeName(), "AB10 1SH", "219164", "11"},
            {TribunalOffice.ABERDEEN.getOfficeName(), "AB10 1SH", "219164", "11"},
            {TribunalOffice.DUNDEE.getOfficeName(), "DD1 4QB", "367564", "11"},
            {TribunalOffice.DUNDEE.getOfficeName(), "DD1 4QB", "367564", "11"},
            {TribunalOffice.DUNDEE.getOfficeName(), "DD1 4QB", "367564", "11"},
            {TribunalOffice.EDINBURGH.getOfficeName(), "EH3 7HF", "368308", "11"},
            {UNASSIGNED_OFFICE, "", "", ""},
            {null, "", "", ""},
    };

    public TribunalOfficeServiceTest(String officeName, String expectedPostcode, String epimmsId, String region) {
        this.managingOffice = officeName;
        this.expectedPostcode = expectedPostcode;
        this.epimmsId = epimmsId;
        this.region = region;
    }

    @Before
    void setUp() {
        caseDetails = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withClaimantWorkAddress("11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withRespondentWithAddress("Juan Garcia",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        "2987/6543/01")
                .withRespondentWithAddress("Juan Garcia",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null)
                .withRespondentWithAddress("Juan Garcia",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null)
                .withRespondentWithAddress("Juan Garcia",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null)
                .withRespondentWithAddress("Juan Garcia",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null)
                .withManagingOffice("Manchester")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);
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

    @Test
    public void testSetsEpimmId() {
        setUp();
        tribunalOfficesService.setCaseManagementLocationCode(caseDetails.getCaseData());
        // assertEquals("301017", caseDetails.getCaseData().getCaseManagementLocationCode());
        assertEquals("test should fail", caseDetails.getCaseData().getCaseManagementLocationCode());
    }
}