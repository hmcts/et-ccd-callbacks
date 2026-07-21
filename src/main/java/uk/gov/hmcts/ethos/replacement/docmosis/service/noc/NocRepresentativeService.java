package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CallbacksCollectionUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_ROLES_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.SYSTEM_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocRepresentativeService {

    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final NocClaimantRepresentativeService nocClaimantRepresentativeService;
    private final CcdCaseAssignment ccdCaseAssignment;
    private final UserService userService;

    private static final String CLASS_NAME = NocRepresentativeService.class.getSimpleName();

    /**
     * Updates legal representation details for a case following a Notice of Change request.
     * <p>
     * The method validates the change request and updates either claimant or respondent
     * representation depending on the selected case role.
     *
     * <p>
     * If the role is a claimant solicitor role:
     * <ul>
     *     <li>Updates claimant representation details</li>
     *     <li>Revokes respondent representatives belonging to the same organisation
     *     as the claimant representative</li>
     * </ul>
     *
     * <p>
     * If the role is a respondent solicitor role:
     * <ul>
     *     <li>Updates respondent representation details</li>
     *     <li>Prepopulates organisation address details</li>
     *     <li>Removes conflicting claimant representation where applicable</li>
     * </ul>
     *
     * <p><strong>Assumptions:</strong></p>
     * <ul>
     *     <li>The supplied case details contain valid case data</li>
     *     <li>The change request and case role information have been populated</li>
     *     <li>The user token belongs to an authenticated and authorised user</li>
     *     <li>Representation conflicts are resolved by removing or revoking
     *     conflicting representatives</li>
     * </ul>
     *
     * @param caseDetails the case details containing the case data and change request information
     * @param userToken the authenticated user token used for organisation and representation updates
     * @return the updated case data containing the latest representation details
     * @throws IOException if an error occurs while retrieving or updating representation data
     */
    public CaseData updateRepresentation(CaseDetails caseDetails, String userToken) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        ChangeOrganisationRequest change = validateChangeRequest(caseData);
        DynamicFixedListType caseRoleId = change.getCaseRoleId();

        if (caseRoleId.getValue().getCode().equals(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())) {
            caseData = nocClaimantRepresentativeService.updateClaimantRepresentation(caseDetails, userToken);
            caseDetails.setCaseData(caseData);
            nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(
                    caseDetails);
        } else {
            caseData = nocRespondentRepresentativeService.updateRespondentRepresentation(caseDetails);
            caseData = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, userToken);
            caseDetails.setCaseData(caseData);
            caseData = nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails);
        }
        return caseData;
    }

    private ChangeOrganisationRequest validateChangeRequest(CaseData caseData) {
        ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();
        if (Objects.isNull(change)
                || Objects.isNull(change.getCaseRoleId())
                || Objects.isNull(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }
        return change;
    }

    /**
     * Retrieves all representative case roles associated with the user identified
     * by the provided user token for the specified submission reference.
     * <p>
     * This method:
     * <ul>
     *     <li>Retrieves and validates the case user assignments for the case.</li>
     *     <li>Retrieves and validates the user details associated with the user token.</li>
     *     <li>Finds all case roles directly assigned to the user.</li>
     *     <li>Finds additional case roles based on the first assignment grouped by solicitor role.</li>
     *     <li>Merges both role collections while removing duplicate roles.</li>
     * </ul>
     * <p>
     * Assumptions:
     * <ul>
     *     <li>The user token belongs to a valid authenticated user.</li>
     *     <li>The submission reference uniquely identifies a case.</li>
     *     <li>The case contains valid case user assignments.</li>
     *     <li>A representative may hold multiple case roles.</li>
     *     <li>Role ordering is preserved based on first occurrence during merge.</li>
     * </ul>
     *
     * @param userToken the authentication token associated with the user
     * @param submissionReference the unique reference identifying the submission/case
     * @return a list of unique representative case roles associated with the user
     * @throws GenericServiceException if:
     *         <ul>
     *             <li>case user assignments cannot be retrieved or validated,</li>
     *             <li>user details cannot be retrieved or validated, or</li>
     *             <li>no representative roles are found for the user</li>
     *         </ul>
     */
    public List<String> getValidatedRepresentativeRolesByUserToken(String userToken,
                                                                   String submissionReference)
            throws GenericServiceException {

        final String methodName = "getValidatedRepresentativeRolesByUserToken";
        log.info("************** Starting to getValidatedRepresentativeRolesByUserToken");
        List<CaseUserAssignment> caseUserAssignments = getValidatedCaseUserAssignments(submissionReference);
        log.info("********** Validated Representative Roles: " + caseUserAssignments);
        UserDetails userDetails = userService.getValidatedUserDetails(userToken, submissionReference);
        log.info("********** Validated User Details: " + userDetails);
        List<String> rolesByUserId = RoleUtils.getCaseRolesForUser(caseUserAssignments, userDetails.getUid());
        log.info("********** ROLES By USER ID: " + rolesByUserId);
        List<String> rolesByAssignmentOrder = RoleUtils.findFirstCaseRolesByUserId(caseUserAssignments,
                userDetails.getUid());
        log.info("********** ROLES By ASSIGNMENT ORDER: " + rolesByAssignmentOrder);
        List<String> allRoles = CallbacksCollectionUtils.mergeListsWithoutDuplicates(rolesByUserId,
                rolesByAssignmentOrder);
        log.info("********** All ROLES: " + allRoles);
        if (CollectionUtils.isEmpty(allRoles)) {
            throw new GenericServiceException(ERROR_CASE_ROLES_NOT_FOUND,
                    new Exception(ERROR_CASE_ROLES_NOT_FOUND),
                    ERROR_CASE_ROLES_NOT_FOUND,
                    submissionReference,
                    CLASS_NAME,
                    methodName);
        }
        return allRoles;
    }

    /**
     * Retrieves and validates the case user assignments associated with the
     * specified submission reference.
     * <p>
     * This method calls the CCD case assignment service to obtain case user role
     * information and validates that:
     * <ul>
     *     <li>The response object is not null or empty.</li>
     *     <li>The list of case user assignments is not null or empty.</li>
     * </ul>
     * <p>
     * Assumptions:
     * <ul>
     *     <li>The submission reference uniquely identifies a case.</li>
     *     <li>The CCD case assignment service is available and returns valid data.</li>
     *     <li>A valid case must contain at least one case user assignment.</li>
     * </ul>
     *
     * @param submissionReference the unique reference identifying the submission/case
     * @return the validated list of case user assignments associated with the case
     * @throws GenericServiceException if:
     *         <ul>
     *             <li>an error occurs while retrieving case user assignments,</li>
     *             <li>the response is null or empty, or</li>
     *             <li>no case user assignments are found for the case</li>
     *         </ul>
     */
    public List<CaseUserAssignment> getValidatedCaseUserAssignments(String submissionReference)
            throws GenericServiceException {
        String methodName = "getCaseUserAssignmentData";
        CaseUserAssignmentData caseUserAssignmentData;
        try {
            caseUserAssignmentData = ccdCaseAssignment.getCaseUserRoles(submissionReference);
        } catch (IOException ioe) {
            throw new GenericServiceException(SYSTEM_ERROR,
                    new Exception(ioe),
                    ioe.getMessage(),
                    submissionReference,
                    CLASS_NAME,
                    methodName);
        }
        if (ObjectUtils.isEmpty(caseUserAssignmentData)) {
            throw new GenericServiceException(ERROR_CASE_ROLES_NOT_FOUND,
                    new Exception(ERROR_CASE_ROLES_NOT_FOUND),
                    ERROR_CASE_ROLES_NOT_FOUND,
                    submissionReference,
                    CLASS_NAME,
                    methodName);
        }
        if (CollectionUtils.isEmpty(caseUserAssignmentData.getCaseUserAssignments())) {
            throw new GenericServiceException(ERROR_CASE_ROLES_NOT_FOUND,
                    new Exception(ERROR_CASE_ROLES_NOT_FOUND),
                    ERROR_CASE_ROLES_NOT_FOUND,
                    submissionReference,
                    CLASS_NAME,
                    methodName);
        }
        return caseUserAssignmentData.getCaseUserAssignments();
    }
}
