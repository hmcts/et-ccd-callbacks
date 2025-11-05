package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VETTED_STATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeFieldsTask {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;
    private final CaseConverter caseConverter;
    private final NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;
    private final List<String> validStates = List.of(SUBMITTED_STATE, VETTED_STATE, ACCEPTED_STATE, REJECTED_STATE);

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    @Scheduled(cron = "${cron.noticeOfChange}")
    public void generateNoticeOfChangeFields() {
        if (!featureToggleService.isNoticeOfChangeFieldsEnabled()) {
            return;
        }

        String query = buildQuery();
        String adminUserToken = adminUserService.getAdminUserToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                log.info("{} - Notice of change fields task - Retrieved {} cases", caseTypeId, cases.size());
                if (cases.isEmpty()) {
                    log.info("{} - NOC fields task - No cases to process", caseTypeId);
                    return;
                }
                updateCases(cases, caseTypeId, adminUserToken);
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage());
            }
        });
    }

    private void updateCases(List<SubmitEvent> cases, String caseTypeId, String adminUserToken)
            throws InterruptedException {
        final int poolSize = Math.min(15, Runtime.getRuntime().availableProcessors() * 2);
        final long awaitTimeoutSeconds = 120;

        try (ExecutorService executor = Executors.newFixedThreadPool(poolSize)) {
            for (SubmitEvent submitEvent : cases) {
                executor.execute(() -> {
                    try {
                        triggerEventForCase(adminUserToken, submitEvent, caseTypeId);
                    } catch (GenericServiceException ex) {
                        log.warn("{} - NOC fields task - Failed for case {}: {}",
                                caseTypeId, findCaseId(submitEvent), ex.getMessage(), ex);
                    }
                });
            }
            executor.shutdown();
            if (!executor.awaitTermination(awaitTimeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("{} - Executor did not terminate within {}s, forcing shutdown",
                        caseTypeId, awaitTimeoutSeconds);
                executor.shutdownNow();
            }
        }
    }

    public static String findCaseId(SubmitEvent se) {
        try {
            return ObjectUtils.isNotEmpty(se) && se.getCaseId() != 0 ? String.valueOf(se.getCaseId()) : "<unknown>";
        } catch (Exception ignored) {
            return "<unknown>";
        }
    }

    private void triggerEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId)
            throws GenericServiceException {
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                    String.valueOf(submitEvent.getCaseId()), "UPDATE_CASE_SUBMITTED");
            CaseDetails caseDetails = ccdRequest.getCaseDetails();

            Map<String, Object> caseDataAsMap = caseConverter.toMap(caseDetails.getCaseData());
            CaseData caseData = caseConverter.convert(caseDataAsMap, CaseData.class);
            caseData.setClaimantRepresentativeOrganisationPolicy(
                    OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                            ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    caseDetails.getJurisdiction(), ccdRequest, String.valueOf(submitEvent.getCaseId()));
            log.info("Added claimant solicitor organisation policy to case with id {}", submitEvent.getCaseId());
        } catch (Exception e) {
            throw new GenericServiceException(e.getMessage(), e, e.getMessage(), findCaseId(submitEvent),
                    "NoticeOfChangeFieldsTask", "triggerEventForCase");
        }
    }

    private String buildQuery() {
        return new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermsQueryBuilder("state.keyword", validStates))
                        .mustNot(new ExistsQueryBuilder("data.claimantRepresentativeOrganisationPolicy"))
                ).toString();
    }
}
