package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class WaTaskCreationCronForExpiredBfActionsTest {
    @Mock
    private AdminUserService adminUserService;

    @Mock
    private CcdClient ccdClient;

    @Mock
    private FeatureToggleService featureToggleService;

    private WaTaskCreationCronForExpiredBfActions waTaskCreationCronForExpiredBfActions;

    @BeforeEach
    void setUp() {
        waTaskCreationCronForExpiredBfActions = new WaTaskCreationCronForExpiredBfActions(
                adminUserService, ccdClient, featureToggleService);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(waTaskCreationCronForExpiredBfActions,
                "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(waTaskCreationCronForExpiredBfActions, "maxCases", 10);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
    }

    @Test
    void runsSuccessfullyWhenFeatureToggleEnabled() throws IOException {
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any())).thenReturn(new ArrayList<>());

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0)).submitEventForCase(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void doesNotRunWhenFeatureToggleDisabled() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(any(), any(), any());
    }

    @Test
    void processesCasesSuccessfully() throws IOException, URISyntaxException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        submitEvent.setCaseId(Long.parseLong("1741710954147332"));
        submitEvent.getCaseData().getBfActions().getFirst().getValue().setBfDate("2025-08-10");
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(Collections.emptyList());

        CaseData caseData = submitEvent.getCaseData();
        CCDRequest build = CCDRequestBuilder.builder().withCaseData(caseData).build();
        build.getCaseDetails().setJurisdiction("EMPLOYMENT");
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(build);

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(1)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(1)).submitEventForCase(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void handlesExceptionDuringProcessing() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any())).thenThrow(new RuntimeException("Error"));

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0)).submitEventForCase(
                any(), any(), any(), any(), any(), any());
    }
}
