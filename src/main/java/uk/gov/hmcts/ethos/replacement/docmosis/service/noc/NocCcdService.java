package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RepresentativeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REMAINING_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND_BY_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_LEAD;

@Service
@RequiredArgsConstructor
public class NocCcdService {
    private final CcdClient ccdClient;
    private final UserIdamService userIdamService;
    private final AdminUserService adminUserService;
    private final UserService userService;

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
     * Retrieves all case user assignments associated with the specified case.
     *
     * <p>If {@code caseId} is blank, or if no assignments are found,
     * an empty mutable list is returned.</p>
     *
     * @param caseId the identifier of the case
     * @return a mutable list of {@link CaseUserAssignment} objects associated with the case,
     *         or an empty mutable list if none are found
     */
    public List<CaseUserAssignment> findCaseUserAssignmentsByCaseId(String caseId) {
        if (StringUtils.isBlank(caseId)) {
            return new ArrayList<>();
        }
        CaseUserAssignmentData caseUserAssignmentData =
                retrieveCaseUserAssignments(adminUserService.getAdminUserToken(), caseId);

        if (caseUserAssignmentData == null
                || CollectionUtils.isEmpty(caseUserAssignmentData.getCaseUserAssignments())) {
            return new ArrayList<>();
        }
        return new ArrayList<>(caseUserAssignmentData.getCaseUserAssignments());
    }

    /**
     * Retrieves case user assignments for the specified case that match the given case role.
     *
     * <p>If either {@code caseId} or {@code role} is blank, or if no assignments are found,
     * an empty mutable list is returned.</p>
     *
     * @param caseId the identifier of the case
     * @param role the case role to filter assignments by
     * @return a mutable list of matching {@link CaseUserAssignment} objects, or an empty mutable list if none are found
     */
    public List<CaseUserAssignment> findCaseUserAssignmentsByCaseIdAndRole(String caseId, String role) {
        if (StringUtils.isBlank(role)) {
            return new ArrayList<>();
        }
        return findCaseUserAssignmentsByCaseId(caseId).stream()
                .filter(Objects::nonNull)
                .filter(assignment -> role.equals(assignment.getCaseRole()))
                .collect(Collectors.toCollection(ArrayList::new));
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
     * Revokes the claimant solicitor's case role assignment for the given case,
     * if such an assignment exists.
     *
     * <p>This method performs the following steps:
     * <ul>
     *     <li>Returns immediately if the provided {@code userToken} is blank.</li>
     *     <li>Attempts to locate a {@link CaseUserAssignment} for the
     *         {@link ClaimantSolicitorRole#CLAIMANTSOLICITOR} role
     *         associated with the given case ID.</li>
     *     <li>If no matching assignment is found, the method exits without action.</li>
     *     <li>If an assignment is found, it is revoked via {@code revokeCaseAssignments}.</li>
     * </ul>
     *
     * <p>No action is taken if the user token is invalid or the claimant solicitor
     * role is not currently assigned to the case.
     *
     * @param userToken   the authorisation token of the user performing the operation;
     *                    must not be blank
     * @param caseDetails the case details containing the case ID from which the
     *                    claimant solicitor role should be revoked
     */
    public void revokeClaimantRepresentation(String userToken, CaseDetails caseDetails) throws GenericServiceException {
        if (StringUtils.isBlank(userToken)) {
            return;
        }
        if (Strings.CS.equals(NOC_REMOVE_OPTION_YOURSELF, caseDetails.getCaseData().getNocRemoveOption())) {
            revokeUserClaimantRepresentation(userToken, caseDetails);
            return;
        }
        List<CaseUserAssignment> caseUserAssignments = findCaseUserAssignmentsByCaseIdAndRole(caseDetails.getCaseId(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        if (CollectionUtils.isEmpty(caseUserAssignments)) {
            return;
        }
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().caseUserAssignments(
                caseUserAssignments).build();
        revokeCaseAssignments(adminUserService.getAdminUserToken(), caseUserAssignmentData);
    }

    /**
     * Revokes the claimant solicitor case assignment associated with the user
     * identified by the supplied authentication token.
     *
     * <p>If {@code userToken} is blank, or if no matching claimant solicitor
     * assignment exists for the user and case, the method returns without making
     * any changes.</p>
     *
     * <p>If the user details cannot be resolved from the supplied token, or the
     * resolved user does not contain a valid IDAM user ID, a
     * {@link GenericServiceException} is thrown.</p>
     *
     * @param userToken the authentication token used to identify the user
     * @param caseDetails the case whose claimant representation should be revoked
     * @throws GenericServiceException if the user cannot be identified from the supplied token
     */
    public void revokeUserClaimantRepresentation(String userToken, CaseDetails caseDetails)
            throws GenericServiceException {
        if (StringUtils.isBlank(userToken)) {
            return;
        }
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        final String methodName = "revokeUserClaimantRepresentation";
        if (userDetails == null || StringUtils.isBlank(userDetails.getUid())) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND_BY_TOKEN,
                    caseDetails.getCaseId());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseDetails.getCaseId(), NocRemoveClaimantRepresentationService.class.getSimpleName(), methodName);
        }
        List<CaseUserAssignment> caseUserAssignments = findCaseUserAssignmentsByCaseIdAndRole(
                caseDetails.getCaseId(), ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        List<CaseUserAssignment> userAssignments = RepresentativeUtils.findCaseUserAssignmentsByIdamId(
                caseUserAssignments, userDetails.getUid());
        if (CollectionUtils.isEmpty(userAssignments)) {
            return;
        }
        reassignLeadClaimantRepresentative(caseDetails, caseUserAssignments, userDetails);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().caseUserAssignments(
                userAssignments).build();
        revokeCaseAssignments(adminUserService.getAdminUserToken(), caseUserAssignmentData);
    }

    /**
     * Reassigns the lead claimant representative to the first remaining representative.
     *
     * <p>Reassignment is performed only when the case's NOC representative type is
     * {@code NOC_REPRESENTATIVE_TYPE_LEAD}. Otherwise, this method returns without
     * making changes.</p>
     *
     * <p>Excludes the specified user's assignment when selecting a replacement,
     * retrieves the remaining representative's user details from IDAM, and updates
     * the claimant representative's IDAM ID, email address, and full name in the
     * case data.</p>
     *
     * @param caseDetails the case details containing the claimant representative
     *                    information to update
     * @param caseUserAssignments the assignments used to find a remaining claimant
     *                            representative; may be {@code null} or empty
     * @param userDetails the user whose assignment is excluded when selecting
     *                    a replacement
     * @throws GenericServiceException if reassignment is required but no remaining
     *                                 representative is found, including when the
     *                                 assignment list is null or empty
     */
    public void reassignLeadClaimantRepresentative(CaseDetails caseDetails,
                                                   List<CaseUserAssignment> caseUserAssignments,
                                                   UserDetails userDetails) throws GenericServiceException {
        if (!Strings.CS.equals(NOC_REPRESENTATIVE_TYPE_LEAD, caseDetails.getCaseData().getNocRepresentativeType())) {
            return;
        }
        UserDetails remainingRepresentative = findRemainingClaimantRepresentative(caseUserAssignments, userDetails);
        if (remainingRepresentative == null) {
            String exceptionMessage = String.format(EXCEPTION_REMAINING_REPRESENTATIVE_NOT_FOUND,
                    caseDetails.getCaseId());
            final String methodName = "reassignLeadClaimantRepresentative";
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseDetails.getCaseId(), NocRemoveClaimantRepresentationService.class.getSimpleName(),
                    methodName);
        }
        caseDetails.getCaseData().getRepresentativeClaimantType().setIdamId(remainingRepresentative.getUid());
        caseDetails.getCaseData().getRepresentativeClaimantType().setRepresentativeEmailAddress(
                remainingRepresentative.getEmail());
        caseDetails.getCaseData().getRepresentativeClaimantType().setNameOfRepresentative(
                UserUtils.getFullName(remainingRepresentative));
    }

    /**
     * Finds the user details of the first remaining claimant representative after
     * excluding the existing user's assignment by UID.
     *
     * <p>If a remaining assignment is found, retrieves the corresponding user
     * details from IDAM. Returns {@code null} if the assignment list is null or
     * empty, or if no assignment remains after excluding the existing user.</p>
     *
     * @param caseUserAssignments the case user assignments; may be {@code null} or empty
     * @param existingUserDetails the user whose assignment is to be excluded;
     *                            must not be {@code null} when the assignment list is non-empty
     * @return the user details for the first remaining assignment, or {@code null}
     *         if the assignment list is null or empty, or no assignment remains
     * @throws NullPointerException if {@code existingUserDetails} is {@code null}
     *                              and the assignment list is non-empty
     */
    public UserDetails findRemainingClaimantRepresentative(List<CaseUserAssignment> caseUserAssignments,
                                                           UserDetails existingUserDetails) {
        if (CollectionUtils.isEmpty(caseUserAssignments)) {
            return null;
        }
        String userId = ObjectUtils.isEmpty(existingUserDetails) ? null : existingUserDetails.getUid();
        CaseUserAssignment caseUserAssignment = ClaimantRepresentativeUtils
                .findFirstAssignmentForOtherRepresentative(caseUserAssignments, userId);
        if (caseUserAssignment == null) {
            return null;
        }
        return userService.findUserDetailsByIdamId(caseUserAssignment.getUserId());
    }
}
