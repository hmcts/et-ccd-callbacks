package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(MockitoExtension.class)
class ResetEraFlagTaskTest {

    private static final String ADMIN_TOKEN = "test-admin-token";
    private static final String S2S_TOKEN = "test-s2s-token";
    private static final String CASE_ID = "1620473176786991";
    private static final String CASE_ID_2 = "1620473176786992";
    private static final String FIX_CASE_API = "fixCaseAPI";

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private CcdClient ccdClient;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Captor
    private ArgumentCaptor<CaseData> caseDataCaptor;

    private ResetEraFlagTask task;

    @BeforeEach
    void setUp() {
        task = new ResetEraFlagTask(adminUserService, ccdClient, coreCaseDataApi, authTokenGenerator);
        lenient().when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        lenient().when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Test
    void run_doesNothingWhenCaseIdsIsEmptyOrNull() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", null);
        task.run();
        verify(coreCaseDataApi, never()).getCase(anyString(), anyString(), anyString());

        ReflectionTestUtils.setField(task, "caseIdsToUpdate", "");
        task.run();
        verify(coreCaseDataApi, never()).getCase(anyString(), anyString(), anyString());

        ReflectionTestUtils.setField(task, "caseIdsToUpdate", " , ");
        task.run();
        verify(coreCaseDataApi, never()).getCase(anyString(), anyString(), anyString());
    }

    @Test
    void run_resetsEraFlagAndSubmitsCase_whenCaseHadEraYes() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID);
        mockCoreCaseDataApi(CASE_ID, ENGLANDWALES_CASE_TYPE_ID);

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);
        caseData.setStateAPI("Accepted");

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

        verify(coreCaseDataApi).getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID);
        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), caseDataCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));

        CaseData updatedCaseData = caseDataCaptor.getValue();
        assertNotNull(updatedCaseData.getAdditionalCaseInfoType());
        assertEquals(NO, updatedCaseData.getAdditionalCaseInfoType().getEra());
        assertNull(updatedCaseData.getStateAPI());
        assertEquals("", updatedCaseData.getFlagsImageAltText());
        assertEquals("EMP-TRIB-0000000000000000.jpg", updatedCaseData.getFlagsImageFileName());
    }

    @Test
    void run_createsAdditionalCaseInfoWhenNull_andResetsEra() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID);
        mockCoreCaseDataApi(CASE_ID, ENGLANDWALES_CASE_TYPE_ID);

        CaseData caseData = new CaseData();
        caseData.setAdditionalCaseInfoType(null);

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), caseDataCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));

        CaseData updatedCaseData = caseDataCaptor.getValue();
        assertNotNull(updatedCaseData.getAdditionalCaseInfoType());
        assertEquals(NO, updatedCaseData.getAdditionalCaseInfoType().getEra());
    }

    @Test
    void run_resolvesScotlandCaseType_andSubmitsSuccessfully() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID);
        mockCoreCaseDataApi(CASE_ID, SCOTLAND_CASE_TYPE_ID);

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);

        CCDRequest ccdRequest = buildCcdRequest(caseData, SCOTLAND_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, SCOTLAND_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), caseDataCaptor.capture(),
                eq(SCOTLAND_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));

        assertEquals(NO, caseDataCaptor.getValue().getAdditionalCaseInfoType().getEra());
    }

    @Test
    void run_doesNotSubmitWhenDryRunIsTrue() throws IOException {
        task = new ResetEraFlagTask(adminUserService, ccdClient, coreCaseDataApi, authTokenGenerator,
                CASE_ID, true);
        mockCoreCaseDataApi(CASE_ID, ENGLANDWALES_CASE_TYPE_ID);

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

        verify(coreCaseDataApi).getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID);
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_skipsWhenCoreCaseDataApiReturnsNullOrBlankCaseType() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID);
        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenReturn(null);

        task.run();

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());

        mockCoreCaseDataApi(CASE_ID, "   ");
        task.run();

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void run_continuesProcessingWhenOneCaseFails() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID + "," + CASE_ID_2);
        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID))
                .thenThrow(new RuntimeException("CCD failure"));

        mockCoreCaseDataApi(CASE_ID_2, ENGLANDWALES_CASE_TYPE_ID);
        CaseData caseData2 = new CaseData();
        CCDRequest ccdRequest2 = buildCcdRequest(caseData2, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID_2, FIX_CASE_API))
                .thenReturn(ccdRequest2);

        task.run();

        verify(ccdClient, times(1)).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest2), eq(CASE_ID_2));
    }

    @Test
    void run_skipsWhenCcdRequestOrCaseDetailsOrCaseDataIsNull() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID);
        mockCoreCaseDataApi(CASE_ID, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(null);

        task.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_skipsWhenCaseDataIsNull() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID);
        mockCoreCaseDataApi(CASE_ID, ENGLANDWALES_CASE_TYPE_ID);
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseData(null);
        ccdRequest.setCaseDetails(caseDetails);

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void processCases_handlesBlankCaseIdInList() {
        task.processCases(List.of("   ", ""));
        verify(coreCaseDataApi, never()).getCase(anyString(), anyString(), anyString());
    }

    @Test
    void run_usesDefaultEmploymentJurisdiction_whenCaseDetailsJurisdictionIsNull() throws IOException {
        ReflectionTestUtils.setField(task, "caseIdsToUpdate", CASE_ID);
        mockCoreCaseDataApi(CASE_ID, ENGLANDWALES_CASE_TYPE_ID);
        CaseData caseData = new CaseData();
        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        ccdRequest.getCaseDetails().setJurisdiction(null);

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), caseDataCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));
        assertEquals(NO, caseDataCaptor.getValue().getAdditionalCaseInfoType().getEra());
    }

    @Test
    void parseCaseIds_trimsAndRemovesBlanks() {
        List<String> result = ResetEraFlagTask.parseCaseIds(" " + CASE_ID + " ,, " + CASE_ID_2 + " ");
        assertEquals(List.of(CASE_ID, CASE_ID_2), result);

        assertTrue(ResetEraFlagTask.parseCaseIds(null).isEmpty());
        assertTrue(ResetEraFlagTask.parseCaseIds("").isEmpty());
        assertTrue(ResetEraFlagTask.parseCaseIds(" , ").isEmpty());
    }

    private void mockCoreCaseDataApi(String caseId, String caseTypeId) {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails details =
                uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .caseTypeId(caseTypeId)
                        .build();
        when(coreCaseDataApi.getCase(ADMIN_TOKEN, S2S_TOKEN, caseId)).thenReturn(details);
    }

    private CCDRequest buildCcdRequest(CaseData caseData, String caseTypeId) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(caseTypeId);
        caseDetails.setJurisdiction(EMPLOYMENT);
        caseDetails.setCaseData(caseData);

        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
