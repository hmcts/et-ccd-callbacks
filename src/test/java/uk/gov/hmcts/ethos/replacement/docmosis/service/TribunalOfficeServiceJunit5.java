package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = TribunalOfficesConfiguration.class)
@TestPropertySource("classpath:defaults.yml")
@ContextConfiguration(classes = TribunalOfficesConfiguration.class)

public class TribunalOfficeServiceJunit5 {

   @Autowired
   private final TribunalOfficesConfiguration config;

    private final TribunalOfficesService tribunalOfficesService;

    private CaseDetails caseDetails;

    public TribunalOfficeServiceJunit5(TribunalOfficesConfiguration config, TribunalOfficesService tribunalOfficesService){
        this.config = config;
        this.tribunalOfficesService = tribunalOfficesService;
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

    @Test
    void givenTribunalOfficeService_whenBindingPropertiesFilsse_2() {
        // config.getContactDetails().get(tribunalName);
        System.out.println(this.config.getContactDetails().get("Manchester"));
    }


    @Test
    void givenTribunalOfficeService_whenBindingPropertiesFile_2() {
        // config.getContactDetails().get(tribunalName);
        ContactDetails manc = new ContactDetails();
        manc.setManagingOffice("Manchester");

      //  when(config.getContactDetails().get("Manchester")).thenReturn(manc);
        when(config.getContactDetails()).thenReturn(this.config.getContactDetails());

        ContactDetails retrievedContactDetails = tribunalOfficesService.getTribunalContactDetails("Manchester");
        assertEquals(retrievedContactDetails.getManagingOffice(),"Manchester" );
    }

    @Test
    public void testSetsEpimmId() {
        tribunalOfficesService.setCaseManagementLocationCode(caseDetails.getCaseData());
        assertEquals("12345", caseDetails.getCaseData().getCaseManagementLocationCode());
    }
}