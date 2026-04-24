package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.compat.common.client.CcdClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseTransferTask implements Runnable {

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    @Value("${cron.reconfigurationCaseIds}")
    private String casesToUpdate;

    public record CaseTransferDetails(String caseId, String oldCaseTypeId, String newCaseTypeId) {}

    @Override
    public void run() {
        log.info("Running Case Transfer Task");
        if (isNullOrEmpty(casesToUpdate)) {
            log.info("No cases to transfer");
            return;
        }

        String[] caseIds = casesToUpdate.split(",");
        if (caseIds.length % 3 != 0) {
            log.error("Invalid case ids format. Expected format: caseId1,oldCaseTypeId1,newCaseTypeId1,"
                + "caseId2,oldCaseTypeId2,newCaseTypeId2,...");
            return;
        }

        List<CaseTransferDetails> caseTransferDetailsList =
            IntStream.iterate(0, i -> i < caseIds.length, i -> i + 3)
                .mapToObj(i -> new CaseTransferDetails(caseIds[i], caseIds[i + 1], caseIds[i + 2]))
                .toList();

        if (caseTransferDetailsList.isEmpty()) {
            log.info("No cases to transfer");
            return;
        }
        log.info("Number of cases to transfer - {}", caseTransferDetailsList.size());

        String adminUserToken = adminUserService.getAdminUserToken();
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<String> updatedCases = new AtomicReference<>();
        AtomicReference<String> failedCases = new AtomicReference<>();
        caseTransferDetailsList.parallelStream().forEach(details -> {
            log.info("Transferring case {} from type {} to type {}",
                details.caseId, details.oldCaseTypeId, details.newCaseTypeId);
            try {
                CCDRequest ccdRequest = ccdClient.returnCaseCreationTransfer(
                    adminUserToken,
                    details.oldCaseTypeId,
                    EMPLOYMENT,
                    details.caseId
                );
                ccdClient.submitEventForCase(adminUserToken,
                    ccdRequest.getCaseDetails().getCaseData(),
                    details.oldCaseTypeId,
                    ccdRequest.getCaseDetails().getJurisdiction(),
                    ccdRequest,
                    details.caseId);
                log.info("Case {} transferred reset successfully", details.caseId);
                ccdRequest = ccdClient.startEventForCase(
                    adminUserToken,
                    details.oldCaseTypeId,
                    EMPLOYMENT,
                    details.caseId,
                    "caseTransfer");
                CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
                caseData.setOfficeCT(new DynamicFixedListType(details.newCaseTypeId));
                caseData.setPositionTypeCT(POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY);
                caseData.setReasonForCT("ET Data Quality");
                ccdClient.submitEventForCase(adminUserToken,
                    caseData,
                    details.oldCaseTypeId,
                    ccdRequest.getCaseDetails().getJurisdiction(),
                    ccdRequest,
                    details.caseId);
                log.info("Case {} transferred successfully", details.caseId);
                counter.incrementAndGet();
                updatedCases.updateAndGet(updated -> updated == null ? details.caseId : updated + "," + details.caseId);
            } catch (IOException e) {
                log.error("Error transferring case {} from type {} to type {}",
                    details.caseId, details.oldCaseTypeId, details.newCaseTypeId);
                failedCases.updateAndGet(failed -> failed == null ? details.caseId : failed + "," + details.caseId);
            }
        });
        log.info("Completed transfer of {} cases", counter.get());
        log.info("Updated cases: {}", updatedCases.get());
        log.info("Failed cases: {}", failedCases.get());
    }
}
