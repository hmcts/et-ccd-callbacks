package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PreAcceptanceCaseService;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.ECM_CASE_TYPES;

/**
 * Scheduled task that clears stale pre-acceptance dates on cases where both
 * {@code dateAccepted} and {@code dateRejected} are set simultaneously since only
 * one of the two is valid depending on whether the case
 * was accepted or rejected.
 *
 * <p>The task runs against all case type IDs configured via {@code cron.caseTypeId},
 * routing ET Reform cases ({@code ET_EnglandWales}, {@code ET_Scotland}) through the
 * Reform CCD client and legacy ECM cases through the ECM compat client.
 *
 * <p>Each eligible case is updated via the {@code fixCaseAPI} CCD event, which clears
 * {@code dateRejected} for accepted cases and {@code dateAccepted} for rejected cases.
 * Cases are processed in parallel using a fixed thread pool.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CaseAcceptanceDateTask implements Runnable {

    private static final String FIX_CASE_API_EVENT_ID = "fixCaseAPI";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final uk.gov.hmcts.ecm.compat.common.client.CcdClient ecmCcdClient;
    private final PreAcceptanceCaseService preAcceptanceCaseService;

    @Value("${cron.reconfigurationCaseIds}")
    private String casesToUpdate;

    public record CasesToUpdate(String caseId, String caseTypeId) {}

    @Override
    public void run() {
        log.info("CaseAcceptanceDateTask started");
        if (isNullOrEmpty(casesToUpdate)) {
            log.info("No cases to transfer");
            return;
        }

        String[] caseIds = casesToUpdate.split(",");
        if (caseIds.length % 2 != 0) {
            log.info("Invalid case ids format. Expected format: caseId1,caseTypeId1, caseId2,caseTypeId2,...");
            return;
        }

        List<CasesToUpdate> casesToUpdateList =
            IntStream.iterate(0, i -> i < caseIds.length, i -> i + 2)
                .mapToObj(i -> new CasesToUpdate(caseIds[i], caseIds[i + 1]))
                .toList();

        if (casesToUpdateList.isEmpty()) {
            log.info("No cases to transfer");
            return;
        }

        log.info("Number of cases to update - {}", casesToUpdateList.size());

        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<String> updatedCases = new AtomicReference<>();
        AtomicReference<String> failedCases = new AtomicReference<>();

        String adminUserToken = adminUserService.getAdminUserToken();

        casesToUpdateList.parallelStream().forEach(caseToUpdate -> {
            log.info("Updating case {} of type {}", caseToUpdate.caseId, caseToUpdate.caseTypeId);
            try {
                String caseTypeId = caseToUpdate.caseTypeId;
                if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId) || SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                    triggerEventForCase(adminUserToken, caseToUpdate.caseId, caseTypeId);
                } else if (ECM_CASE_TYPES.contains(caseTypeId)) {
                    triggerEcmEventForCase(adminUserToken, caseToUpdate.caseId, caseTypeId);
                }
            } catch (Exception e) {
                log.error("Error updating case {} of type {}: {}", caseToUpdate.caseId, caseToUpdate.caseTypeId,
                    e.getMessage());
                failedCases.updateAndGet(v -> v == null ? caseToUpdate.caseId : v + ", " + caseToUpdate.caseId);
            }
        });

        log.info("Completed transfer of {} cases", counter.get());
        log.info("Updated cases: {}", updatedCases.get());
        log.info("Failed cases: {}", failedCases.get());
    }

    private void triggerEventForCase(String adminUserToken, String caseId, String caseTypeId) {
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                caseId, FIX_CASE_API_EVENT_ID);
            CaseDetails caseDetails = ccdRequest.getCaseDetails();
            CaseData caseData = caseDetails.getCaseData();
            caseData.setStateAPI(null);
            preAcceptanceCaseService.clearPreAcceptanceDates(caseData);
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } catch (Exception e) {
            log.warn("{} - Failed to update case {}: {}", caseTypeId, caseId, e.getMessage(), e);
        }
    }

    private void triggerEcmEventForCase(String adminUserToken, String caseId, String caseTypeId) {
        try {
            uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest =
                ecmCcdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                    caseId, FIX_CASE_API_EVENT_ID);
            uk.gov.hmcts.ecm.common.model.ccd.CaseDetails caseDetails = ccdRequest.getCaseDetails();
            uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData = caseDetails.getCaseData();
            caseData.setStateAPI(null);
            clearEcmPreAcceptanceDates(caseData);
            ecmCcdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } catch (Exception e) {
            log.warn("{} - Failed to update case {}: {}", caseTypeId, caseId, e.getMessage(), e);
        }
    }

    private void clearEcmPreAcceptanceDates(uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData) {
        if (Objects.nonNull(caseData) && Objects.nonNull(caseData.getPreAcceptCase())) {
            String caseAccepted = defaultIfEmpty(caseData.getPreAcceptCase().getCaseAccepted(), "");
            if (YES.equals(caseAccepted)) {
                caseData.getPreAcceptCase().setDateRejected(null);
            } else if (NO.equals(caseAccepted)) {
                caseData.getPreAcceptCase().setDateAccepted(null);
            }
        }
    }

}
