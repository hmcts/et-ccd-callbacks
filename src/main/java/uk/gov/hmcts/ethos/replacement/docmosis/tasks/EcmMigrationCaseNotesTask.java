package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EcmMigrationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EcmMigrationCaseNotesTask implements Runnable {

    private static final String FIX_CASE_API = "fixCaseAPI";
    private static final Set<String> VALID_ECM_CASE_TYPES = Set.of("Newcastle", "MidlandsEast", "Wales");
    private static final Pattern CASE_ID_PATTERN = Pattern.compile("\\d{16}");

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${cron.ecmMigrationCaseNoteIds:}")
    private String ecmCaseIds;

    @Value("${cron.ecmMigrationCaseNotesDryRun:true}")
    private boolean dryRun;

    @Override
    public void run() {
        log.info("ECM migration case-notes task started; dryRun={}", dryRun);

        List<String> configuredCaseIds = parseCaseIds(ecmCaseIds);
        if (configuredCaseIds.isEmpty()) {
            log.info("No ECM case IDs configured for the migration case-notes task");
            return;
        }

        String adminUserToken = adminUserService.getAdminUserToken();
        TaskSummary summary = new TaskSummary();
        configuredCaseIds.forEach(ecmCaseId -> processCase(adminUserToken, ecmCaseId, summary));

        log.info("ECM migration case-notes task completed: updated={}, skipped={}, failed={}",
                summary.updatedCaseIds, summary.skippedCaseIds, summary.failedCaseIds);
    }

    private void processCase(String adminUserToken, String ecmCaseId, TaskSummary summary) {
        try {
            if (!CASE_ID_PATTERN.matcher(ecmCaseId).matches()) {
                summary.failedCaseIds.add(ecmCaseId);
                log.warn("Skipping invalid ECM case ID {}", ecmCaseId);
                return;
            }

            var ecmCaseDetails = coreCaseDataApi.getCase(adminUserToken, authTokenGenerator.generate(), ecmCaseId);
            String ecmCaseType = ecmCaseDetails.getCaseTypeId();
            if (!VALID_ECM_CASE_TYPES.contains(ecmCaseType)) {
                summary.skippedCaseIds.add(ecmCaseId);
                log.warn("Skipping ECM case {} with unsupported case type {}", ecmCaseId, ecmCaseType);
                return;
            }

            var ecmRequest = ccdClient.startEventForEcmCase(
                    adminUserToken, ecmCaseType, EMPLOYMENT, ecmCaseId, FIX_CASE_API);
            uk.gov.hmcts.ecm.common.model.ccd.CaseData ecmCaseData = ecmRequest.getCaseDetails().getCaseData();
            List<GenericTypeItem<CaseNote>> ecmCaseNotes = ecmCaseData.getCaseNotesCollection();
            if (CollectionUtils.isEmpty(ecmCaseNotes)) {
                summary.skippedCaseIds.add(ecmCaseId);
                log.info("Skipping ECM case {} because it has no case notes", ecmCaseId);
                return;
            }

            String reformCaseId = EcmMigrationService.getCaseIdFromLink(ecmCaseData.getReformCaseLink());
            updateReformCase(adminUserToken, reformCaseId, ecmCaseNotes, summary);
        } catch (Exception exception) {
            summary.failedCaseIds.add(ecmCaseId);
            log.error("Failed to copy notes from ECM case {}", ecmCaseId, exception);
        }
    }

    private void updateReformCase(String adminUserToken, String reformCaseId,
                                  List<GenericTypeItem<CaseNote>> ecmCaseNotes, TaskSummary summary)
            throws IOException {
        CCDRequest reformRequest = ccdClient.startEventForCase(adminUserToken, ENGLANDWALES_CASE_TYPE_ID,
                EMPLOYMENT, reformCaseId, FIX_CASE_API);
        CaseDetails reformCaseDetails = reformRequest.getCaseDetails();
        CaseData reformCaseData = reformCaseDetails.getCaseData();
        List<uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<uk.gov.hmcts.et.common.model.ccd.types.CaseNote>>
                mergedNotes = mergeCaseNotes(ecmCaseNotes, reformCaseData.getCaseNotesCollection());

        if (mergedNotes.equals(reformCaseData.getCaseNotesCollection())) {
            summary.skippedCaseIds.add(reformCaseId);
            log.info("Skipping Reform case {} because its case notes are already up to date", reformCaseId);
            return;
        }

        if (!dryRun) {
            reformCaseData.setStateAPI(null); // Clear state field to avoid changing the state of the case
            reformCaseData.setCaseNotesCollection(mergedNotes);
            ccdClient.submitEventForCase(adminUserToken, reformCaseData, reformCaseDetails.getCaseTypeId(),
                    reformCaseDetails.getJurisdiction(), reformRequest, reformCaseId);
        }
        summary.updatedCaseIds.add(reformCaseId);
        log.info("{} ECM case notes for Reform case {}", dryRun ? "Would update" : "Updated", reformCaseId);
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

    static List<uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<uk.gov.hmcts.et.common.model.ccd.types.CaseNote>>
            mergeCaseNotes(List<GenericTypeItem<CaseNote>> ecmCaseNotes,
                           List<uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<
                                   uk.gov.hmcts.et.common.model.ccd.types.CaseNote>> reformCaseNotes) {
        Map<String, uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem<
            uk.gov.hmcts.et.common.model.ccd.types.CaseNote>>
                notesById = new LinkedHashMap<>();

        for (GenericTypeItem<CaseNote> ecmCaseNote : ecmCaseNotes) {
            CaseNote value = ecmCaseNote.getValue();
            uk.gov.hmcts.et.common.model.ccd.types.CaseNote reformCaseNote =
                    uk.gov.hmcts.et.common.model.ccd.types.CaseNote.builder()
                            .title(value.getTitle())
                            .note(value.getNote())
                            .author(value.getAuthor())
                            .date(value.getDate())
                            .build();
            notesById.putIfAbsent(ecmCaseNote.getId(),
                    uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem.from(ecmCaseNote.getId(), reformCaseNote));
        }

        if (CollectionUtils.isNotEmpty(reformCaseNotes)) {
            reformCaseNotes.forEach(note -> notesById.putIfAbsent(note.getId(), note));
        }

        return new ArrayList<>(notesById.values());
    }

    private static class TaskSummary {
        private final List<String> updatedCaseIds = new ArrayList<>();
        private final List<String> skippedCaseIds = new ArrayList<>();
        private final List<String> failedCaseIds = new ArrayList<>();
    }
}
