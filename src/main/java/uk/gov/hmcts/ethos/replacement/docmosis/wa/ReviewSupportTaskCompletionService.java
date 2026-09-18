package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.client.WaTaskApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchParameter;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TerminateInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TerminateTaskRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.WaTask;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewSupportTaskCompletionService {
    private static final String OPERATOR_IN = "IN";
    private static final String TERMINATE_REASON_COMPLETED = "completed";
    private static final List<String> ACTIVE_TASK_STATES = List.of("assigned", "unassigned");

    private final WaTaskApiClient waTaskApiClient;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    public void completeTasks(String caseId, String userToken, Set<String> taskTypes) {
        if (isBlank(caseId) || isBlank(userToken) || taskTypes == null || taskTypes.isEmpty()) {
            return;
        }

        try {
            String serviceToken = serviceAuthTokenGenerator.generate();
            List<WaTask> tasks = findActiveTasks(caseId, userToken, serviceToken, taskTypes);
            log.info("Case {}: {} active Review Support task(s) to complete", caseId, tasks.size());
            tasks.forEach(task -> terminate(task, caseId, serviceToken));
        } catch (Exception e) {
            log.error("Error completing Review Support tasks on case {}: {}", caseId, e.getMessage(), e);
        }
    }

    private List<WaTask> findActiveTasks(String caseId, String userToken, String serviceToken,
                                         Set<String> taskTypes) {
        TaskSearchRequest request = TaskSearchRequest.builder()
                .searchParameters(List.of(
                        searchParameter("case_id", List.of(caseId)),
                        searchParameter("task_type", List.copyOf(taskTypes)),
                        searchParameter("state", ACTIVE_TASK_STATES)))
                .build();
        TaskSearchResponse response = waTaskApiClient.searchTasks(userToken, serviceToken, request);
        return response == null || response.getTasks() == null ? List.of() : response.getTasks();
    }

    private void terminate(WaTask task, String caseId, String serviceToken) {
        TerminateTaskRequest request = TerminateTaskRequest.builder()
                .terminateInfo(TerminateInfo.builder().terminateReason(TERMINATE_REASON_COMPLETED).build())
                .build();
        try {
            waTaskApiClient.terminateTask(serviceToken, task.getId(), request);
            log.info("Completed task {} ({}) on case {}", task.getId(), task.getType(), caseId);
        } catch (Exception e) {
            log.error("Could not complete task {} ({}) on case {}: {}",
                    task.getId(), task.getType(), caseId, e.getMessage(), e);
        }
    }

    private static TaskSearchParameter searchParameter(String key, List<String> values) {
        return TaskSearchParameter.builder().key(key).operator(OPERATOR_IN).values(values).build();
    }
}
