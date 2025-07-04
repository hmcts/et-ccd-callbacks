package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.configuration.PostcodeToOfficeMappings;
import uk.gov.hmcts.ecm.common.service.JurisdictionCodesMapperService;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.ecm.common.service.pdf.ET1PdfMapperService;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.utils.ResourceUtils.generateCaseDetails;

@SpringBootTest(classes = { Et1SubmissionService.class, Et1ReppedService.class, TribunalOfficesService.class,
    PostcodeToOfficeService.class, PdfService.class})
@EnableConfigurationProperties({ CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class,
    PostcodeToOfficeMappings.class })
class Et1SubmissionServiceTest {
    @MockitoBean
    private PostcodeToOfficeMappings postcodeToOfficeMappings;

    @MockitoBean
    private AcasService acasService;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private CcdCaseAssignment ccdCaseAssignment;
    @MockitoBean
    private DocumentManagementService documentManagementService;
    @MockitoBean
    private JurisdictionCodesMapperService jurisdictionCodesMapperService;
    @MockitoBean
    private OrganisationClient organisationClient;
    @MockitoBean
    private TornadoService tornadoService;
    @MockitoBean
    private UserIdamService userIdamService;
    @MockitoBean
    private AdminUserService adminUserService;
    @Mock
    private RestTemplate restTemplate;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private ET1PdfMapperService et1PdfMapperService;
    @MockitoBean
    private FeatureToggleService featureToggleService;

    private Et1SubmissionService et1SubmissionService;
    private CaseDetails caseDetails;
    @MockitoBean
    private Et1ReppedService et1ReppedService;

    @BeforeEach
    void setUp() {
        caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        emailService = spy(new EmailUtils());
        PdfService pdfService = new PdfService(new ET1PdfMapperService());
        PostcodeToOfficeService postcodeToOfficeService = new PostcodeToOfficeService(postcodeToOfficeMappings);
        TribunalOfficesService tribunalOfficesService = new TribunalOfficesService(new TribunalOfficesConfiguration(),
                postcodeToOfficeService);
        et1SubmissionService = new Et1SubmissionService(acasService, documentManagementService,
                pdfService, tornadoService, userIdamService, emailService, featureToggleService);
        et1ReppedService = new Et1ReppedService(authTokenGenerator, ccdCaseAssignment,
                jurisdictionCodesMapperService, organisationClient, postcodeToOfficeService, tribunalOfficesService,
                userIdamService, adminUserService, et1SubmissionService);
        ReflectionTestUtils.setField(et1SubmissionService, "et1ProfessionalSubmissionTemplateId",
                "ec815e00-39b0-4711-8b24-614ea1f2de89");
        ReflectionTestUtils.setField(et1SubmissionService, "claimantSubmissionTemplateId",
                "7b1f33eb-31c5-4a00-b1a4-c1bca84bc441");
        ReflectionTestUtils.setField(et1SubmissionService, "claimantSubmissionTemplateIdWelsh",
                "7b1f33eb-31c5-4a00-b1a4-c1bca84bc441");
    }

    @Test
    void createAndUploadEt1DocsMyHmcts() throws Exception {
        caseDetails =  generateCaseDetails("et1ReppedDraftStillWorking.json");
        Et1ReppedHelper.setEt1SubmitData(caseDetails.getCaseData());
        et1ReppedService.addDefaultData(caseDetails.getCaseTypeId(), caseDetails.getCaseData());

        caseDetails.getCaseData().setEt1SectionThreeDocumentUpload(UploadedDocumentBuilder.builder()
                .withUrl("http://test.com/documents/random-uuid")
                .withFilename("SupportingDoc.pdf")
                .build());

        DocumentInfo documentInfo = DocumentInfo.builder()
                .description("ET1 - John Doe")
                .url("http://test.com/documents/random-uuid")
                .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
                .build();
        when(tornadoService.createDocumentInfoFromBytes(anyString(), any(), anyString(), anyString()))
                .thenReturn(documentInfo);
        UploadedDocumentType uploadedDocument = UploadedDocumentBuilder.builder()
                .withUrl("http://test.com/documents/random-uuid")
                .withFilename("ET1 - John Doe.pdf")
                .build();
        when(documentManagementService.addDocumentToDocumentField(any())).thenReturn(uploadedDocument);
        DocumentInfo acasDocument = DocumentInfo.builder()
                .description("ACAS Certificate")
                .url("http://test.com/documents/random-uuid")
                .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
                .build();
        when(acasService.getAcasCertificates(any(), anyList(), anyString(), anyString()))
                .thenReturn(List.of(acasDocument));

        assertDoesNotThrow(() -> et1SubmissionService.createAndUploadEt1Docs(caseDetails, "authToken"));
        assertEquals(3, caseDetails.getCaseData().getDocumentCollection().size());
    }

    @Test
    void shouldAddDocsIfAcasCertThrowsError() throws Exception {
        caseDetails =  generateCaseDetails("et1ReppedDraftStillWorking.json");
        caseDetails.getCaseData().setRespondentAcasNumber("123456");
        Et1ReppedHelper.setEt1SubmitData(caseDetails.getCaseData());
        et1ReppedService.addDefaultData(caseDetails.getCaseTypeId(), caseDetails.getCaseData());

        DocumentInfo documentInfo = DocumentInfo.builder()
                .description("ET1 - John Doe")
                .url("http://test.com/documents/random-uuid")
                .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
                .build();
        when(tornadoService.createDocumentInfoFromBytes(anyString(), any(), anyString(), anyString()))
                .thenReturn(documentInfo);
        UploadedDocumentType uploadedDocument = UploadedDocumentBuilder.builder()
                .withUrl("http://test.com/documents/random-uuid")
                .withFilename("ET1 - John Doe.pdf")
                .build();
        when(documentManagementService.addDocumentToDocumentField(any())).thenReturn(uploadedDocument);

        assertDoesNotThrow(() -> et1SubmissionService.createAndUploadEt1Docs(caseDetails, "authToken"));
        assertEquals(1, caseDetails.getCaseData().getDocumentCollection().size());
    }

    @Test
    void shouldSendEmailMyHmcts() throws URISyntaxException, IOException {
        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        caseDetails =  generateCaseDetails("et1ReppedDraftStillWorking.json");
        Et1ReppedHelper.setEt1SubmitData(caseDetails.getCaseData());
        et1SubmissionService.sendEt1ConfirmationMyHmcts(caseDetails, "authToken");
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), any());
    }

    @Test
    void createAndUploadEt1DocsEt1() throws Exception {
        caseDetails =  generateCaseDetails("citizenCaseData.json");
        DocumentInfo documentInfo = DocumentInfo.builder()
                .description("ET1 - John Doe")
                .url("http://test.com/documents/random-uuid")
                .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
                .build();
        when(tornadoService.createDocumentInfoFromBytes(anyString(), any(), anyString(), anyString()))
                .thenReturn(documentInfo);
        UploadedDocumentType uploadedDocument = UploadedDocumentBuilder.builder()
                .withUrl("http://test.com/documents/random-uuid")
                .withFilename("ET1 - John Doe.pdf")
                .build();
        when(documentManagementService.addDocumentToDocumentField(any())).thenReturn(uploadedDocument);
        DocumentInfo acasDocument = DocumentInfo.builder()
                .description("ACAS Certificate")
                .url("http://test.com/documents/random-uuid")
                .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
                .build();
        // Mock the ACAS service to return a list of 5 documents as the test case is expecting 5
        when(acasService.getAcasCertificates(any(), anyList(), anyString(), anyString()))
                .thenReturn(List.of(acasDocument, acasDocument, acasDocument, acasDocument, acasDocument));

        assertDoesNotThrow(() -> et1SubmissionService.createAndUploadEt1Docs(caseDetails, "authToken"));
        assertEquals(7, caseDetails.getCaseData().getDocumentCollection().size());
    }

    @ParameterizedTest
    @MethodSource("shouldSendEmailClaimantArguments")
    void shouldSendEmailClaimant(String languagePreference) throws URISyntaxException, IOException {
        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        caseDetails =  generateCaseDetails("citizenCaseData.json");
        caseDetails.getCaseData().getClaimantHearingPreference().setContactLanguage(languagePreference);
        et1SubmissionService.sendEt1ConfirmationClaimant(caseDetails, "authToken");
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), any());
    }

    private static Stream<Arguments> shouldSendEmailClaimantArguments() {
        return Stream.of(
            Arguments.of(ENGLISH_LANGUAGE),
            Arguments.of(WELSH_LANGUAGE)
        );
    }

    @Test
    void shouldNotAddAcasDocsIfNewLogicIsEnabled() throws Exception {
        when(featureToggleService.isAcasCertificatePostSubmissionEnabled()).thenReturn(true);
        caseDetails =  generateCaseDetails("citizenCaseData.json");
        DocumentInfo documentInfo = DocumentInfo.builder()
                .description("ET1 - John Doe")
                .url("http://test.com/documents/random-uuid")
                .markUp("<a target=\"_blank\" href=\"https://test.com/documents/random-uuid\">Document</a>")
                .build();
        when(tornadoService.createDocumentInfoFromBytes(anyString(), any(), anyString(), anyString()))
                .thenReturn(documentInfo);
        UploadedDocumentType uploadedDocument = UploadedDocumentBuilder.builder()
                .withUrl("http://test.com/documents/random-uuid")
                .withFilename("ET1 - John Doe.pdf")
                .build();
        when(documentManagementService.addDocumentToDocumentField(any())).thenReturn(uploadedDocument);

        assertDoesNotThrow(() -> et1SubmissionService.createAndUploadEt1Docs(caseDetails, "authToken"));
        assertEquals(2, caseDetails.getCaseData().getDocumentCollection().size());
        assertEquals(YES, caseDetails.getCaseData().getAcasCertificateRequired());
        verify(acasService, times(0)).getAcasCertificates(any(), anyList(), anyString(), anyString());
    }
}
