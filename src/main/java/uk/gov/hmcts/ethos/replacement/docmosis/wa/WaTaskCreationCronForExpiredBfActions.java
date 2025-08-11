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

                var validSubmitEvents = caseSubmitEvents.stream().filter(submitEvent -> {
                    if (submitEvent.getCaseData() != null && submitEvent.getCaseData().getBfActions() != null) {
                        return submitEvent.getCaseData().getBfActions().stream()
                                .anyMatch(bfAction -> bfAction.getValue() != null
                                    && bfAction.getValue().getBfDate() != null
                                    && !bfAction.getValue().getBfDate().isEmpty()
                                    && !Boolean.parseBoolean(bfAction.getValue().getCleared())
                                    && !Boolean.parseBoolean(bfAction.getValue().getIsWaTaskCreated())
                                    && (LocalDate.parse(bfAction.getValue().getBfDate())
                                            .isAfter(LocalDate.parse(BFHelper.getEffectiveYesterday())
                                                    .minusDays(1))
                                            && LocalDate.parse(bfAction.getValue().getBfDate()).isBefore(
                                                    LocalDate.now())));
                    }
                    return false;
                });

                validSubmitEvents.forEach(submitEvent ->
                    //invoke wa task creation event for each case with one or more expired bf action
                    triggerTaskEventForCase(adminUserToken, submitEvent, caseTypeId)
                );
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

        if (!CollectionUtils.isEmpty(initialSearchResultSubmitEvents)) {
            log.info("Found {} cases for case type: {}", initialSearchResultSubmitEvents.size(), caseTypeId);
            String searchAfterValue = String.valueOf(initialSearchResultSubmitEvents.getLast().getCaseId());
            log.info("First search after case id is {} cases for case type: {}", searchAfterValue, caseTypeId);

            boolean keepSearching;
            // Initialize the list with the initial search results so that to aggregate total search results returned
            List<SubmitEvent> caseSubmitEvents = new ArrayList<>(initialSearchResultSubmitEvents);
            int tempCaseCounter = 0;
            do {
                // A follow-up search query
                ElasticSearchQuery followUpESQuery = ElasticSearchQuery.builder()
                        .initialSearch(false)
                        .size(maxCases)
                        .searchAfterValue(searchAfterValue)
                        .build();

                String followUpQuery = followUpESQuery.getQuery(BFHelper.getEffectiveYesterday());
                List<SubmitEvent> subsequentSearchResultSubmitEvents = ccdClient.buildAndGetElasticSearchRequest(
                        adminUserToken, caseTypeId, followUpQuery);
                log.info("Follow up fetch {} cases for {} retrieved for Expired BF Task",
                        subsequentSearchResultSubmitEvents.size(), caseTypeId);
                subsequentSearchResultSubmitEvents.forEach(se -> log.info("Case ID: {}", se.getCaseId()));

                // Check if there are more cases to process
                keepSearching = !subsequentSearchResultSubmitEvents.isEmpty() && tempCaseCounter < 10;
                if (keepSearching) {
                    log.info("Adding {} cases to the list for case type: {}", subsequentSearchResultSubmitEvents.size(),
                            caseTypeId);
                    subsequentSearchResultSubmitEvents.forEach(followUpSubmitEvent ->
                            addCaseToSubmitEvents(caseSubmitEvents, followUpSubmitEvent));
                    // Update the searchAfterValue for the next iteration
                    searchAfterValue = String.valueOf(subsequentSearchResultSubmitEvents.getLast().getCaseId());
                    log.info("Next search after case id is {} cases for case type: {}", searchAfterValue, caseTypeId);
                    log.info("Current tempCaseCounter is {} for case type: {}", tempCaseCounter, caseTypeId);
                    tempCaseCounter++;
                }
                if (tempCaseCounter == 10) {
                    log.info("Reached the maximum temp number of cases limit to process in this run: {}",
                            tempCaseCounter);
                    break;
                }
            } while (keepSearching);
            log.info("The search for cases with expired bf action returned {} cases.", caseSubmitEvents.size());
            caseSubmitEvents.forEach(se -> log.info("Case type id of {} found with Case ID: {}",
                    se.getCaseId()));
            return caseSubmitEvents;

        } else {
            return new ArrayList<>();
        }
    }

    //Distinct/Unique list of submit events, i.e. no duplicate cases
    private void addCaseToSubmitEvents(List<SubmitEvent> caseSubmitEvents, SubmitEvent submitEvent) {
        if (submitEvent != null) {
            if (!caseSubmitEvents.contains(submitEvent)) {
                caseSubmitEvents.add(submitEvent);
            }
        } else {
            log.warn("Submit event is null and cannot be added to the list.");
        }
    }
}
