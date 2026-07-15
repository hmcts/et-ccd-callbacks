package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.ECM_CASE_TYPES;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataQualityTask implements Runnable {

    private static final String FIX_CASE_API_EVENT_ID = "fixCaseAPI";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final uk.gov.hmcts.ecm.compat.common.client.CcdClient ecmCcdClient;

    @Value("${cron.reconfigurationCaseIds}")
    private String casesToUpdate;

    public record CasesToUpdate(String caseId, String caseTypeId) {}

    private record UpdateTracker(AtomicInteger count,
                                 AtomicReference<String> updated,
                                 AtomicReference<String> failed) {
        void recordSuccess(String caseId) {
            count.incrementAndGet();
            updated.updateAndGet(v -> v == null ? caseId : v + ", " + caseId);
        }

        void recordFailure(String caseId) {
            failed.updateAndGet(v -> v == null ? caseId : v + ", " + caseId);
        }
    }

    @Override
    public void run() {
        log.info("Data Quality Task started");
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

        log.info("Number of cases to update - {}", casesToUpdateList.size());

        UpdateTracker tracker = new UpdateTracker(new AtomicInteger(0),
            new AtomicReference<>(), new AtomicReference<>());

        String adminUserToken = adminUserService.getAdminUserToken();

        casesToUpdateList.parallelStream().forEach(caseToUpdate ->
            processCaseUpdate(adminUserToken, caseToUpdate, tracker));

        log.info("Completed transfer of {} cases", tracker.count().get());
        log.info("Updated cases: {}", tracker.updated().get());
        log.info("Failed cases: {}", tracker.failed().get());
    }

    private void processCaseUpdate(String adminUserToken, CasesToUpdate caseToUpdate, UpdateTracker tracker) {
        log.info("Updating case {} of type {}", caseToUpdate.caseId, caseToUpdate.caseTypeId);
        try {
            String caseTypeId = caseToUpdate.caseTypeId;
            if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId) || SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                triggerEventForCase(adminUserToken, caseToUpdate.caseId, caseTypeId);
            } else if (ECM_CASE_TYPES.contains(caseTypeId)) {
                triggerEcmEventForCase(adminUserToken, caseToUpdate.caseId, caseTypeId);
            }
            tracker.recordSuccess(caseToUpdate.caseId);
        } catch (Exception e) {
            log.error("Error updating case {} of type {}: {}", caseToUpdate.caseId, caseToUpdate.caseTypeId,
                e.getMessage());
            tracker.recordFailure(caseToUpdate.caseId);
        }
    }

    private void triggerEventForCase(String adminUserToken, String caseId, String caseTypeId) {
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                caseId, FIX_CASE_API_EVENT_ID);
            CaseDetails caseDetails = ccdRequest.getCaseDetails();
            CaseData caseData = caseDetails.getCaseData();
            caseData.setStateAPI(null);
            updateHearingInJudgment(caseData);
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
            updateHearingInJudgment(caseData);
            ecmCcdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } catch (Exception e) {
            log.warn("{} - Failed to update case {}: {}", caseTypeId, caseId, e.getMessage(), e);
        }
    }

    private void updateHearingInJudgment(CaseData caseData) {
        if (isEmpty(caseData.getHearingCollection()) || isEmpty(caseData.getJudgementCollection())) {
            return;
        }

        List<LocalDate> heardDates = caseData.getHearingCollection().stream()
            .filter(h -> h.getValue() != null && isNotEmpty(h.getValue().getHearingDateCollection()))
            .flatMap(h -> h.getValue().getHearingDateCollection().stream())
            .filter(d -> d.getValue() != null
                && HEARING_STATUS_HEARD.equals(d.getValue().getHearingStatus())
                && !isNullOrEmpty(d.getValue().getListedDate()))
            .map(d -> {
                try {
                    return LocalDate.parse(d.getValue().getListedDate(), OLD_DATE_TIME_PATTERN);
                } catch (Exception e) {
                    log.warn("Failed to parse hearing listed date: {}", d.getValue().getListedDate());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();

        if (heardDates.isEmpty()) {
            return;
        }

        caseData.getJudgementCollection().stream()
            .map(JudgementTypeItem::getValue)
            .filter(Objects::nonNull)
            .forEach(judgment -> findCorrectedHearingDate(
                    judgment.getJudgmentHearingDate(), judgment.getDateJudgmentSent(), heardDates)
                .ifPresent(judgment::setJudgmentHearingDate));
    }

    private void updateHearingInJudgment(uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData) {
        if (isEmpty(caseData.getHearingCollection()) || isEmpty(caseData.getJudgementCollection())) {
            return;
        }

        List<LocalDate> heardDates = caseData.getHearingCollection().stream()
            .filter(h -> h.getValue() != null && isNotEmpty(h.getValue().getHearingDateCollection()))
            .flatMap(h -> h.getValue().getHearingDateCollection().stream())
            .filter(d -> d.getValue() != null
                && HEARING_STATUS_HEARD.equals(d.getValue().getHearingStatus())
                && !isNullOrEmpty(d.getValue().getListedDate()))
            .map(d -> {
                try {
                    return LocalDate.parse(d.getValue().getListedDate(), OLD_DATE_TIME_PATTERN);
                } catch (Exception e) {
                    log.warn("Failed to parse hearing listed date: {}", d.getValue().getListedDate());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();

        if (heardDates.isEmpty()) {
            return;
        }

        caseData.getJudgementCollection().stream()
            .map(uk.gov.hmcts.ecm.common.model.ccd.items.JudgementTypeItem::getValue)
            .filter(Objects::nonNull)
            .forEach(judgment -> findCorrectedHearingDate(
                    judgment.getJudgmentHearingDate(), judgment.getDateJudgmentSent(), heardDates)
                .ifPresent(judgment::setJudgmentHearingDate));
    }

    private Optional<String> findCorrectedHearingDate(String judgmentHearingDate, String dateJudgmentSent,
            List<LocalDate> heardDates) {
        if (isNullOrEmpty(dateJudgmentSent) || isNullOrEmpty(judgmentHearingDate)) {
            return Optional.empty();
        }
        try {
            LocalDate sentDate = LocalDate.parse(dateJudgmentSent, OLD_DATE_TIME_PATTERN2);
            LocalDate currentHearingDate = LocalDate.parse(judgmentHearingDate, OLD_DATE_TIME_PATTERN2);
            if (!currentHearingDate.isAfter(sentDate)) {
                return Optional.empty();
            }
            return heardDates.stream()
                .filter(d -> !d.isAfter(sentDate))
                .max(Comparator.naturalOrder())
                .map(d -> d.format(OLD_DATE_TIME_PATTERN2));
        } catch (Exception e) {
            log.warn("Failed to process judgment hearing date: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
