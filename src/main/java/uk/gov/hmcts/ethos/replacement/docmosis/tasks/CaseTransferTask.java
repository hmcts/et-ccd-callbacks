package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

/**
 * Scheduled task that transfers ET Reform cases back to ECM using the {@code caseTransferECM} event.
 *
 * <p>Configure cases via {@code cron.reconfigurationCaseIds} as comma-separated pairs of 16-digit
 * CCD case ID and target ECM case type (or office name), for example:
 * {@code 1234567890123456,MidlandsWest,9876543210987654,MidlandsWest}.</p>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CaseTransferTask implements Runnable {

    static final String CASE_TRANSFER_TO_ECM_EVENT_ID = "caseTransferECM";
    static final String REASON_FOR_CT = "Multiple";

    private static final Pattern CASE_ID_PATTERN = Pattern.compile("\\d{16}");
    private static final Map<String, String> ECM_OFFICE_MAP = buildEcmOfficeMap();

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${cron.reconfigurationCaseIds}")
    private String casesToUpdate;

    public record CaseTransferDetails(String caseId, String targetCaseType) {}

    @Override
    public void run() {
        log.info("Running Case Transfer Task");
        if (isNullOrEmpty(casesToUpdate) || casesToUpdate.isBlank()) {
            log.info("No cases to transfer");
            return;
        }

        String[] tokens = Arrays.stream(casesToUpdate.split(",", -1))
            .map(String::strip)
            .toArray(String[]::new);
        if (tokens.length % 2 != 0) {
            log.error("Invalid case ids format. Expected format: caseId1,targetCaseType1,"
                + "caseId2,targetCaseType2,...");
            return;
        }

        List<CaseTransferDetails> caseTransferDetailsList =
            IntStream.iterate(0, idx -> idx < tokens.length, idx -> idx + 2)
                .mapToObj(idx -> new CaseTransferDetails(tokens[idx], tokens[idx + 1]))
                .toList();

        if (caseTransferDetailsList.isEmpty()) {
            log.info("No cases to transfer");
            return;
        }
        log.info("Number of cases to transfer - {}", caseTransferDetailsList.size());

        String adminUserToken = adminUserService.getAdminUserToken();
        AtomicInteger counter = new AtomicInteger(0);
        ConcurrentLinkedQueue<String> updatedCases = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> failedCases = new ConcurrentLinkedQueue<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            caseTransferDetailsList.forEach(details ->
                executor.submit(() -> transferCase(adminUserToken, details, counter, updatedCases, failedCases)));
        }

        log.info("Completed transfer of {} cases", counter.get());
        log.info("Updated cases: {}", String.join(",", updatedCases));
        log.info("Failed cases: {}", String.join(",", failedCases));
    }

    private void transferCase(String adminUserToken,
                              CaseTransferDetails details,
                              AtomicInteger counter,
                              ConcurrentLinkedQueue<String> updatedCases,
                              ConcurrentLinkedQueue<String> failedCases) {
        if (!CASE_ID_PATTERN.matcher(details.caseId()).matches()) {
            log.error("Invalid 16-digit CCD case id: {}", details.caseId());
            failedCases.add(details.caseId());
            return;
        }

        String ecmOffice = resolveEcmOffice(details.targetCaseType());
        if (ecmOffice == null) {
            log.error("Invalid target case type {} for case {}", details.targetCaseType(), details.caseId());
            failedCases.add(details.caseId());
            return;
        }

        log.info("Transferring case {} to ECM office {} (target case type {})",
            details.caseId(), ecmOffice, details.targetCaseType());
        try {
            var caseDetails = coreCaseDataApi.getCase(
                adminUserToken, authTokenGenerator.generate(), details.caseId());
            if (caseDetails == null || isNullOrEmpty(caseDetails.getCaseTypeId())) {
                log.error("Unable to resolve source case type for case {}", details.caseId());
                failedCases.add(details.caseId());
                return;
            }

            String sourceCaseTypeId = caseDetails.getCaseTypeId();
            if (!ENGLANDWALES_CASE_TYPE_ID.equals(sourceCaseTypeId)
                && !SCOTLAND_CASE_TYPE_ID.equals(sourceCaseTypeId)) {
                log.error("Case {} has unsupported source case type {}", details.caseId(), sourceCaseTypeId);
                failedCases.add(details.caseId());
                return;
            }

            String jurisdiction = isNullOrEmpty(caseDetails.getJurisdiction())
                ? EMPLOYMENT
                : caseDetails.getJurisdiction();
            CCDRequest ccdRequest = ccdClient.startEventForCase(
                adminUserToken,
                sourceCaseTypeId,
                jurisdiction,
                details.caseId(),
                CASE_TRANSFER_TO_ECM_EVENT_ID);
            if (ccdRequest == null
                || ccdRequest.getCaseDetails() == null
                || ccdRequest.getCaseDetails().getCaseData() == null) {
                log.error("CCD returned empty case data for case {}", details.caseId());
                failedCases.add(details.caseId());
                return;
            }

            CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
            caseData.setEcmOfficeCT(ecmOffice);
            caseData.setReasonForCT(REASON_FOR_CT);
            clearOpenBfActions(caseData);

            String submitJurisdiction = isNullOrEmpty(ccdRequest.getCaseDetails().getJurisdiction())
                ? jurisdiction
                : ccdRequest.getCaseDetails().getJurisdiction();
            ccdClient.submitEventForCase(
                adminUserToken,
                caseData,
                sourceCaseTypeId,
                submitJurisdiction,
                ccdRequest,
                details.caseId());

            log.info("Case {} transferred to ECM ({}) successfully", details.caseId(), ecmOffice);
            counter.incrementAndGet();
            updatedCases.add(details.caseId());
        } catch (Exception ex) {
            log.error("Error transferring case {} to target case type {}: {}",
                details.caseId(), details.targetCaseType(), ex.getMessage(), ex);
            failedCases.add(details.caseId());
        }
    }

    private static void clearOpenBfActions(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getBfActions())) {
            caseData.getBfActions().stream()
                .map(BFActionTypeItem::getValue)
                .filter(Objects::nonNull)
                .filter(value -> isNullOrEmpty(value.getCleared()))
                .forEach(value -> value.setCleared(LocalDate.now().toString()));
        }
    }

    private static String resolveEcmOffice(String targetCaseType) {
        if (isNullOrEmpty(targetCaseType)) {
            return null;
        }
        return ECM_OFFICE_MAP.get(targetCaseType.strip().toLowerCase(Locale.ROOT));
    }

    private static Map<String, String> buildEcmOfficeMap() {
        Map<String, String> map = new HashMap<>();
        Stream.concat(TribunalOffice.ENGLANDWALES_OFFICES.stream(), TribunalOffice.SCOTLAND_OFFICES.stream())
            .forEach(office -> {
                String officeName = office.getOfficeName();
                map.put(officeName.toLowerCase(Locale.ROOT), officeName);
                map.put(deleteWhitespace(officeName).toLowerCase(Locale.ROOT), officeName);
            });
        map.put(TribunalOffice.SCOTLAND.getOfficeName().toLowerCase(Locale.ROOT),
            TribunalOffice.GLASGOW.getOfficeName());
        return Map.copyOf(map);
    }
}
