package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VETTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class EcmMigrationNoticeOfChangeTask implements Runnable {

    private static final String UNKNOWN_CASE_ID = "<unknown>";
    private static final String INITIAL_CASE_ID = "0";
    private static final String FIX_CASE_API = "fixCaseAPI";
    private static final List<String> VALID_STATES = List.of(
            SUBMITTED_STATE, VETTED_STATE, ACCEPTED_STATE, REJECTED_STATE, CLOSED_STATE);

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @Value("${cron.caseTypeId:ET_EnglandWales,ET_Scotland}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch:500}")
    private int maxCases;

    @Value("${cron.ecmMigrationNocCaseIds:}")
    private String ecmMigrationNocCaseIds;

    @Value("${cron.ecmMigrationNocDryRun:false}")
    private boolean dryRun;

    @Value("${cron.ecmMigrationNocConcurrency:20}")
    private int concurrency;

    private String lastCaseId;

    @Override
    public void run() {
        log.info("****** ECM migration notice of change task - Started (dryRun={}) ******", dryRun);
        String adminUserToken = adminUserService.getAdminUserToken();

        List<String> configuredCaseIds = parseCaseIds(ecmMigrationNocCaseIds);
        if (!configuredCaseIds.isEmpty()) {
            processConfiguredCases(adminUserToken, configuredCaseIds);
            log.info("****** ECM migration notice of change task - Completed for configured cases ******");
            return;
        }

        String[] caseTypeIds = caseTypeIdsString.split(",");
        Arrays.stream(caseTypeIds).forEach(rawCaseTypeId -> {
            String caseTypeId = rawCaseTypeId.strip();
            lastCaseId = INITIAL_CASE_ID;
            List<SubmitEvent> cases;
            try {
                do {
                    log.info("{} - ECM migration NOC task - Searching for cases", caseTypeId);
                    String query = buildQuery();
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                    setLastCaseId(cases);
                    log.info("{} - ECM migration NOC task - Retrieved {} cases", caseTypeId, cases.size());
                    updateCases(cases, caseTypeId, adminUserToken);
                } while (CollectionUtils.isNotEmpty(cases));
            } catch (IOException ex) {
                log.error("IO error while searching cases for caseType {}: {}", caseTypeId, ex.getMessage(), ex);
            } catch (InterruptedException ie) {
                log.error("Task interrupted for caseType {}: {}", caseTypeId, ie.getMessage(), ie);
                Thread.currentThread().interrupt();
            }
        });
        log.info("****** ECM migration notice of change task - Completed ******");
    }

    private void processConfiguredCases(String adminUserToken, List<String> configuredCaseIds) {
        log.info("Processing {} configured case IDs for ECM migration NOC task with concurrency {}",
                configuredCaseIds.size(), concurrency);
        String[] caseTypeIds = caseTypeIdsString.split(",");
        final long awaitTimeoutSeconds = 300;

        try (ExecutorService executor = Executors.newFixedThreadPool(concurrency, Thread.ofVirtual().factory())) {
            for (String caseId : configuredCaseIds) {
                executor.execute(() -> {
                    for (String rawCaseTypeId : caseTypeIds) {
                        String caseTypeId = rawCaseTypeId.strip();
                        try {
                            if (processCase(adminUserToken, caseTypeId, caseId)) {
                                break;
                            }
                        } catch (GenericServiceException ex) {
                            log.warn("{} - ECM migration NOC task failed for case {}: {}",
                                    caseTypeId, caseId, ex.getMessage(), ex);
                        }
                    }
                });
            }
            executor.shutdown();
            if (!executor.awaitTermination(awaitTimeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate within {}s, forcing shutdown", awaitTimeoutSeconds);
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            log.error("Task interrupted while processing configured cases: {}", ie.getMessage(), ie);
            Thread.currentThread().interrupt();
        }
    }

    private void updateCases(List<SubmitEvent> cases, String caseTypeId, String adminUserToken)
            throws InterruptedException {
        final long awaitTimeoutSeconds = 300;

        try (ExecutorService executor = Executors.newFixedThreadPool(concurrency, Thread.ofVirtual().factory())) {
            for (SubmitEvent submitEvent : cases) {
                executor.execute(() -> {
                    String caseId = findCaseId(submitEvent);
                    try {
                        processCase(adminUserToken, caseTypeId, caseId);
                    } catch (GenericServiceException ex) {
                        log.warn("{} - ECM migration NOC task failed for case {}: {}",
                                caseTypeId, caseId, ex.getMessage(), ex);
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

    public boolean processCase(String adminUserToken, String caseTypeId, String caseId)
            throws GenericServiceException {
        try {
            if (StringUtils.isBlank(adminUserToken)
                    || StringUtils.isBlank(caseTypeId)
                    || StringUtils.isBlank(caseId)) {
                return false;
            }

            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                    caseId, FIX_CASE_API);
            if (!isCcdRequestValid(ccdRequest)) {
                log.warn("{} - Case {} returned invalid CCD request", caseTypeId, caseId);
                return false;
            }

            CaseDetails caseDetails = ccdRequest.getCaseDetails();
            CaseData caseData = caseDetails.getCaseData();

            if (!isMigratedFromEcm(caseData)) {
                log.info("{} - Case {} is not an ECM migrated case, skipping", caseTypeId, caseId);
                return false;
            }

            if (!needsOrgPolicyOrNoc(caseData)) {
                log.info("{} - Case {} already has org policy and NOC answers, skipping", caseTypeId, caseId);
                return false;
            }

            if (CollectionUtils.isNotEmpty(caseData.getRepCollection())) {
                NoticeOfChangeFieldsTask.updateRepCollection(caseData);
            }

            CaseData updatedCaseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);
            preserveExistingValues(caseData, updatedCaseData);
            updatedCaseData.setStateAPI(null);

            if (dryRun) {
                log.info("{} - Dry run enabled: would update org policy and NOC answers for case {}",
                        caseTypeId, caseId);
                return true;
            }

            ccdClient.submitEventForCase(adminUserToken, updatedCaseData, caseTypeId,
                    caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Updated org policy and NOC answers for case {}", caseTypeId, caseId);
            return true;
        } catch (Exception ex) {
            throw new GenericServiceException(ex.getMessage(), ex, ex.getMessage(), caseId,
                    "EcmMigrationNoticeOfChangeTask", "processCase");
        }
    }

    public static boolean isMigratedFromEcm(CaseData caseData) {
        return caseData != null
                && (YES.equalsIgnoreCase(caseData.getMigratedFromEcm())
                || StringUtils.isNotBlank(caseData.getEcmCaseLink()));
    }

    public static boolean needsOrgPolicyOrNoc(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        if (caseData.getRespondentOrganisationPolicy0() == null) {
            return true;
        }
        if (caseData.getClaimantRepresentativeOrganisationPolicy() == null) {
            return true;
        }
        return isNoticeOfChangeAnswersMissing(caseData);
    }

    public static boolean isNoticeOfChangeAnswersMissing(CaseData caseData) {
        if (caseData == null || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return false;
        }
        int numRespondents = Math.min(caseData.getRespondentCollection().size(), 10);
        for (int idx = 0; idx < numRespondents; idx++) {
            NoticeOfChangeAnswers answer = getNoticeOfChangeAnswersByIndex(caseData, idx);
            if (answer == null || StringUtils.isBlank(answer.getRespondentName())) {
                return true;
            }
        }
        return false;
    }

    public static NoticeOfChangeAnswers getNoticeOfChangeAnswersByIndex(CaseData caseData, int index) {
        return switch (index) {
            case 0 -> caseData.getNoticeOfChangeAnswers0();
            case 1 -> caseData.getNoticeOfChangeAnswers1();
            case 2 -> caseData.getNoticeOfChangeAnswers2();
            case 3 -> caseData.getNoticeOfChangeAnswers3();
            case 4 -> caseData.getNoticeOfChangeAnswers4();
            case 5 -> caseData.getNoticeOfChangeAnswers5();
            case 6 -> caseData.getNoticeOfChangeAnswers6();
            case 7 -> caseData.getNoticeOfChangeAnswers7();
            case 8 -> caseData.getNoticeOfChangeAnswers8();
            case 9 -> caseData.getNoticeOfChangeAnswers9();
            default -> null;
        };
    }

    public static void setNoticeOfChangeAnswersByIndex(CaseData caseData, int index,
                                                        NoticeOfChangeAnswers answers) {
        if (caseData == null) {
            return;
        }
        switch (index) {
            case 0 -> caseData.setNoticeOfChangeAnswers0(answers);
            case 1 -> caseData.setNoticeOfChangeAnswers1(answers);
            case 2 -> caseData.setNoticeOfChangeAnswers2(answers);
            case 3 -> caseData.setNoticeOfChangeAnswers3(answers);
            case 4 -> caseData.setNoticeOfChangeAnswers4(answers);
            case 5 -> caseData.setNoticeOfChangeAnswers5(answers);
            case 6 -> caseData.setNoticeOfChangeAnswers6(answers);
            case 7 -> caseData.setNoticeOfChangeAnswers7(answers);
            case 8 -> caseData.setNoticeOfChangeAnswers8(answers);
            case 9 -> caseData.setNoticeOfChangeAnswers9(answers);
            default -> { }
        }
    }

    public static OrganisationPolicy getRespondentOrganisationPolicyByIndex(CaseData caseData, int index) {
        if (caseData == null) {
            return null;
        }
        return switch (index) {
            case 0 -> caseData.getRespondentOrganisationPolicy0();
            case 1 -> caseData.getRespondentOrganisationPolicy1();
            case 2 -> caseData.getRespondentOrganisationPolicy2();
            case 3 -> caseData.getRespondentOrganisationPolicy3();
            case 4 -> caseData.getRespondentOrganisationPolicy4();
            case 5 -> caseData.getRespondentOrganisationPolicy5();
            case 6 -> caseData.getRespondentOrganisationPolicy6();
            case 7 -> caseData.getRespondentOrganisationPolicy7();
            case 8 -> caseData.getRespondentOrganisationPolicy8();
            case 9 -> caseData.getRespondentOrganisationPolicy9();
            default -> null;
        };
    }

    public static void setRespondentOrganisationPolicyByIndex(CaseData caseData, int index,
                                                               OrganisationPolicy policy) {
        if (caseData == null) {
            return;
        }
        switch (index) {
            case 0 -> caseData.setRespondentOrganisationPolicy0(policy);
            case 1 -> caseData.setRespondentOrganisationPolicy1(policy);
            case 2 -> caseData.setRespondentOrganisationPolicy2(policy);
            case 3 -> caseData.setRespondentOrganisationPolicy3(policy);
            case 4 -> caseData.setRespondentOrganisationPolicy4(policy);
            case 5 -> caseData.setRespondentOrganisationPolicy5(policy);
            case 6 -> caseData.setRespondentOrganisationPolicy6(policy);
            case 7 -> caseData.setRespondentOrganisationPolicy7(policy);
            case 8 -> caseData.setRespondentOrganisationPolicy8(policy);
            case 9 -> caseData.setRespondentOrganisationPolicy9(policy);
            default -> {
                // Do nothing for invalid index
            }
        }
    }

    public static void preserveExistingValues(CaseData sourceCaseData, CaseData updatedCaseData) {
        if (sourceCaseData == null || updatedCaseData == null) {
            return;
        }

        if (hasOrganisation(sourceCaseData.getClaimantRepresentativeOrganisationPolicy())) {
            updatedCaseData.setClaimantRepresentativeOrganisationPolicy(
                    sourceCaseData.getClaimantRepresentativeOrganisationPolicy());
        }

        for (int idx = 0; idx < 10; idx++) {
            OrganisationPolicy existingPolicy = getRespondentOrganisationPolicyByIndex(sourceCaseData, idx);
            if (hasOrganisation(existingPolicy)) {
                setRespondentOrganisationPolicyByIndex(updatedCaseData, idx, existingPolicy);
            }

            NoticeOfChangeAnswers existingAnswer = getNoticeOfChangeAnswersByIndex(sourceCaseData, idx);
            if (isValidAnswer(existingAnswer)) {
                setNoticeOfChangeAnswersByIndex(updatedCaseData, idx, existingAnswer);
            }
        }
    }

    private static boolean hasOrganisation(OrganisationPolicy policy) {
        return policy != null
                && policy.getOrganisation() != null
                && StringUtils.isNotBlank(policy.getOrganisation().getOrganisationID());
    }

    private static boolean isValidAnswer(NoticeOfChangeAnswers answer) {
        return answer != null && StringUtils.isNotBlank(answer.getRespondentName());
    }

    public static boolean isCcdRequestValid(CCDRequest ccdRequest) {
        return ObjectUtils.isNotEmpty(ccdRequest)
                && ObjectUtils.isNotEmpty(ccdRequest.getCaseDetails())
                && StringUtils.isNotBlank(ccdRequest.getCaseDetails().getCaseId())
                && StringUtils.isNotBlank(ccdRequest.getCaseDetails().getCaseTypeId())
                && StringUtils.isNotBlank(ccdRequest.getCaseDetails().getJurisdiction())
                && ObjectUtils.isNotEmpty(ccdRequest.getCaseDetails().getCaseData());
    }

    public static String findCaseId(SubmitEvent submitEvent) {
        return ObjectUtils.isNotEmpty(submitEvent) && submitEvent.getCaseId() != 0
                ? String.valueOf(submitEvent.getCaseId()) : UNKNOWN_CASE_ID;
    }

    public void setLastCaseId(List<SubmitEvent> cases) {
        if (CollectionUtils.isEmpty(cases)) {
            lastCaseId = INITIAL_CASE_ID;
            return;
        }

        for (int idx = cases.size() - 1; idx >= 0; idx--) {
            SubmitEvent submitEvent = cases.get(idx);
            String caseId = findCaseId(submitEvent);
            if (!UNKNOWN_CASE_ID.equals(caseId)) {
                lastCaseId = caseId;
                return;
            }
            log.warn("{} - ECM migration NOC task - Case ID is unknown", submitEvent.getCaseId());
        }
        lastCaseId = INITIAL_CASE_ID;
    }

    static List<String> parseCaseIds(String configuredCaseIds) {
        if (StringUtils.isBlank(configuredCaseIds)) {
            return List.of();
        }

        return Arrays.stream(configuredCaseIds.split(","))
                .map(String::strip)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    private String buildQuery() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermsQueryBuilder("state.keyword", VALID_STATES))
                        .must(new TermsQueryBuilder("jurisdiction.keyword", EMPLOYMENT))
                        .must(new BoolQueryBuilder()
                                .should(new TermQueryBuilder("data.migratedFromEcm", YES))
                                .should(new ExistsQueryBuilder("data.ecmCaseLink"))
                                .minimumShouldMatch(1))
                        .must(new BoolQueryBuilder()
                                .should(new BoolQueryBuilder().mustNot(
                                        new ExistsQueryBuilder("data.respondentOrganisationPolicy0")))
                                .should(new BoolQueryBuilder().mustNot(
                                        new ExistsQueryBuilder("data.noticeOfChangeAnswers0")))
                                .should(new BoolQueryBuilder().mustNot(
                                        new ExistsQueryBuilder("data.claimantRepresentativeOrganisationPolicy")))
                                .minimumShouldMatch(1))
                ).sort("reference.keyword", SortOrder.ASC);

        if (StringUtils.isNotBlank(lastCaseId) && !INITIAL_CASE_ID.equals(lastCaseId)) {
            searchSourceBuilder.searchAfter(new Object[] {lastCaseId});
        }
        return searchSourceBuilder.toString();
    }
}
