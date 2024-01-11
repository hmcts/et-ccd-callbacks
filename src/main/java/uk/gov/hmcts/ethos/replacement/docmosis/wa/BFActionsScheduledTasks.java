package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Component
@Slf4j
@RequiredArgsConstructor
public class BFActionsScheduledTasks {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    @Scheduled(cron = "${cron.bfActionTask}")
    public void createTasksForBFDates() {
        log.info("Checking for expired BFDates");

        String now = UtilHelper.formatCurrentDate2(LocalDate.now().plusDays(-1));
        String query = buildQueryForExpiredBFActionsWithNoResponse(now);

        String adminUserToken = adminUserService.getAdminUserToken();

        try {
            List<SubmitEvent> englandCases =
                    ccdClient.buildAndGetElasticSearchRequest(adminUserToken, ENGLANDWALES_CASE_TYPE_ID, query);
            List<SubmitEvent> scotlandCases =
                    ccdClient.buildAndGetElasticSearchRequest(adminUserToken, SCOTLAND_CASE_TYPE_ID, query);

            englandCases.forEach(o -> triggerTaskEventForCase(adminUserToken, o, ENGLANDWALES_CASE_TYPE_ID));
            scotlandCases.forEach(o -> triggerTaskEventForCase(adminUserToken, o, SCOTLAND_CASE_TYPE_ID));

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String buildQueryForExpiredBFActionsWithNoResponse(String now) {
        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(new BoolQueryBuilder()
                        .must(new TermQueryBuilder("data.respondentCollection.value.responseReceived", NO))
                        .must(QueryBuilders.rangeQuery("data.bfActions.value.bfDate").to(now).includeUpper(true))
                        .mustNot(new TermQueryBuilder("data.waRule21ReferralSent", YES))
                        .must(new TermQueryBuilder("data.bfActions.value.allActions.keyword", "Claim served")))
                .toString();
    }

    private void triggerTaskEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId,
                    "EMPLOYMENT", String.valueOf(submitEvent.getCaseId()), "WA_REVIEW_RULE21_REFERRAL");

            CaseData caseData = submitEvent.getCaseData();
            caseData.setWaRule21ReferralSent(YES);

            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    "EMPLOYMENT", returnedRequest, String.valueOf(submitEvent.getCaseId()));
            log.info("Called WA_REVIEW_RULE21_REFERRAL for " + submitEvent.getCaseId());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
