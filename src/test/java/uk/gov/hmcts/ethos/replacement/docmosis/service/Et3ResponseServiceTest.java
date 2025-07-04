package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

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
@SuppressWarnings({"PMD.ExcessiveImports"})
class Et3ResponseServiceTest {
    private Et3ResponseService et3ResponseService;
    @MockitoBean
    private DocumentManagementService documentManagementService;
    @MockitoBean
    private PdfBoxService pdfBoxService;
    private EmailService emailService;
    private CaseData caseData;
    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        emailService = spy(new EmailUtils());

        et3ResponseService = new Et3ResponseService(documentManagementService, pdfBoxService, emailService);
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
    void generateEt3ProcessingDocumentNullWithoutException() {
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
    }

    @Test
    @SneakyThrows
    void generateEt3ProcessingDocumentNoExceptions() {
        try (MockedStatic<ET3FormMapper> et3FormMapperMockedStatic = Mockito.mockStatic(ET3FormMapper.class)) {
            et3FormMapperMockedStatic.when(() -> ET3FormMapper.mapEt3Form(any(), anyString())).thenReturn(
                    new ConcurrentHashMap<String, Optional<String>>());
            when(documentManagementService.uploadDocument(anyString(), any(), anyString(), anyString(), anyString()))
                    .thenReturn(new URI("testUri"));
            when(pdfBoxService.generatePdfDocumentInfo(any(), anyString(),
                    anyString(), anyString(), anyString(), anyString())).thenReturn(documentInfo);
            assertDoesNotThrow(() -> et3ResponseService.generateEt3ResponseDocument(new CaseData(), "userToken",
                    ENGLANDWALES_CASE_TYPE_ID, SUBMIT_ET3));
        }
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
        assertNotNull(caseData.getRespondentCollection().get(0).getValue().getEt3Form());
        assertThat(caseData.getDocumentCollection().get(0).getValue().getUploadedDocument().getCategoryId())
                .isEqualTo("C18");
    }

    @Test
    void assertDataSavedWithTrailingSpace() {
        caseData.getRespondentCollection().get(0).getValue().setRespondentName("Antonio Vazquez ");
        et3ResponseService.saveEt3Response(caseData, documentInfo);

        assertNotNull(caseData.getRespondentCollection().get(0).getValue().getEt3Form());
        assertEquals(YES, caseData.getRespondentCollection().get(0).getValue().getResponseReceived());
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
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
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
}
