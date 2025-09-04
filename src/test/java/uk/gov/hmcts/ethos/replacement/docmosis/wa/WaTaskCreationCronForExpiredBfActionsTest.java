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
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.expiredbftask.WaTaskCreationCronForExpiredBfActions;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
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
        ReflectionTestUtils.setField(waTaskCreationCronForExpiredBfActions, "maxCasesPerSearch", 10);
        ReflectionTestUtils.setField(waTaskCreationCronForExpiredBfActions, "maxCasesToProcess", 10);
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
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(any(), any(), any());
    }

    @Test
    void skipsProcessingWhenFeatureTogglesAreDisabled() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(false);

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(any(), any(), any());
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0))
                .submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void processesCasesSuccessfully() throws IOException, URISyntaxException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        submitEvent.setCaseId(Long.parseLong("1741710954147332"));
        submitEvent.getCaseData().getBfActions().getFirst().getValue().setBfDate(
                BFHelper.getEffectiveYesterday(LocalDate.of(2025, 5, 1)));
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

    @Test
    void skipsProcessingWhenNoCasesFound() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any())).thenReturn(Collections.emptyList());

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0))
                .submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void skipsCasesWithoutExpiredBfActions() throws IOException, URISyntaxException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);

        String resource = ResourceLoader.getResource("bfActionTask_NoExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(Collections.emptyList());

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0))
                .submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void handlesEmptyCaseTypeIdsGracefully() throws IOException {
        ReflectionTestUtils.setField(waTaskCreationCronForExpiredBfActions, "caseTypeIdsString", "");

        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        waTaskCreationCronForExpiredBfActions.run();

        try {
            verify(ccdClient, times(1)).buildAndGetElasticSearchRequest(any(), any(), any());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void skipsCasesWithNullCaseDataOrNullBfActions() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);

        SubmitEvent eventWithNullCaseData = new SubmitEvent();
        eventWithNullCaseData.setCaseId(1L);
        eventWithNullCaseData.setCaseData(null);

        SubmitEvent eventWithNullBfActions = new SubmitEvent();
        eventWithNullBfActions.setCaseId(2L);
        CaseData caseData = new CaseData();
        caseData.setBfActions(null);
        eventWithNullBfActions.setCaseData(caseData);

        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(eventWithNullCaseData, eventWithNullBfActions))
                .thenReturn(Collections.emptyList());

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0))
                .submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void logsErrorWhenTriggerTaskEventForCaseThrowsException() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        BFActionType bfActionType = new BFActionType();
        bfActionType.setBfDate(String.valueOf(LocalDate.now().minusDays(1)));
        bfActionType.setCleared(null);
        bfActionType.setIsWaTaskCreated(null);
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setValue(bfActionType);
        bfActionTypeItem.setId("TestId");
        List<BFActionTypeItem> bfActions = new ArrayList<>();
        bfActions.add(bfActionTypeItem);

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(123L);
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1234567890");
        caseData.setBfActions(bfActions);
        submitEvent.setCaseData(caseData);

        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(List.of(submitEvent)).thenReturn(Collections.emptyList());
        when(ccdClient.startEventForCase(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Simulated error"));

        waTaskCreationCronForExpiredBfActions.run();

        verify(ccdClient, times(1)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0))
                .submitEventForCase(any(), any(), any(), any(), any(), any());
    }
}
