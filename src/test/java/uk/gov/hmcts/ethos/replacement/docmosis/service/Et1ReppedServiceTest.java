package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.configuration.PostcodeToOfficeMappings;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.JurisdictionCodesMapperService;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.ecm.common.service.pdf.PdfMapperService;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@SpringBootTest(classes = { Et1ReppedService.class, TribunalOfficesService.class, PostcodeToOfficeService.class,
    PdfService.class, PdfMapperService.class})
@EnableConfigurationProperties({ CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class,
    PostcodeToOfficeMappings.class })
class Et1ReppedServiceTest {

    private Et1ReppedService et1ReppedService;
    @MockBean
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
    private TornadoService tornadoService;
    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private AdminUserService adminUserService;
    @Mock
    private RestTemplate restTemplate;
    @MockBean
    private Et1SubmissionService et1SubmissionService;

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

        PostcodeToOfficeService postcodeToOfficeService = new PostcodeToOfficeService(postcodeToOfficeMappings);
        TribunalOfficesService tribunalOfficesService = new TribunalOfficesService(new TribunalOfficesConfiguration(),
                postcodeToOfficeService);
        et1ReppedService = new Et1ReppedService(authTokenGenerator, ccdCaseAssignment,
                jurisdictionCodesMapperService, organisationClient, postcodeToOfficeService, tribunalOfficesService,
                userIdamService, adminUserService, et1SubmissionService);

        when(postcodeToOfficeMappings.getPostcodes()).thenReturn(getPostcodes());
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
        when(adminUserService.getAdminUserToken()).thenReturn("userToken");
        et1ReppedService.addClaimantRepresentativeDetails(draftCaseDetails.getCaseData(), "authToken");
        verify(organisationClient, times(1)).retrieveOrganisationDetailsByUserId(anyString(), anyString(), anyString());
        assertEquals(YES, draftCaseDetails.getCaseData().getClaimantRepresentedQuestion());
        assertNotNull(draftCaseDetails.getCaseData().getRepresentativeClaimantType());
    }

    @Test
    void shouldReturnNoIfNoPostcodeEntered() throws InvalidPostcodeException {
        caseData = new CaseData();
        caseData.setEt1ReppedTriageAddress(new Address());
        List<String> errors =  et1ReppedService.validatePostcode(caseData, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(1, errors.size());
        assertEquals("Please enter a valid postcode", errors.get(0));
    }

    @ParameterizedTest
    @MethodSource("validatePostcodes")
    void validatePostcode(String postcode, String caseTypeId, int expected, String condition)
            throws InvalidPostcodeException {
        caseData = new CaseData();
        Address address = new Address();
        address.setPostCode(postcode);
        caseData.setEt1ReppedTriageAddress(address);
        List<String> errors =  et1ReppedService.validatePostcode(caseData, caseTypeId);
        assertEquals(expected, errors.size());
        assertEquals(condition, caseData.getEt1ReppedTriageYesNo());
    }

    private static Stream<Arguments> validatePostcodes() {
        return Stream.of(
                Arguments.of("LS16 6NB", ENGLANDWALES_CASE_TYPE_ID, 0, YES),
                Arguments.of("B1 1AA", ENGLANDWALES_CASE_TYPE_ID, 0, YES),
                Arguments.of("EH1 1AA", ENGLANDWALES_CASE_TYPE_ID, 1, NO),
                Arguments.of("RM1 1AA", SCOTLAND_CASE_TYPE_ID, 1, NO),
                Arguments.of("EC1 1AA", SCOTLAND_CASE_TYPE_ID, 1, NO),
                Arguments.of("AL1 1AA", SCOTLAND_CASE_TYPE_ID, 1, NO),
                Arguments.of("BA1 1AA", ENGLANDWALES_CASE_TYPE_ID, 0, YES),
                Arguments.of("G1 1AA", SCOTLAND_CASE_TYPE_ID, 0, YES)
        );
    }

    @Test
    void assignCaseAccess() throws IOException {
        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(adminUserService.getAdminUserToken()).thenReturn("userToken");
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .name("TestOrg")
                .organisationIdentifier("AA11BB")
                .build();
        when(organisationClient.retrieveOrganisationDetailsByUserId("userToken", "serviceAuthToken", "id"))
                .thenReturn(ResponseEntity.status(200).body(organisationsResponse));
        et1ReppedService.assignCaseAccess(caseDetails, "authToken");
        verify(ccdCaseAssignment, times(1)).removeCaseUserRoles(any());
        verify(ccdCaseAssignment, times(1)).addCaseUserRoles(any());
    }

    @Test
    void retrieveOrganisationException() {
        when(organisationClient.retrieveOrganisationDetailsByUserId("authToken", "serviceAuthToken", "id"))
                .thenReturn(ResponseEntity.status(404).build());
        assertNull(et1ReppedService.getOrganisationDetailsFromUserId("id"));
    }

    @Test
    void createDraftEt1() throws Exception {
        caseDetails =  generateCaseDetails("et1ReppedDraftStillWorking.json");
        Et1ReppedHelper.setEt1SubmitData(caseDetails.getCaseData());
        et1ReppedService.addDefaultData(caseDetails.getCaseTypeId(), caseDetails.getCaseData());

        DocumentInfo documentInfo = DocumentInfo.builder()
                .description("Draft ET1 - R111111/11/11")
                .url("http://test.com/documents/random-uuid")
                .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
                .build();
        when(et1SubmissionService.createEt1(any(), anyString(), anyString()))
                .thenReturn(documentInfo);
        assertDoesNotThrow(() -> et1ReppedService.createDraftEt1(caseDetails, "authToken"));
        assertNotNull(caseDetails.getCaseData().getDocMarkUp());
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    private Map<String, String> getPostcodes() {
        return Map.of(
                "LS", TribunalOffice.LEEDS.getOfficeName(),
                "EH", TribunalOffice.EDINBURGH.getOfficeName(),
                "G", TribunalOffice.GLASGOW.getOfficeName(),
                "B", TribunalOffice.MIDLANDS_WEST.getOfficeName(),
                "CH", TribunalOffice.MANCHESTER.getOfficeName(),
                "BN", TribunalOffice.LONDON_SOUTH.getOfficeName(),
                "RM", TribunalOffice.LONDON_EAST.getOfficeName(),
                "EC", TribunalOffice.LONDON_CENTRAL.getOfficeName(),
                "AL", TribunalOffice.WATFORD.getOfficeName(),
                "BA", TribunalOffice.BRISTOL.getOfficeName()
        );
    }

}
