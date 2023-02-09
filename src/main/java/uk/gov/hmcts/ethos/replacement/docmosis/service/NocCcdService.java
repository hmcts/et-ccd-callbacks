package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
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
}
