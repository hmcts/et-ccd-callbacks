package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PreAcceptanceCaseService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.ECM_CASE_TYPES;

/**
 * Scheduled task that clears stale pre-acceptance dates on cases where both
 * {@code dateAccepted} and {@code dateRejected} are set simultaneously since only
 * one of the two is valid depending on whether the case
 * was accepted or rejected.
 *
 * <p>The task runs against all case type IDs configured via {@code cron.caseTypeId},
 * routing ET Reform cases ({@code ET_EnglandWales}, {@code ET_Scotland}) through the
 * Reform CCD client and legacy ECM cases through the ECM compat client.
 *
 * <p>Each eligible case is updated via the {@code fixCaseAPI} CCD event, which clears
 * {@code dateRejected} for accepted cases and {@code dateAccepted} for rejected cases.
 * Cases are processed in parallel using a fixed thread pool.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CaseAcceptanceDateTask implements Runnable {

    private static final String FIX_CASE_API_EVENT_ID = "fixCaseAPI";
    private static final long EXECUTOR_TIMEOUT_SECONDS = 120;

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final uk.gov.hmcts.ecm.compat.common.client.CcdClient ecmCcdClient;
    private final PreAcceptanceCaseService preAcceptanceCaseService;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Override
    public void run() {
        log.info("CaseAcceptanceDateTask started");
        String[] caseTypeIds = caseTypeIdsString.split(",");
        String query = buildQuery();

        String adminUserToken = adminUserService.getAdminUserToken();

        for (String caseTypeId : caseTypeIds) {
            if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId) || SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                processCaseType(caseTypeId,
                    () -> ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query),
                    submitEvent -> triggerEventForCase(adminUserToken, submitEvent, caseTypeId));
            } else if (ECM_CASE_TYPES.contains(caseTypeId)) {
                processCaseType(caseTypeId,
                    () -> ecmCcdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query),
                    submitEvent -> triggerEcmEventForCase(adminUserToken, submitEvent, caseTypeId));
            }
        }
    }

    /**
     * Fetches cases for a single case type and dispatches them for processing.
     * Errors during the fetch are logged and swallowed so that remaining case types
     * in the loop are still attempted.
     *
     * @param caseTypeId the CCD case type ID to process
     * @param fetcher    supplier that retrieves the matching cases from Elasticsearch
     * @param processor  per-case action to execute (start event, clear dates, submit event)
     */
    private <T> void processCaseType(String caseTypeId, CasesFetcher<T> fetcher, Consumer<T> processor) {
        try {
            List<T> cases = fetcher.fetch();
            log.info("{} - Case Acceptance Date task - Retrieved {} cases", caseTypeId, cases.size());
            if (cases.isEmpty()) {
                log.info("{} - Case Acceptance Date task - No cases to process", caseTypeId);
                return;
            }
            updateCases(cases, caseTypeId, processor);
        } catch (IOException e) {
            log.warn("{} - Case Acceptance Date task - error: {}", caseTypeId, e.getMessage(), e);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Dispatches all cases to a fixed thread pool and awaits completion.
     * If the pool does not terminate within {@value #EXECUTOR_TIMEOUT_SECONDS} seconds
     * it is forcibly shut down.
     *
     * @param cases      the cases to process
     * @param caseTypeId the case type ID, used for logging
     * @param processor  per-case action to execute on each thread
     * @throws InterruptedException if the awaiting thread is interrupted
     */
    private <T> void updateCases(List<T> cases, String caseTypeId, Consumer<T> processor)
        throws InterruptedException {
        final int poolSize = Math.min(15, Runtime.getRuntime().availableProcessors() * 2);

        try (ExecutorService executor = Executors.newFixedThreadPool(poolSize)) {
            for (T submitEvent : cases) {
                executor.execute(() -> processor.accept(submitEvent));
            }
            executor.shutdown();
            if (!executor.awaitTermination(EXECUTOR_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("{} - Executor did not terminate within {}s, forcing shutdown",
                    caseTypeId, EXECUTOR_TIMEOUT_SECONDS);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private void triggerEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        String caseId = String.valueOf(submitEvent.getCaseId());
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                caseId, FIX_CASE_API_EVENT_ID);
            CaseDetails caseDetails = ccdRequest.getCaseDetails();
            CaseData caseData = caseDetails.getCaseData();
            caseData.setStateAPI(null);
            preAcceptanceCaseService.clearPreAcceptanceDates(caseData);
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } catch (Exception e) {
            log.warn("{} - Failed to update case {}: {}", caseTypeId, caseId, e.getMessage(), e);
        }
    }

    private void triggerEcmEventForCase(String adminUserToken,
                                        uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent submitEvent,
                                        String caseTypeId) {
        String caseId = String.valueOf(submitEvent.getCaseId());
        try {
            uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest =
                ecmCcdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                    caseId, FIX_CASE_API_EVENT_ID);
            uk.gov.hmcts.ecm.common.model.ccd.CaseDetails caseDetails = ccdRequest.getCaseDetails();
            uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData = caseDetails.getCaseData();
            caseData.setStateAPI(null);
            clearEcmPreAcceptanceDates(caseData);
            ecmCcdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } catch (Exception e) {
            log.warn("{} - Failed to update case {}: {}", caseTypeId, caseId, e.getMessage(), e);
        }
    }

    private void clearEcmPreAcceptanceDates(uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData) {
        if (Objects.nonNull(caseData) && Objects.nonNull(caseData.getPreAcceptCase())) {
            String caseAccepted = defaultIfEmpty(caseData.getPreAcceptCase().getCaseAccepted(), "");
            if (YES.equals(caseAccepted)) {
                caseData.getPreAcceptCase().setDateRejected(null);
            } else if (NO.equals(caseAccepted)) {
                caseData.getPreAcceptCase().setDateAccepted(null);
            }
        }
    }

    /**
     * Functional interface for retrieving a list of cases from Elasticsearch.
     * Declared separately to allow the fetch operation to propagate {@link IOException}.
     *
     * @param <T> the submit event type ({@code ET_EnglandWales}/{@code ET_Scotland} or ECM)
     */
    @FunctionalInterface
    private interface CasesFetcher<T> {
        List<T> fetch() throws IOException;
    }

    private String buildQuery() {
        return new SearchSourceBuilder()
            .size(MAX_ES_SIZE)
            .query(new BoolQueryBuilder()
                .must(QueryBuilders.existsQuery("data.preAcceptCase.dateAccepted"))
                .must(QueryBuilders.existsQuery("data.preAcceptCase.dateRejected"))
            ).toString();
    }
}
