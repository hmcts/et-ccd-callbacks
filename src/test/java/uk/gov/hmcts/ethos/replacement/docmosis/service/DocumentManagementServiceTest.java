package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.DocumentDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

@ExtendWith(SpringExtension.class)
class DocumentManagementServiceTest {

    @Mock
    private DocumentUploadClientApi documentUploadClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserIdamService userIdamService;
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
    private static final String DOC_FILE_NAME_1 = "DOC_FILE_NAME_1";
    private static final String DOC_FILE_NAME_2 = "DOC_FILE_NAME_2";
    private static final String DOC_FILE_NAME_3 = "DOC_FILE_NAME_3";

    @BeforeEach
    void setUp() {
        file = createTestFile();
        markup =
                "<a target=\"_blank\" href=\"null/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary\">Document</a>";
        when(authTokenGenerator.generate()).thenReturn("authString");
        responseEntity = MultipleUtil.getResponseOK();
        UserDetails userDetails = HelperTest.getUserDetails();
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
        ReflectionTestUtils.setField(documentManagementService, "ccdDMStoreBaseUrl", "http://dm-store:8080");
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", false);
    }

    @Test
    void shouldUploadToDocumentManagement() throws IOException, URISyntaxException {
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
    @Disabled // Mock is returning null - cannot map to an UploadResponse
    void uploadDocumentToDocumentManagementThrowsException() throws IOException, URISyntaxException {
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList()))
                .thenReturn(unsuccessfulDocumentManagementUploadResponse());

        DocumentManagementException ex = assertThrows(DocumentManagementException.class, () ->
                documentManagementService.uploadDocument("authString", Files.readAllBytes(file.toPath()),
                        OUTPUT_FILE_NAME, APPLICATION_DOCX_VALUE, "123")
        );
        assertEquals("Unable to upload document document.docx to document management", ex.getMessage());
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
    void downloadFile() {
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

    @Test
    void downloadFileException() {
        when(documentDownloadClientApi.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY));

        assertThrows(IllegalStateException.class, () ->
                documentManagementService.downloadFile("authString",
                        "documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary")
        );
    }

    @Test
    void getDocumentUUID() {
        String urlString = "http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary";
        assertEquals("85d97996-22a5-40d7-882e-3a382c8ae1b4", documentManagementService.getDocumentUUID(urlString));
    }

    @Test
    void downloadFileSecureDocStoreTrue() {
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", true);
        when(documentDownloadClientApi.downloadBinary(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(responseEntity);
        UploadedDocument uploadedDocument = documentManagementService.downloadFile("authString",
                "http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");
        assertEquals("fileName", uploadedDocument.getName());
        assertEquals("xslx", uploadedDocument.getContentType());
    }

    @Test
    void uploadFileSecureDocStoreTrue() throws URISyntaxException, IOException {
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
    void addDocumentToDocumentField() {
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setDescription("Test description");
        documentInfo.setUrl("http://dm-store:8080/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary");

        UploadedDocumentType uploadedDocumentType = documentManagementService.addDocumentToDocumentField(documentInfo);
        assertThat(uploadedDocumentType.getDocumentFilename()).isEqualTo(documentInfo.getDescription());
        assertThat(uploadedDocumentType.getDocumentBinaryUrl()).isEqualTo(documentInfo.getUrl());
    }

    @Test
    void displayDocNameTypeSizeLink_GetSuccess_ReturnString() {
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
        String expected =
                "<a href=\"/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4/binary\" target=\"_blank\">Test (TXT, "
                        + size + ")</a>";

        assertEquals(expected, actual);
    }

    @Test
    void setBFAction() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        documentGenerationService.setBfActions(caseData);

        LocalDate servingDate = LocalDate.parse(caseData.getClaimServedDate());
        LocalDate et3DueDate = LocalDate.parse(caseData.getEt3DueDate());

        assertThat(et3DueDate).isEqualTo(servingDate.plusDays(28));
    }

    @Test
    void addUploadedDocsToCaseDocCollection_WithNullDateOfCorrespondence() {
        DocumentTypeItem doc1 = new DocumentTypeItem();
        DocumentType dt = new DocumentType();
        dt.setTopLevelDocuments("ET1 Vetting");
        doc1.setValue(dt);
        UploadedDocumentType uploadedDocType1 = new UploadedDocumentType();
        uploadedDocType1.setDocumentUrl("test doc url");
        uploadedDocType1.setDocumentFilename("test file name");
        uploadedDocType1.setDocumentBinaryUrl("test binary doc url");
        doc1.getValue().setUploadedDocument(uploadedDocType1);

        doc1.getValue().setDocNumber("1");
        doc1.getValue().setDocumentIndex("1");
        doc1.getValue().setTopLevelDocuments("ET1 Vetting");
        doc1.getValue().setTypeOfDocument("ET1 being vetted");
        CaseData caseData = new CaseData();
        caseData.setAddDocumentCollection(new ArrayList<>());
        caseData.getAddDocumentCollection().add(doc1);

        DocumentType dt4 = new DocumentType();
        DocumentTypeItem doc4 = new DocumentTypeItem();
        doc4.setValue(dt4);
        UploadedDocumentType uploadedDocType4 = new UploadedDocumentType();
        uploadedDocType4.setDocumentUrl("test doc url 4");
        uploadedDocType4.setDocumentFilename("test file name 4");
        uploadedDocType4.setDocumentBinaryUrl("test binary doc url 4");

        doc4.getValue().setUploadedDocument(uploadedDocType4);
        doc4.getValue().setDateOfCorrespondence("2024-03-04");
        doc4.getValue().setDocNumber("2");
        doc1.getValue().setDocumentIndex("2");
        doc4.getValue().setTopLevelDocuments("ET3");
        doc4.getValue().setTypeOfDocument("ET3 reconsidered");
        caseData.setDocumentCollection(new ArrayList<>());
        caseData.getDocumentCollection().add(doc4);

        documentManagementService.addUploadedDocsToCaseDocCollection(caseData);

        assertEquals(2, caseData.getDocumentCollection().size());
        assertNull(caseData.getDocumentCollection().get(1).getValue().getDateOfCorrespondence());
    }

    @Test
    void addUploadedDocsToCaseDocCollection_DocumentCollection_NotNull() {
        DocumentTypeItem doc1 = new DocumentTypeItem();
        DocumentType dt = new DocumentType();
        dt.setTopLevelDocuments("ET3");
        doc1.setValue(dt);

        UploadedDocumentType uploadedDocType1 = new UploadedDocumentType();
        uploadedDocType1.setDocumentUrl("test doc url");
        uploadedDocType1.setDocumentFilename("test file name");
        uploadedDocType1.setDocumentBinaryUrl("test binary doc url");
        doc1.getValue().setUploadedDocument(uploadedDocType1);
        doc1.getValue().setDateOfCorrespondence("2024-03-04");
        doc1.getValue().setDocNumber("2");
        doc1.getValue().setDocumentIndex("2");
        doc1.getValue().setTopLevelDocuments("ET3");
        doc1.getValue().setTypeOfDocument("ET3 being vetted");

        //docs that already existing in collection
        UploadedDocumentType uploadedDocType2 = new UploadedDocumentType();
        uploadedDocType2.setDocumentUrl("test doc url 2");
        uploadedDocType2.setDocumentFilename("test file name 2");
        uploadedDocType2.setDocumentBinaryUrl("test binary doc url 2");
        DocumentTypeItem doc2 = new DocumentTypeItem();
        DocumentType dt2 = new DocumentType();
        doc2.setValue(dt2);
        doc2.getValue().setUploadedDocument(uploadedDocType2);
        doc2.getValue().setDateOfCorrespondence("2024-03-04");
        doc2.getValue().setDocNumber("1");
        doc2.getValue().setDocumentIndex("1");
        doc2.getValue().setTopLevelDocuments("ET3");
        doc2.getValue().setTypeOfDocument("ET3 Accepted");

        UploadedDocumentType uploadedDocType3 = new UploadedDocumentType();
        uploadedDocType3.setDocumentUrl("test doc url 3");
        uploadedDocType3.setDocumentFilename("test file name 3");
        uploadedDocType3.setDocumentBinaryUrl("test binary doc url 3");

        DocumentTypeItem doc3 = new DocumentTypeItem();
        DocumentType dt3 = new DocumentType();
        doc3.setValue(dt3);
        doc3.getValue().setUploadedDocument(uploadedDocType3);
        doc3.getValue().setDateOfCorrespondence("2024-03-04");
        doc3.getValue().setDocNumber("2");
        doc3.getValue().setDocumentIndex("2");
        doc3.getValue().setTopLevelDocuments("ET3");
        doc3.getValue().setTypeOfDocument("ET3 rejected");

        CaseData caseData = new CaseData();
        caseData.setDocumentCollection(new ArrayList<>());
        caseData.getDocumentCollection().add(doc2);
        caseData.getDocumentCollection().add(doc3);
        caseData.setAddDocumentCollection(new ArrayList<>());
        caseData.getAddDocumentCollection().add(doc1);
        documentManagementService.addUploadedDocsToCaseDocCollection(caseData);

        assertEquals(3, caseData.getDocumentCollection().size());
        assertEquals("2024-03-04",
                caseData.getDocumentCollection().get(2).getValue().getDateOfCorrespondence());

    }

    @Test
    void addUploadedDocsToCaseDocCollection_DocumentCollection_Null() {
        DocumentTypeItem doc1 = new DocumentTypeItem();
        DocumentType dt = new DocumentType();
        dt.setTopLevelDocuments("ET1 Vetting");
        doc1.setValue(dt);
        UploadedDocumentType uploadedDocType1 = new UploadedDocumentType();
        uploadedDocType1.setDocumentUrl("test doc url");
        uploadedDocType1.setDocumentFilename("test file name");
        uploadedDocType1.setDocumentBinaryUrl("test binary doc url");
        doc1.getValue().setUploadedDocument(uploadedDocType1);

        doc1.getValue().setDocNumber("1");
        doc1.getValue().setDocumentIndex("1");
        doc1.getValue().setTopLevelDocuments("ET1 Vetting");
        doc1.getValue().setTypeOfDocument("ET1 vetted");
        CaseData caseData = new CaseData();
        caseData.setAddDocumentCollection(new ArrayList<>());
        caseData.getAddDocumentCollection().add(doc1);

        documentManagementService.addUploadedDocsToCaseDocCollection(caseData);

        assertEquals(1, caseData.getDocumentCollection().size());
        assertNotNull(caseData.getDocumentCollection());
        assertNull(caseData.getDocumentCollection().getFirst().getValue().getDateOfCorrespondence());
    }

    @Test
    void addUploadedDocsToCaseDocCollection_WithDocumentIndex() {
        DocumentTypeItem doc1 = new DocumentTypeItem();
        DocumentType dt = new DocumentType();
        dt.setTopLevelDocuments("ET1 Vetting");
        doc1.setValue(dt);
        UploadedDocumentType uploadedDocType1 = new UploadedDocumentType();
        uploadedDocType1.setDocumentUrl("test doc url");
        uploadedDocType1.setDocumentFilename(DOC_FILE_NAME_1);
        uploadedDocType1.setDocumentBinaryUrl("test binary doc url");
        doc1.getValue().setUploadedDocument(uploadedDocType1);

        doc1.getValue().setDocumentIndex("1");
        doc1.getValue().setTopLevelDocuments("ET1 Vetting");
        doc1.getValue().setTypeOfDocument("ET1 being vetted");
        CaseData caseData = new CaseData();
        caseData.setAddDocumentCollection(new ArrayList<>());
        caseData.getAddDocumentCollection().add(doc1);

        DocumentType dt4 = new DocumentType();
        DocumentTypeItem doc4 = new DocumentTypeItem();
        doc4.setValue(dt4);
        UploadedDocumentType uploadedDocType4 = new UploadedDocumentType();
        uploadedDocType4.setDocumentUrl("test doc url 4");
        uploadedDocType4.setDocumentFilename(DOC_FILE_NAME_2);
        uploadedDocType4.setDocumentBinaryUrl("test binary doc url 4");

        doc4.getValue().setUploadedDocument(uploadedDocType4);
        doc4.getValue().setDateOfCorrespondence("2024-03-04");
        doc4.getValue().setDocumentIndex("2");
        doc4.getValue().setTopLevelDocuments("ET3");
        doc4.getValue().setTypeOfDocument("ET3 reconsidered");
        caseData.getAddDocumentCollection().add(doc4);

        documentManagementService.addUploadedDocsToCaseDocCollection(caseData);

        assertEquals(2, caseData.getDocumentCollection().size());
        assertEquals(DOC_FILE_NAME_1,
                caseData.getDocumentCollection().get(0).getValue().getUploadedDocument().getDocumentFilename());
        assertEquals(DOC_FILE_NAME_2,
                caseData.getDocumentCollection().get(1).getValue().getUploadedDocument().getDocumentFilename());
    }

    @Test
    void addUploadedDocsToCaseDocCollection_WithoutDocumentIndex() {
        DocumentTypeItem doc1 = new DocumentTypeItem();
        DocumentType dt = new DocumentType();
        dt.setTopLevelDocuments("ET1 Vetting");
        doc1.setValue(dt);
        UploadedDocumentType uploadedDocType1 = new UploadedDocumentType();
        uploadedDocType1.setDocumentUrl("test doc url");
        uploadedDocType1.setDocumentFilename(DOC_FILE_NAME_1);
        uploadedDocType1.setDocumentBinaryUrl("test binary doc url");
        doc1.getValue().setUploadedDocument(uploadedDocType1);

        doc1.getValue().setDocumentIndex("1");
        doc1.getValue().setTopLevelDocuments("ET1 Vetting");
        doc1.getValue().setTypeOfDocument("ET1 being vetted");
        CaseData caseData = new CaseData();
        caseData.setAddDocumentCollection(new ArrayList<>());
        caseData.getAddDocumentCollection().add(doc1);

        DocumentType dt2 = new DocumentType();
        DocumentTypeItem doc2 = new DocumentTypeItem();
        doc2.setValue(dt2);
        UploadedDocumentType uploadedDocType2 = new UploadedDocumentType();
        uploadedDocType2.setDocumentUrl("test doc url 4");
        uploadedDocType2.setDocumentFilename(DOC_FILE_NAME_2);
        uploadedDocType2.setDocumentBinaryUrl("test binary doc url 4");

        doc2.getValue().setUploadedDocument(uploadedDocType2);
        doc2.getValue().setDateOfCorrespondence("2024-03-04");
        doc2.getValue().setTopLevelDocuments("ET3");
        doc2.getValue().setTypeOfDocument("ET3 reconsidered");
        caseData.getAddDocumentCollection().add(doc2);

        DocumentType dt4 = new DocumentType();
        DocumentTypeItem doc4 = new DocumentTypeItem();
        doc4.setValue(dt4);
        UploadedDocumentType uploadedDocType4 = new UploadedDocumentType();
        uploadedDocType4.setDocumentUrl("test doc url 4");
        uploadedDocType4.setDocumentFilename(DOC_FILE_NAME_3);
        uploadedDocType4.setDocumentBinaryUrl("test binary doc url 4");

        doc4.getValue().setUploadedDocument(uploadedDocType4);
        doc4.getValue().setDateOfCorrespondence("2024-03-04");
        doc4.getValue().setDocumentIndex("2");
        doc4.getValue().setTopLevelDocuments("ET3");
        doc4.getValue().setTypeOfDocument("ET3 reconsidered");
        caseData.getAddDocumentCollection().add(doc4);

        documentManagementService.addUploadedDocsToCaseDocCollection(caseData);

        assertEquals(3, caseData.getDocumentCollection().size());
        assertEquals(DOC_FILE_NAME_1,
                caseData.getDocumentCollection().get(0).getValue().getUploadedDocument().getDocumentFilename());
        assertEquals(DOC_FILE_NAME_3,
                caseData.getDocumentCollection().get(1).getValue().getUploadedDocument().getDocumentFilename());
        assertEquals(DOC_FILE_NAME_2,
                caseData.getDocumentCollection().get(2).getValue().getUploadedDocument().getDocumentFilename());
    }

    @Test
    void uploadDocument_shouldThrowException_whenSecureDocStoreDisabledAndUploadFails() throws IOException {
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", false);
        MultipartFile multipartFile = new InMemoryMultipartFile("files", "test.docx",
                "application/docx", "content".getBytes());
        byte[] fileBytes = multipartFile.getBytes();
        UserDetails userDetails = HelperTest.getUserDetails();

        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList(), any(), anyList()))
                .thenThrow(new DocumentManagementException("Upload failed"));

        DocumentManagementException exception = assertThrows(
                DocumentManagementException.class,
                () -> documentManagementService.uploadDocument(
                        "authToken",
                        fileBytes,
                        "test.docx",
                        "application/docx",
                        "caseType")
        );

        assertEquals("Unable to upload document test.docx to document management", exception.getMessage());
    }

    @Test
    void uploadDocument_shouldThrowException_whenSecureDocStoreEnabledAndUploadFails() throws IOException {
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", true);
        MultipartFile multipartFile = new InMemoryMultipartFile("files", "test.docx",
                "application/docx", "content".getBytes());
        byte[] fileBytes = multipartFile.getBytes();

        when(caseDocumentClient.uploadDocuments(anyString(), anyString(), anyString(), anyString(), anyList(), any()))
                .thenThrow(new DocumentManagementException("Upload failed"));

        DocumentManagementException exception = assertThrows(
                DocumentManagementException.class,
                () -> documentManagementService.uploadDocument(
                        "authToken",
                        fileBytes,
                        "test.docx",
                        "application/docx",
                        "caseType")
        );

        assertEquals("Unable to upload document test.docx to document management", exception.getMessage());
    }

    @Test
    void uploadDocument_shouldReturnUri_whenSecureDocStoreDisabled() throws Exception {
        ReflectionTestUtils.setField(documentManagementService, "secureDocStoreEnabled", false);
        MultipartFile multipartFile = new InMemoryMultipartFile("files", "test.docx",
                "application/docx", "content".getBytes());
        UploadResponse response = successfulDocumentManagementUploadResponse();
        UserDetails userDetails = HelperTest.getUserDetails();
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);
        when(documentUploadClient.upload(anyString(), anyString(), anyString(), anyList(), any(), anyList()))
                .thenReturn(response);

        URI result = documentManagementService.uploadDocument("authToken", multipartFile.getBytes(),
                "test.docx", "application/docx", "caseType");

        assertEquals("/documents/85d97996-22a5-40d7-882e-3a382c8ae1b4", result.getPath());
    }

    @ParameterizedTest
    @MethodSource("provideDocumentTypeItemsForCreateLinkToBinaryDocument")
    void createLinkToBinaryDocument_parameterized(DocumentTypeItem item, String expected) {
        String result = DocumentManagementService.createLinkToBinaryDocument(item);
        org.junit.jupiter.api.Assertions.assertEquals(expected, result);
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments>
        provideDocumentTypeItemsForCreateLinkToBinaryDocument() {
        UploadedDocumentType docWithUrl = new UploadedDocumentType();
        docWithUrl.setDocumentBinaryUrl("http://dm-store:8080/documents/abc-123/binary");
        DocumentType typeWithUrl = new DocumentType();
        typeWithUrl.setUploadedDocument(docWithUrl);
        DocumentTypeItem itemWithUrl = new DocumentTypeItem();
        itemWithUrl.setValue(typeWithUrl);

        UploadedDocumentType docWithNull = new UploadedDocumentType();
        docWithNull.setDocumentBinaryUrl(null);
        DocumentType typeWithNull = new DocumentType();
        typeWithNull.setUploadedDocument(docWithNull);
        DocumentTypeItem itemWithNull = new DocumentTypeItem();
        itemWithNull.setValue(typeWithNull);

        UploadedDocumentType docWithNoDocuments = new UploadedDocumentType();
        docWithNoDocuments.setDocumentBinaryUrl("http://example.com/file.pdf");
        DocumentType typeWithNoDocuments = new DocumentType();
        typeWithNoDocuments.setUploadedDocument(docWithNoDocuments);
        DocumentTypeItem itemWithNoDocuments = new DocumentTypeItem();
        itemWithNoDocuments.setValue(typeWithNoDocuments);

        return java.util.stream.Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(itemWithUrl, "/documents/abc-123/binary"),
                org.junit.jupiter.params.provider.Arguments.of(itemWithNull, ""),
                org.junit.jupiter.params.provider.Arguments.of(itemWithNoDocuments, "")
        );
    }
}
