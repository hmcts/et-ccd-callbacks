package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class WaTaskCreationCronForExpiredBfActionsTest {

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private CcdClient ccdClient;

    @Mock
    private FeatureToggleService featureToggleService;

    private WaTaskCreationCronForExpiredBfActions cron;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cron = new WaTaskCreationCronForExpiredBfActions(adminUserService, ccdClient, featureToggleService);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(cron, "caseTypeIdsString", "ET_EnglandWales");
        ReflectionTestUtils.setField(cron, "maxCases", 10);
    }

    @Test
    void createWaTasksForExpiredBFDates_featureToggleDisabled_doesNothing() {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);

        assertDoesNotThrow(() -> cron.createWaTasksForExpiredBFDates());

        verifyNoInteractions(adminUserService, ccdClient);
    }

    @Test
    void createWaTasksForExpiredBFDates_noCasesFound_doesNothing() throws Exception {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("mockToken");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> cron.createWaTasksForExpiredBFDates());

        verify(ccdClient, times(1)).buildAndGetElasticSearchRequest(anyString(), anyString(), anyString());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void createWaTasksForExpiredBFDates_casesFound_triggersTaskEvent() throws Exception {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("mockToken");

        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);

        submitEvent.setCaseId(Long.parseLong("1741710954147332"));
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(Collections.emptyList());

        CaseData caseData = submitEvent.getCaseData();
        CCDRequest build = CCDRequestBuilder.builder().withCaseData(caseData).build();
        build.getCaseDetails().setJurisdiction("EMPLOYMENT");

        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(build);

        assertDoesNotThrow(() -> cron.createWaTasksForExpiredBFDates());

        verify(ccdClient, atLeastOnce()).buildAndGetElasticSearchRequest(anyString(), anyString(), anyString());
        verify(ccdClient, atLeastOnce()).startEventForCase(anyString(), anyString(), anyString(), anyString(),
                anyString());
        verify(ccdClient, atLeastOnce()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void buildQueryForExpiredBFActions_validInput_returnsQuery() {
        String yesterday = UtilHelper.formatCurrentDate2(LocalDate.now().minusDays(1));
        String today = UtilHelper.formatCurrentDate2(LocalDate.now());
        String query = cron.buildQueryForExpiredBFActions(yesterday, today);
        assertDoesNotThrow(() -> cron.createWaTasksForExpiredBFDates());
        assert query.contains("data.bfActions.value.bfDate");
        assert query.contains(yesterday);
    }
}
