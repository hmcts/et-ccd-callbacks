package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_REMOVE_CLAIMANT_REP_AND_ORG_POLICY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_START_REMOVE_CLAIMANT_REP_AND_ORG_POLICY_INVALID_CCD_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_START_REMOVE_CLAIMANT_REP_AND_ORG_POLICY_INVALID_PARAMETERS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_UPDATE_CASE_SUBMITTED;

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

    /**
     * Starts a CCD event to update representation details for the specified case.
     * <p>
     * This method delegates to the CCD client to initiate an update-representation
     * event using the provided authorisation token, case identifiers, and jurisdiction.
     *
     * @param authToken    the authorisation token used to authenticate with CCD
     * @param jurisdiction the jurisdiction of the case for which the event is started
     * @param caseType     the case type identifier
     * @param caseId       the identifier of the case to update representation for
     * @return the {@link CCDRequest} containing the started event details
     * @throws IOException if an error occurs while starting the CCD event
     */
    public CCDRequest startEventForUpdateRepresentation(String authToken,
                                                        String jurisdiction, String caseType,
                                                        String caseId) throws IOException {
        return ccdClient.startEventForUpdateRep(authToken, caseType, jurisdiction, caseId);
    }

    /**
     * Retrieves all user assignments associated with the specified case from CCD.
     * <p>
     * This method delegates to the CCD client to fetch case user assignments using
     * the provided user authorisation token and case identifier.
     * <p>
     * If an I/O error occurs while communicating with CCD, the error is logged and
     * rethrown as a {@link CcdInputOutputException}.
     *
     * @param userToken the user authorisation token used to authenticate with CCD
     * @param caseId    the identifier of the case whose user assignments are to be retrieved
     * @return the {@link CaseUserAssignmentData} containing the case user assignments
     * @throws CcdInputOutputException if an error occurs while retrieving case assignments from CCD
     */
    public CaseUserAssignmentData retrieveCaseUserAssignments(String userToken, String caseId) {
        try {
            return ccdClient.retrieveCaseAssignments(userToken, caseId);
        } catch (IOException exception) {
            LoggingUtils.logCcdErrorMessageAtInfoLevel(exception);
            throw new CcdInputOutputException("Failed to retrieve case assignments", exception);
        }
    }

    /**
     * Finds and returns the first case user assignment for the given case that matches
     * the specified case role.
     * <p>
     * The method retrieves all user assignments associated with the provided case ID
     * and iterates through them to locate an assignment whose case role matches the
     * supplied role.
     * <p>
     * If any input parameter is blank, no assignments are found, or no assignment
     * matches the given role, this method returns {@code null}.
     *
     * @param userToken the user authorisation token used to retrieve case assignments
     * @param caseId    the identifier of the case whose user assignments are to be searched
     * @param role      the case role to match against user assignments
     * @return the first {@link CaseUserAssignment} matching the given role,
     *         or {@code null} if no matching assignment is found
     */
    public CaseUserAssignment findCaseUserAssignmentByRole(String userToken, String caseId, String role) {
        if (StringUtils.isBlank(userToken) || StringUtils.isBlank(caseId) || StringUtils.isBlank(role)) {
            return null;
        }
        CaseUserAssignmentData caseUserAssignmentData = retrieveCaseUserAssignments(userToken, caseId);
        if (ObjectUtils.isEmpty(caseUserAssignmentData)
                || CollectionUtils.isEmpty(caseUserAssignmentData.getCaseUserAssignments())) {
            return null;
        }
        for (CaseUserAssignment caseUserAssignment : caseUserAssignmentData.getCaseUserAssignments()) {
            if (ObjectUtils.isNotEmpty(caseUserAssignment)
                    && StringUtils.isNotBlank(caseUserAssignment.getCaseRole())
                    && role.equals(caseUserAssignment.getCaseRole())) {
                return caseUserAssignment;
            }
        }
        return null;
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
            LoggingUtils.logCcdErrorMessageAtInfoLevel(exception);
            throw new CcdInputOutputException("Failed to revoke case assignments", exception);
        }
    }

    /**
     * Starts a CCD {@code UPDATE_CASE_SUBMITTED} event for the specified case.
     * <p>
     * This method validates the supplied parameters before attempting to start the
     * event. If any required input is blank, or if the returned CCD response is
     * incomplete or invalid, the method returns {@code null}.
     * <p>
     * When successful, the returned {@link CCDRequest} contains fully populated
     * case details and case data required for submitting updates to CCD.
     *
     * @param userToken     the user authorisation token used to authenticate with CCD
     * @param caseTypeId    the case type identifier
     * @param jurisdiction the jurisdiction of the case
     * @param caseId        the identifier of the case for which the event is started
     * @return a populated {@link CCDRequest} for the {@code UPDATE_CASE_SUBMITTED} event,
     *         or {@code null} if validation fails or the CCD response is invalid
     * @throws IOException if an error occurs while communicating with CCD
     */
    public CCDRequest startEventForUpdateCaseSubmitted(String userToken,
                                                       String caseTypeId,
                                                       String jurisdiction,
                                                       String caseId) throws IOException {
        if (StringUtils.isBlank(userToken)
                || StringUtils.isBlank(caseTypeId)
                || StringUtils.isBlank(jurisdiction)
                || StringUtils.isBlank(caseId)) {
            return null;
        }
        CCDRequest ccdRequest = ccdClient.startEventForCase(userToken, caseTypeId, jurisdiction, caseId,
                EVENT_UPDATE_CASE_SUBMITTED);
        if (ObjectUtils.isEmpty(ccdRequest)
                || ObjectUtils.isEmpty(ccdRequest.getCaseDetails())
                || StringUtils.isEmpty(ccdRequest.getCaseDetails().getCaseId())
                || StringUtils.isEmpty(ccdRequest.getCaseDetails().getJurisdiction())
                || StringUtils.isEmpty(ccdRequest.getCaseDetails().getCaseTypeId())
                || ObjectUtils.isEmpty(ccdRequest.getCaseDetails().getCaseData())) {
            return null;
        }
        return ccdRequest;
    }

    /**
     * Removes the claimant representative from the specified case and updates the
     * associated organisation policy accordingly.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Validates the supplied user token and case details</li>
     *   <li>Finds and revokes the claimant solicitor case user assignment, if present</li>
     *   <li>Starts a CCD {@code UPDATE_CASE_SUBMITTED} event</li>
     *   <li>Clears the claimant representative details from the case data</li>
     *   <li>Resets the claimant representative organisation policy</li>
     *   <li>Submits the updated case data back to CCD</li>
     * </ul>
     * <p>
     * If validation fails, the CCD event cannot be started, or an error occurs while
     * communicating with CCD, the method logs the error and exits without propagating
     * the exception to the caller.
     *
     * @param userToken   the user authorisation token used to authenticate with CCD
     * @param caseDetails the case details containing identifiers and case data required
     *                    to remove the claimant representation
     */
    public void removeClaimantRepresentation(String userToken, CaseDetails caseDetails) {
        if (StringUtils.isBlank(userToken)
                || ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isBlank(caseDetails.getCaseId())
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || StringUtils.isBlank(caseDetails.getCaseTypeId())
                || StringUtils.isBlank(caseDetails.getJurisdiction())) {
            String caseId = ObjectUtils.isNotEmpty(caseDetails) && StringUtils.isNotBlank(caseDetails.getCaseId())
                    ? caseDetails.getCaseId() : StringUtils.EMPTY;
            log.error(ERROR_UNABLE_TO_START_REMOVE_CLAIMANT_REP_AND_ORG_POLICY_INVALID_PARAMETERS, caseId);
            return;
        }
        try {
            CaseUserAssignment caseUserAssignment = findCaseUserAssignmentByRole(userToken, caseDetails.getCaseId(),
                    ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
            if (ObjectUtils.isNotEmpty(caseUserAssignment)) {
                CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().caseUserAssignments(
                        List.of(caseUserAssignment)).build();
                revokeCaseAssignments(userToken, caseUserAssignmentData);
            }
            CCDRequest ccdRequest = startEventForUpdateCaseSubmitted(userToken,
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    caseDetails.getCaseId());
            if (ObjectUtils.isEmpty(ccdRequest)) {
                log.error(ERROR_UNABLE_TO_START_REMOVE_CLAIMANT_REP_AND_ORG_POLICY_INVALID_CCD_REQUEST,
                        caseDetails.getCaseId());
                return;
            }
            CaseDetails ccdRequestCaseDetails = ccdRequest.getCaseDetails();
            ccdRequestCaseDetails.getCaseData().setRepresentativeClaimantType(null);
            ccdRequestCaseDetails.getCaseData().setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder()
                    .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
            ccdClient.submitEventForCase(userToken, ccdRequestCaseDetails.getCaseData(),
                    ccdRequestCaseDetails.getCaseTypeId(),
                    ccdRequestCaseDetails.getJurisdiction(),
                    ccdRequest,
                    ccdRequestCaseDetails.getCaseId());
        } catch (IOException | CcdInputOutputException exception) {
            log.error(ERROR_FAILED_TO_REMOVE_CLAIMANT_REP_AND_ORG_POLICY, caseDetails.getCaseId(),
                    exception.getMessage(), exception);
        }
    }
}
