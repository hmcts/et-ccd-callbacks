package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.TTL;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;

/**
 * Clears legacy TTL values from Reform cases using the existing CCD rollback
 * event.
 *
 * <p>Set {@code TASK_NAME=ClearCaseTtlTask} to run this task. It is dry-run by
 * default; set {@code CRON_CLEAR_CASE_TTL_DRY_RUN=false} only after validating
 * the matching cases and intended rollback.</p>
 */
@Component
@Slf4j
public class ClearCaseTtlTask implements Runnable {

    static final String ROLLBACK_TTL_EVENT = "rollbackMigrateCaseTTLDetails";
    static final String FIND_CASES_WITH_TTL = """
        SELECT reference
        FROM ccd.case_data
        WHERE case_type_id = :caseType
          AND reference > :afterReference
          AND (
              data #>> '{TTL,SystemTTL}' IS NOT NULL
              OR data #>> '{TTL,OverrideTTL}' IS NOT NULL
              OR data #>> '{TTL,Suspended}' IS NOT NULL
          )
        ORDER BY reference
        LIMIT :pageSize
        """;

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final String caseTypeIds;
    private final boolean dryRun;
    private final int maxCasesPerSearch;
    private final int maxCasesToProcess;
    private final int maxConcurrentCcdCalls;

    public ClearCaseTtlTask(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            NamedParameterJdbcTemplate jdbcTemplate,
            @Value("${cron.caseTypeId}") String caseTypeIds,
            @Value("${cron.clearCaseTtlDryRun:true}") boolean dryRun,
            @Value("${cron.clearCaseTtlMaxCasesPerSearch:500}") int maxCasesPerSearch,
            @Value("${cron.clearCaseTtlMaxCasesToProcess:1000}") int maxCasesToProcess,
            @Value("${cron.clearCaseTtlConcurrency:15}") int maxConcurrentCcdCalls) {
        this.adminUserService = adminUserService;
        this.ccdClient = ccdClient;
        this.jdbcTemplate = jdbcTemplate;
        this.caseTypeIds = caseTypeIds;
        this.dryRun = dryRun;
        this.maxCasesPerSearch = maxCasesPerSearch;
        this.maxCasesToProcess = maxCasesToProcess;
        this.maxConcurrentCcdCalls = maxConcurrentCcdCalls;
    }

    @Override
    public void run() {
        validateConfiguration();
        log.info("Clear case TTL task started; dryRun={}, maxCasesToProcess={}, maxConcurrentCcdCalls={}",
                dryRun, maxCasesToProcess, maxConcurrentCcdCalls);

        String adminUserToken = adminUserService.getAdminUserToken();
        MigrationSummary summary = new MigrationSummary();

        Arrays.stream(caseTypeIds.split(",")).forEach(
            caseTypeId -> processCaseType(adminUserToken, caseTypeId.strip(), summary));

        log.info("Clear case TTL task completed; found={}, cleared={}, wouldClear={}, skipped={}, failed={}",
                summary.found.get(), summary.cleared.get(), summary.wouldClear.get(), summary.skipped.get(),
                summary.failed.get());
    }

    private void validateConfiguration() {
        if (maxCasesPerSearch <= 0) {
            throw new IllegalArgumentException("cron.clearCaseTtlMaxCasesPerSearch must be greater than zero");
        }
        if (maxCasesToProcess <= 0) {
            throw new IllegalArgumentException("cron.clearCaseTtlMaxCasesToProcess must be greater than zero");
        }
        if (maxConcurrentCcdCalls <= 0) {
            throw new IllegalArgumentException("cron.clearCaseTtlConcurrency must be greater than zero");
        }
    }

    private void processCaseType(String adminUserToken, String caseType, MigrationSummary summary) {
        long afterReference = 0;
        int foundForCaseType = 0;
        while (foundForCaseType < maxCasesToProcess) {
            int pageSize = Math.min(maxCasesPerSearch, maxCasesToProcess - foundForCaseType);
            List<Long> caseReferences = findCasesWithTtl(caseType, afterReference, pageSize);
            if (caseReferences.isEmpty()) {
                return;
            }

            log.info("Found {} cases with TTL for caseType={}", caseReferences.size(), caseType);

            try (var executor = Executors.newFixedThreadPool(maxConcurrentCcdCalls, Thread.ofVirtual().factory())) {
                for (Long caseReference : caseReferences) {
                    foundForCaseType++;
                    int totalCandidates = summary.found.incrementAndGet();
                    log.info("TTL task progress; caseType={}, caseId={}, processedForCaseType={}/{}",
                            caseType, caseReference, foundForCaseType, totalCandidates);
                    executor.submit(() -> processCandidate(adminUserToken, caseType, String.valueOf(caseReference),
                            summary));
                }
            }
            afterReference = caseReferences.getLast();
        }
        log.warn("TTL processing limit reached; caseType={}, limit={}. Re-run to process remaining candidates",
                caseType, maxCasesToProcess);
    }

    private List<Long> findCasesWithTtl(String caseType, long afterReference, int pageSize) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("caseType", caseType)
                .addValue("afterReference", afterReference)
                .addValue("pageSize", pageSize);

        return jdbcTemplate.queryForList(FIND_CASES_WITH_TTL, parameters, Long.class);
    }

    private void processCandidate(String adminUserToken, String caseType, String caseId, MigrationSummary summary) {
        try {
            CCDRequest request = startRollbackEvent(adminUserToken, caseType, caseId);
            CaseDetails caseDetails = request.getCaseDetails();
            TTL ttl = caseDetails.getCaseData().getTtl();
            if (!hasTtlValues(ttl)) {
                summary.skipped.incrementAndGet();
                log.info("Skipping TTL clear after reading current case; caseType={}, caseId={}, ttl={}",
                        caseType, caseId, ttl);
                return;
            }
            if (dryRun) {
                summary.wouldClear.incrementAndGet();
                log.info("Dry run would clear TTL; caseType={}, caseId={}, ttl={}", caseType, caseId, ttl);
                return;
            }

            clearTtl(adminUserToken, caseType, caseId, request);
            summary.cleared.incrementAndGet();
            log.info("Cleared TTL; caseType={}, caseId={}", caseType, caseId);
        } catch (IOException | RestClientException | IllegalStateException exception) {
            summary.failed.incrementAndGet();
            log.error("Failed to clear TTL; caseType={}, caseId={}", caseType, caseId, exception);
        }
    }

    private CCDRequest startRollbackEvent(String adminUserToken, String caseType, String caseId) throws IOException {
        CCDRequest request = ccdClient.startEventForCase(adminUserToken, caseType, EMPLOYMENT, caseId,
                ROLLBACK_TTL_EVENT);
        if (request == null || request.getCaseDetails() == null || request.getCaseDetails().getCaseData() == null) {
            throw new IllegalStateException("CCD returned no case data for case " + caseId);
        }
        return request;
    }

    private void clearTtl(String adminUserToken, String caseType, String caseId,
                          CCDRequest request) throws IOException {
        CaseData caseData = request.getCaseDetails().getCaseData();
        caseData.setTtl(new TTL());
        caseData.setStateAPI(null);
        SubmitEvent result = ccdClient.submitEventForCase(adminUserToken, caseData, caseType, EMPLOYMENT, request,
                caseId);
        if (result == null || result.getCaseData() == null || hasTtlValues(result.getCaseData().getTtl())) {
            throw new IllegalStateException("CCD did not clear TTL for case " + caseId);
        }
    }

    private static boolean hasTtlValues(TTL ttl) {
        return ttl != null
                && (ttl.getSystemTTL() != null || ttl.getOverrideTTL() != null || ttl.getSuspended() != null);
    }

    private static final class MigrationSummary {
        private final AtomicInteger found = new AtomicInteger();
        private final AtomicInteger cleared = new AtomicInteger();
        private final AtomicInteger wouldClear = new AtomicInteger();
        private final AtomicInteger skipped = new AtomicInteger();
        private final AtomicInteger failed = new AtomicInteger();
    }
}
