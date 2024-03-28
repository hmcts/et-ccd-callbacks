package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.is;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class BFActionsScheduledTasksTest {

    private BFActionsScheduledTasks bfActionsScheduledTasks;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private CcdClient ccdClient;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        bfActionsScheduledTasks = new BFActionsScheduledTasks(adminUserService, ccdClient, featureToggleService);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        ReflectionTestUtils.setField(bfActionsScheduledTasks, "caseTypeId", "ET_EnglandWales,ET_Scotland");
        ReflectionTestUtils.setField(bfActionsScheduledTasks, "maxCases", 10);
    }

    @Test
    void testCreateTasksForBFDates_success() throws IOException, URISyntaxException {
        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent));

        SubmitEvent submitEvent2 = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent2));

        CCDRequest returnedRequest = CCDRequestBuilder.builder()
                .withCaseData(submitEvent.getCaseData())
                .build();
        CCDRequest returnedRequestSct = CCDRequestBuilder.builder()
                .withCaseData(submitEvent2.getCaseData())
                .build();

        when(ccdClient.startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID),
                any(), any(), any())).thenReturn(returnedRequest);
        when(ccdClient.startEventForCase(any(), eq(SCOTLAND_CASE_TYPE_ID),
                any(), any(), any())).thenReturn(returnedRequestSct);

        bfActionsScheduledTasks.createTasksForBFDates();

        assertThat(returnedRequest.getCaseDetails().getCaseData().getWaRule21ReferralSent(), is(YES));
        assertThat(returnedRequestSct.getCaseDetails().getCaseData().getWaRule21ReferralSent(), is(YES));
    }

    @Test
    void testCreateTasksForBFDates_featureOff() throws IOException, URISyntaxException {
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);

        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);
        when(ccdClient.buildAndGetElasticSearchRequest(any(), any(), any())).thenReturn(List.of(submitEvent));
        bfActionsScheduledTasks.createTasksForBFDates();
        verifyNoInteractions(adminUserService);
        verifyNoInteractions(ccdClient);
    }
}