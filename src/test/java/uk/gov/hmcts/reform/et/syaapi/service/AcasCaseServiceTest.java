package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocumentAcasResponse;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.data.TestDataProvider;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_ACCEPTED;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOTICE_OF_CLAIM_DOCUMENTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOTICE_OF_HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.USER_ID;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class AcasCaseServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi ccdApiClient;
    @Mock
    private IdamClient idamClient;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CaseDocumentService caseDocumentService;
    @Mock
    private TaskExecutor taskExecutor;
    @Mock
    private FeatureToggleService featureToggleService;
    @InjectMocks
    private AcasCaseService acasCaseService;
    private final CaseTestData testData;
    private static final String EXAMPLE_CASE_ID = "1646225213651598";

    AcasCaseServiceTest() {
        testData = new CaseTestData();
    }

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
        lenient().when(adminUserService.getAdminUserToken()).thenReturn(TestConstants.TEST_SERVICE_AUTH_TOKEN);
        lenient().doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
    }

    @Test
    void shouldLastModifiedCasesIdWhenCaseFoundThenCaseId() {
        LocalDateTime requestDateTime =
            LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(testData.getRequestCaseDataListEngland())
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(testData.getRequestCaseDataListScotland())
            .build();

        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(scotlandSearchResult);

        assertThat(acasCaseService.getLastModifiedCasesId(TestConstants.TEST_SERVICE_AUTH_TOKEN, requestDateTime))
            .hasSize(3)
            .isEqualTo(List.of(1_646_225_213_651_598L, 1_646_225_213_651_533L, 1_646_225_213_651_512L));
    }

    @Test
    void shouldLastModifiedCasesIdWhenNoCaseFoundThenEmpty() {
        LocalDateTime requestDateTime =
            LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(scotlandSearchResult);

        assertThat(acasCaseService.getLastModifiedCasesId(TestConstants.TEST_SERVICE_AUTH_TOKEN, requestDateTime))
            .isEmpty();
    }

    private String generateCaseDataEsQueryWithDate(LocalDateTime requestDateTime) {
        return """
            {
              "size": %d,
              "query": {
                "bool": {
                  "filter": [
                    {
                      "range": {
                        "last_modified": {
                          "gte": "%s"
                        }
                      }
                    },
                    {
                      "range": {
                        "data.receiptDate": {
                          "gte": "2026-05-30"
                        }
                      }
                    }
                  ],
                  "must_not": [
                    {
                      "term": {
                        "data.migratedFromEcm": "Yes"
                      }
                    }
                  ]
                }
              },
              "_source": [
                "reference"
              ]
            }
            """.formatted(Constants.MAX_ES_SIZE, requestDateTime.toString());
    }

    @Test
    void shouldReturnCaseData() {
        List<String> caseIds = List.of(EXAMPLE_CASE_ID, "1646225213651533", "1646225213651512");
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(testData.getRequestCaseDataListEngland())
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(testData.getRequestCaseDataListScotland())
            .build();

        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(scotlandSearchResult);

        List<CaseDetails> caseDetailsList = acasCaseService.getCaseData(TestConstants.TEST_SERVICE_AUTH_TOKEN, caseIds);
        assertThat(caseDetailsList).hasSize(3).isEqualTo(testData.getExpectedCaseDataListCombined());
    }

    @Test
    void shouldReturnCaseDataNoCasesFound() {
        List<String> caseIds = List.of(EXAMPLE_CASE_ID, "1646225213651533");
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            TestConstants.TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(scotlandSearchResult);

        List<CaseDetails> caseDetailsList = acasCaseService.getCaseData(TestConstants.TEST_SERVICE_AUTH_TOKEN, caseIds);
        assertThat(caseDetailsList).isEmpty();
    }

    private String generateCaseDataEsQuery(List<String> caseIds) {
        return """
            {
              "size": %d,
              "query": {
                "bool": {
                  "filter": [
                    {
                      "terms": {
                        "reference.keyword": %s,
                        "boost": 1.0
                      }
                    }
                  ],
                  "boost": 1.0
                }
              }
            }
            """.formatted(Constants.MAX_ES_SIZE, caseIds);
    }

    @Test
    void shouldReturnVisibleAcasDocuments() {
        String caseId = EXAMPLE_CASE_ID;
        CaseDetails caseDetails = caseDetailsWithAcasDocuments();
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(ccdApiClient.searchCases(
            anyString(),
            anyString(),
            eq(EtSyaConstants.ENGLAND_CASE_TYPE), anyString()
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            anyString(),
            anyString(),
            eq(EtSyaConstants.SCOTLAND_CASE_TYPE), anyString()
        )).thenReturn(scotlandSearchResult);
        when(ccdApiClient.getCase(anyString(), anyString(), eq(caseId))).thenReturn(caseDetails);

        doCallRealMethod().when(caseDocumentService).getDocumentUuid(isA(String.class));
        when(caseDocumentService.getDocumentDetails(any(), any()))
            .thenReturn(TestDataProvider.getDocumentDetailsFromCdam());
        List<CaseDocumentAcasResponse> documents = acasCaseService.retrieveAcasDocuments(caseId);
        assertNotNull(documents);
        assertThat(documents)
            .extracting(CaseDocumentAcasResponse::getDocumentType)
            .containsExactlyInAnyOrder("ET1", "ET3");
    }

    @Test
    void retrieveAcasDocumentsShouldIncludePhase2DocumentsWhenFeatureIsEnabled() {
        CaseDetails caseDetails = caseDetailsWithAcasDocuments();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        List<DocumentTypeItem> documentCollection = new ArrayList<>(caseData.getDocumentCollection());
        documentCollection.add(document(RESPONSE_ACCEPTED, UUID.randomUUID().toString()));
        caseData.setDocumentCollection(documentCollection);
        caseData.setServingDocumentCollection(List.of(
            document(NOTICE_OF_CLAIM_DOCUMENTS.getFirst(), UUID.randomUUID().toString()),
            document(NOTICE_OF_HEARING_DOCUMENTS.getFirst(), UUID.randomUUID().toString())
        ));
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));

        String caseId = EXAMPLE_CASE_ID;
        when(featureToggleService.isAcasDocumentsPhase2Enabled()).thenReturn(true);
        when(ccdApiClient.searchCases(
            anyString(),
            anyString(),
            eq(EtSyaConstants.ENGLAND_CASE_TYPE), anyString()
        )).thenReturn(SearchResult.builder().total(1).cases(List.of(caseDetails)).build());
        when(ccdApiClient.searchCases(
            anyString(),
            anyString(),
            eq(EtSyaConstants.SCOTLAND_CASE_TYPE), anyString()
        )).thenReturn(SearchResult.builder().total(0).cases(null).build());
        when(ccdApiClient.getCase(anyString(), anyString(), eq(caseId))).thenReturn(caseDetails);
        doCallRealMethod().when(caseDocumentService).getDocumentUuid(isA(String.class));
        when(caseDocumentService.getDocumentDetails(any(), any()))
            .thenReturn(TestDataProvider.getDocumentDetailsFromCdam());

        List<CaseDocumentAcasResponse> documents = acasCaseService.retrieveAcasDocuments(caseId);

        assertThat(documents)
            .extracting(CaseDocumentAcasResponse::getDocumentType)
            .containsExactlyInAnyOrder("ET1", "ET3", RESPONSE_ACCEPTED, NOTICE_OF_CLAIM, NOTICE_OF_HEARING);
    }

    private CaseDetails caseDetailsWithAcasDocuments() {
        CaseData caseData = new CaseData();
        caseData.setDocumentCollection(List.of(
            document("ET1", UUID.randomUUID().toString()),
            document("ET3", UUID.randomUUID().toString())
        ));
        return CaseDetails.builder()
            .id(Long.valueOf(EXAMPLE_CASE_ID))
            .data(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData))
            .build();
    }

    private DocumentTypeItem document(String type, String documentId) {
        UploadedDocumentType uploadedDocument = new UploadedDocumentType();
        uploadedDocument.setDocumentUrl("http://document.binary.url/" + documentId);
        uploadedDocument.setDocumentFilename("document.pdf");
        uploadedDocument.setDocumentBinaryUrl("http://document.binary.url/" + documentId + "/binary");

        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(type);
        documentType.setUploadedDocument(uploadedDocument);

        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(documentId);
        documentTypeItem.setValue(documentType);
        return documentTypeItem;
    }

    @Test
    void vetAndAcceptCase() {
        when(idamClient.getUserInfo(TestConstants.TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null, USER_ID, TEST_NAME, "ET", "Admin", null));
        when(ccdApiClient.getCase(TestConstants.TEST_SERVICE_AUTH_TOKEN, TestConstants.TEST_SERVICE_AUTH_TOKEN,
                EXAMPLE_CASE_ID))
            .thenReturn(testData.getCaseDetails());
        when(ccdApiClient.startEventForCaseWorker(
            eq(TestConstants.TEST_SERVICE_AUTH_TOKEN), eq(TestConstants.TEST_SERVICE_AUTH_TOKEN), eq(USER_ID),
                eq(EMPLOYMENT), eq(EtSyaConstants.SCOTLAND_CASE_TYPE), eq(EXAMPLE_CASE_ID), anyString()))
            .thenReturn(testData.getStartEventResponse());
        when(ccdApiClient.submitEventForCaseWorker(
            eq(TestConstants.TEST_SERVICE_AUTH_TOKEN), eq(TestConstants.TEST_SERVICE_AUTH_TOKEN), eq(USER_ID),
                eq(EMPLOYMENT), eq(EtSyaConstants.SCOTLAND_CASE_TYPE), eq(EXAMPLE_CASE_ID), eq(true),
                any(CaseDataContent.class)))
            .thenReturn(testData.getCaseDetails());

        acasCaseService.vetAndAcceptCase(EXAMPLE_CASE_ID);
        verify(ccdApiClient, times(1)).getCase(
            TestConstants.TEST_SERVICE_AUTH_TOKEN, TestConstants.TEST_SERVICE_AUTH_TOKEN, EXAMPLE_CASE_ID);
        verify(ccdApiClient, times(1)).startEventForCaseWorker(
            TestConstants.TEST_SERVICE_AUTH_TOKEN, TestConstants.TEST_SERVICE_AUTH_TOKEN, USER_ID, EMPLOYMENT,
                EtSyaConstants.SCOTLAND_CASE_TYPE, EXAMPLE_CASE_ID, "et1Vetting");
        verify(ccdApiClient, times(1)).startEventForCaseWorker(
            TestConstants.TEST_SERVICE_AUTH_TOKEN, TestConstants.TEST_SERVICE_AUTH_TOKEN, USER_ID, EMPLOYMENT,
                EtSyaConstants.SCOTLAND_CASE_TYPE, EXAMPLE_CASE_ID, "preAcceptanceCase");
        ArgumentCaptor<CaseDataContent> argumentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(ccdApiClient, times(2)).submitEventForCaseWorker(
            eq(TestConstants.TEST_SERVICE_AUTH_TOKEN), eq(TestConstants.TEST_SERVICE_AUTH_TOKEN), eq(USER_ID),
                eq(EMPLOYMENT), eq(EtSyaConstants.SCOTLAND_CASE_TYPE), eq(EXAMPLE_CASE_ID), eq(true),
                argumentCaptor.capture());

        // Check Vetting Data
        CaseData caseData = (CaseData) argumentCaptor.getAllValues().getFirst().getData();
        assertThat(caseData.getAreTheseCodesCorrect()).isEqualTo(TestConstants.YES);
        assertThat(caseData.getEt1VettingCanServeClaimYesOrNo()).isEqualTo(TestConstants.YES);
        // Check Pre Acceptance Data
        caseData = (CaseData) argumentCaptor.getAllValues().get(1).getData();
        assertThat(caseData.getPreAcceptCase().getCaseAccepted()).isEqualTo(TestConstants.YES);
        assertThat(caseData.getPreAcceptCase().getDateAccepted()).isEqualTo(LocalDate.now().toString());
    }

    @Test
    void vetAndAcceptCaseWhenCaseNotFound() {
        when(ccdApiClient.getCase(TestConstants.TEST_SERVICE_AUTH_TOKEN, TestConstants.TEST_SERVICE_AUTH_TOKEN,
                EXAMPLE_CASE_ID)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> acasCaseService.vetAndAcceptCase(EXAMPLE_CASE_ID));
        verify(ccdApiClient, times(1)).getCase(
            TestConstants.TEST_SERVICE_AUTH_TOKEN, TestConstants.TEST_SERVICE_AUTH_TOKEN, EXAMPLE_CASE_ID);
    }
}
