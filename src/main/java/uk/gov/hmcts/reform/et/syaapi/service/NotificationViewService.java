package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications.NotificationViews;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.NotificationViewRepository;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Records the items a claimant has viewed in the notification_view table, which the case view merges back into the
 * case data.
 */
@Service
@RequiredArgsConstructor
public class NotificationViewService {

    static final String EVENT_ID = "UPDATE_NOTIFICATION_STATE";
    static final String EVENT_NAME = "Respond to a notification";

    private final CitizenSystemEventService citizenSystemEventService;
    private final NotificationViewRepository notificationViewRepository;

    /**
     * Marks items viewed for the claimant.
     *
     * @param authorization the claimant's bearer token
     * @param caseId        the case reference
     * @param caseTypeId    the case type
     * @param markViewed    marks the viewed items in the case data the claimant can see
     * @return the case as the claimant can see it, with the items marked viewed
     */
    public CaseDetails recordViews(String authorization, String caseId, String caseTypeId,
                                   Consumer<CaseData> markViewed) {
        CitizenSystemEventService.CitizenCase citizenCase =
            citizenSystemEventService.readCase(authorization, caseId, caseTypeId);
        CaseDetails caseDetails = citizenCase.caseDetails();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        markViewed.accept(caseData);
        Set<String> viewedIds = NotificationViews.viewedItemIds(caseData);

        citizenSystemEventService.record(citizenCase, EVENT_ID, EVENT_NAME, caseReference -> {
            Set<String> alreadyViewed = notificationViewRepository.findItemIds(caseReference);
            viewedIds.stream()
                .filter(id -> !alreadyViewed.contains(id))
                .forEach(id -> notificationViewRepository.markViewed(caseReference, id));
        });

        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        return caseDetails;
    }
}
