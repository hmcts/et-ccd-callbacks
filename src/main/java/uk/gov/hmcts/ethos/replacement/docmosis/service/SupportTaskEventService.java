package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.SupportTaskState;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SupportTaskConfiguration.ClosureRetry;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_JUDGE_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CLOSE_LEGAL_OFFICER_REVIEW_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_CREATE_ARRANGE_SUPPORT_TASK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_ADMIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_JUDGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER;

@Service
@Slf4j
public class SupportTaskEventService {
    private final CcdClient ccdClient;
    private final AdminUserService adminUserService;
    private final RetryTemplate retryTemplate;
    private final int maxAttempts;

    public SupportTaskEventService(CcdClient ccdClient,
                                   AdminUserService adminUserService,
                                   SupportTaskConfiguration configuration) {
        this.ccdClient = ccdClient;
        this.adminUserService = adminUserService;
        ClosureRetry retry = configuration.getClosureRetry();
        maxAttempts = retry.getMaxAttempts();
        retryTemplate = RetryTemplate.builder()
                .maxAttempts(maxAttempts)
                .exponentialBackoff(retry.getInitialBackoffMs(), retry.getMultiplier(), retry.getMaxBackoffMs())
                .retryOn(SupportTaskEventService::isTransientFailure)
                .build();
    }

    public void triggerReviewTaskClosureEvents(CaseDetails caseDetails, Set<String> taskTypes) {
        triggerIfRequired(caseDetails, taskTypes, TASK_TYPE_REVIEW_SUPPORT_ADMIN,
                EVENT_CLOSE_ADMIN_REVIEW_SUPPORT_TASK,
                SupportTaskState::getAdminTaskCreated, SupportTaskState::setAdminTaskCreated);
        triggerIfRequired(caseDetails, taskTypes, TASK_TYPE_REVIEW_SUPPORT_LEGAL_OFFICER,
                EVENT_CLOSE_LEGAL_OFFICER_REVIEW_SUPPORT_TASK,
                SupportTaskState::getLegalOfficerTaskCreated, SupportTaskState::setLegalOfficerTaskCreated);
        triggerIfRequired(caseDetails, taskTypes, TASK_TYPE_REVIEW_SUPPORT_JUDGE,
                EVENT_CLOSE_JUDGE_REVIEW_SUPPORT_TASK,
                SupportTaskState::getJudgeTaskCreated, SupportTaskState::setJudgeTaskCreated);
    }

    public void triggerArrangeSupportTaskEvents(
            CaseDetails caseDetails, List<SupportTaskService.ArrangeSupportTask> tasks) {
        tasks.forEach(task -> triggerArrangeSupportTaskEvent(caseDetails, task));
    }

    private void triggerArrangeSupportTaskEvent(
            CaseDetails caseDetails, SupportTaskService.ArrangeSupportTask task) {
        String caseId = caseDetails.getCaseId();
        try {
            boolean submitted = retryTemplate.execute(context -> submitArrangeSupportTaskEvent(caseDetails, task));
            if (submitted) {
                log.info("Triggered {} for flag {} on case {}", EVENT_CREATE_ARRANGE_SUPPORT_TASK,
                        task.flagId(), caseId);
            }
        } catch (Exception exception) {
            log.error("Unable to trigger {} for flag {} on case {} after {} attempts",
                    EVENT_CREATE_ARRANGE_SUPPORT_TASK, task.flagId(), caseId, maxAttempts, exception);
            throw new GenericRuntimeException(exception);
        }
    }

    private boolean submitArrangeSupportTaskEvent(
            CaseDetails caseDetails, SupportTaskService.ArrangeSupportTask task) throws IOException {
        String token = adminUserService.getAdminUserToken();
        String caseId = caseDetails.getCaseId();
        CCDRequest request = ccdClient.startEventForCase(token, caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(), caseId, EVENT_CREATE_ARRANGE_SUPPORT_TASK);
        CaseData latestCaseData = request.getCaseDetails().getCaseData();
        SupportTaskState state = latestCaseData.getSupportTaskState();
        if (state != null && Objects.equals(task.flagId(), state.getArrangeSupportTaskFlagId())) {
            return false;
        }
        if (state == null) {
            state = new SupportTaskState();
            latestCaseData.setSupportTaskState(state);
        }
        state.setArrangeSupportTaskName(task.taskName());
        state.setArrangeSupportTaskFlagId(task.flagId());
        ccdClient.submitEventForCase(token, latestCaseData, caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(), request, caseId);
        return true;
    }

    private void triggerIfRequired(CaseDetails caseDetails,
                                   Set<String> taskTypes,
                                   String taskType,
                                   String eventId,
                                   Function<SupportTaskState, String> taskCreatedGetter,
                                   BiConsumer<SupportTaskState, String> taskCreatedSetter) {
        if (!taskTypes.contains(taskType)) {
            return;
        }

        String caseId = caseDetails.getCaseId();
        try {
            boolean submitted = retryTemplate.execute(context -> {
                if (context.getRetryCount() > 0) {
                    log.warn("Retrying {} for case {}, attempt {} of {}", eventId, caseId,
                            context.getRetryCount() + 1, maxAttempts);
                }
                return submitClosureEvent(caseDetails, eventId, taskCreatedGetter, taskCreatedSetter);
            });
            if (submitted) {
                log.info("Triggered {} for case {}", eventId, caseId);
            } else {
                log.info("Closure event {} was already submitted for case {}", eventId, caseId);
            }
        } catch (Exception exception) {
            log.error("Unable to trigger {} for case {} after {} attempts", eventId, caseId, maxAttempts, exception);
            throw new GenericRuntimeException(exception);
        }
    }

    private boolean submitClosureEvent(CaseDetails caseDetails,
                                       String eventId,
                                       Function<SupportTaskState, String> taskCreatedGetter,
                                       BiConsumer<SupportTaskState, String> taskCreatedSetter) throws IOException {
        String token = adminUserService.getAdminUserToken();
        String caseId = caseDetails.getCaseId();
        CCDRequest request = ccdClient.startEventForCase(token, caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(), caseId, eventId);
        CaseData latestCaseData = request.getCaseDetails().getCaseData();
        SupportTaskState state = latestCaseData.getSupportTaskState();
        if (state != null && !isTaskCreated(taskCreatedGetter.apply(state))) {
            return false;
        }
        if (state == null) {
            state = new SupportTaskState();
            latestCaseData.setSupportTaskState(state);
        }
        taskCreatedSetter.accept(state, NO);
        ccdClient.submitEventForCase(token, latestCaseData, caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(), request, caseId);
        return true;
    }

    private static boolean isTaskCreated(String taskCreated) {
        return YES.equalsIgnoreCase(taskCreated) || Boolean.parseBoolean(taskCreated);
    }

    private static boolean isTransientFailure(Throwable exception) {
        if (exception instanceof IOException
                || exception instanceof ResourceAccessException
                || exception instanceof HttpServerErrorException) {
            return true;
        }
        if (exception instanceof HttpClientErrorException clientError) {
            return clientError.getStatusCode() == HttpStatus.CONFLICT
                    || clientError.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        }
        return false;
    }
}
