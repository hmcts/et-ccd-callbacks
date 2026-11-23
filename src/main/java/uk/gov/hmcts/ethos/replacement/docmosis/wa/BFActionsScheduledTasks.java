package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.impl.PostgresAdvisoryLock;
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
import javax.sql.DataSource;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Component
@Slf4j
public class BFActionsScheduledTasks {
    private static final String LOCK_NAMESPACE = "et-bf-actions";
    private static final String LOCK_NAME = "task";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;
    private final PostgresAdvisoryLock advisoryLock;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Value("${cron.bfActionCronCaseIdsToSkip}")
    private String bfActionCronCaseIdsToSkip;

    @Autowired
    public BFActionsScheduledTasks(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            FeatureToggleService featureToggleService,
            DataSource dataSource) {
        this(adminUserService, ccdClient, featureToggleService, new PostgresAdvisoryLock(dataSource));
    }

    BFActionsScheduledTasks(
            AdminUserService adminUserService,
            CcdClient ccdClient,
            FeatureToggleService featureToggleService,
            PostgresAdvisoryLock advisoryLock) {
        this.adminUserService = adminUserService;
        this.ccdClient = ccdClient;
        this.featureToggleService = featureToggleService;
        this.advisoryLock = advisoryLock;
    }

    @Scheduled(cron = "${cron.bfActionTask}")
    public void createTasksForBFDates() {
        if (!featureToggleService.isWorkAllocationEnabled()) {
            return;
        }

        boolean acquired = advisoryLock.runIfAcquired(
                LOCK_NAMESPACE,
                LOCK_NAME,
                this::processExpiredBfDates
        );

        if (!acquired) {
            log.info("BF actions scheduled task is already running; skipping this invocation");
        }
    }

    private void processExpiredBfDates() {
        log.info("Checking for expired BFDates");

        String yesterday = UtilHelper.formatCurrentDate2(LocalDate.now().minusDays(1));
        String query = buildQueryForExpiredBFActionsWithNoResponse(yesterday);

        String adminUserToken = adminUserService.getAdminUserToken();

        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                log.info("Found {} cases for caseTypeId {}", cases.size(), caseTypeId);
                if (CollectionUtils.isEmpty(cases)) {
                    return;
                }
                cases.stream()
                    .filter(submitEvent -> !bfActionCronCaseIdsToSkip.contains(String.valueOf(submitEvent.getCaseId())))
                    .forEach(submitEvent -> triggerTaskEventForCase(adminUserToken, submitEvent, caseTypeId));

            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });
    }

    private String buildQueryForExpiredBFActionsWithNoResponse(String yesterday) {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermQueryBuilder("data.respondentCollection.value.responseReceived", NO))
                        .must(QueryBuilders.rangeQuery("data.bfActions.value.bfDate").to(yesterday).includeUpper(true))
                        .mustNot(new MatchQueryBuilder("data.waRule21ReferralSent", YES))
                        .must(new TermQueryBuilder("data.bfActions.value.allActions.keyword", "Claim served")))
                .toString();
    }

    private void triggerTaskEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId,
                    "EMPLOYMENT", String.valueOf(submitEvent.getCaseId()), "WA_REVIEW_RULE21_REFERRAL");

            CaseData caseData = returnedRequest.getCaseDetails().getCaseData();
            if (caseData.getWaRule21ReferralSent() != null && caseData.getWaRule21ReferralSent().equals(YES)) {
                return;
            }
            caseData.setWaRule21ReferralSent(YES);

            String jurisdiction = returnedRequest.getCaseDetails().getJurisdiction();
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    jurisdiction, returnedRequest, String.valueOf(submitEvent.getCaseId())
            );

            log.info("Called WA_REVIEW_RULE21_REFERRAL for {}", submitEvent.getCaseId());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
