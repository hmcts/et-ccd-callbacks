package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class AcasCertificateRetrievalTask {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Scheduled(cron = "${cron.acasCertTask}")
    public void retrieveAcasCertificate() {
        if (!featureToggleService.isEt1CronJobEnabled()) {
            return;
        }

        String query = buildQuery();
        String adminUserToken = adminUserService.getAdminUserToken();

        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                log.error("ACAS Task - Retrieved {} cases", cases.size());
                while (CollectionUtils.isNotEmpty(cases)) {
                    cases.forEach(c -> triggerEventForCase(adminUserToken, c, caseTypeId));
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                    log.error("ACAS Task - Retrieved {} cases", cases.size());
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    private void triggerEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId) {
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                    String.valueOf(submitEvent.getCaseId()), "generateEt1Documents");
            ccdClient.submitEventForCase(adminUserToken, ccdRequest.getCaseDetails().getCaseData(), caseTypeId,
                    ccdRequest.getCaseDetails().getJurisdiction(), ccdRequest, String.valueOf(submitEvent.getCaseId()));
            log.error("Added ACAS Certificate for case {}", submitEvent.getCaseId());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String buildQuery() {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermQueryBuilder("data.requiresSubmissionDocuments", YES))
                ).toString();
    }

}
