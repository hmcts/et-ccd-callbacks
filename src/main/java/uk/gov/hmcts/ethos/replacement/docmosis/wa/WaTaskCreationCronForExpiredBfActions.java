package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@Component
@Slf4j
@RequiredArgsConstructor
public class WaTaskCreationCronForExpiredBfActions {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Scheduled(cron = "${cron.waTaskForExpiredBfActionTask}")
    public void createWaTasksForExpiredBFDates() {
        if (!featureToggleService.isWorkAllocationEnabled()) {
            return;
        }

        log.info("Checking for expired BFDates");
        String yesterday = UtilHelper.formatCurrentDate2(LocalDate.now().minusDays(1));
        String query = buildQueryForExpiredBFActions(yesterday);
        String adminUserToken = adminUserService.getAdminUserToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                while (CollectionUtils.isNotEmpty(cases)) {
                    cases.forEach(o -> triggerTaskEventForCase(adminUserToken, o, caseTypeId));
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    /**
     * builds query for Expired BFActions that are not reviewed yet
     * or its 'date cleared' field is empty.
     *
     * @param yesterday - bfaction due date, from which on cases meet the search criterion
     */
    private String buildQueryForExpiredBFActions(String yesterday) {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermQueryBuilder("data.respondentCollection.value.responseReceived", NO))
                        .must(QueryBuilders.rangeQuery("data.bfActions.value.bfDate").to(yesterday).includeUpper(true))
                        .must(new TermQueryBuilder("data.bfActions.value.allActions.keyword", "Claim served"))
                        .mustNot(QueryBuilders.existsQuery("data.bfActions.value.cleared"))
                ).toString();
    }

    private void triggerTaskEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId,
                    "EMPLOYMENT", String.valueOf(submitEvent.getCaseId()),
                    "WA_EXPIRED_BF_ACTION_TASK_CREATION");

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
