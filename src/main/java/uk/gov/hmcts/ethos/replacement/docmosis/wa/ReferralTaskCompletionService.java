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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Completes the Work Allocation tasks belonging to one specific referral.
 *
 * <p>The task completion DMN can only match on the CCD event id, so it cannot tell one referral
 * from another - acting on any referral completes the review tasks for every open referral on the
 * case. This service closes only the tasks carrying the referral number that was acted on, which
 * the configuration DMN stamps onto each task as the "referralNumber" additional property.</p>
 *
 * <p>Tasks are found using the signed in user's token, which needs only Read permission, and then
 * closed service-to-service. Terminating carries no user token and performs no permission check,
 * so a task held by a different caseworker is still closed - matching what the blanket
 * auto-completion did before.</p>
 *
 * <p>The task types closed per event mirror the completion DMN exactly, so that removing those
 * events from the DMN is behaviour preserving apart from the cross-referral over-reach.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralTaskCompletionService {

    private static final String TERMINATE_REASON_COMPLETED = "completed";
    private static final String REFERRAL_NUMBER = "referralNumber";

    /**
     * Fallback for tasks created before the referralNumber property existed. The configuration DMN
     * has long titled these tasks "Review Referral #32 - Subject" (with EJ - / LO - prefixes and a
     * Response variant), so the number can be recovered from the title.
     *
     * <p>Additional properties cannot be written to an existing task - CFTTaskMapper only
     * reconfigures a fixed list of fields and additionalProperties is not on it - so pre-existing
     * tasks can never acquire the property and would otherwise be left permanently open once the
     * completion DMN rules are removed. This can be deleted once no such tasks remain.</p>
     */
    private static final Pattern TITLE_REFERRAL_NUMBER = Pattern.compile("#(\\d+)");
    private static final String OPERATOR_IN = "IN";

    private static final List<String> ALL_REFERRAL_TASK_TYPES = List.of(
        "ReviewReferralAdmin",
        "ReviewReferralJudiciary",
        "ReviewReferralLegalOps",
        "ReviewReferralResponseAdmin",
        "ReviewReferralResponseJudiciary",
        "ReviewReferralResponseLegalOps"
    );

    private static final List<String> UPDATE_REFERRAL_TASK_TYPES = List.of("ReviewReferralLegalOps");

    /**
     * Without an explicit state the search also returns completed and terminated tasks, which
     * must not be terminated a second time.
     */
    private static final List<String> ACTIVE_TASK_STATES = List.of("assigned", "unassigned");

    private final WaTaskApiClient waTaskApiClient;
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    /**
     * Closes every referral task raised for the referral that has just been closed.
     */
    public void completeTasksForClosedReferral(String caseId, String referralNumber, String userToken) {
        completeTasks(caseId, referralNumber, userToken, ALL_REFERRAL_TASK_TYPES);
    }

    /**
     * Closes every referral task raised for the referral that has just been replied to. A fresh
     * review referral response task is raised by initiation once the event is submitted.
     */
    public void completeTasksForReferralReply(String caseId, String referralNumber, String userToken) {
        completeTasks(caseId, referralNumber, userToken, ALL_REFERRAL_TASK_TYPES);
    }

    /**
     * Closes the legal ops review task for the referral that has just been updated. Updating a
     * referral has never completed the admin or judicial tasks.
     */
    public void completeTasksForUpdatedReferral(String caseId, String referralNumber, String userToken) {
        completeTasks(caseId, referralNumber, userToken, UPDATE_REFERRAL_TASK_TYPES);
    }

    private void completeTasks(String caseId, String referralNumber, String userToken, List<String> taskTypes) {
        if (isBlank(caseId) || isBlank(referralNumber)) {
            log.warn("Skipping referral task completion, case id or referral number missing for case {}", caseId);
            return;
        }

        try {
            String serviceToken = serviceAuthTokenGenerator.generate();
            List<WaTask> visibleTasks = findReferralTasks(caseId, userToken, serviceToken, taskTypes);
            List<WaTask> tasksForReferral = visibleTasks.stream()
                .filter(task -> referralNumber.equals(referralNumberOf(task)))
                .toList();

            log.info("Referral {} on case {}: {} referral task(s) visible, {} for this referral",
                referralNumber, caseId, visibleTasks.size(), tasksForReferral.size());

            tasksForReferral.forEach(task -> terminate(task, caseId, serviceToken));
        } catch (Exception e) {
            // A Work Allocation problem must never turn a successful CCD event into an error.
            log.error("Error completing tasks for referral {} on case {}: {}",
                referralNumber, caseId, e.getMessage(), e);
        }
    }

    private List<WaTask> findReferralTasks(String caseId, String userToken, String serviceToken,
                                           List<String> taskTypes) {
        TaskSearchRequest searchRequest = TaskSearchRequest.builder()
            .searchParameters(List.of(
                searchParameter("case_id", List.of(caseId)),
                searchParameter("task_type", taskTypes),
                searchParameter("state", ACTIVE_TASK_STATES)
            ))
            .build();

        TaskSearchResponse response = waTaskApiClient.searchTasks(userToken, serviceToken, searchRequest);
        return response == null || response.getTasks() == null ? List.of() : response.getTasks();
    }

    /**
     * Failures are contained per task so that one task refusing to close does not leave the
     * remaining tasks for the same referral open.
     */
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

    /**
     * Prefers the referralNumber additional property, falling back to the number embedded in the
     * task title for tasks created before that property existed.
     */
    private static String referralNumberOf(WaTask task) {
        Map<String, String> additionalProperties = task.getAdditionalProperties();
        String fromProperty = additionalProperties == null ? null : additionalProperties.get(REFERRAL_NUMBER);
        return isBlank(fromProperty) ? referralNumberFromTitle(task.getTaskTitle()) : fromProperty;
    }

    private static String referralNumberFromTitle(String taskTitle) {
        if (isBlank(taskTitle)) {
            return null;
        }
        Matcher matcher = TITLE_REFERRAL_NUMBER.matcher(taskTitle);
        return matcher.find() ? matcher.group(1) : null;
    }
}
