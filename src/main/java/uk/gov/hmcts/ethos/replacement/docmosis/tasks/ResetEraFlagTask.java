package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

/**
 * Standalone runnable task to reset the ERA flag in additionalCaseInfo to No,
 * recompute the flag image filename, and submit the case using fixCaseAPI.
 *
 * <p>Uses {@link CoreCaseDataApi#getCase} to automatically resolve each case's
 * caseTypeId without requiring it to be configured manually.</p>
 *
 * <p>Set {@code TASK_NAME=ResetEraFlagTask} and pass case IDs via
 * {@code CRON_RESET_ERA_FLAG_CASE_IDS=1620473176786991,1620473176786992}.</p>
 */
@Component
@Slf4j
public class ResetEraFlagTask implements Runnable {

    private static final String FIX_CASE_API = "fixCaseAPI";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final String caseIdsToUpdate;
    private final boolean dryRun;

    @Autowired
    public ResetEraFlagTask(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            CoreCaseDataApi coreCaseDataApi,
            AuthTokenGenerator authTokenGenerator,
            @Value("${cron.resetEraFlagCaseIds:${cron.reconfigurationCaseIds:}}") String caseIdsToUpdate,
            @Value("${cron.resetEraFlagDryRun:false}") boolean dryRun) {
        this.adminUserService = adminUserService;
        this.ccdClient = ccdClient;
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.caseIdsToUpdate = caseIdsToUpdate;
        this.dryRun = dryRun;
    }

    public ResetEraFlagTask(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            CoreCaseDataApi coreCaseDataApi,
            AuthTokenGenerator authTokenGenerator) {
        this(adminUserService, ccdClient, coreCaseDataApi, authTokenGenerator, "", false);
    }

    @Override
    public void run() {
        log.info("Reset ERA flag task started (dryRun={})", dryRun);
        List<String> caseIds = parseCaseIds(caseIdsToUpdate);
        if (caseIds.isEmpty()) {
            log.info("No case IDs configured for reset ERA flag task; exiting job");
            return;
        }

        processCases(caseIds);
        log.info("Reset ERA flag task finished");
    }

    /**
     * Processes a list of case IDs, resolving each case's type via coreCaseDataApi,
     * resetting the ERA flag to No, and submitting each case via fixCaseAPI.
     *
     * @param caseIds the list of CCD case IDs to process
     */
    public void processCases(List<String> caseIds) {
        String adminUserToken = adminUserService.getAdminUserToken();
        String s2sToken = authTokenGenerator.generate();
        List<String> updatedCases = new ArrayList<>();
        List<String> failedCases = new ArrayList<>();

        for (String caseId : caseIds) {
            boolean updated = processCase(adminUserToken, s2sToken, caseId);
            if (updated) {
                updatedCases.add(caseId);
            } else {
                failedCases.add(caseId);
            }
        }

        log.info("Reset ERA flag task summary: total={}, updated={}, failed={}",
                caseIds.size(), updatedCases.size(), failedCases.size());
        if (!failedCases.isEmpty()) {
            log.warn("Failed to reset ERA flag for case IDs: {}", failedCases);
        }
    }

    private boolean processCase(String adminUserToken, String s2sToken, String caseId) {
        if (StringUtils.isBlank(caseId)) {
            log.warn("Skipping blank case ID");
            return false;
        }

        try {
            var fetchedCase = coreCaseDataApi.getCase(adminUserToken, s2sToken, caseId);
            if (fetchedCase == null || StringUtils.isBlank(fetchedCase.getCaseTypeId())) {
                log.error("Could not resolve case type for case {}", caseId);
                return false;
            }

            String caseTypeId = fetchedCase.getCaseTypeId();
            log.info("Case {} resolved to case type {}", caseId, caseTypeId);

            return updateCaseForCaseType(adminUserToken, caseTypeId, caseId);
        } catch (Exception ex) {
            log.error("Failed to reset ERA flag for case {}: {}", caseId, ex.getMessage(), ex);
            return false;
        }
    }

    private boolean updateCaseForCaseType(String adminUserToken, String caseTypeId, String caseId)
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

    /**
     * Parses a comma-separated string of case IDs into a list of trimmed, non-blank strings.
     *
     * @param caseIds the comma-separated case IDs string
     * @return list of non-blank case IDs
     */
    public static List<String> parseCaseIds(String caseIds) {
        if (StringUtils.isBlank(caseIds)) {
            return List.of();
        }
        return Arrays.stream(caseIds.split(","))
                .map(String::strip)
                .filter(StringUtils::isNotBlank)
                .toList();
    }
}
