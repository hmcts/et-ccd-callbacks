package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.SupportTaskState;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CREATE_ARRANGE_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_ADMIN;

@ExtendWith(SpringExtension.class)
class SupportTaskEventServiceTest {
    private static final String TOKEN = "token";
    private static final String CASE_ID = "1234567890123456";
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String JURISDICTION = "EMPLOYMENT";

    @Mock
    private CcdClient ccdClient;
    @Mock
    private AdminUserService adminUserService;
    private SupportTaskEventService service;

    private CaseDetails caseDetails;
    private CCDRequest ccdRequest;

    @BeforeEach
    void setUp() {
        SupportTaskConfiguration configuration = new SupportTaskConfiguration();
        configuration.getClosureRetry().setMaxAttempts(3);
        configuration.getClosureRetry().setInitialBackoffMs(1);
        configuration.getClosureRetry().setMultiplier(2);
        configuration.getClosureRetry().setMaxBackoffMs(2);
        service = new SupportTaskEventService(ccdClient, adminUserService, configuration);

        caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(CASE_TYPE);
        caseDetails.setJurisdiction(JURISDICTION);

        CaseData latestCaseData = new CaseData();
        latestCaseData.setSupportTaskState(SupportTaskState.builder()
                .adminTaskCreated(YES)
                .legalOfficerTaskCreated(YES)
                .judgeTaskCreated(YES)
                .build());
        CaseDetails latestCaseDetails = new CaseDetails();
        latestCaseDetails.setCaseData(latestCaseData);
        ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(latestCaseDetails);
    }

    @Test
    void triggersTheCategorySpecificClosureEventAndResetsOnlyItsMarker() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK)).thenReturn(ccdRequest);
        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);

        service.triggerReviewTaskClosureEvents(caseDetails, Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN));

        verify(ccdClient).submitEventForCase(eq(TOKEN), caseDataCaptor.capture(), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID));
        SupportTaskState state = caseDataCaptor.getValue().getSupportTaskState();
        assertEquals(NO, state.getAdminTaskCreated());
        assertEquals(YES, state.getLegalOfficerTaskCreated());
        assertEquals(YES, state.getJudgeTaskCreated());
    }

    @Test
    void doesNothingWhenNoReviewTaskCategoryNeedsClosing() throws IOException {
        service.triggerReviewTaskClosureEvents(caseDetails, Set.of());

        verify(ccdClient, never()).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, never()).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void triggersOneArrangeSupportEventForEachAdditionalFlag() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CREATE_ARRANGE_SUPPORT_TASK)).thenReturn(ccdRequest);
        List<SupportTaskService.ArrangeSupportTask> submitted = new ArrayList<>();
        when(ccdClient.submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID))).thenAnswer(invocation -> {
                    SupportTaskState state = invocation.<CaseData>getArgument(1).getSupportTaskState();
                    submitted.add(new SupportTaskService.ArrangeSupportTask(
                            state.getArrangeSupportTaskFlagId(), state.getArrangeSupportTaskName()));
                    assertEquals(YES, state.getAdminTaskCreated());
                    assertEquals(YES, state.getJudgeTaskCreated());
                    assertEquals(YES, state.getLegalOfficerTaskCreated());
                    return null;
                });

        List<SupportTaskService.ArrangeSupportTask> tasks = List.of(
                new SupportTaskService.ArrangeSupportTask("flag-2", "Support filling in forms"),
                new SupportTaskService.ArrangeSupportTask("flag-3", "Sign language interpreter"));
        service.triggerArrangeSupportTaskEvents(caseDetails, tasks);

        verify(ccdClient, times(2)).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID));
        assertEquals(tasks, submitted);
    }

    @Test
    void doesNotDuplicateArrangeSupportTaskWhenRetryFollowsACommittedEvent() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CREATE_ARRANGE_SUPPORT_TASK)).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        service.triggerArrangeSupportTaskEvents(caseDetails, List.of(
                new SupportTaskService.ArrangeSupportTask("flag-2", "Support filling in forms")));

        verify(ccdClient, times(2)).startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CREATE_ARRANGE_SUPPORT_TASK);
        verify(ccdClient).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID));
    }

    @Test
    void retriesFailedBatchRemainderWithoutDuplicatingTheCommittedFlag() throws IOException {
        AtomicReference<String> persistedFlagId = new AtomicReference<>();
        List<SupportTaskService.ArrangeSupportTask> committed = new ArrayList<>();
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CREATE_ARRANGE_SUPPORT_TASK)).thenAnswer(invocation -> {
                    CCDRequest freshRequest = requestWithAdminTaskCreated(YES);
                    freshRequest.getCaseDetails().getCaseData().getSupportTaskState()
                            .setArrangeSupportTaskFlagId(persistedFlagId.get());
                    return freshRequest;
                });
        when(ccdClient.submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), any(CCDRequest.class), eq(CASE_ID))).thenAnswer(invocation -> {
                    SupportTaskState state = invocation.<CaseData>getArgument(1).getSupportTaskState();
                    if ("flag-4".equals(state.getArrangeSupportTaskFlagId())) {
                        throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
                    }
                    persistedFlagId.set(state.getArrangeSupportTaskFlagId());
                    committed.add(new SupportTaskService.ArrangeSupportTask(
                            state.getArrangeSupportTaskFlagId(), state.getArrangeSupportTaskName()));
                    return null;
                });
        List<SupportTaskService.ArrangeSupportTask> tasks = List.of(
                new SupportTaskService.ArrangeSupportTask("flag-2", "Intermediary"),
                new SupportTaskService.ArrangeSupportTask("flag-3", "Intermediary"),
                new SupportTaskService.ArrangeSupportTask("flag-4", "Lip speaker"));

        assertThrows(GenericRuntimeException.class,
                () -> service.triggerArrangeSupportTaskEvents(caseDetails, tasks));
        assertEquals(tasks.subList(0, 2), committed);

        doAnswer(invocation -> {
            SupportTaskState state = invocation.<CaseData>getArgument(1).getSupportTaskState();
            persistedFlagId.set(state.getArrangeSupportTaskFlagId());
            committed.add(new SupportTaskService.ArrangeSupportTask(
                    state.getArrangeSupportTaskFlagId(), state.getArrangeSupportTaskName()));
            return null;
        }).when(ccdClient).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), any(CCDRequest.class), eq(CASE_ID));

        service.triggerArrangeSupportTaskEvents(caseDetails, tasks);

        assertEquals(tasks, committed);
        service.triggerArrangeSupportTaskEvents(caseDetails, tasks);
        assertEquals(tasks, committed, "Replaying a completed batch must not create duplicate tasks");
        verify(ccdClient, times(6)).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), any(CCDRequest.class), eq(CASE_ID));
    }

    @Test
    void wrapsFailureToTriggerTheClosureEvent() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK)).thenThrow(new IOException("CCD unavailable"));

        assertThrows(GenericRuntimeException.class,
                () -> service.triggerReviewTaskClosureEvents(caseDetails, Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN)));
        verify(ccdClient, times(3)).startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK);
    }

    @Test
    void retriesTransientFailureAndSubmitsTheClosureEvent() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK))
                .thenThrow(new IOException("CCD temporarily unavailable"))
                .thenReturn(ccdRequest);

        service.triggerReviewTaskClosureEvents(caseDetails, Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN));

        verify(ccdClient, times(2)).startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK);
        verify(ccdClient).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID));
    }

    @Test
    void doesNotRetryPermanentClientFailure() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(GenericRuntimeException.class,
                () -> service.triggerReviewTaskClosureEvents(caseDetails, Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN)));
        verify(ccdClient).startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK);
    }

    @Test
    void doesNotResubmitWhenPreviousAttemptWasCommitted() throws IOException {
        CCDRequest persistedRequest = requestWithAdminTaskCreated(NO);
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK)).thenReturn(ccdRequest, persistedRequest);
        when(ccdClient.submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID)))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        service.triggerReviewTaskClosureEvents(caseDetails, Set.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN));

        verify(ccdClient, times(2)).startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK);
        verify(ccdClient).submitEventForCase(eq(TOKEN), any(CaseData.class), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID));
    }

    private static CCDRequest requestWithAdminTaskCreated(String taskCreated) {
        CaseData caseData = new CaseData();
        caseData.setSupportTaskState(SupportTaskState.builder().adminTaskCreated(taskCreated).build());
        CaseDetails details = new CaseDetails();
        details.setCaseData(caseData);
        CCDRequest request = new CCDRequest();
        request.setCaseDetails(details);
        return request;
    }
}
