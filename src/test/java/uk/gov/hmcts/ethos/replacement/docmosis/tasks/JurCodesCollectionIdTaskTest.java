package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.impl.PostgresAdvisoryLock;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.util.List;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class JurCodesCollectionIdTaskTest {

    private static final String ADMIN_TOKEN = "Bearer admin-token";
    private static final Long CASE_REF = 1_704_985_449_005_627L;
    private static final String CASE_ID = "1704985449005627";

    private JurCodesCollectionIdTask jurCodesCollectionIdTask;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private CcdClient ccdClient;

    @MockitoBean
    private NamedParameterJdbcTemplate jdbcTemplate;

    @MockitoBean
    private PostgresAdvisoryLock advisoryLock;

    @BeforeEach
    void setUp() {
        jurCodesCollectionIdTask = new JurCodesCollectionIdTask(
                adminUserService, ccdClient, jdbcTemplate, advisoryLock);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(advisoryLock.runIfAcquired(eq("et-jur-codes-id-fix"), eq("task"), any(Runnable.class)))
                .thenAnswer(invocation -> {
                    Runnable operation = invocation.getArgument(2);
                    operation.run();
                    return true;
                });
        ReflectionTestUtils.setField(jurCodesCollectionIdTask, "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(jurCodesCollectionIdTask, "maxCases", 10);
    }

    @Test
    void run_populatesMissingIdsAndSubmitsFixCaseApi() throws IOException {
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(List.of(CASE_REF))
                .thenReturn(List.of());

        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("DAG");
        JurCodesTypeItem itemMissingId = new JurCodesTypeItem();
        itemMissingId.setValue(jurCodesType);

        CaseData caseData = new CaseData();
        caseData.setStateAPI(ACCEPTED_STATE);
        caseData.setJurCodesCollection(List.of(itemMissingId));

        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setJurisdiction(EMPLOYMENT);

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, "fixCaseAPI"))
                .thenReturn(ccdRequest);

        jurCodesCollectionIdTask.run();

        assertNotNull(itemMissingId.getId());
        assertNull(caseData.getStateAPI());
        verify(ccdClient, times(1)).submitEventForCase(
                ADMIN_TOKEN, caseData, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, ccdRequest, CASE_ID);
    }

    @Test
    void run_whenAllItemsAlreadyHaveIds_doesNotSubmitEvent() throws IOException {
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(List.of(CASE_REF))
                .thenReturn(List.of());

        JurCodesTypeItem itemWithId = new JurCodesTypeItem();
        itemWithId.setId("existing-id");
        itemWithId.setValue(new JurCodesType());

        CaseData caseData = new CaseData();
        caseData.setJurCodesCollection(List.of(itemWithId));

        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setJurisdiction(EMPLOYMENT);

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, "fixCaseAPI"))
                .thenReturn(ccdRequest);

        jurCodesCollectionIdTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_whenJurCodesCollectionIsEmpty_doesNotSubmitEvent() throws IOException {
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenReturn(List.of(CASE_REF))
                .thenReturn(List.of());

        CaseData caseData = new CaseData();
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setJurisdiction(EMPLOYMENT);

        when(ccdClient.startEventForCase(ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, "fixCaseAPI"))
                .thenReturn(ccdRequest);

        jurCodesCollectionIdTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void run_whenLockNotAcquired_skipsExecution() throws IOException {
        when(advisoryLock.runIfAcquired(eq("et-jur-codes-id-fix"), eq("task"), any(Runnable.class)))
                .thenReturn(false);

        jurCodesCollectionIdTask.run();

        verify(advisoryLock, times(1)).runIfAcquired(eq("et-jur-codes-id-fix"), eq("task"), any(Runnable.class));
        verify(adminUserService, never()).getAdminUserToken();
        verify(jdbcTemplate, never()).queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class));
        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void run_whenQueryOrCcdThrowsException_logsAndContinues() throws IOException {
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class), eq(Long.class)))
                .thenThrow(new RuntimeException("DB error"))
                .thenReturn(List.of(CASE_REF));
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any()))
                .thenThrow(new IOException("CCD error"));

        jurCodesCollectionIdTask.run();

        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testDataSourceConstructor() {
        DataSource dataSource = mock(DataSource.class);
        JurCodesCollectionIdTask task = new JurCodesCollectionIdTask(
                adminUserService, ccdClient, jdbcTemplate, dataSource);
        assertNotNull(task);
    }
}
