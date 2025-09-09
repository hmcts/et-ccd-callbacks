package uk.gov.hmcts.ethos.replacement.docmosis.wa.expiredbftask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
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
    private int maxCasesPerSearch;

    @Value("${cron.maxCasesToProcess}")
    private int maxCasesToProcess;

    private static final int MAX_VALID_CASE_TO_UPDATE = 10;

    /**
     * This cron job runs every day at 00:01 to create WA tasks for expired BF actions.
     * It checks for cases with BF actions that have a date of yesterday and are not cleared or
     * already have a WA task created.
     */
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
                // Fetch cases with one or more bf actions, and
                // with bf action date being yesterday, and
                // the case is in accepted, rejected, submitted or vetted state
                Set<SubmitEvent> caseSubmitEvents = findCasesByCaseType(adminUserToken, caseTypeId);
                if (CollectionUtils.isEmpty(caseSubmitEvents)) {
                    log.info("No cases found for case type: {}", caseTypeId);
                    return;
                } else {
                    log.info("Total cases fetched from findCasesByCaseType for case type {}: {}",
                            caseTypeId, caseSubmitEvents.size());
                }

                List<SubmitEvent> validSubmitEvents = caseSubmitEvents.stream().filter(submitEvent -> {
                    if (submitEvent.getCaseData() != null && submitEvent.getCaseData().getBfActions() != null) {
                        return hasAtLeastOneValidBfAction(submitEvent);
                    }
                    return false;
                }).limit(MAX_VALID_CASE_TO_UPDATE).toList();

                if (validSubmitEvents.isEmpty()) {
                    log.info("No valid cases found for case type: {}", caseTypeId);
                } else {
                    try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
                        log.info("Processing {} valid cases for case type: {}", validSubmitEvents.size(), caseTypeId);
                        validSubmitEvents.forEach(submitEvent -> executorService.execute(() -> {
                            triggerTaskEventForCase(adminUserToken, submitEvent, caseTypeId);
                        }));
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
        log.info("WaTaskCreationCronForExpiredBfActions completed execution");
    }

    /**
     * Checks if there is at least one valid BF action in the submit event.
     * A valid BF action is one that has a non-empty BF date, is expired, not yet cleared
     * and does not have a WA task created yet.
     *
     * @param submitEvent the submit event to check
     * @return true if there is at least one valid BF action, false otherwise
     */
    private static boolean hasAtLeastOneValidBfAction(SubmitEvent submitEvent) {
        return submitEvent.getCaseData().getBfActions().stream()
                .filter(bfAction -> bfAction != null && bfAction.getValue() != null)
                .map(BFActionTypeItem::getValue)
                .anyMatch(bfActionValue -> !isNullOrEmpty(bfActionValue.getBfDate())
                        && BFHelper.isBfExpired(bfActionValue, BFHelper.getEffectiveYesterday(
                                LocalDate.of(2025, 5, 1)))
                        && isNullOrEmpty(bfActionValue.getIsWaTaskCreated()));
    }

    private void triggerTaskEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId,
                    "EMPLOYMENT", String.valueOf(submitEvent.getCaseId()),
                    "WA_EXPIRED_BF_ACTION_TASK");

            CaseData caseData = returnedRequest.getCaseDetails().getCaseData();
            String jurisdiction = returnedRequest.getCaseDetails().getJurisdiction();
            BFHelper.updateWaTaskCreationTrackerOfBfActionItems(caseData);
            log.info("Invoked expired bf wa task creation event for case ID: {} for case type: {}",
                    submitEvent.getCaseId(), caseTypeId);
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    jurisdiction, returnedRequest, String.valueOf(submitEvent.getCaseId())
            );
        } catch (Exception e) {
            log.error("Error triggering task event for case {}: {}", submitEvent.getCaseId(), e.getMessage());
        }
    }

    private Set<SubmitEvent> findCasesByCaseType(String adminUserToken, String caseTypeId) throws IOException {
        log.info("Processing expired BF action search for case type {}.", caseTypeId);
        String query = ElasticSearchQuery.builder()
                .initialSearch(true)
                .size(maxCasesPerSearch)
                .build()
                .getQuery(BFHelper.getEffectiveYesterday(LocalDate.of(2025, 5, 1)));

        List<SubmitEvent> searchResults = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
        if (CollectionUtils.isEmpty(searchResults)) {
            return new HashSet<>();
        }

        log.info("Found {} cases for case type: {} for initial search", searchResults.size(), caseTypeId);
        log.info("Case ids list: {}", searchResults.stream()
                        .map(i -> String.valueOf(i.getCaseId()))
                        .collect(Collectors.joining(", "))
        );
        Set<SubmitEvent> caseSubmitEvents = searchResults.stream().filter(Objects::nonNull)
                .collect(Collectors.toSet());
        String searchAfterValue = String.valueOf(searchResults.getLast().getCaseId());
        log.info("The first searchAfterValue is {}", searchAfterValue);
        int round = 0;
        while (caseSubmitEvents.size() < maxCasesToProcess) {
            query = ElasticSearchQuery.builder()
                    .initialSearch(false)
                    .size(maxCasesPerSearch)
                    .searchAfterValue(searchAfterValue)
                    .build()
                    .getQuery(BFHelper.getEffectiveYesterday(LocalDate.of(2025, 5, 1)));

            List<SubmitEvent> nextResults = ccdClient.buildAndGetElasticSearchRequest(
                    adminUserToken, caseTypeId, query);
            if (CollectionUtils.isEmpty(nextResults)) {
                break;
            }

            log.info("Fetched {} additional cases for case type: {}", nextResults.size(), caseTypeId);
            nextResults.stream().filter(Objects::nonNull).forEach(caseSubmitEvents::add);
            log.info("Updated caseSubmitEvents set now has {} cases of {} type:", nextResults.size(), caseTypeId);

            searchAfterValue = String.valueOf(nextResults.getLast().getCaseId());
            log.info("The {} round/loop searchAfterValue is {}", String.valueOf(++round), searchAfterValue);
        }

        log.info("Total cases found: {}", caseSubmitEvents.size());
        return caseSubmitEvents;
    }
}
