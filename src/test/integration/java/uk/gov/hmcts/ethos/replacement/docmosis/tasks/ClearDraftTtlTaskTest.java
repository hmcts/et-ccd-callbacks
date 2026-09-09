package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.TTL;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(MockitoExtension.class)
class ClearDraftTtlTaskTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String CASE_ID = "1234567890123456";
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
        .parse("hmctspublic.azurecr.io/imported/postgres:16-alpine")
        .asCompatibleSubstituteFor("postgres");
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    private static NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;

    private ClearDraftTtlTask task;

    @BeforeAll
    static void setUpDatabase() {
        POSTGRES.start();
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(),
            POSTGRES.getUsername(),
            POSTGRES.getPassword()
        );
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate.getJdbcTemplate().execute("CREATE SCHEMA IF NOT EXISTS ccd");
        jdbcTemplate.getJdbcTemplate().execute("""
            CREATE TABLE IF NOT EXISTS ccd.case_data (
                reference BIGINT PRIMARY KEY,
                case_type_id TEXT NOT NULL,
                state TEXT NOT NULL,
                data JSONB NOT NULL
            )
            """);
    }

    @AfterAll
    static void tearDownDatabase() {
        POSTGRES.stop();
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.getJdbcTemplate().execute("TRUNCATE ccd.case_data");
        task = createTask(true);
    }

    @Test
    void dryRunFindsOnlyDraftCasesContainingLegacyTtlDataInReferenceOrder() throws IOException {
        String secondCaseId = "1234567890123457";
        String thirdCaseId = "1234567890123458";
        insertCase(thirdCaseId, ENGLANDWALES_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"Suspended\":\"No\"}}");
        insertCase(CASE_ID, ENGLANDWALES_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"SystemTTL\":\"2026-10-01\"}}");
        insertCase(secondCaseId, ENGLANDWALES_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"OverrideTTL\":\"2026-10-02\"}}");
        insertCase("1234567890123459", ENGLANDWALES_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{}}");
        insertCase("1234567890123460", ENGLANDWALES_CASE_TYPE_ID, "Submitted",
                   "{\"TTL\":{\"SystemTTL\":\"2026-10-01\"}}");
        insertCase("1234567890123461", SCOTLAND_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"Suspended\":\"No\"}}");
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(ccdClient.startEventForCase(
            eq(ADMIN_TOKEN),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            eq(EMPLOYMENT),
            anyString(),
            eq(ClearDraftTtlTask.ROLLBACK_TTL_EVENT)
        )).thenAnswer(invocation -> requestWithTtl(invocation.getArgument(3), ClearDraftTtlTask.DRAFT_STATE));

        task.run();

        InOrder calls = inOrder(ccdClient);
        calls.verify(ccdClient).startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        );
        calls.verify(ccdClient).startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, secondCaseId, ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        );
        calls.verify(ccdClient).startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, thirdCaseId, ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        );
        calls.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ET_EnglandWales,ET_Scotland", " ET_EnglandWales , ET_Scotland "})
    void dryRunFindsDraftsForEachConfiguredCaseType(String caseTypeIds) throws IOException {
        String scotlandCaseId = "1234567890123457";
        insertCase(CASE_ID, ENGLANDWALES_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"SystemTTL\":\"2026-10-01\"}}");
        insertCase(scotlandCaseId, SCOTLAND_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"SystemTTL\":\"2026-10-01\"}}");
        task = new ClearDraftTtlTask(adminUserService, ccdClient, jdbcTemplate, caseTypeIds, true, 2, 1000);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(ccdClient.startEventForCase(
            eq(ADMIN_TOKEN),
            anyString(),
            eq(EMPLOYMENT),
            anyString(),
            eq(ClearDraftTtlTask.ROLLBACK_TTL_EVENT)
        )).thenAnswer(invocation -> {
            CCDRequest request = requestWithTtl(invocation.getArgument(3), ClearDraftTtlTask.DRAFT_STATE);
            request.getCaseDetails().setCaseTypeId(invocation.getArgument(1));
            return request;
        });

        task.run();

        InOrder calls = inOrder(ccdClient);
        calls.verify(ccdClient).startEventForCase(
            ADMIN_TOKEN, ENGLANDWALES_CASE_TYPE_ID, EMPLOYMENT, CASE_ID, ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        );
        calls.verify(ccdClient).startEventForCase(
            ADMIN_TOKEN, SCOTLAND_CASE_TYPE_ID, EMPLOYMENT, scotlandCaseId, ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        );
        calls.verifyNoMoreInteractions();
    }

    @Test
    void liveRunClearsTtlThroughRollbackEvent() throws IOException {
        insertCase(CASE_ID, ENGLANDWALES_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"SystemTTL\":\"2026-10-01\"}}");
        task = createTask(false);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);

        CCDRequest request = requestWithTtl(CASE_ID, ClearDraftTtlTask.DRAFT_STATE);
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN,
            ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT,
            CASE_ID,
            ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        )).thenReturn(request);
        SubmitEvent result = new SubmitEvent();
        result.setCaseData(new CaseData());
        when(ccdClient.submitEventForCase(
            eq(ADMIN_TOKEN),
            org.mockito.ArgumentMatchers.any(CaseData.class),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            eq(EMPLOYMENT),
            eq(request),
            eq(CASE_ID)
        )).thenReturn(result);

        task.run();

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(ccdClient).submitEventForCase(
            eq(ADMIN_TOKEN),
            caseDataCaptor.capture(),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            eq(EMPLOYMENT),
            eq(request),
            eq(CASE_ID)
        );
        assertThat(caseDataCaptor.getValue().getTtl()).isNotNull();
        assertThat(caseDataCaptor.getValue().getTtl().getSystemTTL()).isNull();
        assertThat(caseDataCaptor.getValue().getTtl().getOverrideTTL()).isNull();
        assertThat(caseDataCaptor.getValue().getTtl().getSuspended()).isNull();
    }

    @Test
    void liveRunDoesNotClearTtlAfterDraftHasBeenSubmitted() throws IOException {
        insertCase(CASE_ID, ENGLANDWALES_CASE_TYPE_ID, ClearDraftTtlTask.DRAFT_STATE,
                   "{\"TTL\":{\"SystemTTL\":\"2026-10-01\"}}");
        task = createTask(false);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN,
            ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT,
            CASE_ID,
            ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        )).thenReturn(requestWithTtl(CASE_ID, "Submitted"));

        task.run();

        verify(ccdClient, never()).submitEventForCase(
            anyString(),
            org.mockito.ArgumentMatchers.any(CaseData.class),
            anyString(),
            anyString(),
            org.mockito.ArgumentMatchers.any(CCDRequest.class),
            anyString()
        );
    }

    @Test
    void emptyTtlSerialisesAsTheEstablishedRollbackPayload() {
        CaseData caseData = new CaseData();
        caseData.setTtl(new TTL());
        ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        Map<String, Object> payload = objectMapper.convertValue(caseData, Map.class);

        assertThat(payload).containsEntry("TTL", Map.of());
    }

    private static void insertCase(String reference, String caseType, String state, String data) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("reference", Long.parseLong(reference))
            .addValue("caseType", caseType)
            .addValue("state", state)
            .addValue("data", data);
        jdbcTemplate.update("""
            INSERT INTO ccd.case_data (reference, case_type_id, state, data)
            VALUES (:reference, :caseType, :state, CAST(:data AS JSONB))
            """, parameters);
    }

    private ClearDraftTtlTask createTask(boolean dryRun) {
        return new ClearDraftTtlTask(
            adminUserService,
            ccdClient,
            jdbcTemplate,
            ENGLANDWALES_CASE_TYPE_ID,
            dryRun,
            2,
            1000
        );
    }

    private static CCDRequest requestWithTtl(String caseId, String state) {
        TTL ttl = new TTL();
        ttl.setSystemTTL(LocalDate.now().plusDays(30));
        ttl.setOverrideTTL(LocalDate.now().plusDays(60));
        ttl.setSuspended("No");

        CaseData caseData = new CaseData();
        caseData.setTtl(ttl);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(caseId);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setJurisdiction(EMPLOYMENT);
        caseDetails.setState(state);
        caseDetails.setCaseData(caseData);

        CCDRequest request = new CCDRequest();
        request.setCaseDetails(caseDetails);
        return request;
    }
}
