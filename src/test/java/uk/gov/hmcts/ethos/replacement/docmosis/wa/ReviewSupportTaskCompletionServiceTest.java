package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.client.WaTaskApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchParameter;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TerminateTaskRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.WaTask;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewSupportTaskCompletionServiceTest {
    private static final String CASE_ID = "1234567890123456";
    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "service-token";
    private static final String ADMIN_TASK = "ReviewSupportRequestAdmin";
    private static final String JUDGE_TASK = "ReviewSupportRequestJudge";

    @Mock
    private WaTaskApiClient waTaskApiClient;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    private ReviewSupportTaskCompletionService service;

    @BeforeEach
    void setUp() {
        service = new ReviewSupportTaskCompletionService(waTaskApiClient, serviceAuthTokenGenerator);
    }

    @Test
    void searchesActiveTasksForTheCaseAndRequestedTaskTypes() {
        stubSearch(TaskSearchResponse.builder().tasks(List.of()).build());

        service.completeTasks(CASE_ID, USER_TOKEN, Set.of(ADMIN_TASK, JUDGE_TASK));

        ArgumentCaptor<TaskSearchRequest> request = ArgumentCaptor.forClass(TaskSearchRequest.class);
        verify(waTaskApiClient).searchTasks(eq(USER_TOKEN), eq(SERVICE_TOKEN), request.capture());
        List<TaskSearchParameter> parameters = request.getValue().getSearchParameters();
        assertThat(parameters).extracting(TaskSearchParameter::getKey)
                .containsExactly("case_id", "task_type", "state");
        assertThat(parameters.get(0).getValues()).containsExactly(CASE_ID);
        assertThat(parameters.get(1).getValues()).containsExactlyInAnyOrder(ADMIN_TASK, JUDGE_TASK);
        assertThat(parameters.get(2).getValues()).containsExactly("assigned", "unassigned");
        assertThat(parameters).extracting(TaskSearchParameter::getOperator).containsOnly("IN");
    }

    @Test
    void terminatesEveryReturnedTaskWithCompletedReason() {
        stubSearch(TaskSearchResponse.builder().tasks(List.of(
                WaTask.builder().id("admin-id").type(ADMIN_TASK).build(),
                WaTask.builder().id("judge-id").type(JUDGE_TASK).build())).build());

        service.completeTasks(CASE_ID, USER_TOKEN, Set.of(ADMIN_TASK, JUDGE_TASK));

        ArgumentCaptor<String> taskIds = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TerminateTaskRequest> requests = ArgumentCaptor.forClass(TerminateTaskRequest.class);
        verify(waTaskApiClient, times(2)).terminateTask(eq(SERVICE_TOKEN), taskIds.capture(), requests.capture());
        assertThat(taskIds.getAllValues()).containsExactlyInAnyOrder("admin-id", "judge-id");
        assertThat(requests.getAllValues())
                .extracting(request -> request.getTerminateInfo().getTerminateReason())
                .containsOnly("completed");
    }

    @Test
    void continuesWhenOneTerminationFails() {
        stubSearch(TaskSearchResponse.builder().tasks(List.of(
                WaTask.builder().id("fails").type(ADMIN_TASK).build(),
                WaTask.builder().id("succeeds").type(JUDGE_TASK).build())).build());
        doThrow(new IllegalStateException("unavailable"))
                .when(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), eq("fails"), any());

        service.completeTasks(CASE_ID, USER_TOKEN, Set.of(ADMIN_TASK, JUDGE_TASK));

        verify(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), eq("succeeds"), any());
    }

    @Test
    void doesNotPropagateSearchFailure() {
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(waTaskApiClient.searchTasks(anyString(), anyString(), any()))
                .thenThrow(new IllegalStateException("unavailable"));

        service.completeTasks(CASE_ID, USER_TOKEN, Set.of(ADMIN_TASK));

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @Test
    void toleratesMissingSearchResponseOrTaskList() {
        stubSearch(null);
        service.completeTasks(CASE_ID, USER_TOKEN, Set.of(ADMIN_TASK));
        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());

        when(waTaskApiClient.searchTasks(anyString(), anyString(), any()))
                .thenReturn(TaskSearchResponse.builder().build());
        service.completeTasks(CASE_ID, USER_TOKEN, Set.of(ADMIN_TASK));
        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void skipsWhenCaseIdIsMissing(String caseId) {
        service.completeTasks(caseId, USER_TOKEN, Set.of(ADMIN_TASK));
        verifyNoInteractions(waTaskApiClient, serviceAuthTokenGenerator);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void skipsWhenUserTokenIsMissing(String userToken) {
        service.completeTasks(CASE_ID, userToken, Set.of(ADMIN_TASK));
        verifyNoInteractions(waTaskApiClient, serviceAuthTokenGenerator);
    }

    @Test
    void skipsWhenNoTaskTypesNeedCompletion() {
        service.completeTasks(CASE_ID, USER_TOKEN, Set.of());
        service.completeTasks(CASE_ID, USER_TOKEN, null);
        verifyNoInteractions(waTaskApiClient, serviceAuthTokenGenerator);
    }

    private void stubSearch(TaskSearchResponse response) {
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(waTaskApiClient.searchTasks(anyString(), anyString(), any())).thenReturn(response);
    }
}
