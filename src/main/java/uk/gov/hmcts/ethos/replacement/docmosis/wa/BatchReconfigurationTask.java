package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchReconfigurationTask implements Runnable {
    private static final String ERROR_TRIGGERING_RECONFIGURATION_EVENT_FOR_CASE =
            "Error triggering reconfiguration event for case {}: {}";
    private static final String NO_CASE_IDS_TO_RECONFIGURE_EXITING_JOB = "No case ids to reconfigure, exiting job";
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    @Value("${cron.reconfigurationCaseIds}")
    private String caseIdsToReconfigure;
    @Value("${cron.reconfigurationLimit}")
    private int limit;
    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;

    @Override
    public void run() {
        log.info("Running batch reconfiguration task");
        if (isNullOrEmpty(caseIdsToReconfigure)) {
            log.info(NO_CASE_IDS_TO_RECONFIGURE_EXITING_JOB);
            return;
        }
        List<String> caseIds = List.of(caseIdsToReconfigure.split(","));
        if (caseIds.isEmpty()) {
            log.info(NO_CASE_IDS_TO_RECONFIGURE_EXITING_JOB);
            return;
        }

        log.info("Number of cases to reconfigure - {}", caseIds.size());
        String adminUserToken = adminUserService.getAdminUserToken();
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<String> reconfiguredCases = new AtomicReference<>();
        caseIds.stream()
                .limit(limit)
                .parallel()
                .forEach(caseId -> {
                    try {
                        log.info("Reconfiguring case {}", caseId);
                        triggerReconfigureEvent(adminUserToken, caseId);
                        counter.incrementAndGet();
                        reconfiguredCases.updateAndGet(v -> v == null ? caseId : v + ", " + caseId);
                    } catch (Exception e) {
                        log.error(ERROR_TRIGGERING_RECONFIGURATION_EVENT_FOR_CASE, caseId, e.getMessage());
                    }
                });
        log.info("Completed reconfiguration for {} cases", counter.get());
        log.info("Reconfigured cases: {}", reconfiguredCases.get());
    }

    private void triggerReconfigureEvent(String adminUserToken, String caseId) {
        try {
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeIdsString, EMPLOYMENT,
                    caseId, "RECONFIGURE_WA_TASKS");
            ccdClient.submitEventForCase(adminUserToken, returnedRequest.getCaseDetails().getCaseData(),
                    caseTypeIdsString, returnedRequest.getCaseDetails().getJurisdiction(), returnedRequest,
                    caseId);
            log.info("Reconfiguration event triggered for case {}", caseId);
        } catch (Exception e) {
            log.error(ERROR_TRIGGERING_RECONFIGURATION_EVENT_FOR_CASE, caseId, e.getMessage());
        }
    }
}
