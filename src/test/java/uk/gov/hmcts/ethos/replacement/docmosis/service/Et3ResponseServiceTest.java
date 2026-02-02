package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.assertj.core.api.AssertionsForClassTypes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
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
import java.util.NoSuchElementException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_ROLES_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_CASE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_USER_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_NO_REPRESENTED_RESPONDENT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_ORGANISATION_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.SYSTEM_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.NO_RESPONDENTS_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.SUBMIT_ET3;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports"})
class Et3ResponseServiceTest {
    private Et3ResponseService et3ResponseService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private PdfBoxService pdfBoxService;
    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private CcdCaseAssignment ccdCaseAssignment;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private MyHmctsService myHmctsService;

    private static final String INVALID_USER_TOKEN = "invalidUserToken";
    private static final String VALID_USER_TOKEN = "validUserToken";
    private static final String VALID_USER_TOKEN_RETURNS_INVALID_USER = "validUserTokenReturnsInvalidUser";
    private static final String INVALID_CASE_ID = "invalidCaseId";
    private static final String INVALID_CASE_ID_FOR_GET_USER_ROLES = "invalidCaseIdForGetUserRoles";
    private static final String VALID_CASE_ID = "validCaseId";
    private static final String VALID_CASE_ID_RETURNS_INVALID_CASE = "validCaseIdReturnsInvalidCase";
    private static final String VALID_CASE_ID_RETURNS_INVALID_USER = "validCaseIdReturnsInvalidUser";
    private static final String USER_ID = "userId";
    private static final String INVALID_USER_ID = "invalidUserId";
    private static final String ERROR_PDF_BOX_SERVICE_GENERATE_PDF_DOCUMENT_INFO =
            "Dummy pdf box service generate pdf document info error";
    private static final String ADDRESS_LINE_1 = "addressLine1";
    private static final String ADDRESS_LINE_2 = "addressLine2";
    private static final String ADDRESS_LINE_3 = "addressLine3";
    private static final String POST_CODE = "postalCode";
    private static final String COUNTRY = "country";
    private static final String COUNTY = "county";
    private static final String POST_TOWN = "postTown";
    private static final String PHONE_NUMBER = "1234567890";

    private EmailService emailService;
    private CaseData caseData;
    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        emailService = spy(new EmailUtils());

        et3ResponseService = new Et3ResponseService(documentManagementService, pdfBoxService, emailService,
                userIdamService, ccdCaseAssignment, myHmctsService);
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
        assertThrows(DocumentManagementException.class,
                () -> et3ResponseService.generateEt3ResponseDocument(new CaseData(), "userToken",
                ENGLANDWALES_CASE_TYPE_ID, SUBMIT_ET3));
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

    @ParameterizedTest
    @MethodSource("generateTestGetDataForRepresentedRespondentIndexes")
    @SneakyThrows
    void theGetRepresentedRespondentIndexesTest(String userToken, String caseId) {
        when(userIdamService.getUserDetails(INVALID_USER_TOKEN)).thenReturn(null);

        UserDetails validUserDetails = new UserDetails();
        validUserDetails.setUid(USER_ID);
        when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);

        UserDetails invalidUserDetails = new UserDetails();
        invalidUserDetails.setUid(null);
        when(userIdamService.getUserDetails(VALID_USER_TOKEN_RETURNS_INVALID_USER)).thenReturn(invalidUserDetails);

        when(ccdCaseAssignment.getCaseUserRoles(INVALID_CASE_ID)).thenReturn(null);

        CaseUserAssignmentData caseUserAssignmentData = getValidCaseUserAssignmentData();
        when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(caseUserAssignmentData);

        CaseUserAssignmentData invalidCaseUserAssignmentData = getInvalidCaseUserAssignmentData();
        when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID_RETURNS_INVALID_CASE))
                .thenReturn(invalidCaseUserAssignmentData);

        CaseUserAssignmentData validCaseInvalidUserAssignmentData = getValidCaseInvalidUserAssignmentData();
        when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID_RETURNS_INVALID_USER))
                .thenReturn(validCaseInvalidUserAssignmentData);

        when(ccdCaseAssignment.getCaseUserRoles(INVALID_CASE_ID_FOR_GET_USER_ROLES))
                .thenThrow(new IOException(SYSTEM_ERROR));

        if (StringUtils.isBlank(userToken)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_INVALID_USER_TOKEN);
            return;
        }
        if (StringUtils.isBlank(caseId)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_INVALID_CASE_ID);
            return;
        }
        if (INVALID_USER_TOKEN.equals(userToken)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_USER_NOT_FOUND);
            return;
        }
        if (VALID_USER_TOKEN_RETURNS_INVALID_USER.equals(userToken)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_USER_ID_NOT_FOUND);
        }
        if (VALID_USER_TOKEN.equals(userToken) && INVALID_CASE_ID.equals(caseId)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_CASE_ROLES_NOT_FOUND);
        }
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID_RETURNS_INVALID_CASE.equals(caseId)) {
            assertThrows(NoSuchElementException.class,
                    () -> et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId));
        }
        if (VALID_USER_TOKEN.equals(userToken) && INVALID_CASE_ID_FOR_GET_USER_ROLES.equals(caseId)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(SYSTEM_ERROR);
        }
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID_RETURNS_INVALID_USER.equals(caseId)) {
            assertThat(et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId)).isEmpty();
        }
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseId)) {
            List<Integer> solicitorIndexList = et3ResponseService.getRepresentedRespondentIndexes(userToken, caseId);
            assertThat(solicitorIndexList.getFirst()).isZero();
        }
    }

    private static @NotNull CaseUserAssignmentData getValidCaseInvalidUserAssignmentData() {
        CaseUserAssignment validRespondentSolicitorCaseUserAssignment = new CaseUserAssignment();
        validRespondentSolicitorCaseUserAssignment.setCaseId(VALID_CASE_ID);
        validRespondentSolicitorCaseUserAssignment.setUserId(INVALID_USER_ID);
        validRespondentSolicitorCaseUserAssignment.setCaseRole("[SOLICITORA]");
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(validRespondentSolicitorCaseUserAssignment));
        return caseUserAssignmentData;
    }

    private static @NotNull CaseUserAssignmentData getValidCaseUserAssignmentData() {
        CaseUserAssignment validRespondentSolicitorCaseUserAssignment = new CaseUserAssignment();
        validRespondentSolicitorCaseUserAssignment.setCaseId(VALID_CASE_ID);
        validRespondentSolicitorCaseUserAssignment.setUserId(USER_ID);
        validRespondentSolicitorCaseUserAssignment.setCaseRole("[SOLICITORA]");
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(validRespondentSolicitorCaseUserAssignment));
        return caseUserAssignmentData;
    }

    private static @NotNull CaseUserAssignmentData getInvalidCaseUserAssignmentData() {
        CaseUserAssignment validRespondentSolicitorCaseUserAssignment = new CaseUserAssignment();
        validRespondentSolicitorCaseUserAssignment.setCaseId(VALID_CASE_ID);
        validRespondentSolicitorCaseUserAssignment.setUserId(USER_ID);
        validRespondentSolicitorCaseUserAssignment.setCaseRole("[CLAIMANTSOLICITOR]");
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(validRespondentSolicitorCaseUserAssignment));
        return caseUserAssignmentData;
    }

    private static Stream<Arguments> generateTestGetDataForRepresentedRespondentIndexes() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, VALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN, null),
                Arguments.of(INVALID_USER_TOKEN, INVALID_CASE_ID),
                Arguments.of(INVALID_USER_TOKEN, VALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN_RETURNS_INVALID_USER, VALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN, INVALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN, VALID_CASE_ID_RETURNS_INVALID_CASE),
                Arguments.of(VALID_USER_TOKEN, INVALID_CASE_ID_FOR_GET_USER_ROLES),
                Arguments.of(VALID_USER_TOKEN, VALID_CASE_ID_RETURNS_INVALID_USER),
                Arguments.of(VALID_USER_TOKEN, VALID_CASE_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("generateTestSetRespondentRepresentsContactDetails")
    @SneakyThrows
    void theSetRespondentRepresentsContactDetails(String userToken, CaseData caseData) {
        // When both user token and case data are empty
        if (StringUtils.isBlank(userToken) && ObjectUtils.isEmpty(caseData)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.setRespondentRepresentsContactDetails(userToken, caseData, null));
            assertThat(gex.getMessage()).isEqualTo(ERROR_CASE_DATA_NOT_FOUND);
            return;
        }
        // When only case data is empty
        if (ObjectUtils.isEmpty(caseData)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.setRespondentRepresentsContactDetails(userToken, caseData, null));
            assertThat(gex.getMessage()).isEqualTo(ERROR_CASE_DATA_NOT_FOUND);
            return;
        }
        // When only user token is empty
        if (ObjectUtils.isEmpty(userToken)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(ERROR_INVALID_USER_TOKEN);
            return;
        }
        // when et3ResponseService.getRepresentedRespondentIndexes(INVALID_USER_TOKEN, INVALID_CASE_ID)
        // gives case roles not found exception.
        if (VALID_USER_TOKEN.equals(userToken) && INVALID_CASE_ID.equals(caseData.getCcdID())) {
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(INVALID_CASE_ID)).thenReturn(getInvalidCaseUserAssignmentData());
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(SYSTEM_ERROR);
        }

        // when case data doesn't have any representative in representative collection
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseData.getCcdID())) {
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(getValidCaseUserAssignmentData());
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(ERROR_NO_REPRESENTED_RESPONDENT_FOUND);
        }

        // when there is no user role found should throw no represented respondent found exception
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID_RETURNS_INVALID_USER.equals(caseData.getCcdID())) {
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID_RETURNS_INVALID_USER))
                    .thenReturn(getValidCaseInvalidUserAssignmentData());
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> et3ResponseService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(ERROR_NO_REPRESENTED_RESPONDENT_FOUND);
        }

        // when case data has no representative but representative has no value
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseData.getCcdID())) {
            caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(null).build()));
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(getValidCaseUserAssignmentData());
            et3ResponseService.setRespondentRepresentsContactDetails(userToken, caseData, caseData.getCcdID());
            assertThat(caseData.getRepCollection().getFirst().getValue()).isNull();
        }

        // when case data has representative in representative collection
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseData.getCcdID())) {
            RepresentedTypeR representedTypeR = new RepresentedTypeR();
            caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(representedTypeR).build()));
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(getValidCaseUserAssignmentData());
            et3ResponseService.setRespondentRepresentsContactDetails(userToken, caseData, caseData.getCcdID());
            assertThat(caseData.getRepCollection().getFirst().getValue().getRepresentativeAddress()
                    .getAddressLine1()).isEqualTo("Address Line 1");
            assertThat(caseData.getRepCollection().getFirst().getValue().getRepresentativePhoneNumber())
                    .isEqualTo("1234567890");
        }
    }

    private static Stream<Arguments> generateTestSetRespondentRepresentsContactDetails() {
        CaseData validCaseData = CaseDataBuilder.builder()
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
        validCaseData.setCcdID(VALID_CASE_ID);
        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        validCaseData.setEt3ResponseAddress(address);
        validCaseData.setEt3ResponsePhone("1234567890");

        CaseData invalidCaseData = CaseDataBuilder.builder()
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
        invalidCaseData.setCcdID(INVALID_CASE_ID);

        CaseData notRepresentedCaseData = CaseDataBuilder.builder()
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
        invalidCaseData.setCcdID(VALID_CASE_ID_RETURNS_INVALID_USER);
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(VALID_USER_TOKEN, null),
                Arguments.of(null, validCaseData),
                Arguments.of(VALID_USER_TOKEN, invalidCaseData),
                Arguments.of(VALID_USER_TOKEN, notRepresentedCaseData),
                Arguments.of(VALID_USER_TOKEN, validCaseData)
        );
    }

    @Test
    @SneakyThrows
    void testSetRepresentativeContactInfo() {
        String userToken = "mockToken";
        String submissionReference = "mockRef";
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(USER_ID);
        when(userIdamService.getUserDetails(userToken)).thenReturn(userDetails);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(CaseUserAssignment.builder().userId(USER_ID)
                        .caseRole(RespondentSolicitorType.SOLICITORA.getLabel()).build())).build();
        when(ccdCaseAssignment.getCaseUserRoles(submissionReference)).thenReturn(caseUserAssignmentData);
        // Scenario 2: Use MyHMCTS contact details
        CaseData caseData2 = new CaseData();
        caseData2.setRepCollection(
                List.of(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().build()).build()));
        caseData2.setEt3ResponsePhone(PHONE_NUMBER);
        caseData2.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS);
        when(userIdamService.getUserDetails(userToken)).thenReturn(userDetails);
        OrganisationAddress organisationAddress = OrganisationAddress.builder()
                .addressLine1(ADDRESS_LINE_1)
                .addressLine2(ADDRESS_LINE_2)
                .addressLine3(ADDRESS_LINE_3)
                .townCity(POST_TOWN)
                .postCode(POST_CODE)
                .county(COUNTY)
                .country(COUNTRY)
                .build();
        when(authTokenGenerator.generate()).thenReturn(userToken);
        when(myHmctsService.getOrganisationAddress(userToken)).thenReturn(organisationAddress);
        et3ResponseService.setRespondentRepresentsContactDetails(userToken, caseData2, submissionReference);
        assertThat(caseData2.getEt3ResponseAddress()).isNotNull();
        assertThat(caseData2.getEt3ResponseAddress().getAddressLine1()).isEqualTo(ADDRESS_LINE_1);
        assertThat(caseData2.getEt3ResponseAddress().getAddressLine2()).isEqualTo(ADDRESS_LINE_2);
        assertThat(caseData2.getEt3ResponseAddress().getAddressLine3()).isEqualTo(ADDRESS_LINE_3);
        assertThat(caseData2.getEt3ResponseAddress().getPostTown()).isEqualTo(POST_TOWN);
        assertThat(caseData2.getEt3ResponseAddress().getPostCode()).isEqualTo(POST_CODE);
        assertThat(caseData2.getEt3ResponseAddress().getCounty()).isEqualTo(COUNTY);
        assertThat(caseData2.getEt3ResponseAddress().getCountry()).isEqualTo(COUNTRY);
        assertThat(caseData2.getEt3ResponsePhone()).isEqualTo(PHONE_NUMBER);

        // Scenario 3: Throws when user not found
        CaseData caseData3 = new CaseData();
        caseData3.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS);

        when(userIdamService.getUserDetails(userToken)).thenReturn(null);

        GenericServiceException userNotFound = assertThrows(GenericServiceException.class, () ->
                et3ResponseService.setRespondentRepresentsContactDetails(userToken, caseData3, submissionReference));
        assertThat(userNotFound.getMessage()).isEqualTo("User not found");

        // Scenario 3: Throws when organisation not found
        CaseData caseData4 = new CaseData();
        caseData4.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS);

        when(userIdamService.getUserDetails(userToken)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(userToken);
        doThrow(new GenericServiceException(ERROR_ORGANISATION_DETAILS_NOT_FOUND,
                new Exception(ERROR_ORGANISATION_DETAILS_NOT_FOUND),
                ERROR_ORGANISATION_DETAILS_NOT_FOUND,
                org.apache.commons.lang3.StringUtils.EMPTY,
                "MyHmctsService",
                "getOrganisationAddress - organisation details not found"))
                .when(myHmctsService).getOrganisationAddress(userToken);
        GenericServiceException organisationNotFound = assertThrows(GenericServiceException.class, () ->
                et3ResponseService.setRespondentRepresentsContactDetails(userToken, caseData4, submissionReference));
        assertThat(organisationNotFound.getMessage()).isEqualTo("Organisation details not found");
    }

    @Test
    void createDynamicListSelection_noRespondents() {
        caseData.setRespondentCollection(null);
        List<String> errors = et3ResponseService.createDynamicListSelection(
            caseData,
            VALID_USER_TOKEN,
            caseData.getCcdID()
        );
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst()).isEqualTo(NO_RESPONDENTS_FOUND);
    }

    @Test
    void createDynamicListSelection_responseContinueNo_returnsNoRespondentsRequireEt3() throws IOException {
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(USER_ID);
        when(userIdamService.getUserDetails(anyString())).thenReturn(userDetails);

        when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID))
            .thenReturn(getValidCaseUserAssignmentData());

        caseData.getRespondentCollection().getFirst().getValue().setResponseContinue(NO);
        List<String> errors = et3ResponseService.createDynamicListSelection(
            caseData,
            VALID_USER_TOKEN,
            caseData.getCcdID()
        );
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst()).isEqualTo("There are no respondents that require an ET3");
    }

    @Test
    void createDynamicListSelection_responseContinueYes_allowsSubmissionChoice() {
        caseData.getRespondentCollection().getFirst().getValue().setResponseContinue(YES);
        caseData.getRespondentCollection().getFirst().getValue().setResponseReceived(NO);
        List<String> errors = et3ResponseService.createDynamicListSelection(
            caseData,
            VALID_USER_TOKEN,
            caseData.getCcdID()
        );
        assertThat(errors).isEmpty();
        assertThat(caseData.getEt3RepresentingRespondent()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("createDynamicListSelectionExtension")
    void createDynamicListSelection_extensionRequested(String responseReceived, String extensionRequested,
                                                       String extensionGranted, String extensionDate,
                                                       String extensionResubmitted, int count, int errorsSize) {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().getFirst().getValue();
        respondentSumType.setResponseReceived(responseReceived);
        respondentSumType.setExtensionRequested(extensionRequested);
        respondentSumType.setExtensionGranted(extensionGranted);
        respondentSumType.setExtensionDate(extensionDate);
        respondentSumType.setExtensionResubmitted(extensionResubmitted);
        List<String> errors = et3ResponseService.createDynamicListSelection(
            caseData,
            VALID_USER_TOKEN,
            caseData.getCcdID()
        );
        assertThat(errors).hasSize(errorsSize);
        assertThat(caseData.getEt3RepresentingRespondent().getFirst().getValue().getDynamicList().getListItems())
            .hasSize(count);
    }

    private static Stream<Arguments> createDynamicListSelectionExtension() {
        return Stream.of(
            Arguments.of(NO, null, null, null, null, 1, 0),
            Arguments.of(YES, YES, YES, "2000-12-31", null, 1, 1),
            Arguments.of(YES, YES, YES, "2999-12-31", null, 1, 0),
            Arguments.of(YES, YES, YES, "2999-12-31", YES, 1, 1)
        );
    }
}
