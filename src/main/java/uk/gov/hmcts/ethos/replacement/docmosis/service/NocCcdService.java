package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocCcdService {
    private final CcdClient ccdClient;

    public Optional<AuditEvent> getLatestAuditEventByName(String authToken, String caseId, String eventName)
        throws IOException {
        AuditEventsResponse auditEventsResponse = ccdClient.retrieveCaseEvents(authToken, caseId);

        return auditEventsResponse.getAuditEvents().stream()
            .filter(auditEvent -> eventName.equals(auditEvent.getId()))
            .max(Comparator.comparing(AuditEvent::getCreatedDate));
    }

    public void updateCaseRepresentation(String authToken, ChangeOrganisationRequest changeRequest,
                                         String jurisdiction, String caseType,
                                         String caseId) throws IOException {
        CCDRequest ccdRequest = ccdClient.startEventForUpdateRep(
            authToken,
            caseType,
            jurisdiction,
            caseId);

        ccdClient.submitUpdateRepEvent(
            authToken,
            Map.of("changeOrganisationRequestField", changeRequest),
            caseType,
            jurisdiction,
            ccdRequest,
            caseId);
    }

    /**
     * Gets all case assignments for a given case id.
     * @param userToken - bearer token
     * @param caseId - ccd case id
     * @return list of case assignments for given case id
     */
    public CaseUserAssignmentData getCaseAssignments(String userToken, String caseId) {
        try {
            return ccdClient.retrieveCaseAssignments(userToken, caseId);
        } catch (IOException exception) {
            log.info("Error form ccd - {}", exception.getMessage());
            throw new RuntimeException(exception);
        }
    }

    /**
     * Revokes access to case for given users.
     * @param userToken - bearer token
     * @param caseUserAssignmentData - containing list of user id, case id and role id mappings for removal
     */
    public void revokeCaseAssignments(String userToken, CaseUserAssignmentData caseUserAssignmentData) {
        try {
            ccdClient.revokeCaseAssignments(userToken, caseUserAssignmentData);
        } catch (IOException exception) {
            log.info("Error form ccd - {}", exception.getMessage());
            throw new RuntimeException(exception);
        }
    }
}
