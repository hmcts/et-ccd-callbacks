package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.DocumentDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService.APPLICATION_DOCX_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.successfulDocStoreUpload;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.successfulDocumentManagementUploadResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.unsuccessfulDocumentManagementUploadResponse;

@SuppressWarnings({"PMD.AvoidPrintStackTrace", "PMD.LinguisticNaming", "PMD.ExcessiveImports"})
@RunWith(SpringJUnit4ClassRunner.class)
public class DocumentManagementServiceTest {

    @Mock
    private DocumentUploadClientApi documentUploadClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserService userService;
    @Mock
    private DocumentDownloadClientApi documentDownloadClientApi;
    @Mock
    private CaseDocumentClient caseDocumentClient;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private DocumentManagementService documentManagementService;
    @InjectMocks
    private DocumentGenerationService documentGenerationService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private File file;
    private String markup;
    private ResponseEntity<Resource> responseEntity;

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String AUTH_TOKEN = "Bearer authToken";

    @Before
    public void setUp() {
        file = createTestFile();
        markup = "<a target=\"_blank\" href=\"null/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary\">Document</a>";
        when(authTokenGenerator.generate()).thenReturn("authString");
        responseEntity = MultipleUtil.getResponseOK();
        UserDetails userDetails = HelperTest.getUserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        ReflectionTestUtils.setField(documentManagementService, "ccdDMStoreBaseUrl", "http://dm-store:8080");
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", false);
    }

    @Test
    public void shouldUploadToDocumentManagement() throws IOException, URISyntaxException {
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList(), any(), anyList()))
                .thenReturn(successfulDocumentManagementUploadResponse());
        URI documentSelfPath = documentManagementService.uploadDocument("authString", Files.readAllBytes(file.toPath()),
                OUTPUT_FILE_NAME, APPLICATION_DOCX_VALUE, anyString());
        String documentDownloadableURL = documentManagementService.generateDownloadableURL(documentSelfPath);
        assertEquals(documentManagementService.generateMarkupDocument(documentDownloadableURL), markup);
        assertNotNull(documentSelfPath);
        assertEquals("/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4", documentSelfPath.getPath());
    }

    @Test
    public void uploadDocumentToDocumentManagementThrowsException() throws IOException, URISyntaxException {
        expectedException.expect(DocumentManagementException.class);
        expectedException.expectMessage("Unable to upload document document.docx to document management");
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(unsuccessfulDocumentManagementUploadResponse());
        documentManagementService.uploadDocument("authString", Files.readAllBytes(file.toPath()),
                OUTPUT_FILE_NAME, APPLICATION_DOCX_VALUE, anyString());
    }

    private File createTestFile() {
        Path path = Paths.get(OUTPUT_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("Hello World !!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path.toFile();
    }

    @Test
    public void downloadFile() {
        when(documentDownloadClientApi.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(responseEntity);

        UploadedDocument uploadedDocument = documentManagementService.downloadFile("authString",
                "http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");
        assertEquals("fileName", uploadedDocument.getName());
        assertEquals("xslx", uploadedDocument.getContentType());

        uploadedDocument = documentManagementService.downloadFile("authString",
                "documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");
        assertEquals("fileName", uploadedDocument.getName());
        assertEquals("xslx", uploadedDocument.getContentType());
    }

    @Test(expected = IllegalStateException.class)
    public void downloadFileException() {
        when(documentDownloadClientApi.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY));
        documentManagementService.downloadFile("authString",
                "documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");
    }

    @Test
    public void getDocumentUUID() {
        String urlString = "http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary";
        assertEquals("85d97996-22a5-40d7-882e-3a382c8ae1b4", documentManagementService.getDocumentUUID(urlString));
    }

    @Test
    public void downloadFileSecureDocStoreTrue() {
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", true);
        when(documentDownloadClientApi.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(responseEntity);
        UploadedDocument uploadedDocument = documentManagementService.downloadFile("authString",
            "http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");
        assertEquals("fileName", uploadedDocument.getName());
        assertEquals("xslx", uploadedDocument.getContentType());
    }

    @Test
    public void uploadFileSecureDocStoreTrue() throws URISyntaxException, IOException {
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", true);
        when(caseDocumentClient.uploadDocuments(anyString(), anyString(), anyString(), anyString(), anyList(), any()))
                .thenReturn(successfulDocStoreUpload());
        URI documentSelfPath = documentManagementService.uploadDocument("authString",
                Files.readAllBytes(file.toPath()), OUTPUT_FILE_NAME, APPLICATION_DOCX_VALUE, "LondonSouth");
        String documentDownloadableURL = documentManagementService.generateDownloadableURL(documentSelfPath);
        assertEquals(documentManagementService.generateMarkupDocument(documentDownloadableURL), markup);
        assertNotNull(documentSelfPath);
        assertEquals("/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4", documentSelfPath.getPath());
    }

    @Test
    public void addDocumentToDocumentField() {
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setDescription("Test description");
        documentInfo.setUrl("http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");

        UploadedDocumentType uploadedDocumentType = documentManagementService.addDocumentToDocumentField(documentInfo);
        assertThat(uploadedDocumentType.getDocumentFilename()).isEqualTo(documentInfo.getDescription());
        assertThat(uploadedDocumentType.getDocumentBinaryUrl()).isEqualTo(documentInfo.getUrl());
    }

    @Test
    public void displayDocNameTypeSizeLink_GetSuccess_ReturnString() {
        DocumentDetails documentDetails = DocumentDetails.builder()
                .size("2000").mimeType("mimeType").hashToken("token").createdOn("createdOn").createdBy("createdBy")
                .lastModifiedBy("lastModifiedBy").modifiedOn("modifiedOn").ttl("ttl")
                .metadata(Map.of("test", "test"))
                .originalDocumentName("docName.txt").classification("PUBLIC")
                .links(Map.of("self", Map.of("href", "TestURL.com")))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, AUTH_TOKEN);
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        ResponseEntity<DocumentDetails> response = new ResponseEntity<>(documentDetails, headers, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(DocumentDetails.class)))
                .thenReturn(response);

        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setDescription("Test.txt");
        documentInfo.setUrl("http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");
        UploadedDocumentType uploadedDocumentType = documentManagementService.addDocumentToDocumentField(documentInfo);
        String actual = documentManagementService.displayDocNameTypeSizeLink(uploadedDocumentType, AUTH_TOKEN);

        String size = FileUtils.byteCountToDisplaySize(Long.parseLong("2000"));
        String expected = "<a href=\"/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary\" target=\"_blank\">Test (TXT, " + size + ")</a>";

        assertEquals(expected, actual);
    }

    @Test
    public void setBFAction() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        documentGenerationService.setBfActions(caseData);

        LocalDate servingDate = LocalDate.parse(caseData.getClaimServedDate());
        LocalDate et3DueDate = LocalDate.parse(caseData.getEt3DueDate());
        assertThat(et3DueDate).isEqualTo(servingDate.plusDays(28));
    }
}
