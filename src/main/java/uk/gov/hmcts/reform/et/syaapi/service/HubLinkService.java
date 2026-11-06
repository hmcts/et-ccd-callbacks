package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;

@RequiredArgsConstructor
@Service
public class HubLinkService {

    static final String EVENT_ID = "UPDATE_HUBLINK_STATUS";
    static final String EVENT_NAME = "Update hublink status";

    private final CitizenSystemEventService citizenSystemEventService;
    private final HubLinkStatusRepository hubLinkStatusRepository;

    /**
     * Stores the citizen's hub link statuses in the hub_link_status table.
     *
     * @param request       hub link status data
     * @param authorization the citizen's bearer token
     * @return the case as the citizen can see it, with the new statuses
     */
    public CaseDetails updateHubLinkStatuses(HubLinksStatusesRequest request,
                                             String authorization) {
        if (request.getHubLinksStatuses() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hub-link statuses are required");
        }
        CitizenSystemEventService.CitizenCase citizenCase =
            citizenSystemEventService.readCase(authorization, request.getCaseId(), request.getCaseTypeId());

        // Intentionally last write wins mirroring the SYA frontend client.
        // Do not copy this pattern for data requiring concurrency protection.
        citizenSystemEventService.record(citizenCase, EVENT_ID, EVENT_NAME, caseReference ->
            hubLinkStatusRepository.save(HubLinkStatus.create(caseReference, request.getHubLinksStatuses())));

        CaseDetails caseDetails = citizenCase.caseDetails();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.setHubLinksStatuses(request.getHubLinksStatuses());
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        return caseDetails;
    }
}
