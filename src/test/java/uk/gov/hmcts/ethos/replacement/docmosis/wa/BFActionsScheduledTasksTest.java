package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.impl.PostgresAdvisoryLock;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @MockitoBean
    private PostgresAdvisoryLock advisoryLock;

    @BeforeEach
    void setUp() {
        bfActionsScheduledTasks = new BFActionsScheduledTasks(
                adminUserService, ccdClient, featureToggleService, advisoryLock);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);
        when(advisoryLock.runIfAcquired(eq("et-bf-actions"), eq("task"), any(Runnable.class)))
                .thenAnswer(invocation -> {
                    Runnable operation = invocation.getArgument(2);
                    operation.run();
                    return true;
                });
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
        verify(advisoryLock, times(0)).runIfAcquired(any(), any(), any(Runnable.class));
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void testCreateTasksForBFDates_lockNotAcquired() throws IOException {
        when(advisoryLock.runIfAcquired(eq("et-bf-actions"), eq("task"), any(Runnable.class)))
                .thenReturn(false);

        bfActionsScheduledTasks.createTasksForBFDates();

        verify(advisoryLock, times(1)).runIfAcquired(eq("et-bf-actions"), eq("task"), any(Runnable.class));
        verify(adminUserService, times(0)).getAdminUserToken();
        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(any(), any(), any());
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
    }

    @Test
    void testDataSourceConstructor() {
        DataSource dataSource = mock(DataSource.class);
        BFActionsScheduledTasks tasks = new BFActionsScheduledTasks(
                adminUserService, ccdClient, featureToggleService, dataSource);
        assertNotNull(tasks);
    }

    @Test
    void shouldNotTriggerRule22WhenOtherBfExpiredAndClaimServedNotExpired() throws IOException {
        SubmitEvent ewSubmitEvent = createSubmitEvent(1001L, List.of(
                createBfAction(LocalDate.now().minusDays(2), null, null, null),
                createBfAction(LocalDate.now().plusDays(10), "Claim served", YES, null)
        ));
        SubmitEvent scotSubmitEvent = createSubmitEvent(1002L, List.of(
                createBfAction(LocalDate.now().minusDays(2), null, null, null),
                createBfAction(LocalDate.now().plusDays(10), "Claim served", YES, null)
        ));

        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(ewSubmitEvent));
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(List.of(scotSubmitEvent));

        bfActionsScheduledTasks.createTasksForBFDates();

        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldNotTriggerRule22WhenClaimServedBfIsCleared() throws IOException {
        SubmitEvent submitEvent = createSubmitEvent(1003L, List.of(
                createBfAction(LocalDate.now().minusDays(1), "Claim served", YES, "2026-11-20")
        ));

        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent));
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(new ArrayList<>());

        bfActionsScheduledTasks.createTasksForBFDates();

        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldTriggerRule22WhenClaimServedBfHasExpired() throws IOException {
        SubmitEvent ewSubmitEvent = createSubmitEvent(1004L, List.of(
                createBfAction(LocalDate.now().minusDays(5), null, null, null),
                createBfAction(LocalDate.now().minusDays(1), "Claim served", YES, null)
        ));
        SubmitEvent scotSubmitEvent = createSubmitEvent(1005L, List.of(
                createBfAction(LocalDate.now().minusDays(1), "Claim served", YES, null)
        ));

        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(ewSubmitEvent));
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(List.of(scotSubmitEvent));

        CCDRequest ewRequest = CCDRequestBuilder.builder().withCaseData(ewSubmitEvent.getCaseData()).build();
        ewRequest.getCaseDetails().setJurisdiction("EMPLOYMENT");
        CCDRequest scotRequest = CCDRequestBuilder.builder().withCaseData(scotSubmitEvent.getCaseData()).build();
        scotRequest.getCaseDetails().setJurisdiction("EMPLOYMENT");

        when(ccdClient.startEventForCase(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any(), eq("1004"),
                eq("WA_REVIEW_RULE21_REFERRAL"))).thenReturn(ewRequest);
        when(ccdClient.startEventForCase(any(), eq(SCOTLAND_CASE_TYPE_ID), any(), eq("1005"),
                eq("WA_REVIEW_RULE21_REFERRAL"))).thenReturn(scotRequest);

        bfActionsScheduledTasks.createTasksForBFDates();

        assertThat(ewRequest.getCaseDetails().getCaseData().getWaRule21ReferralSent(), is(YES));
        assertThat(scotRequest.getCaseDetails().getCaseData().getWaRule21ReferralSent(), is(YES));
        verify(ccdClient, times(2)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    private SubmitEvent createSubmitEvent(long caseId, List<BFActionTypeItem> bfActions) {
        CaseData caseData = new CaseData();
        caseData.setBfActions(bfActions);
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(caseId);
        submitEvent.setCaseData(caseData);
        return submitEvent;
    }

    private BFActionTypeItem createBfAction(LocalDate bfDate, String allActions, String letters, String cleared) {
        BFActionType bfActionType = new BFActionType();
        bfActionType.setBfDate(bfDate.toString());
        bfActionType.setAllActions(allActions);
        bfActionType.setLetters(letters);
        bfActionType.setCleared(cleared);
        BFActionTypeItem item = new BFActionTypeItem();
        item.setValue(bfActionType);
        return item;
    }
}