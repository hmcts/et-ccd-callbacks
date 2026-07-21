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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

/**
 * Scheduled task that fixes data quality issues across a configurable set of
 * cases.
 *
 * <p>
 * Specifically, for each case the task:
 * <ul>
 * <li>Resolves the case type by calling the CCD Data Store API, so only a case
 * ID need be
 * supplied in configuration.</li>
 * <li>Triggers the {@code fixCaseAPI} CCD event to obtain an editable snapshot
 * of the case.</li>
 * <li>Inspects every judgment: if a judgment's {@code judgmentHearingDate}
 * falls after its
 * {@code dateJudgmentSent}, the date is corrected to the latest "Heard" hearing
 * date that
 * does not exceed the sent date. The {@code dynamicJudgementHearing} field is
 * also cleared
 * so it is re-evaluated on next access.</li>
 * <li>Submits the corrected case back to CCD.</li>
 * </ul>
 *
 * <p>
 * Cases are processed in parallel using Virtual Threads. The task supports both
 * ET Reform cases
 * ({@code ET_EnglandWales}, {@code ET_Scotland}) and legacy ECM cases.
 *
 * <p>
 * Configure the cases to update via the {@code cron.reconfigurationCaseIds}
 * property as a
 * comma-separated list of CCD case IDs, for example:
 * 
 * <pre>{@code
 * CRON_RECONFIGURATION_CASE_IDS=1784038633249743,9876543210
 * }</pre>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataQualityTask implements Runnable {

    private static final String FIX_CASE_API_EVENT_ID = "fixCaseAPI";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final uk.gov.hmcts.ecm.compat.common.client.CcdClient ecmCcdClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${cron.reconfigurationCaseIds}")
    private String casesToUpdate;

    private record UpdateTracker(AtomicInteger count,
            ConcurrentLinkedQueue<String> updated,
            ConcurrentLinkedQueue<String> failed) {
        void recordSuccess(String caseId) {
            count.incrementAndGet();
            updated.add(caseId);
        }

        void recordFailure(String caseId) {
            failed.add(caseId);
        }
    }

    @Override
    public void run() {
        log.info("Data Quality Task started");
        if (isNullOrEmpty(casesToUpdate)) {
            log.info("No cases to transfer");
            return;
        }

        List<String> caseIds = Arrays.stream(casesToUpdate.split(","))
                .map(String::strip)
                .filter(id -> !id.isEmpty())
                .toList();

        if (caseIds.isEmpty()) {
            log.info("No cases to transfer");
            return;
        }

        log.info("Number of cases to update - {}", caseIds.size());

        UpdateTracker tracker = new UpdateTracker(new AtomicInteger(0),
                new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>());

        String adminUserToken = adminUserService.getAdminUserToken();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            caseIds.forEach(caseId -> executor.submit(() -> processCaseUpdate(adminUserToken, caseId, tracker)));
        }

        log.info("Completed transfer of {} cases", tracker.count().get());
        log.info("Updated cases: {}", String.join(", ", tracker.updated()));
        log.info("Failed cases: {}", String.join(", ", tracker.failed()));
    }

    /**
     * Looks up the case type for {@code caseId} via the CCD Data Store API, then
     * routes to the
     * appropriate event trigger for either ET Reform or ECM cases. Failures are
     * recorded in
     * {@code tracker} without interrupting the processing of other cases.
     *
     * @param adminUserToken bearer token for the admin service account
     * @param caseId         the CCD case ID to process
     * @param tracker        thread-safe counter used to accumulate success and
     *                       failure outcomes
     */
    private void processCaseUpdate(String adminUserToken, String caseId, UpdateTracker tracker) {
        log.info("Updating case {}", caseId);
        try {
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = coreCaseDataApi.getCase(adminUserToken,
                    authTokenGenerator.generate(), caseId);
            String caseTypeId = caseDetails.getCaseTypeId();
            log.info("Case {} resolved to type {}", caseId, caseTypeId);
            if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId) || SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                triggerEventForCase(adminUserToken, caseId, caseTypeId);
            } else if (ECM_CASE_TYPES.contains(caseTypeId)) {
                triggerEcmEventForCase(adminUserToken, caseId, caseTypeId);
            } else {
                log.warn("Skipping case {} — unknown case type {}", caseId, caseTypeId);
            }
            tracker.recordSuccess(caseId);
        } catch (Exception e) {
            log.error("Error updating case {}: {}", caseId, e.getMessage(), e);
            tracker.recordFailure(caseId);
        }
    }

    /**
     * Starts the {@code fixCaseAPI} event for an ET Reform case, applies data
     * quality fixes, and
     * submits the updated case back to CCD if any changes were made.
     *
     * @param adminUserToken bearer token for the admin service account
     * @param caseId         the CCD case ID
     * @param caseTypeId     the ET Reform case type (e.g. {@code ET_EnglandWales})
     */
    private void triggerEventForCase(String adminUserToken, String caseId, String caseTypeId) throws IOException {
        CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
                caseId, FIX_CASE_API_EVENT_ID);
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        caseData.setStateAPI(null);
        boolean updated = updateHearingInJudgment(caseData, caseId);
        if (updated) {
            ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } else {
            log.info("{} - No data quality fixes needed for case {}", caseTypeId, caseId);
        }
    }

    /**
     * Starts the {@code fixCaseAPI} event for a legacy ECM case, applies data
     * quality fixes, and
     * submits the updated case back to CCD if any changes were made.
     *
     * @param adminUserToken bearer token for the admin service account
     * @param caseId         the CCD case ID
     * @param caseTypeId     the ECM case type (e.g. {@code Manchester})
     */
    private void triggerEcmEventForCase(String adminUserToken, String caseId, String caseTypeId) throws IOException {
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest = ecmCcdClient.startEventForCase(adminUserToken,
                caseTypeId, EMPLOYMENT,
                caseId, FIX_CASE_API_EVENT_ID);
        uk.gov.hmcts.ecm.common.model.ccd.CaseDetails caseDetails = ccdRequest.getCaseDetails();
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData = caseDetails.getCaseData();
        caseData.setStateAPI(null);
        boolean updated = updateHearingInJudgment(caseData, caseId);
        if (updated) {
            ecmCcdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    caseDetails.getJurisdiction(), ccdRequest, caseId);
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } else {
            log.info("{} - No data quality fixes needed for case {}", caseTypeId, caseId);
        }
    }

    /**
     * Corrects invalid judgment hearing dates for an ET Reform case.
     *
     * <p>
     * Collects all hearing dates with status {@code Heard} from the case's hearing
     * collection.
     * For each judgment whose {@code judgmentHearingDate} is after
     * {@code dateJudgmentSent},
     * replaces it with the latest heard date that does not exceed the sent date,
     * and clears
     * {@code dynamicJudgementHearing} so the link is re-resolved. Judgments that
     * are already
     * valid, have missing dates, or have no qualifying heard date are left
     * unchanged.
     *
     * @param caseData the ET Reform case data to inspect and mutate
     * @param caseId   the CCD case ID, used for logging context
     * @return true if any modifications were made to the judgment dates, false
     *         otherwise
     */
    private boolean updateHearingInJudgment(CaseData caseData, String caseId) {
        if (isEmpty(caseData.getHearingCollection()) || isEmpty(caseData.getJudgementCollection())) {
            return false;
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
                        log.warn("Failed to parse hearing listed date for case {}: {}", caseId,
                                d.getValue().getListedDate());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (heardDates.isEmpty()) {
            return false;
        }

        AtomicBoolean updated = new AtomicBoolean(false);
        caseData.getJudgementCollection().stream()
                .filter(item -> item.getValue() != null)
                .forEach(item -> {
                    var judgment = item.getValue();
                    findCorrectedHearingDate(caseId, judgment.getJudgmentHearingDate(), judgment.getDateJudgmentSent(),
                            heardDates)
                            .ifPresent(correctedDate -> {
                                log.info("Judgment {} in case {} is being updated", item.getId(), caseId);
                                judgment.setJudgmentHearingDate(correctedDate);
                                judgment.setDynamicJudgementHearing(null);
                                updated.set(true);
                            });
                });
        return updated.get();
    }

    /**
     * Corrects invalid judgment hearing dates for a legacy ECM case.
     *
     * <p>
     * Behaviour is identical to the ET Reform overload: collects heard dates, finds
     * any
     * judgment whose {@code judgmentHearingDate} exceeds {@code dateJudgmentSent},
     * and resets
     * it to the latest valid heard date while clearing
     * {@code dynamicJudgementHearing}.
     *
     * @param caseData the ECM case data to inspect and mutate
     * @param caseId   the CCD case ID, used for logging context
     * @return true if any modifications were made to the judgment dates, false
     *         otherwise
     */
    private boolean updateHearingInJudgment(uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData, String caseId) {
        if (isEmpty(caseData.getHearingCollection()) || isEmpty(caseData.getJudgementCollection())) {
            return false;
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
                        log.warn("Failed to parse hearing listed date for case {}: {}", caseId,
                                d.getValue().getListedDate());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (heardDates.isEmpty()) {
            return false;
        }

        AtomicBoolean updated = new AtomicBoolean(false);
        caseData.getJudgementCollection().stream()
                .filter(item -> item.getValue() != null)
                .forEach(item -> {
                    var judgment = item.getValue();
                    findCorrectedHearingDate(caseId, judgment.getJudgmentHearingDate(), judgment.getDateJudgmentSent(),
                            heardDates)
                            .ifPresent(correctedDate -> {
                                log.info("Judgment {} in case {} is being updated", item.getId(), caseId);
                                judgment.setJudgmentHearingDate(correctedDate);
                                judgment.setDynamicJudgementHearing(null);
                                updated.set(true);
                            });
                });
        return updated.get();
    }

    /**
     * Returns the corrected judgment hearing date if one is needed, or
     * {@link Optional#empty()}
     * if the existing date is already valid or cannot be corrected.
     *
     * <p>
     * A correction is needed when {@code judgmentHearingDate} is strictly after
     * {@code dateJudgmentSent}. In that case the method returns the latest date
     * from {@code heardDates} that does not exceed {@code dateJudgmentSent}.
     * If no such date exists,
     * {@link Optional#empty()} is returned and the judgment is left unchanged.
     *
     * @param caseId              the CCD case ID, used for logging context
     * @param judgmentHearingDate the current hearing date on the judgment
     *                            ({@code yyyy-MM-dd})
     * @param dateJudgmentSent    the date the judgment was sent
     *                            ({@code yyyy-MM-dd})
     * @param heardDates          the pool of candidate heard dates to choose from
     * @return an {@link Optional} containing the corrected date string, or empty if
     *         no correction is required or possible
     */
    private Optional<String> findCorrectedHearingDate(String caseId, String judgmentHearingDate,
            String dateJudgmentSent,
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
            log.warn("Failed to process judgment hearing date for case {}: {}", caseId, e.getMessage());
            return Optional.empty();
        }
    }
}
