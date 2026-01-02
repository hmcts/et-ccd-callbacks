package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.configuration.PostcodeToOfficeMappings;
import uk.gov.hmcts.ecm.common.service.JurisdictionCodesMapperService;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.ecm.common.service.pdf.ET1PdfMapperService;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.utils.ResourceUtils.generateCaseDetails;

@SpringBootTest(classes = { Et1SubmissionService.class, Et1ReppedService.class, TribunalOfficesService.class,
    PostcodeToOfficeService.class, PdfService.class})
@EnableConfigurationProperties({ CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class,
    PostcodeToOfficeMappings.class })
class Et1SubmissionServiceTest {
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
    private EmailService emailService;
    @MockBean
    private ET1PdfMapperService et1PdfMapperService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private MyHmctsService myHmctsService;

    private Et1SubmissionService et1SubmissionService;
    private CaseDetails caseDetails;
    @MockBean
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
                pdfService, tornadoService, userIdamService, emailService, featureToggleService, ccdClient);
        et1ReppedService = new Et1ReppedService(authTokenGenerator, ccdCaseAssignment,
                jurisdictionCodesMapperService, organisationClient, postcodeToOfficeService, tribunalOfficesService,
                userIdamService, adminUserService, et1SubmissionService, myHmctsService);
        ReflectionTestUtils.setField(et1SubmissionService, "et1ProfessionalSubmissionTemplateId",
                "ec815e00-39b0-4711-8b24-614ea1f2de89");
        ReflectionTestUtils.setField(et1SubmissionService, "claimantSubmissionTemplateId",
                "7b1f33eb-31c5-4a00-b1a4-c1bca84bc441");
        ReflectionTestUtils.setField(et1SubmissionService, "claimantSubmissionTemplateIdWelsh",
                "7b1f33eb-31c5-4a00-b1a4-c1bca84bc441");
    }

    @Test
    @SneakyThrows
    void createAndUploadEt1DocsMyHmcts() {
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
    @SneakyThrows
    void shouldAddDocsIfAcasCertThrowsError() {
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
    @SneakyThrows
    void shouldSendEmailMyHmcts() {
        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        caseDetails =  generateCaseDetails("et1ReppedDraftStillWorking.json");
        Et1ReppedHelper.setEt1SubmitData(caseDetails.getCaseData());
        et1SubmissionService.sendEt1ConfirmationMyHmcts(caseDetails, "authToken");
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), any());
    }

    @Test
    @SneakyThrows
    void createAndUploadEt1DocsEt1() {
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
    @SneakyThrows
    @MethodSource("shouldSendEmailClaimantArguments")
    void shouldSendEmailClaimant(String languagePreference) {
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
    @SneakyThrows
    void shouldNotAddAcasDocsIfNewLogicIsEnabled() {
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

    @SneakyThrows
    @Test
    void shouldNotAddVexationNotes() {
        when(featureToggleService.isFeatureEnabled("vexationCheck")).thenReturn(true);
        caseDetails = generateCaseDetails("citizenCaseData.json");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
            .thenReturn(Collections.emptyList());
        et1SubmissionService.vexationCheck(caseDetails, "authToken");
        assertNull(caseDetails.getCaseData().getCaseNotes());
        assertNull(caseDetails.getCaseData().getAdditionalCaseInfoType());
    }

    @SneakyThrows
    @Test
    void shouldAddVexationNotes() {
        when(featureToggleService.isFeatureEnabled("vexationCheck")).thenReturn(true);
        caseDetails = generateCaseDetails("citizenCaseData.json");
        // CCD client will be called twice to return 4 cases which will trigger adding vexation notes
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
            .thenReturn(createSubmitEventList());
        et1SubmissionService.vexationCheck(caseDetails, "authToken");
        assertEquals(YES, caseDetails.getCaseData().getAdditionalCaseInfoType().getInterventionRequired());
        assertTrue(caseDetails.getCaseData().getFlagsImageAltText().contains("SPEAK TO REJ"));
    }

    @SneakyThrows
    @Test
    void shouldAddVexationNotesWhenToggleIsOff() {
        when(featureToggleService.isFeatureEnabled("vexationCheck")).thenReturn(false);
        caseDetails = generateCaseDetails("citizenCaseData.json");
        et1SubmissionService.vexationCheck(caseDetails, "authToken");
        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(anyString(), anyString(), anyString());

    }

    private List<SubmitEvent> createSubmitEventList() {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setState(SUBMITTED_STATE);
        CaseData caseData = new CaseData();
        caseData.setClaimant("Michael Jackson");
        caseData.setRespondent(RandomString.make(10));
        caseData.setReceiptDate(LocalDate.now().minusDays(RandomUtils.secure().randomInt(10, 90)).toString());
        caseData.setEthosCaseReference("6000010/2025");
        submitEvent.setCaseData(caseData);
        // Returns a list of 2 cases
        return List.of(submitEvent, submitEvent);
    }

    @SneakyThrows
    @Test
    void shouldNotAddVexationNotesIfCcdThrowsAnError() {
        caseDetails = generateCaseDetails("citizenCaseData.json");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
            .thenThrow(new IOException("CCD error"));
        et1SubmissionService.vexationCheck(caseDetails, "authToken");
        assertNull(caseDetails.getCaseData().getCaseNotes());
        assertNull(caseDetails.getCaseData().getAdditionalCaseInfoType());
    }
}
