package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.SUBMIT_ET3;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

@ExtendWith(SpringExtension.class)
class Et3ResponseServiceTest {
    private Et3ResponseService et3ResponseService;
    @MockitoBean
    private DocumentManagementService documentManagementService;
    @MockitoBean
    private PdfBoxService pdfBoxService;
    @MockitoBean
    private UserIdamService userIdamService;
    @MockitoBean
    private CcdCaseAssignment ccdCaseAssignment;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private MyHmctsService myHmctsService;

    private static final String VALID_USER_TOKEN = "validUserToken";
    private static final String ERROR_PDF_BOX_SERVICE_GENERATE_PDF_DOCUMENT_INFO =
            "Dummy pdf box service generate pdf document info error";
    private static final String ADDRESS_LINE_1 = "addressLine1";
    private static final String ADDRESS_LINE_2 = "addressLine2";
    private static final String ADDRESS_LINE_3 = "addressLine3";
    private static final String POST_CODE = "postalCode";
    private static final String COUNTRY = "country";
    private static final String COUNTY = "county";
    private static final String POST_TOWN = "postTown";

    private EmailService emailService;
    private CaseData caseData;
    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        emailService = spy(new EmailUtils());

        et3ResponseService = new Et3ResponseService(documentManagementService, pdfBoxService, emailService,
                myHmctsService);
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("Doris", "Johnson")
            .withClaimantType("232 Petticoat Square", "3 House", null,
                "London", "W10 4AG", "United Kingdom")
            .withRespondentWithAddress("Antonio Vazquez",
                "11 Small Street", "22 House", null,
                "Manchester", "M12 42R", "United Kingdom",
                "1234/5678/90")
            .withEt3RepresentingRespondent("Antonio Vazquez")
            .withSubmitEt3Respondent("Antonio Vazquez")
            .build();
        caseData.setEt3NoEt3Response("Test data");
        documentInfo = DocumentInfo.builder()
            .description("test-description")
            .url("https://test.com/documents/random-uuid")
            .build();
        doCallRealMethod().when(documentManagementService).addDocumentToDocumentField(documentInfo);
    }

    @Test
    @SneakyThrows
    void generateEt3ResponseDocumentNullWithoutException() {
        // when pdfboxService.generatePdfDocumentInfo returns null
        try (MockedStatic<ET3FormMapper> et3FormMapperMockedStatic = Mockito.mockStatic(ET3FormMapper.class)) {
            et3FormMapperMockedStatic.when(() -> ET3FormMapper.mapEt3Form(any(), anyString())).thenReturn(
                    new ConcurrentHashMap<String, Optional<String>>());
            when(documentManagementService.uploadDocument(anyString(), any(), anyString(), anyString(), anyString()))
                    .thenReturn(new URI("testUri"));
            when(pdfBoxService.generatePdfDocumentInfo(any(), anyString(),
                    anyString(), anyString(), anyString(), anyString())).thenReturn(null);
            CaseData testGeneratePdfCaseData = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
            DocumentInfo documentInfo1 = et3ResponseService.generateEt3ResponseDocument(testGeneratePdfCaseData,
                    "userToken", ENGLANDWALES_CASE_TYPE_ID, SUBMIT_ET3);
            assertThat(documentInfo1).isNull();
        }
        // when pdfboxService.generatePdfDocumentInfo returns documentInfo
        try (MockedStatic<ET3FormMapper> et3FormMapperMockedStatic = Mockito.mockStatic(ET3FormMapper.class)) {
            et3FormMapperMockedStatic.when(() -> ET3FormMapper.mapEt3Form(any(), anyString())).thenThrow(
                    new GenericServiceException(
                            ERROR_PDF_BOX_SERVICE_GENERATE_PDF_DOCUMENT_INFO,
                            new Throwable(ERROR_PDF_BOX_SERVICE_GENERATE_PDF_DOCUMENT_INFO),
                            ERROR_PDF_BOX_SERVICE_GENERATE_PDF_DOCUMENT_INFO,
                            "123456",
                            "PDFBoxService",
                            "generatePdfDocumentInfo"));
            when(documentManagementService.uploadDocument(anyString(), any(), anyString(), anyString(), anyString()))
                    .thenReturn(new URI("testUri"));
            when(pdfBoxService.generatePdfDocumentInfo(any(), anyString(),
                    anyString(), anyString(), anyString(), anyString())).thenReturn(documentInfo);
            assertDoesNotThrow(() -> et3ResponseService.generateEt3ResponseDocument(new CaseData(), "userToken",
                    ENGLANDWALES_CASE_TYPE_ID, SUBMIT_ET3));
        }
        // when pdfboxService.generatePdfDocumentInfo throws exception
        when(pdfBoxService.generatePdfDocumentInfo(any(), anyString(),
                anyString(), anyString(), eq(null), anyString()))
                .thenThrow(new IOException(ERROR_PDF_BOX_SERVICE_GENERATE_PDF_DOCUMENT_INFO));
        Executable generateDocument = () ->
                et3ResponseService.generateEt3ResponseDocument(
                        new CaseData(),
                        "userToken",
                        ENGLANDWALES_CASE_TYPE_ID,
                        SUBMIT_ET3
                );
        assertThrows(DocumentManagementException.class, generateDocument);
    }

    /**
     * Ignored because ET3 form should not be saved before response is accepted
     * <a href="https://tools.hmcts.net/jira/browse/RET-5483">RET-5483</a>.
     */
    @Disabled("Disabled according to Ticket https://tools.hmcts.net/jira/browse/RET-5483")
    @Test
    void assertThatEt3DocumentIsSaved() {
        et3ResponseService.saveEt3Response(caseData, documentInfo);

        assertThat(caseData.getDocumentCollection()).hasSize(1);
        assertNotNull(caseData.getRespondentCollection().getFirst().getValue().getEt3Form());
        assertThat(caseData.getDocumentCollection().getFirst().getValue().getUploadedDocument().getCategoryId())
                .isEqualTo("C18");
    }

    @Test
    void assertDataSavedWithTrailingSpace() {
        caseData.getRespondentCollection().getFirst().getValue().setRespondentName("Antonio Vazquez ");
        et3ResponseService.saveEt3Response(caseData, documentInfo);

        assertNotNull(caseData.getRespondentCollection().getFirst().getValue().getEt3Form());
        assertEquals(YES, caseData.getRespondentCollection().getFirst().getValue().getResponseReceived());
    }

    @Test
    void saveRelatedDocumentsToDocumentCollection_savesAllDocuments() {
        caseData.setEt3ResponseEmployerClaimDocument(
                DocumentFixtures.getUploadedDocumentType("ecc.docx")
        );
        caseData.setEt3ResponseRespondentSupportNeeded("Yes");
        caseData.setEt3ResponseRespondentSupportDocument(
                DocumentFixtures.getUploadedDocumentType("support.docx")
        );
        caseData.setEt3ResponseRespondentContestClaim("Yes");
        caseData.setEt3ResponseContestClaimDocument(
                List.of(DocumentTypeItem.fromUploadedDocument(DocumentFixtures.getUploadedDocumentType("claim.docx")))
        );
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        assertThat(caseData.getDocumentCollection()).hasSize(3);
    }

    @Test
    void saveRelatedDocumentsToDocumentCollection_doesNotSaveNullDocuments() {
        caseData.setEt3ResponseRespondentContestClaim("Yes");
        caseData.setEt3ResponseContestClaimDocument(
                List.of(DocumentTypeItem.fromUploadedDocument(DocumentFixtures.getUploadedDocumentType("claim.docx")))
        );
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        assertThat(caseData.getDocumentCollection()).hasSize(1);
    }

    @Test
    void saveRelatedDocumentsToDocumentCollection_doesNotSaveSameDocumentTwice() {
        caseData.setEt3ResponseRespondentContestClaim("Yes");
        caseData.setEt3ResponseContestClaimDocument(
                List.of(DocumentTypeItem.fromUploadedDocument(DocumentFixtures.getUploadedDocumentType("claim.docx")))
        );
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        assertThat(caseData.getDocumentCollection()).hasSize(1);
    }

    @Test
    void sendNotifications_returnEmail() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("1234567/1234")
            .withRespondent("Respondent", NO, null, false)
            .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge", "Venue", null, null, null, null)
            .withHearingSession(0, "2099-11-25T12:11:00.000", Constants.HEARING_STATUS_LISTED, false)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1683646754393041");
        caseDetails.getCaseData().setClaimant("Claimant LastName");
        caseDetails.getCaseData().setTribunalCorrespondenceEmail("tribunal@email.com");

        et3ResponseService.sendNotifications(caseDetails);

        Map<String, String> expected = Map.of(
            "case_number", "1234567/1234",
            "claimant", "Claimant LastName",
            "list_of_respondents", "Respondent",
            "date", "25 Nov 2099",
            "linkToExUI", "exuiUrl1683646754393041",
            "ccdId", "1683646754393041"
        );
        verify(emailService, times(1)).sendEmail(any(), eq("tribunal@email.com"), eq(expected));
    }

    @ParameterizedTest
    @MethodSource("addEt3DataToRespondentExtensionResubmitted")
    void addEt3DataToRespondent_setExtensionResubmitted(String responseReceived, String extensionRequested,
                                                        String extensionGranted, String result) {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().getFirst().getValue();
        respondentSumType.setResponseReceived(responseReceived);
        respondentSumType.setExtensionRequested(extensionRequested);
        respondentSumType.setExtensionGranted(extensionGranted);
        et3ResponseService.saveEt3Response(caseData, documentInfo);
        AssertionsForClassTypes.assertThat(respondentSumType.getExtensionResubmitted()).isEqualTo(result);
    }

    private static Stream<Arguments> addEt3DataToRespondentExtensionResubmitted() {
        return Stream.of(
                Arguments.of(YES, YES, YES, YES),
                Arguments.of(NO, null, null, null),
                Arguments.of(null, null, null, null)
        );
    }

    @Test
    @SneakyThrows
    void generateEt3ResponseDocument_withEmptyRepresentativeAddress_setsOrganisationAddress() {
        CaseData testCaseData = new CaseData();
        testCaseData.setSubmitEt3Respondent(DynamicFixedListType.of(DynamicValueType.create("test", "test")));
        
        // Create representative with null address
        RepresentedTypeR representative = new RepresentedTypeR();
        representative.setRespRepName("test");
        representative.setRepresentativeAddress(null);
        testCaseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(representative).build()));
        
        OrganisationAddress organisationAddress = OrganisationAddress.builder()
                .addressLine1(ADDRESS_LINE_1)
                .addressLine2(ADDRESS_LINE_2)
                .addressLine3(ADDRESS_LINE_3)
                .townCity(POST_TOWN)
                .postCode(POST_CODE)
                .county(COUNTY)
                .country(COUNTRY)
                .build();
        
        when(myHmctsService.getUserOrganisationAddress(VALID_USER_TOKEN)).thenReturn(organisationAddress);
        when(pdfBoxService.generatePdfDocumentInfo(any(), anyString(), anyString(), 
                anyString(), anyString(), anyString())).thenReturn(documentInfo);
        
        et3ResponseService.generateEt3ResponseDocument(testCaseData, VALID_USER_TOKEN,
                ENGLANDWALES_CASE_TYPE_ID, SUBMIT_ET3);
        
        assertThat(representative.getRepresentativeAddress()).isNotNull();
        assertThat(representative.getRepresentativeAddress().getAddressLine1()).isEqualTo(ADDRESS_LINE_1);
        assertThat(representative.getRepresentativeAddress().getAddressLine2()).isEqualTo(ADDRESS_LINE_2);
        assertThat(representative.getRepresentativeAddress().getAddressLine3()).isEqualTo(ADDRESS_LINE_3);
        assertThat(representative.getRepresentativeAddress().getPostTown()).isEqualTo(POST_TOWN);
        assertThat(representative.getRepresentativeAddress().getPostCode()).isEqualTo(POST_CODE);
        assertThat(representative.getRepresentativeAddress().getCounty()).isEqualTo(COUNTY);
        assertThat(representative.getRepresentativeAddress().getCountry()).isEqualTo(COUNTRY);
    }

    @Test
    @SneakyThrows
    void generateEt3ResponseDocument_withExistingRepresentativeAddress_doesNotOverwrite() {
        CaseData testCaseData = new CaseData();
        testCaseData.setSubmitEt3Respondent(DynamicFixedListType.of(DynamicValueType.create("test", "test")));
        
        // Create representative with existing address
        Address existingAddress = new Address();
        existingAddress.setAddressLine1("Existing Address Line 1");
        existingAddress.setPostTown("Existing Town");
        
        RepresentedTypeR representative = new RepresentedTypeR();
        representative.setRespRepName("test");
        representative.setRepresentativeAddress(existingAddress);
        testCaseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(representative).build()));
        
        when(pdfBoxService.generatePdfDocumentInfo(any(), anyString(), anyString(), 
                anyString(), anyString(), anyString())).thenReturn(documentInfo);
        
        et3ResponseService.generateEt3ResponseDocument(testCaseData, VALID_USER_TOKEN,
                ENGLANDWALES_CASE_TYPE_ID, SUBMIT_ET3);
        
        assertThat(representative.getRepresentativeAddress()).isNotNull();
        assertThat(representative.getRepresentativeAddress().getAddressLine1()).isEqualTo("Existing Address Line 1");
        assertThat(representative.getRepresentativeAddress().getPostTown()).isEqualTo("Existing Town");
        verify(myHmctsService, times(0)).getUserOrganisationAddress(anyString());
    }

    @Test
    @SneakyThrows
    void generateEt3ResponseDocument_withNullRepresentative_doesNotThrowException() {
        caseData = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withSubmitEt3Respondent("Antonio Vazquez")
                .build();
        
        when(pdfBoxService.generatePdfDocumentInfo(any(), anyString(), anyString(), 
                anyString(), anyString(), anyString())).thenReturn(documentInfo);
        
        assertDoesNotThrow(() -> et3ResponseService.generateEt3ResponseDocument(
                caseData, VALID_USER_TOKEN, ENGLANDWALES_CASE_TYPE_ID, SUBMIT_ET3));
        
        verify(myHmctsService, times(0)).getUserOrganisationAddress(anyString());
    }
}
