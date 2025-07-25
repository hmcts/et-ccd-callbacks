package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.hmcts.ecm.common.model.acas.AcasCertificate;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@ExtendWith(SpringExtension.class)
class AcasServiceTest {

    private static final List<String> ACAS_CERT_LIST = List.of("R111111/11/11");
    @MockBean
    private TornadoService tornadoService;
    private AcasService acasService;
    private CaseData caseData;
    private MockWebServer mockWebServer;
    private List<String> errors;
    private DocumentInfo documentInfo;
    private ObjectMapper objectMapper;

    private static final String ACAS_API_KEY = "dummyApiKey";
    private static final String AUTH_TOKEN = "authToken";
    private static final String NOT_FOUND_CERTIFICATE = "not found";

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        // Initialize MockWebServer for WebClient testing
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        // Create WebClient pointing to mock server
        WebClient webClient = WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .build();
        
        String baseUrl = mockWebServer.url("/").toString();
        acasService = new AcasService(tornadoService, webClient, baseUrl, ACAS_API_KEY);
        
        objectMapper = new ObjectMapper();
        errors = new ArrayList<>();

        caseData = CaseDataBuilder.builder()
                .withManagingOffice(TribunalOffice.LEEDS.getOfficeName())
                .withRespondent("Respondent Name", NO, "R111111/11/11", false)
                .build();
        caseData.setAcasCertificate("R111111/11/11");

        documentInfo = DocumentInfo.builder()
            .description("ACAS Certificate - R111111/11/11")
            .url("http://test.com/documents/random-uuid")
            .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
            .build();
        when(tornadoService.createDocumentInfoFromBytes(anyString(), any(), anyString(), anyString()))
                .thenReturn(documentInfo);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void getAcasCertificate() throws JsonProcessingException {
        // Prepare mock response with valid ACAS certificate
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateNumber("R111111/11/11");
        acasCertificate.setCertificateDocument("dGVzdCBwZGYgZGF0YQ=="); // Base64 encoded "test pdf data"
        
        List<AcasCertificate> certificates = List.of(acasCertificate);
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(certificates)));
        
        errors = acasService.getAcasCertificate(caseData, AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(0, errors.size());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "")
    void nullOrEmptyAcasCert(String certificateNumber) {
        caseData.setAcasCertificate(certificateNumber);
        errors = acasService.getAcasCertificate(caseData, AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(1, errors.size());
    }

    @Test
    void unauthorisedResponseFromAcas() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        
        errors = acasService.getAcasCertificate(caseData, AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals("Error retrieving ACAS Certificate", errors.getFirst());
    }

    @Test
    void certificateNotFound() throws JsonProcessingException {
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateNumber("A123456/12/12");
        acasCertificate.setCertificateDocument(NOT_FOUND_CERTIFICATE);
        
        List<AcasCertificate> certificates = List.of(acasCertificate);
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(certificates)));
                
        errors = acasService.getAcasCertificate(caseData, AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID);
        assertEquals("No ACAS Certificate found", errors.getFirst());
    }

    @Test
    void getAcasCertificates() throws JsonProcessingException {
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateNumber("R111111/11/11");
        acasCertificate.setCertificateDocument("dGVzdCBwZGYgZGF0YQ=="); // Base64 encoded "test pdf data"
        
        List<AcasCertificate> certificates = List.of(acasCertificate);
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(certificates)));
        
        List<DocumentInfo> actual = acasService.getAcasCertificates(caseData, ACAS_CERT_LIST, AUTH_TOKEN,
                ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(1, actual.size());
        assertEquals(documentInfo.getDescription(), actual.getFirst().getDescription());
    }

    @Test
    void getAcasCertificatesHttpException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));
        
        assertThrows(WebClientResponseException.class,
                () -> acasService.getAcasCertificates(caseData, ACAS_CERT_LIST, AUTH_TOKEN,
                        ENGLANDWALES_CASE_TYPE_ID));
    }

    @Test
    void getAcasCertificatesNoCertFound() throws JsonProcessingException {
        // Mock empty response
        List<AcasCertificate> emptyCertificates = new ArrayList<>();
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(emptyCertificates)));
        
        List<DocumentInfo> actual = acasService.getAcasCertificates(caseData, ACAS_CERT_LIST, AUTH_TOKEN,
                ENGLANDWALES_CASE_TYPE_ID);
        assertEquals(0, actual.size());
    }

}
