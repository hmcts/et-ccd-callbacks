package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Component
@Slf4j
@RequiredArgsConstructor
public class BFActionsScheduledTasks {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;

    @Value("${cron.caseTypeId}")
    private String caseTypeId;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Scheduled(cron = "${cron.bfActionTask}")
    public void createTasksForBFDates() {
        if (!featureToggleService.isWorkAllocationEnabled()) {
            return;
        }

        log.info("Checking for expired BFDates");

        String yesterday = UtilHelper.formatCurrentDate2(LocalDate.now().minusDays(1));
        String query = buildQueryForExpiredBFActionsWithNoResponse(yesterday);

        String adminUserToken = adminUserService.getAdminUserToken();

        String[] caseTypeIds = caseTypeId.split(",");

        Arrays.stream(caseTypeIds).forEach(caseType -> {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseType, query);

                cases.forEach(o -> triggerTaskEventForCase(adminUserToken, String.valueOf(o.getCaseId()), caseType));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    private String buildQueryForExpiredBFActionsWithNoResponse(String yesterday) {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermQueryBuilder("data.respondentCollection.value.responseReceived", NO))
                        .must(QueryBuilders.rangeQuery("data.bfActions.value.bfDate").to(yesterday).includeUpper(true))
                        .mustNot(new TermQueryBuilder("data.waRule21ReferralSent", YES))
                        .must(new TermQueryBuilder("data.bfActions.value.allActions.keyword", "Claim served")))
                .toString();
    }

    private void triggerTaskEventForCase(String adminUserToken, String caseId, String caseTypeId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId,
                    EMPLOYMENT, caseId, "WA_REVIEW_RULE21_REFERRAL");

            returnedRequest.getCaseDetails().getCaseData().setWaRule21ReferralSent(YES);

            String jurisdiction = returnedRequest.getCaseDetails().getJurisdiction();
            ccdClient.submitEventForCase(adminUserToken, returnedRequest.getCaseDetails().getCaseData(),
                    caseTypeId, jurisdiction, returnedRequest, caseId);

            log.info("Called WA_REVIEW_RULE21_REFERRAL for {}", caseId);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
