package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
public class NoticeOfChangeFieldsTask implements Runnable {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final List<String> validStates = List.of(SUBMITTED_STATE, VETTED_STATE, ACCEPTED_STATE, REJECTED_STATE);

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;

    private static final String UNKNOWN_CASE_ID = "<unknown>";
    private static final String INITIAL_CASE_ID = "0";

    private String lastCaseId;

    @Override
    public void run() {
        log.info("****** Notice of change fields task - Started ******");
        String adminUserToken = adminUserService.getAdminUserToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            lastCaseId = INITIAL_CASE_ID;
            List<SubmitEvent> cases;
            try {
                do {
                    log.info("{} - Notice of change fields task - Searching for cases", caseTypeId);
                    String query = buildQuery();
                    cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                    setLastCaseId(cases);
                    log.info("{} - Notice of change fields task - Retrieved {} cases", caseTypeId, cases.size());
                    updateCases(cases, caseTypeId, adminUserToken);
                } while (CollectionUtils.isNotEmpty(cases));
            } catch (IOException e) {
                log.error(e.getMessage());

            } catch (InterruptedException ie) {
                log.error(ie.getMessage());
                Thread.currentThread().interrupt();
            }
        });
    }

    private void updateCases(List<SubmitEvent> cases, String caseTypeId, String adminUserToken)
            throws InterruptedException {
        final long awaitTimeoutSeconds = 120;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
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
        return ObjectUtils.isNotEmpty(se) && se.getCaseId() != 0 ? String.valueOf(se.getCaseId()) : UNKNOWN_CASE_ID;
    }

    public void setLastCaseId(List<SubmitEvent> cases) {
        if (cases == null || cases.isEmpty()) {
            lastCaseId = INITIAL_CASE_ID;
            return;
        }

        for (int index = cases.size() - 1; index >= 0; index--) {
            SubmitEvent submitEvent = cases.get(index);
            String caseId = findCaseId(submitEvent);
            if (!UNKNOWN_CASE_ID.equals(caseId)) {
                lastCaseId = caseId;
                return;
            }
            log.warn(
                    "{} - NOC fields task - Case ID is unknown",
                    submitEvent.getCaseId()
            );
        }
        lastCaseId = INITIAL_CASE_ID;
    }

    public void triggerEventForCase(String adminUserToken, SubmitEvent submitEvent, String caseTypeId)
            throws GenericServiceException {
        try {
            if (StringUtils.isNotBlank(adminUserToken)
                    && ObjectUtils.isNotEmpty(submitEvent)
                    && ObjectUtils.isNotEmpty(submitEvent.getCaseData())
                    && CollectionUtils.isNotEmpty(submitEvent.getCaseData().getRespondentCollection())
                    && CollectionUtils.isNotEmpty(submitEvent.getCaseData().getRepCollection())
                    && StringUtils.isNotBlank(caseTypeId)) {
                CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                        String.valueOf(submitEvent.getCaseId()), "UPDATE_CASE_SUBMITTED");
                if (!isCCDRequestValid(ccdRequest)) {
                    return;
                }
                CaseDetails caseDetails = ccdRequest.getCaseDetails();
                CaseData caseData = caseDetails.getCaseData();
                if (updateRepCollection(caseData)) {
                    ccdClient.submitEventForCase(adminUserToken, caseData, caseDetails.getCaseTypeId(),
                            caseDetails.getJurisdiction(), ccdRequest, caseDetails.getCaseId());
                    log.info("Updated respondent representative repId, role and respondent representative id {}",
                            submitEvent.getCaseId());
                }
            }
        } catch (Exception e) {
            throw new GenericServiceException(e.getMessage(), e, e.getMessage(), findCaseId(submitEvent),
                    "NoticeOfChangeFieldsTask", "triggerEventForCase");
        }
    }

    public static boolean updateRepCollection(CaseData caseData) {
        boolean caseUpdated = false;
        for (RepresentedTypeRItem representative : caseData.getRepCollection()) {
            if (!isValidRepresentative(representative)) {
                continue;
            }
            if (StringUtils.isBlank(representative.getId())) {
                representative.setId(UUID.randomUUID().toString());
                caseUpdated = true;
            }
            RespondentSumTypeItem respondent = RespondentRepresentativeUtils
                    .findRespondentByRepresentative(caseData, representative);
            if (setRespondentValues(caseData, representative, respondent)) {
                caseUpdated = true;
            }
        }
        return caseUpdated;
    }

    public static boolean setRespondentValues(CaseData caseData,
                                    RepresentedTypeRItem representative,
                                    RespondentSumTypeItem respondent) {
        if (!RespondentUtils.isValidRespondent(respondent)) {
            return false;
        }
        boolean caseUpdated = false;
        if (!representative.getId().equals(respondent.getValue().getRepresentativeId())) {
            respondent.getValue().setRepresentativeId(representative.getId());
            caseUpdated = true;
        }
        int respondentIndex = RespondentUtils.getRespondentIndexById(caseData, respondent.getId());
        String role = StringUtils.EMPTY;
        if (respondentIndex != -1) {
            role = RoleUtils.solicitorRoleLabelForIndex(respondentIndex);
        }
        if (StringUtils.isNotBlank(role) && !role.equals(representative.getValue().getRole())) {
            representative.getValue().setRole(RoleUtils.solicitorRoleLabelForIndex(respondentIndex));
            caseUpdated = true;
        }
        return caseUpdated;
    }

    public static boolean isValidRepresentative(RepresentedTypeRItem representative) {
        return ObjectUtils.isNotEmpty(representative) && ObjectUtils.isNotEmpty(representative.getValue());
    }

    public static boolean isCCDRequestValid(CCDRequest ccdRequest) {
        return ObjectUtils.isNotEmpty(ccdRequest)
                && ObjectUtils.isNotEmpty(ccdRequest.getCaseDetails())
                && StringUtils.isNotBlank(ccdRequest.getCaseDetails().getCaseId())
                && StringUtils.isNotBlank(ccdRequest.getCaseDetails().getCaseTypeId())
                && StringUtils.isNotBlank(ccdRequest.getCaseDetails().getJurisdiction())
                && ObjectUtils.isNotEmpty(ccdRequest.getCaseDetails().getCaseData())
                && CollectionUtils.isNotEmpty(ccdRequest.getCaseDetails().getCaseData().getRespondentCollection())
                && CollectionUtils.isNotEmpty(ccdRequest.getCaseDetails().getCaseData().getRepCollection());
    }

    private String buildQuery() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(maxCases)
                .query(new BoolQueryBuilder()
                        .must(new TermsQueryBuilder("state.keyword", validStates))
                        .must(new TermsQueryBuilder("jurisdiction.keyword", EMPLOYMENT))
                        .must(new ExistsQueryBuilder("data.repCollection"))
                ).sort("reference.keyword", SortOrder.ASC);

        if (StringUtils.isNotBlank(lastCaseId) && !lastCaseId.equals(INITIAL_CASE_ID)) {
            searchSourceBuilder.searchAfter(new Object[] { lastCaseId });
        }
        return searchSourceBuilder.toString();
    }
}
