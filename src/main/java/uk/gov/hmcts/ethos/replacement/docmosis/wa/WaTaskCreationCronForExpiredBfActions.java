package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private List<Long> processedCaseIdsToSkip = new ArrayList<>();

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
        String today = UtilHelper.formatCurrentDate2(LocalDate.now());
        String query = buildQueryForExpiredBFActions(BFHelper.getEffectiveYesterday(), today);

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                while (CollectionUtils.isNotEmpty(cases)) {
                    cases.stream()
                            .filter(o ->
                                    !processedCaseIdsToSkip.contains(o.getCaseId()))
                            .forEach(o -> {
                                triggerTaskEventForCase(adminUserToken, o, caseTypeId);
                                log.info("Triggered WA task for case ID: {} in case type: {}",
                                        o.getCaseId(), caseTypeId);
                                processedCaseIdsToSkip.add(o.getCaseId());
                            });
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
        log.info("WaTaskCreationCronForExpiredBfActions processed {} case IDs", processedCaseIdsToSkip.size());
        log.info("WaTaskCreationCronForExpiredBfActions completed execution");
    }

    /**
     * builds query for Expired BFActions that are not reviewed yet
     * or its 'date cleared' field is empty.
     *
     * @param yesterday - bfaction due date, from which on cases meet the search criterion
     */
    String buildQueryForExpiredBFActions(String yesterday, String today) {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(QueryBuilders.existsQuery("data.bfActions"))
                        .mustNot(QueryBuilders.existsQuery("data.bfActions.value.cleared"))
                        .mustNot(QueryBuilders.existsQuery("data.bfActions.value.isWaTaskCreated"))
                        .must(QueryBuilders.rangeQuery("data.bfActions.value.bfDate").from(yesterday)
                                .to(today).includeLower(true).includeUpper(false))
                ).toString();
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
}
