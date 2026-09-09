package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.UncheckedIOException;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ClearDraftTtlTask implements Runnable {

    static final String DRAFT_STATE = "AWAITING_SUBMISSION_TO_HMCTS";
    static final String ROLLBACK_TTL_EVENT = "rollbackMigrateCaseTTLDetails";

    private static final List<String> CASE_TYPES = List.of(
        ENGLANDWALES_CASE_TYPE_ID,
        SCOTLAND_CASE_TYPE_ID
    );

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    @Value("${cron.clearDraftTtlDryRun:true}")
    private boolean dryRun;

    @Value("${cron.clearDraftTtlMaxCasesPerSearch:500}")
    private int maxCasesPerSearch;

    @Value("${cron.clearDraftTtlMaxCasesToProcess:1000}")
    private int maxCasesToProcess;

    @Override
    public void run() {
        validateConfiguration();
        log.info("Clear draft TTL task started; dryRun={}, maxCasesToProcess={}", dryRun, maxCasesToProcess);

        String adminUserToken = adminUserService.getAdminUserToken();
        MigrationSummary summary = new MigrationSummary();

        for (String caseType : CASE_TYPES) {
            processCaseType(adminUserToken, caseType, summary);
        }

        log.info(
            "Clear draft TTL task completed; found={}, cleared={}, wouldClear={}, skipped={}, failed={}",
            summary.found,
            summary.cleared,
            summary.wouldClear,
            summary.skipped,
            summary.failed
        );
    }

    private void validateConfiguration() {
        if (maxCasesPerSearch <= 0) {
            throw new IllegalArgumentException("cron.clearDraftTtlMaxCasesPerSearch must be greater than zero");
        }
        if (maxCasesToProcess <= 0) {
            throw new IllegalArgumentException("cron.clearDraftTtlMaxCasesToProcess must be greater than zero");
        }
    }

    private void processCaseType(String adminUserToken, String caseType, MigrationSummary summary) {
        String searchAfter = null;
        int foundForCaseType = 0;
        while (foundForCaseType < maxCasesToProcess) {
            int pageSize = Math.min(maxCasesPerSearch, maxCasesToProcess - foundForCaseType);
            List<SubmitEvent> cases = search(adminUserToken, caseType, buildQuery(pageSize, searchAfter));
            if (cases.isEmpty()) {
                return;
            }

            for (SubmitEvent candidate : cases) {
                foundForCaseType++;
                summary.found++;
                processCandidate(adminUserToken, caseType, candidate, summary);
            }
            searchAfter = String.valueOf(cases.getLast().getCaseId());
        }
        log.warn(
            "Draft TTL processing limit reached; caseType={}, limit={}. Re-run to process remaining candidates",
            caseType,
            maxCasesToProcess
        );
    }

    private List<SubmitEvent> search(String adminUserToken, String caseType, String query) {
        try {
            return ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseType, query);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to search for draft TTL cases in " + caseType, exception);
        }
    }

    private void processCandidate(
        String adminUserToken,
        String caseType,
        SubmitEvent candidate,
        MigrationSummary summary
    ) {
        String caseId = String.valueOf(candidate.getCaseId());
        try {
            CCDRequest request = startRollbackEvent(adminUserToken, caseType, caseId);
            CaseDetails caseDetails = request.getCaseDetails();
            TTL ttl = caseDetails.getCaseData().getTtl();
            if (!DRAFT_STATE.equals(caseDetails.getState()) || !hasTtlValues(ttl)) {
                summary.skipped++;
                log.info(
                    "Skipping draft TTL clear after reading current case; caseType={}, caseId={}, state={}, ttl={}",
                    caseType,
                    caseId,
                    caseDetails.getState(),
                    ttl
                );
                return;
            }
            if (dryRun) {
                summary.wouldClear++;
                log.info("Dry run would clear draft TTL; caseType={}, caseId={}, ttl={}", caseType, caseId, ttl);
                return;
            }

            clearTtl(adminUserToken, caseType, caseId, request);
            summary.cleared++;
            log.info("Cleared draft TTL; caseType={}, caseId={}", caseType, caseId);
        } catch (IOException | RestClientException | IllegalStateException exception) {
            summary.failed++;
            log.error("Failed to clear draft TTL; caseType={}, caseId={}", caseType, caseId, exception);
        }
    }

    private CCDRequest startRollbackEvent(String adminUserToken, String caseType, String caseId) throws IOException {
        CCDRequest request = ccdClient.startEventForCase(
            adminUserToken,
            caseType,
            EMPLOYMENT,
            caseId,
            ROLLBACK_TTL_EVENT
        );
        if (request == null || request.getCaseDetails() == null || request.getCaseDetails().getCaseData() == null) {
            throw new IllegalStateException("CCD returned no case data for case " + caseId);
        }
        return request;
    }

    private void clearTtl(
        String adminUserToken,
        String caseType,
        String caseId,
        CCDRequest request
    ) throws IOException {
        CaseData caseData = request.getCaseDetails().getCaseData();
        caseData.setTtl(new TTL());
        caseData.setStateAPI(null);
        SubmitEvent result = ccdClient.submitEventForCase(
            adminUserToken,
            caseData,
            caseType,
            EMPLOYMENT,
            request,
            caseId
        );
        if (result == null || result.getCaseData() == null || hasTtlValues(result.getCaseData().getTtl())) {
            throw new IllegalStateException("CCD did not clear TTL for case " + caseId);
        }
    }

    private static boolean hasTtlValues(TTL ttl) {
        return ttl != null
            && (ttl.getSystemTTL() != null || ttl.getOverrideTTL() != null || ttl.getSuspended() != null);
    }

    static String buildQuery(int pageSize, String searchAfter) {
        BoolQueryBuilder hasTtl = new BoolQueryBuilder()
            .should(new ExistsQueryBuilder("data.TTL.SystemTTL"))
            .should(new ExistsQueryBuilder("data.TTL.OverrideTTL"))
            .should(new ExistsQueryBuilder("data.TTL.Suspended"))
            .minimumShouldMatch(1);

        SearchSourceBuilder query = new SearchSourceBuilder()
            .size(pageSize)
            .query(new BoolQueryBuilder()
                .must(new TermQueryBuilder("state.keyword", DRAFT_STATE))
                .must(hasTtl))
            .sort("reference.keyword", SortOrder.ASC);

        if (searchAfter != null) {
            query.searchAfter(new Object[] { searchAfter });
        }
        return query.toString();
    }

    private static class MigrationSummary {
        private int found;
        private int cleared;
        private int wouldClear;
        private int skipped;
        private int failed;
    }
}
