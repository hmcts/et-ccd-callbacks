package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.TTL;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdClient ccdClient;

    private ClearDraftTtlTask task;

    @BeforeEach
    void setUp() {
        task = new ClearDraftTtlTask(adminUserService, ccdClient);
        ReflectionTestUtils.setField(task, "dryRun", true);
        ReflectionTestUtils.setField(task, "maxCasesPerSearch", 100);
        ReflectionTestUtils.setField(task, "maxCasesToProcess", 1000);
    }

    @Test
    void dryRunFindsCasesWithoutStartingRollbackEvents() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        SubmitEvent candidate = candidate(CASE_ID);
        when(ccdClient.buildAndGetElasticSearchRequest(
            eq(ADMIN_TOKEN),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            anyString()
        )).thenReturn(List.of(candidate), List.of());
        when(ccdClient.buildAndGetElasticSearchRequest(
            eq(ADMIN_TOKEN),
            eq(SCOTLAND_CASE_TYPE_ID),
            anyString()
        )).thenReturn(List.of());
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN,
            ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT,
            CASE_ID,
            ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        )).thenReturn(requestWithTtl());

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
    void liveRunClearsTtlThroughRollbackEvent() throws IOException {
        ReflectionTestUtils.setField(task, "dryRun", false);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        SubmitEvent candidate = candidate(CASE_ID);
        when(ccdClient.buildAndGetElasticSearchRequest(
            eq(ADMIN_TOKEN),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            anyString()
        )).thenReturn(List.of(candidate), List.of());
        when(ccdClient.buildAndGetElasticSearchRequest(
            eq(ADMIN_TOKEN),
            eq(SCOTLAND_CASE_TYPE_ID),
            anyString()
        )).thenReturn(List.of());

        CCDRequest request = requestWithTtl();
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
        ReflectionTestUtils.setField(task, "dryRun", false);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(ccdClient.buildAndGetElasticSearchRequest(
            eq(ADMIN_TOKEN),
            eq(ENGLANDWALES_CASE_TYPE_ID),
            anyString()
        )).thenReturn(List.of(candidate(CASE_ID)), List.of());
        when(ccdClient.buildAndGetElasticSearchRequest(
            eq(ADMIN_TOKEN),
            eq(SCOTLAND_CASE_TYPE_ID),
            anyString()
        )).thenReturn(List.of());

        CCDRequest request = requestWithTtl();
        request.getCaseDetails().setState("Submitted");
        when(ccdClient.startEventForCase(
            ADMIN_TOKEN,
            ENGLANDWALES_CASE_TYPE_ID,
            EMPLOYMENT,
            CASE_ID,
            ClearDraftTtlTask.ROLLBACK_TTL_EVENT
        )).thenReturn(request);

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
    void querySelectsOnlyDraftsContainingLegacyTtlData() {
        String query = ClearDraftTtlTask.buildQuery(50, CASE_ID);

        assertThat(query)
            .contains("\"size\":50")
            .contains("\"state.keyword\"")
            .contains(ClearDraftTtlTask.DRAFT_STATE)
            .contains("data.TTL.SystemTTL")
            .contains("data.TTL.OverrideTTL")
            .contains("data.TTL.Suspended")
            .contains("\"search_after\":[\"" + CASE_ID + "\"]");
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

    private static SubmitEvent candidate(String caseId) {
        SubmitEvent candidate = new SubmitEvent();
        candidate.setCaseId(Long.parseLong(caseId));
        return candidate;
    }

    private static CCDRequest requestWithTtl() {
        TTL ttl = new TTL();
        ttl.setSystemTTL(LocalDate.now().plusDays(30));
        ttl.setOverrideTTL(LocalDate.now().plusDays(60));
        ttl.setSuspended("No");

        CaseData caseData = new CaseData();
        caseData.setTtl(ttl);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setJurisdiction(EMPLOYMENT);
        caseDetails.setState(ClearDraftTtlTask.DRAFT_STATE);
        caseDetails.setCaseData(caseData);

        CCDRequest request = new CCDRequest();
        request.setCaseDetails(caseDetails);
        return request;
    }
}
