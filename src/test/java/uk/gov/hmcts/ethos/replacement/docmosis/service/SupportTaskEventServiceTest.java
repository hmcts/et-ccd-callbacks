package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.SupportTaskState;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.SupportTaskClientContextService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_JUDGE_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_LEGAL_OFFICER_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CREATE_ARRANGE_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_JUDGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER;

@ExtendWith(SpringExtension.class)
class SupportTaskEventServiceTest {
    private static final String TOKEN = "token";
    private static final String CASE_ID = "1234567890123456";
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String REQUESTED = "Requested";
    private static final String ACTIVE = "Active";
    private static final String NOT_APPROVED = "Not Approved";
    private static final String INACTIVE = "Inactive";
    private static final String COMPLETE_TASK_PATH = "/client_context/user_task/complete_task";

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

    static Stream<Arguments> closureCategories() {
        return Stream.of(
                Arguments.of(TASK_TYPE_REVIEW_SUPPORT_ADMIN, EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK),
                Arguments.of(TASK_TYPE_REVIEW_SUPPORT_JUDGE, EVENT_CLOSE_JUDGE_REVIEW_SUPPORT_TASK),
                Arguments.of(TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER, EVENT_CLOSE_LEGAL_OFFICER_REVIEW_SUPPORT_TASK));
    }

    @ParameterizedTest
    @MethodSource("closureCategories")
    void triggersTheCategorySpecificClosureEventAndResetsOnlyItsMarker(String taskType, String eventId)
            throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
        when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                eventId)).thenReturn(ccdRequest);
        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);

        service.triggerReviewTaskClosureEvents(caseDetails, Set.of(taskType));

        verify(ccdClient).startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID, eventId);
        verify(ccdClient).submitEventForCase(eq(TOKEN), caseDataCaptor.capture(), eq(CASE_TYPE),
                eq(JURISDICTION), eq(ccdRequest), eq(CASE_ID));
        SupportTaskState state = caseDataCaptor.getValue().getSupportTaskState();
        assertEquals(TASK_TYPE_REVIEW_SUPPORT_ADMIN.equals(taskType) ? NO : YES, state.getAdminTaskCreated());
        assertEquals(TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER.equals(taskType) ? NO : YES,
                state.getLegalOfficerTaskCreated());
        assertEquals(TASK_TYPE_REVIEW_SUPPORT_JUDGE.equals(taskType) ? NO : YES, state.getJudgeTaskCreated());
        verifyNoMoreInteractions(ccdClient);
    }

    static Stream<Arguments> reviewLifecycles() {
        return closureCategories().flatMap(category -> Stream.of(false, true)
                .flatMap(fromTask -> Stream.of(ACTIVE, NOT_APPROVED, INACTIVE)
                        .map(status -> Arguments.of(category.get()[0], category.get()[1], fromTask, status))));
    }

    @ParameterizedTest(name = "{0}, task link={2}, final status={3}")
    @MethodSource("reviewLifecycles")
    void completesReviewLifecycleAndAllowsANewRequest(String taskType, String closureEvent,
                                                     boolean fromTask, String finalStatus) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        final SupportTaskClientContextService contextService = new SupportTaskClientContextService(mapper);
        SupportTaskConfiguration configuration = new SupportTaskConfiguration();
        configuration.getReview().setJudgeFlagCodes(Set.of("RA0037", "RA0038"));
        configuration.getReview().setLegalOfficerFlagCodes(Set.of("RA0034", "RA0035"));
        SupportTaskService taskService = new SupportTaskService(configuration);
        List<String> codes = switch (taskType) {
            case TASK_TYPE_REVIEW_SUPPORT_ADMIN -> List.of("RA0039", "RA0041");
            case TASK_TYPE_REVIEW_SUPPORT_JUDGE -> List.of("RA0037", "RA0038");
            case TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER -> List.of("RA0034", "RA0035");
            default -> throw new IllegalArgumentException(taskType);
        };
        final String context = fromTask ? Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(
                Map.of("client_context", Map.of("user_task", Map.of(
                        "task_data", Map.of("id", "review-task-id", "type", taskType),
                        "complete_task", true))))) : null;
        CaseData data = new CaseData();
        data.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(requestedFlag("claimant-request", codes.getFirst())).build());
        taskService.prepareNewFlagReviewSupportTasks(data, new CaseData());
        assertCategoryState(data, taskType, YES, YES);

        CaseData before = mapper.readValue(mapper.writeValueAsBytes(data), CaseData.class);
        data.getAllPartyFlags().setRespondentExternalFlags(requestedFlag("respondent-request", codes.getLast()));
        taskService.prepareNewFlagReviewSupportTasks(data, before);
        assertCategoryState(data, taskType, YES, null);

        before = mapper.readValue(mapper.writeValueAsBytes(data), CaseData.class);
        data.getAllPartyFlags().getClaimantFlags().getDetails().getFirst().getValue().setStatus(ACTIVE);
        Set<String> closures = taskService.prepareManagedReviewSupportTasks(data, before);
        assertTrue(closures.isEmpty());
        assertCategoryState(data, taskType, YES, null);
        assertCompletionContext(mapper, contextService.updateTaskCompletion(context, closures), fromTask, false);
        service.triggerReviewTaskClosureEvents(caseDetails, taskService.reviewTaskTypesToClose(data));
        verifyNoInteractions(ccdClient);

        before = mapper.readValue(mapper.writeValueAsBytes(data), CaseData.class);
        data.getAllPartyFlags().getRespondentExternalFlags().getDetails().getFirst().getValue().setStatus(finalStatus);
        closures = taskService.prepareManagedReviewSupportTasks(data, before);
        assertEquals(Set.of(taskType), closures);
        assertCategoryState(data, taskType, NO, null);
        assertCompletionContext(mapper, contextService.updateTaskCompletion(context, closures), fromTask, true);
        if (!fromTask) {
            taskService.retainReviewTasksForSubmittedCallback(data, closures);
            assertCategoryState(data, taskType, YES, null);
            ccdRequest.getCaseDetails().setCaseData(data);
            when(adminUserService.getAdminUserToken()).thenReturn(TOKEN);
            when(ccdClient.startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID, closureEvent))
                    .thenReturn(ccdRequest);
            assertEquals(Set.of(taskType), taskService.reviewTaskTypesToClose(data));
            service.triggerReviewTaskClosureEvents(caseDetails, taskService.reviewTaskTypesToClose(data));
            verify(ccdClient).startEventForCase(TOKEN, CASE_TYPE, JURISDICTION, CASE_ID, closureEvent);
            verify(ccdClient).submitEventForCase(TOKEN, data, CASE_TYPE, JURISDICTION, ccdRequest, CASE_ID);
        } else {
            service.triggerReviewTaskClosureEvents(caseDetails, taskService.reviewTaskTypesToClose(data));
        }
        assertCategoryState(data, taskType, NO, null);
        assertTrue(taskService.reviewTaskTypesToClose(data).isEmpty());
        verifyNoMoreInteractions(ccdClient);

        before = mapper.readValue(mapper.writeValueAsBytes(data), CaseData.class);
        data.getAllPartyFlags().setClaimantRepresentativeExternalFlags(requestedFlag("new-request", codes.getFirst()));
        taskService.prepareNewFlagReviewSupportTasks(data, before);
        assertCategoryState(data, taskType, YES, YES);
        taskService.prepareNewFlagReviewSupportTasks(data, before);
        assertCategoryState(data, taskType, YES, null);
    }

    private static CaseFlagsType requestedFlag(String id, String code) {
        return CaseFlagsType.builder().details(ListTypeItem.from(GenericTypeItem.from(id,
                FlagDetailType.builder().flagCode(code).status(REQUESTED).build()))).build();
    }

    private static void assertCategoryState(CaseData data, String taskType, String created, String required) {
        SupportTaskState state = data.getSupportTaskState();
        switch (taskType) {
            case TASK_TYPE_REVIEW_SUPPORT_ADMIN -> {
                assertEquals(created, state.getAdminTaskCreated());
                assertEquals(required, state.getAdminTaskRequired());
            }
            case TASK_TYPE_REVIEW_SUPPORT_JUDGE -> {
                assertEquals(created, state.getJudgeTaskCreated());
                assertEquals(required, state.getJudgeTaskRequired());
            }
            case TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER -> {
                assertEquals(created, state.getLegalOfficerTaskCreated());
                assertEquals(required, state.getLegalOfficerTaskRequired());
            }
            default -> throw new IllegalArgumentException(taskType);
        }
    }

    private static void assertCompletionContext(ObjectMapper mapper, String context, boolean fromTask,
                                                boolean expected) throws IOException {
        if (fromTask) {
            assertEquals(expected, mapper.readTree(Base64.getDecoder().decode(context))
                    .at(COMPLETE_TASK_PATH).booleanValue());
        } else {
            assertNull(context);
        }
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
