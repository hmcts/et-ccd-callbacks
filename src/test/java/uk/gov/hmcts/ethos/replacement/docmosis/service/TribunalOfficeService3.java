package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.junit.Assert.*;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = TribunalOfficesConfiguration.class)
@TestPropertySource("classpath:defaults.yml")
@ContextConfiguration(classes = TribunalOfficesConfiguration.class)

public class TribunalOfficeService3 {

  //  @Autowired
    private TribunalOfficesConfiguration tribunalOfficesConfiguration;

    private TribunalOfficesService tribunalOfficesService;

    private CaseDetails caseDetails;

    TribunalOfficeService3(){
        this.tribunalOfficesConfiguration = new TribunalOfficesConfiguration();
    this.tribunalOfficesService = new TribunalOfficesService(tribunalOfficesConfiguration);
    }

    private static final String CASE_ID = "1655312312192821";


    @BeforeEach
    public void setUp() {
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
//    @Test
//    void givenUserDefinedPOJO_whenBindingPropertiesFile_thenAllFieldsAreSet() {
//        assertEquals("123", tribunalOfficesConfiguration.getCourtLocations());
//
//    }
    @Test
    void givenTribunalOfficeService_whenBindingPropertiesFile_thenAllFieldsAreSet() {
        assertEquals("manchesteret@justice.gov.uk", tribunalOfficesService.getTribunalOffice("Manchester").getOfficeEmail());

    }

    @Test
    void givenTribunalOfficeService_whenBindingPropertiesFile_2() {
        assertEquals("manchesteret@justice.gov.uk", tribunalOfficesService.getTribunalContactDetails("Manchester"));

    }

    @Test
    public void testSetsEpimmId() {
        tribunalOfficesService.setCaseManagementLocationCode(caseDetails.getCaseData());
        assertEquals("12345", caseDetails.getCaseData().getCaseManagementLocationCode());
    }
}
