package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
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
import java.util.Map;

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
class ReferralTaskCompletionServiceTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String USER_TOKEN = "Bearer user-token";
    private static final String SERVICE_TOKEN = "service-token";

    @Mock
    private WaTaskApiClient waTaskApiClient;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    private ReferralTaskCompletionService service;

    @BeforeEach
    void setUp() {
        service = new ReferralTaskCompletionService(waTaskApiClient, serviceAuthTokenGenerator);
    }

    private void stubServiceToken() {
        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    private void tasksReturned(WaTask... tasks) {
        when(waTaskApiClient.searchTasks(anyString(), anyString(), any(TaskSearchRequest.class)))
            .thenReturn(TaskSearchResponse.builder().tasks(List.of(tasks)).build());
    }

    private static WaTask legacyTask(String id, String type, String taskTitle) {
        return WaTask.builder().id(id).type(type).taskTitle(taskTitle).build();
    }

    private static WaTask task(String id, String type, String referralNumber) {
        return WaTask.builder()
            .id(id)
            .type(type)
            .additionalProperties(referralNumber == null ? null : Map.of("referralNumber", referralNumber))
            .build();
    }

    @Test
    void shouldOnlyCompleteTasksForTheReferralActedOn() {
        stubServiceToken();
        tasksReturned(
            task("task-for-5", "ReviewReferralAdmin", "5"),
            task("task-for-32", "ReviewReferralAdmin", "32"),
            task("response-for-32", "ReviewReferralResponseAdmin", "32"));

        service.completeTasksForClosedReferral(CASE_ID, "5", USER_TOKEN);

        ArgumentCaptor<String> taskIds = ArgumentCaptor.forClass(String.class);
        verify(waTaskApiClient, times(1))
            .terminateTask(eq(SERVICE_TOKEN), taskIds.capture(), any(TerminateTaskRequest.class));
        assertThat(taskIds.getValue()).isEqualTo("task-for-5");
    }

    @Test
    void shouldCompleteEveryTaskBelongingToTheReferral() {
        stubServiceToken();
        tasksReturned(
            task("review-32", "ReviewReferralAdmin", "32"),
            task("response-32", "ReviewReferralResponseAdmin", "32"),
            task("review-5", "ReviewReferralAdmin", "5"));

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, times(2))
            .terminateTask(eq(SERVICE_TOKEN), anyString(), any(TerminateTaskRequest.class));
    }

    @Test
    void shouldTerminateWithCompletedReason() {
        stubServiceToken();
        tasksReturned(task("review-32", "ReviewReferralAdmin", "32"));

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        ArgumentCaptor<TerminateTaskRequest> request = ArgumentCaptor.forClass(TerminateTaskRequest.class);
        verify(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), anyString(), request.capture());
        assertThat(request.getValue().getTerminateInfo().getTerminateReason()).isEqualTo("completed");
    }

    @Test
    void shouldSearchOnlyActiveTasksForTheCase() {
        stubServiceToken();
        tasksReturned();

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        ArgumentCaptor<TaskSearchRequest> request = ArgumentCaptor.forClass(TaskSearchRequest.class);
        verify(waTaskApiClient).searchTasks(eq(USER_TOKEN), eq(SERVICE_TOKEN), request.capture());

        List<TaskSearchParameter> parameters = request.getValue().getSearchParameters();
        assertThat(parameters).extracting(TaskSearchParameter::getKey)
            .containsExactly("case_id", "task_type", "state");
        assertThat(parameters.get(0).getValues()).containsExactly(CASE_ID);
        assertThat(parameters.get(1).getValues()).hasSize(6);
        assertThat(parameters.get(2).getValues()).containsExactly("assigned", "unassigned");
    }

    @Test
    void shouldOnlySearchLegalOpsTasksWhenReferralUpdated() {
        stubServiceToken();
        tasksReturned();

        service.completeTasksForUpdatedReferral(CASE_ID, "32", USER_TOKEN);

        ArgumentCaptor<TaskSearchRequest> request = ArgumentCaptor.forClass(TaskSearchRequest.class);
        verify(waTaskApiClient).searchTasks(anyString(), anyString(), request.capture());
        assertThat(request.getValue().getSearchParameters().get(1).getValues())
            .containsExactly("ReviewReferralLegalOps");
    }

    @Test
    void shouldIgnoreTasksWithoutAReferralNumber() {
        stubServiceToken();
        tasksReturned(task("legacy-task", "ReviewReferralAdmin", null));

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @Test
    void shouldSkipWhenReferralNumberIsMissing() {
        service.completeTasksForClosedReferral(CASE_ID, null, USER_TOKEN);

        verifyNoInteractions(waTaskApiClient, serviceAuthTokenGenerator);
    }

    @Test
    void shouldNotPropagateWorkAllocationFailures() {
        stubServiceToken();
        when(waTaskApiClient.searchTasks(anyString(), anyString(), any()))
            .thenThrow(new IllegalStateException("task management unavailable"));

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @Test
    void shouldSearchAllReferralTaskTypesWhenReferralRepliedTo() {
        stubServiceToken();
        tasksReturned(task("review-32", "ReviewReferralAdmin", "32"));

        service.completeTasksForReferralReply(CASE_ID, "32", USER_TOKEN);

        ArgumentCaptor<TaskSearchRequest> request = ArgumentCaptor.forClass(TaskSearchRequest.class);
        verify(waTaskApiClient).searchTasks(eq(USER_TOKEN), eq(SERVICE_TOKEN), request.capture());
        assertThat(request.getValue().getSearchParameters().get(1).getValues())
            .containsExactly("ReviewReferralAdmin", "ReviewReferralJudiciary", "ReviewReferralLegalOps",
                "ReviewReferralResponseAdmin", "ReviewReferralResponseJudiciary",
                "ReviewReferralResponseLegalOps");
        verify(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), eq("review-32"), any());
    }

    @Test
    void shouldTerminateEveryMatchingTaskById() {
        stubServiceToken();
        tasksReturned(
            task("review-32", "ReviewReferralAdmin", "32"),
            task("judicial-32", "ReviewReferralJudiciary", "32"),
            task("review-5", "ReviewReferralAdmin", "5"));

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        ArgumentCaptor<String> ids = ArgumentCaptor.forClass(String.class);
        verify(waTaskApiClient, times(2))
            .terminateTask(eq(SERVICE_TOKEN), ids.capture(), any(TerminateTaskRequest.class));
        assertThat(ids.getAllValues()).containsExactlyInAnyOrder("review-32", "judicial-32");
    }

    @Test
    void shouldStillCompleteRemainingTasksWhenOneTerminateFails() {
        stubServiceToken();
        tasksReturned(
            task("fails", "ReviewReferralAdmin", "32"),
            task("succeeds", "ReviewReferralJudiciary", "32"));
        doThrow(new IllegalStateException("boom"))
            .when(waTaskApiClient).terminateTask(anyString(), eq("fails"), any());

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), eq("succeeds"), any());
    }

    @Test
    void shouldDoNothingWhenSearchReturnsNoTasks() {
        stubServiceToken();
        tasksReturned();

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @Test
    void shouldTolerateANullSearchResponse() {
        stubServiceToken();
        when(waTaskApiClient.searchTasks(anyString(), anyString(), any())).thenReturn(null);

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @Test
    void shouldTolerateAResponseWithNoTaskList() {
        stubServiceToken();
        when(waTaskApiClient.searchTasks(anyString(), anyString(), any()))
            .thenReturn(TaskSearchResponse.builder().tasks(null).build());

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void shouldSkipWhenReferralNumberIsMissingOrBlank(String referralNumber) {
        service.completeTasksForClosedReferral(CASE_ID, referralNumber, USER_TOKEN);

        verifyNoInteractions(waTaskApiClient, serviceAuthTokenGenerator);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void shouldSkipWhenCaseIdIsMissing(String caseId) {
        service.completeTasksForClosedReferral(caseId, "32", USER_TOKEN);

        verifyNoInteractions(waTaskApiClient, serviceAuthTokenGenerator);
    }

    @ParameterizedTest
    @CsvSource({
        "Review Referral #32 - Orders,32",
        "EJ - Review Referral #32 - Orders,32",
        "LO - Review Referral #32 - Orders,32",
        "Review Referral Response #32 - Orders,32",
        "EJ - Review Referral Response #32 - Other - please specify,32",
        "Review Referral #7 - Orders,7"
    })
    void shouldFallBackToTheReferralNumberInTheTitle(String taskTitle, String expectedMatch) {
        stubServiceToken();
        tasksReturned(legacyTask("legacy", "ReviewReferralAdmin", taskTitle));

        service.completeTasksForClosedReferral(CASE_ID, expectedMatch, USER_TOKEN);

        verify(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), eq("legacy"), any());
    }

    @Test
    void shouldNotMatchALegacyTaskBelongingToADifferentReferral() {
        stubServiceToken();
        tasksReturned(legacyTask("legacy-5", "ReviewReferralAdmin", "Review Referral #5 - Orders"));

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @Test
    void shouldPreferTheAdditionalPropertyOverTheTitle() {
        stubServiceToken();
        WaTask conflicting = WaTask.builder()
            .id("conflict")
            .type("ReviewReferralAdmin")
            .taskTitle("Review Referral #5 - Orders")
            .additionalProperties(Map.of("referralNumber", "32"))
            .build();
        tasksReturned(conflicting);

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), eq("conflict"), any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "Review Referral", "Review Referral - Orders"})
    void shouldIgnoreLegacyTasksWithNoNumberInTheTitle(String taskTitle) {
        stubServiceToken();
        tasksReturned(legacyTask("no-number", "ReviewReferralAdmin", taskTitle));

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient, never()).terminateTask(anyString(), anyString(), any());
    }

    @Test
    void shouldFallBackWhenTheAdditionalPropertyIsPresentButBlank() {
        stubServiceToken();
        WaTask blankProperty = WaTask.builder()
            .id("blank-prop")
            .type("ReviewReferralAdmin")
            .taskTitle("Review Referral #32 - Orders")
            .additionalProperties(Map.of("referralNumber", ""))
            .build();
        tasksReturned(blankProperty);

        service.completeTasksForClosedReferral(CASE_ID, "32", USER_TOKEN);

        verify(waTaskApiClient).terminateTask(eq(SERVICE_TOKEN), eq("blank-prop"), any());
    }
}
