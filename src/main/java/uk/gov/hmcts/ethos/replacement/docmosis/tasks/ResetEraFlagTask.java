package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.Arrays;
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
    private static final String UNKNOWN_CASE_ID = "<unknown>";
    private static final String INITIAL_CASE_ID = "0";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final String caseTypeIdsString;
    private final int maxCases;
    private final boolean dryRun;

    private String lastCaseId;

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
        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(rawCaseTypeId -> {
            String caseTypeId = rawCaseTypeId.strip();
            lastCaseId = INITIAL_CASE_ID;
            List<SubmitEvent> cases;
            try {
                do {
                    log.info("{} - Reset ERA flag task - Searching for cases", caseTypeId);
                    String query = buildQuery();
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                    if (CollectionUtils.isEmpty(cases)) {
                        log.info("{} - Reset ERA flag task - No cases found", caseTypeId);
                        break;
                    }
                    setLastCaseId(cases);
                    log.info("{} - Reset ERA flag task - Retrieved {} cases", caseTypeId, cases.size());
                    processCases(cases, caseTypeId, adminUserToken);
                } while (CollectionUtils.isNotEmpty(cases));
            } catch (IOException ex) {
                log.error("IO error while searching cases for caseType {}: {}", caseTypeId, ex.getMessage(), ex);
            }
        });
        log.info("Reset ERA flag task finished");
    }

    /**
     * Processes a batch of retrieved cases, resetting the ERA flag to No and submitting each case.
     *
     * @param cases the list of submit events returned from Elasticsearch
     * @param caseTypeId the CCD case type ID
     * @param adminUserToken the admin user auth token
     */
    public void processCases(List<SubmitEvent> cases, String caseTypeId, String adminUserToken) {
        if (CollectionUtils.isEmpty(cases)) {
            return;
        }
        int updated = 0;
        int failed = 0;
        for (SubmitEvent submitEvent : cases) {
            String caseId = findCaseId(submitEvent);
            if (UNKNOWN_CASE_ID.equals(caseId)) {
                log.warn("{} - Reset ERA flag task - Skipping unknown case ID", caseTypeId);
                failed++;
                continue;
            }
            if (processCase(adminUserToken, caseTypeId, caseId)) {
                updated++;
            } else {
                failed++;
            }
        }
        log.info("{} - Reset ERA flag task batch summary: total={}, updated={}, failed={}",
                caseTypeId, cases.size(), updated, failed);
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

    public static String findCaseId(SubmitEvent submitEvent) {
        return ObjectUtils.isNotEmpty(submitEvent) && submitEvent.getCaseId() != 0
                ? String.valueOf(submitEvent.getCaseId())
                : UNKNOWN_CASE_ID;
    }

    public void setLastCaseId(List<SubmitEvent> cases) {
        if (cases == null || cases.isEmpty()) {
            lastCaseId = INITIAL_CASE_ID;
            return;
        }

        for (int index = cases.size() - 1; index >= 0; index--) {
            SubmitEvent submitEvent = cases.get(index);
            String caseId = findCaseId(submitEvent);
            if (!UNKNOWN_CASE_ID.equals(caseId)) {
                lastCaseId = caseId;
                return;
            }
            log.warn("Reset ERA flag task - Case ID is unknown for submit event {}", submitEvent.getCaseId());
        }
        lastCaseId = INITIAL_CASE_ID;
    }

    String buildQuery() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new BoolQueryBuilder()
                                .should(new TermQueryBuilder("data.additionalCaseInfo.era", YES))
                                .should(new TermQueryBuilder("data.additionalCaseInfo.era.keyword", YES))
                                .minimumShouldMatch(1))
                ).sort("reference.keyword", SortOrder.ASC);

        if (StringUtils.isNotBlank(lastCaseId) && !INITIAL_CASE_ID.equals(lastCaseId)) {
            searchSourceBuilder.searchAfter(new Object[] {lastCaseId});
        }
        return searchSourceBuilder.toString();
    }
}
