package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchReconfigurationTask implements Runnable {
    private static final String LONDON_CENTRAL_VENUE = "London Central";
    private static final String LONDON_TRIBUNALS_CENTRE_VENUE = "London Tribunals Centre";
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
            DynamicFixedListType venueDay = DynamicFixedListType.from(LONDON_TRIBUNALS_CENTRE_VENUE,
                LONDON_TRIBUNALS_CENTRE_VENUE, true);
            CCDRequest returnedRequest = ccdClient.startEventForCase(adminUserToken, caseTypeIdsString, EMPLOYMENT,
                    caseId, "RECONFIGURE_WA_TASKS");
            CaseData caseData = returnedRequest.getCaseDetails().getCaseData();
            emptyIfNull(caseData.getHearingCollection()).stream()
                .filter(hearing -> !ObjectUtils.isEmpty(hearing))
                .map(HearingTypeItem::getValue)
                .forEach(hearingItem -> hearingItem.getHearingDateCollection().stream()
                    .map(DateListedTypeItem::getValue)
                    .filter(BatchReconfigurationTask::isValidVenue)
                    .forEach(hearingDateCollection -> {
                        hearingDateCollection.setHearingVenueDay(venueDay);
                        hearingItem.setHearingVenue(venueDay);
                    }));

            ccdClient.submitEventForCase(adminUserToken, returnedRequest.getCaseDetails().getCaseData(),
                    caseTypeIdsString, returnedRequest.getCaseDetails().getJurisdiction(), returnedRequest,
                    caseId);
            log.info("Reconfiguration event triggered for case {}", caseId);
        } catch (Exception e) {
            log.error(ERROR_TRIGGERING_RECONFIGURATION_EVENT_FOR_CASE, caseId, e.getMessage());
        }
    }

    private static boolean isValidVenue(DateListedType hearingDateCollection) {
        return LONDON_CENTRAL_VENUE.equals(
            defaultIfEmpty(hearingDateCollection.getHearingVenueDay().getSelectedCode(), "venue"))
               && LocalDateTime.parse(hearingDateCollection.getListedDate()).isAfter(LocalDateTime.now())
               && HEARING_STATUS_LISTED.equals(hearingDateCollection.getHearingStatus());
    }
}
