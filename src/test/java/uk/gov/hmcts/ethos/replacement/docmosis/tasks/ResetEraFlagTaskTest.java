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
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private static final String CASE_ID = "1620473176786991";
    private static final String CASE_ID_2 = "1620473176786992";
    private static final String FIX_CASE_API = "fixCaseAPI";

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private CcdClient ccdClient;

    @Captor
    private ArgumentCaptor<CaseData> caseDataCaptor;

    private ResetEraFlagTask task;

    @BeforeEach
    void setUp() {
        task = new ResetEraFlagTask(adminUserService, ccdClient);
        lenient().when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
    }

    @Test
    void run_doesNothingWhenNoCasesFound() throws IOException {
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        task.run();

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_resetsEraFlagAndSubmitsCase_whenCaseFound() throws IOException {
        SubmitEvent submitEvent = buildSubmitEvent(Long.parseLong(CASE_ID));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);
        caseData.setStateAPI("Accepted");

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

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
        SubmitEvent submitEvent = buildSubmitEvent(Long.parseLong(CASE_ID));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

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
    void run_processesScotlandCases_andSubmitsSuccessfully() throws IOException {
        SubmitEvent submitEvent = buildSubmitEvent(Long.parseLong(CASE_ID));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());

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
        task = new ResetEraFlagTask(adminUserService, ccdClient, true);

        SubmitEvent submitEvent = buildSubmitEvent(Long.parseLong(CASE_ID));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient).startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API);
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_continuesProcessingWhenOneCaseFails() throws IOException {
        SubmitEvent submitEvent1 = buildSubmitEvent(Long.parseLong(CASE_ID));
        SubmitEvent submitEvent2 = buildSubmitEvent(Long.parseLong(CASE_ID_2));

        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent1, submitEvent2))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenThrow(new RuntimeException("CCD failure"));

        CaseData caseData2 = new CaseData();
        CCDRequest ccdRequest2 = buildCcdRequest(caseData2, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID_2, FIX_CASE_API))
                .thenReturn(ccdRequest2);

        task.run();

        verify(ccdClient, times(1)).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest2), eq(CASE_ID_2));
    }

    @Test
    void run_handlesSearchIOExceptionGracefully() throws IOException {
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenThrow(new IOException("Elasticsearch connection failed"));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

        task.run();

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void run_skipsWhenCcdRequestOrCaseDetailsOrCaseDataIsNull() throws IOException {
        SubmitEvent submitEvent = buildSubmitEvent(Long.parseLong(CASE_ID));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, FIX_CASE_API))
                .thenReturn(null);

        task.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_skipsWhenCaseDataIsNull() throws IOException {
        SubmitEvent submitEvent = buildSubmitEvent(Long.parseLong(CASE_ID));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

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
    void run_usesDefaultEmploymentJurisdiction_whenCaseDetailsJurisdictionIsNull() throws IOException {
        SubmitEvent submitEvent = buildSubmitEvent(Long.parseLong(CASE_ID));
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), anyString()))
                .thenReturn(List.of(submitEvent))
                .thenReturn(Collections.emptyList());
        when(ccdClient.buildAndGetElasticSearchRequest(eq(ADMIN_TOKEN), eq(SCOTLAND_CASE_TYPE_ID), anyString()))
                .thenReturn(Collections.emptyList());

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
    void processCase_handlesBlankCaseId() {
        assertFalse(task.processCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, "   "));
        assertFalse(task.processCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, null));
    }

    @Test
    void processCases_skipsUnknownCaseId() throws IOException {
        SubmitEvent unknownEvent = new SubmitEvent();
        unknownEvent.setCaseId(0);

        task.processCases(List.of(unknownEvent), ENGLANDWALES_CASE_TYPE_ID, ADMIN_TOKEN);

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void processCases_handlesEmptyList() throws IOException {
        task.processCases(Collections.emptyList(), ENGLANDWALES_CASE_TYPE_ID, ADMIN_TOKEN);
        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void findCaseId_returnsExpectedId() {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(123_456_789L);
        assertEquals("123456789", ResetEraFlagTask.findCaseId(submitEvent));

        SubmitEvent zeroEvent = new SubmitEvent();
        zeroEvent.setCaseId(0);
        assertEquals("<unknown>", ResetEraFlagTask.findCaseId(zeroEvent));

        assertEquals("<unknown>", ResetEraFlagTask.findCaseId(null));
    }

    @Test
    void setLastCaseId_updatesLastCaseIdCorrectly() {
        SubmitEvent event1 = buildSubmitEvent(1111L);
        SubmitEvent event2 = buildSubmitEvent(2222L);

        task.setLastCaseId(List.of(event1, event2));
        String lastCaseId = (String) ReflectionTestUtils.getField(task, "lastCaseId");
        assertEquals("2222", lastCaseId);

        SubmitEvent eventZero = buildSubmitEvent(0);
        task.setLastCaseId(List.of(event1, eventZero));
        lastCaseId = (String) ReflectionTestUtils.getField(task, "lastCaseId");
        assertEquals("1111", lastCaseId);

        task.setLastCaseId(Collections.emptyList());
        lastCaseId = (String) ReflectionTestUtils.getField(task, "lastCaseId");
        assertEquals("0", lastCaseId);

        task.setLastCaseId(null);
        lastCaseId = (String) ReflectionTestUtils.getField(task, "lastCaseId");
        assertEquals("0", lastCaseId);
    }

    @Test
    void buildQuery_buildsElasticsearchQueryWithSearchAfter() {
        String query = task.buildQuery();
        assertTrue(query.contains("data.additionalCaseInfo.era"));
        assertTrue(query.contains("reference.keyword"));
        assertFalse(query.contains("search_after"));

        ReflectionTestUtils.setField(task, "lastCaseId", CASE_ID);
        String queryWithSearchAfter = task.buildQuery();
        assertTrue(queryWithSearchAfter.contains("search_after"));
        assertTrue(queryWithSearchAfter.contains(CASE_ID));
    }

    private SubmitEvent buildSubmitEvent(long caseId) {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(caseId);
        return submitEvent;
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
