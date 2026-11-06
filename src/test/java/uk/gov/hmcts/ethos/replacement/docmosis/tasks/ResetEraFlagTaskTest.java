package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    private static final String AMEND_CASE_DETAILS = "amendCaseDetails";

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private CcdClient ccdClient;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<CaseData> caseDataCaptor;

    @Captor
    private ArgumentCaptor<MapSqlParameterSource> paramCaptor;

    private ResetEraFlagTask task;

    @BeforeEach
    void setUp() {
        task = new ResetEraFlagTask(adminUserService, ccdClient, jdbcTemplate);
        lenient().when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
    }

    @Test
    void run_doesNothingWhenNoCasesFound() throws IOException {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class))).thenReturn(Collections.emptyList());

        task.run();

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_resetsEraFlagAndSubmitsCase_whenCaseFound() throws IOException {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID)))
                .thenReturn(Collections.emptyList());

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);
        caseData.setStateAPI("Accepted");

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS))
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
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID)))
                .thenReturn(Collections.emptyList());

        CaseData caseData = new CaseData();
        caseData.setAdditionalCaseInfoType(null);

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS))
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
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(Long.parseLong(CASE_ID)))
                .thenReturn(Collections.emptyList());

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);

        CCDRequest ccdRequest = buildCcdRequest(caseData, SCOTLAND_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, SCOTLAND_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, AMEND_CASE_DETAILS))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), caseDataCaptor.capture(),
                eq(SCOTLAND_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));

        assertEquals(NO, caseDataCaptor.getValue().getAdditionalCaseInfoType().getEra());
    }

    @Test
    void run_doesNotSubmitWhenDryRunIsTrue() throws IOException {
        task = new ResetEraFlagTask(adminUserService, ccdClient, jdbcTemplate, true);

        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID)))
                .thenReturn(Collections.emptyList());

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);

        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient).startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS);
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_continuesProcessingWhenOneCaseFails() throws IOException {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID), Long.parseLong(CASE_ID_2)))
                .thenReturn(Collections.emptyList());

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS))
                .thenThrow(new RuntimeException("CCD failure"));

        CaseData caseData2 = new CaseData();
        CCDRequest ccdRequest2 = buildCcdRequest(caseData2, ENGLANDWALES_CASE_TYPE_ID);
        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID_2,
            AMEND_CASE_DETAILS))
                .thenReturn(ccdRequest2);

        task.run();

        verify(ccdClient, times(1)).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest2), eq(CASE_ID_2));
    }

    @Test
    void run_handlesDatabaseExceptionGracefully() throws IOException {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenThrow(new DataAccessResourceFailureException("DB connection failed"));

        task.run();

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void run_skipsWhenCcdRequestOrCaseDetailsOrCaseDataIsNull() throws IOException {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID)))
                .thenReturn(Collections.emptyList());

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS))
                .thenReturn(null);

        task.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_skipsWhenCaseDataIsNull() throws IOException {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID)))
                .thenReturn(Collections.emptyList());

        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseData(null);
        ccdRequest.setCaseDetails(caseDetails);

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_usesDefaultEmploymentJurisdiction_whenCaseDetailsJurisdictionIsNull() throws IOException {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID)))
                .thenReturn(Collections.emptyList());

        CaseData caseData = new CaseData();
        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);
        ccdRequest.getCaseDetails().setJurisdiction(null);

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID,
            AMEND_CASE_DETAILS))
                .thenReturn(ccdRequest);

        task.run();

        verify(ccdClient).submitEventForCase(eq(ADMIN_TOKEN), caseDataCaptor.capture(),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), eq(CASE_ID));
        assertEquals(NO, caseDataCaptor.getValue().getAdditionalCaseInfoType().getEra());
    }

    @Test
    void run_paginatesWhenBatchIsFull() throws IOException {
        task = new ResetEraFlagTask(adminUserService, ccdClient, jdbcTemplate, ENGLANDWALES_CASE_TYPE_ID, 2, false);

        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), paramCaptor.capture(), eq(Long.class)))
                .thenReturn(List.of(1_001L, 1_002L))
                .thenReturn(List.of(1_003L));

        CaseData caseData = new CaseData();
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setEra(YES);
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);
        CCDRequest ccdRequest = buildCcdRequest(caseData, ENGLANDWALES_CASE_TYPE_ID);

        when(ccdClient.startEventForCase(eq(ADMIN_TOKEN), eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT),
                anyString(), eq(AMEND_CASE_DETAILS))).thenReturn(ccdRequest);

        task.run();

        verify(ccdClient, times(3)).submitEventForCase(eq(ADMIN_TOKEN), any(CaseData.class),
                eq(ENGLANDWALES_CASE_TYPE_ID), eq(EMPLOYMENT), eq(ccdRequest), anyString());

        List<MapSqlParameterSource> capturedParams = paramCaptor.getAllValues();
        assertEquals(2, capturedParams.size());
        assertEquals(0L, capturedParams.get(0).getValue("afterReference"));
        assertEquals(1_002L, capturedParams.get(1).getValue("afterReference"));
    }

    @Test
    void findCasesForCaseType_queriesWithCorrectParameters() {
        when(jdbcTemplate.queryForList(eq(ResetEraFlagTask.FIND_ERA_CASES), paramCaptor.capture(), eq(Long.class)))
                .thenReturn(List.of(Long.parseLong(CASE_ID)));

        List<Long> result = task.findCasesForCaseType(ENGLANDWALES_CASE_TYPE_ID, 100L);

        assertEquals(List.of(Long.parseLong(CASE_ID)), result);
        MapSqlParameterSource params = paramCaptor.getValue();
        assertEquals(ENGLANDWALES_CASE_TYPE_ID, params.getValue("caseTypeId"));
        assertEquals(100L, params.getValue("afterReference"));
        assertEquals(500, params.getValue("maxCases"));
    }

    @Test
    void processCase_handlesBlankCaseId() {
        assertFalse(task.processCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, "   "));
        assertFalse(task.processCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, null));
    }

    @Test
    void processCases_skipsInvalidCaseId() throws IOException {
        task.processCases(List.of(0L, -1L), ENGLANDWALES_CASE_TYPE_ID, ADMIN_TOKEN);

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void processCases_handlesEmptyList() throws IOException {
        task.processCases(Collections.emptyList(), ENGLANDWALES_CASE_TYPE_ID, ADMIN_TOKEN);
        task.processCases(null, ENGLANDWALES_CASE_TYPE_ID, ADMIN_TOKEN);

        verify(ccdClient, never()).startEventForCase(anyString(), anyString(), anyString(), anyString(), anyString());
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
