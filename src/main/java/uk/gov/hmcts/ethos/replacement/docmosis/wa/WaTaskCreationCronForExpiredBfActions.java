package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class WaTaskCreationCronForExpiredBfActions implements Runnable {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Override
    public void run() {
        log.info("WaTaskCreationCronForExpiredBfActions is running");
        if (!featureToggleService.isWorkAllocationEnabled()
                || !featureToggleService.isWaTaskForExpiredBfActionsEnabled()) {
            log.info("WaTaskCreationCronForExpiredBfActions is disabled by feature toggle");
            return;
        }

        log.info("In WaTaskCreation ... cron job - Checking for expired BFDates");
        String[] caseTypeIds = caseTypeIdsString.split(",");
        String adminUserToken = adminUserService.getAdminUserToken();

        stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                List<SubmitEvent> caseSubmitEvents = findCasesByCaseType(adminUserToken, caseTypeId);
                if (CollectionUtils.isEmpty(caseSubmitEvents)) {
                    log.info("No cases found for case type: {}", caseTypeId);
                    return;
                }

                caseSubmitEvents.stream().filter(submitEvent -> {
                    if (submitEvent.getCaseData() != null && submitEvent.getCaseData().getBfActions() != null) {
                        return submitEvent.getCaseData().getBfActions().stream()
                                .anyMatch(bfAction -> bfAction.getValue() != null
                                    && bfAction.getValue().getBfDate() != null
                                    && !bfAction.getValue().getBfDate().isEmpty()
                                    && !Boolean.parseBoolean(bfAction.getValue().getCleared())
                                    && !Boolean.parseBoolean(bfAction.getValue().getIsWaTaskCreated())
                                    && LocalDate.parse(bfAction.getValue().getBfDate())
                                            .isBefore(LocalDate.parse(BFHelper.getEffectiveYesterday())));
                    }
                    return false;
                }).forEach(submitEvent -> {
                    //invoke wa task creation event for each valid bf action on each case
                    triggerTaskEventForCase(adminUserToken, submitEvent, caseTypeId);

                });
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
        log.info("WaTaskCreationCronForExpiredBfActions completed execution");
    }

    private void triggerTaskEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId,
                    "EMPLOYMENT", String.valueOf(submitEvent.getCaseId()),
                    "WA_EXPIRED_BF_ACTION_TASK");

            CaseData caseData = returnedRequest.getCaseDetails().getCaseData();
            String jurisdiction = returnedRequest.getCaseDetails().getJurisdiction();
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    jurisdiction, returnedRequest, String.valueOf(submitEvent.getCaseId())
            );
        } catch (Exception e) {
            log.error("Error triggering task event for case {}: {}", submitEvent.getCaseId(), e.getMessage());
        }
    }

    private List<SubmitEvent> findCasesByCaseType(String adminUserToken, String caseTypeId) throws IOException {
        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
                .initialSearch(true)
                .size(maxCases)
                .build();

        log.info("Processing the expired bf action search for case type {}.", caseTypeId);
        String query = elasticSearchQuery.getQuery(BFHelper.getEffectiveYesterday());
        List<SubmitEvent> initialSearchResultSubmitEvents = ccdClient.buildAndGetElasticSearchRequest(
                adminUserToken, caseTypeId, query);
        if (initialSearchResultSubmitEvents != null && !initialSearchResultSubmitEvents.isEmpty()) {
            log.info("Found {} cases for case type: {}", initialSearchResultSubmitEvents.size(), caseTypeId);
            String searchAfterValue = String.valueOf(initialSearchResultSubmitEvents.getLast().getCaseId());
            boolean keepSearching;
            // Initialize the list with the initial search results so that to aggregate total search results returned
            List<SubmitEvent> caseSubmitEvents = new ArrayList<>(initialSearchResultSubmitEvents);
            int tempCaseCounter = 0;
            do {
                elasticSearchQuery = ElasticSearchQuery.builder()
                        .initialSearch(false)
                        .size(maxCases)
                        .searchAfterValue(searchAfterValue)
                        .build();
                query = elasticSearchQuery.getQuery(BFHelper.getEffectiveYesterday());
                List<SubmitEvent> subsequentSearchResultSubmitEvents = ccdClient.buildAndGetElasticSearchRequest(
                        adminUserToken, caseTypeId, query);
                log.info("Follow up fetch {} cases for {} retrieved for Expired BF Task",
                        subsequentSearchResultSubmitEvents.size(), caseTypeId);
                // Check if there are more cases to process
                keepSearching = !subsequentSearchResultSubmitEvents.isEmpty();
                if (keepSearching && tempCaseCounter < 10) {
                    caseSubmitEvents.addAll(subsequentSearchResultSubmitEvents);
                    searchAfterValue = String.valueOf(subsequentSearchResultSubmitEvents.getLast().getCaseId());
                    tempCaseCounter++;
                }
                if (tempCaseCounter == 10) {
                    log.info("Reached the maximum temp number of cases limit to process in this run: {}",
                            tempCaseCounter);
                    break;
                }
            } while (keepSearching);
            log.info("The search for cases with expired bf action returned {} cases.", caseSubmitEvents.size());
            return caseSubmitEvents;

        } else {
            return new ArrayList<>();
        }
    }
}
