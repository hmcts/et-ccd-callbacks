package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.compat.common.model.helper.Constants.ECM_CASE_TYPES;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

/**
 * Scheduled task that fixes data quality issues across a configurable set of cases.
 *
 * <p>For each configured CCD case ID, the task:
 * <ul>
 * <li>Resolves the case type by calling the CCD Data Store API.</li>
 * <li>Triggers the {@code fixCaseAPI} CCD event for either ET Reform
 * ({@code ET_EnglandWales}, {@code ET_Scotland}) or legacy ECM cases.</li>
 * <li>Updates {@code preAcceptCase} by setting {@code caseAccepted} to {@code No},
 * copying {@code dateAccepted} to {@code dateRejected} (and clearing {@code dateAccepted}),
 * setting {@code rejectReason} to {@code ["Case Input in Error"]}, and setting
 * {@code stateAPI} to {@code Closed}.</li>
 * <li>Submits the updated case back to CCD unless {@code cron.reconfigurationDryRun} is enabled.</li>
 * </ul>
 *
 * <p>Configure the cases to update via {@code cron.reconfigurationCaseIds} as a comma-separated
 * list of CCD case IDs, for example:
 * <pre>{@code
 * CRON_RECONFIGURATION_CASE_IDS=1784038633249743,1784038633249744
 * }</pre>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataQualityTask implements Runnable {

    private static final String FIX_CASE_API_EVENT_ID = "fixCaseAPI";
    static final String REJECT_REASON_CASE_INPUT_IN_ERROR = "Case Input in Error";

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final uk.gov.hmcts.ecm.compat.common.client.CcdClient ecmCcdClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final EmailService emailService;

    @Value("${cron.reconfigurationCaseIds}")
    private String casesToUpdate;
    @Value("${cron.reconfigurationDryRun:false}")
    private boolean dryRun;
    @Value("${notifications.et1ServiceOwnerNotificationEmail}")
    private String serviceOwnerEmail;
    @Value("${notifications.dataQualityReportTemplateId}")
    private String dataQualityReportTemplateId;

    private record UpdateTracker(AtomicInteger count,
            ConcurrentLinkedQueue<String> updated,
            ConcurrentLinkedQueue<String> failed,
            ConcurrentLinkedQueue<Correction> corrections) {
        void recordSuccess(String caseId) {
            count.incrementAndGet();
            updated.add(caseId);
        }

        void recordFailure(String caseId) {
            failed.add(caseId);
        }
    }

    private record Correction(String caseId, String ethosCaseReference, String caseTypeId,
                              String previousCaseAccepted, String dateAccepted,
                              String dateRejected, String rejectReason) {
    }

    @Override
    public void run() {
        log.info("Data Quality Task started");
        if (isNullOrEmpty(casesToUpdate)) {
            log.info("No cases to update");
            return;
        }

        List<String> caseIds = Arrays.stream(casesToUpdate.split(","))
            .map(String::strip)
            .filter(id -> !id.isEmpty())
            .toList();

        if (caseIds.isEmpty()) {
            log.info("No cases to update");
            return;
        }

        log.info("Number of cases to update - {}", caseIds.size());

        UpdateTracker tracker = new UpdateTracker(new AtomicInteger(0),
            new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>());

        String adminUserToken = adminUserService.getAdminUserToken();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            caseIds.forEach(caseId -> executor.submit(() -> processCaseUpdate(adminUserToken, caseId, tracker)));
        }

        log.info("Completed update of {} cases", tracker.count().get());
        log.info("Updated cases: {}", String.join(", ", tracker.updated()));
        log.info("Failed cases: {}", String.join(", ", tracker.failed()));
        sendReport(tracker);
    }

    private void processCaseUpdate(String adminUserToken, String caseId, UpdateTracker tracker) {
        log.info("Updating case {}", caseId);
        try {
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = coreCaseDataApi.getCase(adminUserToken,
                authTokenGenerator.generate(), caseId);
            String caseTypeId = caseDetails.getCaseTypeId();
            log.info("Case {} resolved to type {}", caseId, caseTypeId);
            if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId) || SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                triggerEventForCase(adminUserToken, caseId, caseTypeId, tracker.corrections());
            } else if (ECM_CASE_TYPES.contains(caseTypeId)) {
                triggerEcmEventForCase(adminUserToken, caseId, caseTypeId, tracker.corrections());
            } else {
                log.warn("Skipping case {} — unknown case type {}", caseId, caseTypeId);
            }
            tracker.recordSuccess(caseId);
        } catch (Exception ex) {
            log.error("Error updating case {}: {}", caseId, ex.getMessage(), ex);
            tracker.recordFailure(caseId);
        }
    }

    private void triggerEventForCase(String adminUserToken, String caseId, String caseTypeId,
                                     ConcurrentLinkedQueue<Correction> corrections) throws IOException {
        CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken, caseTypeId, EMPLOYMENT,
            caseId, FIX_CASE_API_EVENT_ID);
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        caseData.setStateAPI(CLOSED_STATE);
        boolean updated = updatePreAcceptCase(
            caseId,
            caseTypeId,
            caseData.getEthosCaseReference(),
            caseData.getPreAcceptCase(),
            CasePreAcceptType::getCaseAccepted,
            CasePreAcceptType::getDateAccepted,
            CasePreAcceptType::getDateRejected,
            CasePreAcceptType::getRejectReason,
            CasePreAcceptType::setCaseAccepted,
            CasePreAcceptType::setDateAccepted,
            CasePreAcceptType::setDateRejected,
            CasePreAcceptType::setRejectReason,
            corrections
        );
        if (updated) {
            if (!dryRun) {
                ccdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    caseDetails.getJurisdiction(), ccdRequest, caseId);
            }
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } else {
            log.info("{} - No pre-acceptance fixes needed for case {}", caseTypeId, caseId);
        }
    }

    private void triggerEcmEventForCase(String adminUserToken, String caseId, String caseTypeId,
                                        ConcurrentLinkedQueue<Correction> corrections) throws IOException {
        uk.gov.hmcts.ecm.common.model.ccd.CCDRequest ccdRequest = ecmCcdClient.startEventForCase(adminUserToken,
            caseTypeId, EMPLOYMENT, caseId, FIX_CASE_API_EVENT_ID);
        uk.gov.hmcts.ecm.common.model.ccd.CaseDetails caseDetails = ccdRequest.getCaseDetails();
        uk.gov.hmcts.ecm.common.model.ccd.CaseData caseData = caseDetails.getCaseData();
        caseData.setStateAPI(CLOSED_STATE);
        boolean updated = updatePreAcceptCase(
            caseId,
            caseTypeId,
            caseData.getEthosCaseReference(),
            caseData.getPreAcceptCase(),
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::getCaseAccepted,
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::getDateAccepted,
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::getDateRejected,
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::getRejectReason,
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::setCaseAccepted,
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::setDateAccepted,
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::setDateRejected,
            uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType::setRejectReason,
            corrections
        );
        if (updated) {
            if (!dryRun) {
                ecmCcdClient.submitEventForCase(adminUserToken, caseData, caseTypeId,
                    caseDetails.getJurisdiction(), ccdRequest, caseId);
            }
            log.info("{} - Successfully updated case {}", caseTypeId, caseId);
        } else {
            log.info("{} - No pre-acceptance fixes needed for case {}", caseTypeId, caseId);
        }
    }

    private <P> boolean updatePreAcceptCase(
            String caseId,
            String caseTypeId,
            String ethosCaseReference,
            P preAcceptCase,
            Function<P, String> getCaseAccepted,
            Function<P, String> getDateAccepted,
            Function<P, String> getDateRejected,
            Function<P, List<String>> getRejectReason,
            BiConsumer<P, String> setCaseAccepted,
            BiConsumer<P, String> setDateAccepted,
            BiConsumer<P, String> setDateRejected,
            BiConsumer<P, List<String>> setRejectReason,
            ConcurrentLinkedQueue<Correction> corrections) {
        if (preAcceptCase == null) {
            log.warn("No preAcceptCase found for case {}", caseId);
            return false;
        }

        String previousCaseAccepted = getCaseAccepted.apply(preAcceptCase);
        String dateAccepted = getDateAccepted.apply(preAcceptCase);
        String existingDateRejected = getDateRejected.apply(preAcceptCase);
        List<String> expectedRejectReason = List.of(REJECT_REASON_CASE_INPUT_IN_ERROR);

        if (NO.equals(previousCaseAccepted)
                && isNullOrEmpty(dateAccepted)
                && expectedRejectReason.equals(getRejectReason.apply(preAcceptCase))) {
            return false;
        }

        String dateRejectedToSet = isNullOrEmpty(dateAccepted) ? existingDateRejected : dateAccepted;
        setCaseAccepted.accept(preAcceptCase, NO);
        setDateRejected.accept(preAcceptCase, dateRejectedToSet);
        setDateAccepted.accept(preAcceptCase, null);
        setRejectReason.accept(preAcceptCase, expectedRejectReason);

        corrections.add(new Correction(
            caseId,
            ethosCaseReference,
            caseTypeId,
            previousCaseAccepted,
            dateAccepted,
            dateRejectedToSet,
            REJECT_REASON_CASE_INPUT_IN_ERROR
        ));
        return true;
    }

    private void sendReport(UpdateTracker tracker) {
        if (tracker.corrections().isEmpty() && tracker.failed().isEmpty()) {
            return;
        }
        String csv = "case_id,ethos_case_reference,case_type,previous_case_accepted,date_accepted,"
            + "date_rejected,reject_reason\n"
            + tracker.corrections().stream()
                .map(correction -> String.join(",",
                    correction.caseId(),
                    nullToEmpty(correction.ethosCaseReference()),
                    correction.caseTypeId(),
                    nullToEmpty(correction.previousCaseAccepted()),
                    nullToEmpty(correction.dateAccepted()),
                    nullToEmpty(correction.dateRejected()),
                    correction.rejectReason()))
                .collect(Collectors.joining("\n"));
        try {
            emailService.sendEmail(dataQualityReportTemplateId, serviceOwnerEmail, java.util.Map.of(
                "runMode", dryRun ? "DRY_RUN" : "LIVE",
                "correctionsCount", tracker.corrections().size(),
                "failuresCount", tracker.failed().size(),
                "dataQualityReportCsv", prepareUpload(csv.getBytes(StandardCharsets.UTF_8))));
        } catch (NotificationClientException ex) {
            log.error("Unable to prepare data quality CSV report attachment", ex);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
