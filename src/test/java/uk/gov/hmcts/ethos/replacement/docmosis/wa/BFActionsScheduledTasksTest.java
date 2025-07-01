package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.is;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class BFActionsScheduledTasksTest {

    private BFActionsScheduledTasks bfActionsScheduledTasks;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private CcdClient ccdClient;

    @MockitoBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        bfActionsScheduledTasks = new BFActionsScheduledTasks(adminUserService, ccdClient, featureToggleService);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(bfActionsScheduledTasks, "caseTypeIdsString", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(bfActionsScheduledTasks, "maxCases", 10);
        ReflectionTestUtils.setField(bfActionsScheduledTasks, "bfActionCronCaseIdsToSkip", "");
    }

    @Test
    void testCreateTasksForBFDates_success() throws IOException, URISyntaxException {
        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(new ArrayList<>());

        SubmitEvent submitEvent2 = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent2)).thenReturn(null);

        CaseData caseData = submitEvent.getCaseData();

        CCDRequest build = CCDRequestBuilder.builder().withCaseData(caseData).build();
        build.getCaseDetails().setJurisdiction("EMPLOYMENT");

        when(ccdClient.startEventForCase(any(), any(), any(), any(), any())).thenReturn(build);

        bfActionsScheduledTasks.createTasksForBFDates();

        assertThat(build.getCaseDetails().getCaseData().getWaRule21ReferralSent(), is(YES));
        assertThat(build.getCaseDetails().getCaseData().getWaRule21ReferralSent(), is(YES));
        verify(ccdClient, times(1)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldSkipCase() throws URISyntaxException, IOException {
        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent)).thenReturn(new ArrayList<>());
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(null);

        // Set the case ID to be skipped
        ReflectionTestUtils.setField(bfActionsScheduledTasks, "bfActionCronCaseIdsToSkip", "1704985449005627");
        bfActionsScheduledTasks.createTasksForBFDates();
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCreateTasksForBFDates_featureOff() throws IOException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any())).thenThrow(new IOException());

        bfActionsScheduledTasks.createTasksForBFDates();
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
    }
}