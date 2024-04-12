package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.configuration.PostcodeToOfficeMappings;
import uk.gov.hmcts.ecm.common.service.JurisdictionCodesMapperService;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = { Et1ReppedService.class, TribunalOfficesService.class, PostcodeToOfficeService.class})
@EnableConfigurationProperties({ CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class,
    PostcodeToOfficeMappings.class })
class Et1ReppedServiceTest {

    private Et1ReppedService et1ReppedService;

    private PostcodeToOfficeService postcodeToOfficeService;
    private PostcodeToOfficeMappings postcodeToOfficeMappings;
    @MockBean
    private AcasService acasService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private CcdCaseAssignment ccdCaseAssignment;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private JurisdictionCodesMapperService jurisdictionCodesMapperService;
    @MockBean
    private OrganisationClient organisationClient;
    @MockBean
    private PdfService pdfService;
    @MockBean
    private TornadoService tornadoService;
    private TribunalOfficesService tribunalOfficesService;
    @MockBean
    private UserIdamService userIdamService;
    @Mock
    private RestTemplate restTemplate;

    private CaseDetails caseDetails;
    private CaseData caseData;

    private CaseDetails draftCaseDetails;

    @BeforeEach
    void setUp() throws Exception {
        caseDetails = new CaseDetails();
        caseData = new CaseData();
        Address address = new Address();
        caseData.setEt1ReppedTriageAddress(address);
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1234567890123456");
        caseDetails.setCaseTypeId("ET_EnglandWales");

        draftCaseDetails = generateCaseDetails("et1ReppedDraftStillWorking.json");

        postcodeToOfficeService = new PostcodeToOfficeService(new PostcodeToOfficeMappings());
        tribunalOfficesService = new TribunalOfficesService(new TribunalOfficesConfiguration(),
                postcodeToOfficeService);
        et1ReppedService = new Et1ReppedService(acasService, authTokenGenerator, ccdCaseAssignment,
                documentManagementService, jurisdictionCodesMapperService, organisationClient, pdfService,
                postcodeToOfficeService, tornadoService, tribunalOfficesService, userIdamService);
    }

    @Test
    void addsDefaultData() {
        et1ReppedService.addDefaultData(draftCaseDetails.getCaseTypeId(), draftCaseDetails.getCaseData());
        assertEquals(draftCaseDetails.getCaseData().getReceiptDate(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    @Test
    void addClaimantRepresentativeDetails() {
        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .name("TestOrg")
                .organisationIdentifier("AA11BB")
                .contactInformation(List.of(OrganisationAddress.builder()
                        .addressLine1("AddressLine1")
                        .addressLine2("AddressLine2")
                        .addressLine3("AddressLine3")
                        .postCode("AA1 1AA")
                        .build()))
                .build();
        when(organisationClient.retrieveOrganisationDetailsByUserId(anyString(), anyString(), anyString()))
                .thenReturn(ResponseEntity.ok(organisationsResponse));
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        et1ReppedService.addClaimantRepresentativeDetails(draftCaseDetails.getCaseData(), "authToken");
        verify(organisationClient, times(1)).retrieveOrganisationDetailsByUserId(anyString(), anyString(), anyString());
        assertEquals(YES, draftCaseDetails.getCaseData().getClaimantRepresentedQuestion());
        assertNotNull(draftCaseDetails.getCaseData().getRepresentativeClaimantType());
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

}