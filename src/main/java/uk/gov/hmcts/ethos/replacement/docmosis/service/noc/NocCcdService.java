package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;

import java.io.IOException;
import java.util.Comparator;
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

    public CCDRequest startEventForUpdateRepresentation(String authToken,
                                                        String jurisdiction, String caseType,
                                                        String caseId) throws IOException {
        return ccdClient.startEventForUpdateRep(
            authToken,
            caseType,
            jurisdiction,
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
            LoggingUtils.logCCDException(exception);
            throw new CcdInputOutputException("Failed to retrieve case assignments", exception);
        }
    }

    /**
     * Revokes case assignments for a user in CCD.
     * <p>
     * This method delegates to the CCD client to revoke the provided case user
     * assignments. If an I/O error occurs while communicating with CCD, the
     * exception is logged and rethrown as a {@link CcdInputOutputException}.
     *
     * @param userToken the user authentication token used to authorise the CCD request
     * @param caseUserAssignmentData the case user assignment details to be revoked
     * @throws CcdInputOutputException if an I/O error occurs while revoking the case assignments
     */
    public void revokeCaseAssignments(String userToken, CaseUserAssignmentData caseUserAssignmentData) {
        try {
            ccdClient.revokeCaseAssignments(userToken, caseUserAssignmentData);
        } catch (IOException exception) {
            LoggingUtils.logCCDException(exception);
            throw new CcdInputOutputException("Failed to revoke case assignments", exception);
        }
    }
}
