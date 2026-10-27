package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Standalone runnable task to search for ERA cases using Elasticsearch, reset the ERA flag
 * in additionalCaseInfo to No, recompute the flag image filename, and submit the case using fixCaseAPI.
 *
 * <p>Set {@code TASK_NAME=ResetEraFlagTask} to execute this task.</p>
 */
@Component
@Slf4j
public class ResetEraFlagTask implements Runnable {

    private static final String FIX_CASE_API = "fixCaseAPI";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final String caseTypeIdsString;
    private final int maxCases;
    private final boolean dryRun;

    @Autowired
    public ResetEraFlagTask(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            @Value("${cron.caseTypeId:ET_EnglandWales,ET_Scotland}") String caseTypeIdsString,
            @Value("${cron.maxCasesPerSearch:500}") int maxCases,
            @Value("${cron.resetEraFlagDryRun:false}") boolean dryRun) {
        this.adminUserService = adminUserService;
        this.ccdClient = ccdClient;
        this.caseTypeIdsString = caseTypeIdsString;
        this.maxCases = maxCases;
        this.dryRun = dryRun;
    }

    public ResetEraFlagTask(
            AdminUserService adminUserService,
            CcdClient ccdClient) {
        this(adminUserService, ccdClient, "ET_EnglandWales,ET_Scotland", 500, false);
    }

    public ResetEraFlagTask(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            boolean dryRun) {
        this(adminUserService, ccdClient, "ET_EnglandWales,ET_Scotland", 500, dryRun);
    }

    @Override
    public void run() {
        log.info("Reset ERA flag task started (dryRun={})", dryRun);
        String adminUserToken = adminUserService.getAdminUserToken();

        for (String rawCaseTypeId : caseTypeIdsString.split(",")) {
            String caseTypeId = rawCaseTypeId.trim();
            if (!caseTypeId.isEmpty()) {
                resetEraFlagsForCaseType(adminUserToken, caseTypeId);
            }
        }

        log.info("Reset ERA flag task finished");
    }

    public void resetEraFlagsForCaseType(String adminUserToken, String caseTypeId) {
        String searchAfterReference = null;
        while (true) {
            try {
                String query = buildSearchQuery(searchAfterReference);
                log.info("Searching for ERA cases in case type {}", caseTypeId);
                List<SubmitEvent> foundCases =
                        ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);

                if (foundCases == null || foundCases.isEmpty()) {
                    log.info("No matching ERA cases found for case type {}", caseTypeId);
                    break;
                }

                log.info("Retrieved {} ERA cases to reset for case type {}", foundCases.size(), caseTypeId);
                processCases(foundCases, caseTypeId, adminUserToken);

                searchAfterReference = extractLastCaseReference(foundCases);
                if (searchAfterReference == null) {
                    break;
                }
            } catch (IOException ex) {
                log.error("Failed querying CCD for case type {}: {}", caseTypeId, ex.getMessage(), ex);
                break;
            }
        }
    }

    public void processCases(List<SubmitEvent> cases, String caseTypeId, String adminUserToken) {
        if (cases == null || cases.isEmpty()) {
            return;
        }

        int successCount = 0;
        int failureCount = 0;
        for (SubmitEvent submitEvent : cases) {
            if (submitEvent == null || submitEvent.getCaseId() == 0) {
                log.warn("Skipping submit event with invalid or missing case ID in case type {}", caseTypeId);
                failureCount++;
                continue;
            }

            String caseId = String.valueOf(submitEvent.getCaseId());
            if (processCase(adminUserToken, caseTypeId, caseId)) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        log.info("Reset ERA flag task batch for {}: total={}, updated={}, failed={}",
                caseTypeId, cases.size(), successCount, failureCount);
    }

    public boolean processCase(String adminUserToken, String caseTypeId, String caseId) {
        if (StringUtils.isBlank(caseId)) {
            log.warn("Skipping blank case ID");
            return false;
        }

        try {
            return updateCaseForCaseType(adminUserToken, caseTypeId, caseId);
        } catch (Exception ex) {
            log.error("Failed to reset ERA flag for case {}: {}", caseId, ex.getMessage(), ex);
            return false;
        }
    }

    public boolean updateCaseForCaseType(String adminUserToken, String caseTypeId, String caseId)
            throws IOException {
        log.info("Attempting to start event {} for case {} with case type {}",
                FIX_CASE_API, caseId, caseTypeId);
        CCDRequest ccdRequest = ccdClient.startEventForCase(
                adminUserToken, caseTypeId, EMPLOYMENT, caseId, FIX_CASE_API);

        if (ccdRequest == null || ccdRequest.getCaseDetails() == null) {
            log.warn("Returned null CCD request or case details for case {} and case type {}",
                    caseId, caseTypeId);
            return false;
        }

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        if (caseData == null) {
            log.warn("Case data is null for case {}", caseId);
            return false;
        }

        AdditionalCaseInfoType additionalCaseInfo = Optional.ofNullable(caseData.getAdditionalCaseInfoType())
                .orElseGet(AdditionalCaseInfoType::new);
        additionalCaseInfo.setEra(NO);
        caseData.setAdditionalCaseInfoType(additionalCaseInfo);

        FlagsImageHelper.buildFlagsImageFileName(caseTypeId, caseData);
        caseData.setStateAPI(null);

        if (dryRun) {
            log.info("Dry run enabled: would submit case {} with ERA flag set to No", caseId);
            return true;
        }

        String jurisdiction = StringUtils.defaultIfEmpty(caseDetails.getJurisdiction(), EMPLOYMENT);
        ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId, jurisdiction, ccdRequest, caseId);
        log.info("Successfully updated and submitted case {} using {} in case type {}",
                caseId, FIX_CASE_API, caseTypeId);
        return true;
    }

    public static String extractLastCaseReference(List<SubmitEvent> cases) {
        if (cases == null || cases.isEmpty()) {
            return null;
        }

        for (int idx = cases.size() - 1; idx >= 0; idx--) {
            SubmitEvent submitEvent = cases.get(idx);
            if (submitEvent != null && submitEvent.getCaseId() != 0) {
                return String.valueOf(submitEvent.getCaseId());
            }
        }
        return null;
    }

    String buildSearchQuery(String afterReference) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(maxCases)
                .sort("reference.keyword", SortOrder.ASC)
                .query(new BoolQueryBuilder()
                        .must(new BoolQueryBuilder()
                                .should(new TermQueryBuilder("data.additionalCaseInfo.era", YES))
                                .should(new TermQueryBuilder("data.additionalCaseInfo.era.keyword", YES))
                                .minimumShouldMatch(1)));

        if (StringUtils.isNotBlank(afterReference)) {
            searchSourceBuilder.searchAfter(new Object[] {afterReference});
        }
        return searchSourceBuilder.toString();
    }
}
