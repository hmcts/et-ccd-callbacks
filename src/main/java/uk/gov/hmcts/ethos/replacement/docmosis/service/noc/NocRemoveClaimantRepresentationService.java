package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import java.util.List;

import static uk.gov.hmcts.et.common.model.hmc.ValidationError.INVALID_CASE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveClaimantRepresentationService {

    private final NocCcdService nocCcdService;
    private final UserService userService;

    /**
     * Sets the NOC removal option in the supplied case details based on whether
     * the authenticated user is the lead claimant representative.
     *
     * <p>The option is set to {@code NOC_REMOVE_OPTION_ORGANISATION} for the lead
     * claimant representative, or {@code NOC_REMOVE_OPTION_YOURSELF} otherwise.
     * Case details are validated before retrieving the user details or updating
     * the option.
     *
     * @param userToken the authentication token used to retrieve the current user's details
     * @param caseDetails the case details containing the claimant representative
     *                    information and the NOC removal option to update
     * @throws GenericServiceException if the supplied case details are invalid
     */
    public void setNocRemoveOption(String userToken, CaseDetails caseDetails) throws GenericServiceException {
        final String methodName = "setNocRemoveOption";
        if (!CaseDataUtils.areCaseDetailsValid(caseDetails)) {
            throw new GenericServiceException(INVALID_CASE_DETAILS,
                    new Exception(INVALID_CASE_DETAILS),
                    INVALID_CASE_DETAILS,
                    INVALID_CASE_DETAILS,
                    NocRemoveClaimantRepresentationService.class.getSimpleName(),
                    methodName);
        }

        UserDetails userDetails = userService.getValidatedUserDetails(userToken, caseDetails.getCaseId());
        boolean isLeadRepresentative = UserUtils.isLeadClaimantRepresentative(userDetails,
                caseDetails.getCaseData().getRepresentativeClaimantType());
        caseDetails.getCaseData().setNocRemoveOption(
                isLeadRepresentative ? NOC_REMOVE_OPTION_ORGANISATION : NOC_REMOVE_OPTION_YOURSELF);
    }

    /**
     * Finds user details for the first remaining claimant representative after
     * removing the supplied user's first matching assignment from the retrieved list.
     *
     * <p>Only assignments with the claimant solicitor role are considered.
     * The retrieved assignment list is modified. If duplicate assignments exist,
     * the returned details may belong to the supplied user.
     *
     * @param caseDetails the case whose claimant representative assignments are searched
     * @param userDetails the user whose first matching assignment is removed from
     *                    the retrieved list
     * @return the first remaining representative's user details, or {@code null}
     *         if no claimant solicitor assignments exist, no matching assignment
     *         is found for the supplied user, no assignments remain after removal,
     *         or the user details lookup returns {@code null}
     */
    public UserDetails findOtherClaimantRepresentative(CaseDetails caseDetails, UserDetails userDetails) {
        List<CaseUserAssignment> caseUserAssignments = nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(
                caseDetails.getCaseId(), ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        if (caseUserAssignments.isEmpty()) {
            return null;
        }
        CaseUserAssignment caseUserAssignment = ClaimantRepresentativeUtils
                .removeUserAssignmentAndGetFirstRemainingAssignment(caseUserAssignments, userDetails.getUid());
        if (caseUserAssignment == null) {
            return null;
        }
        return userService.findUserDetailsByIdamId(caseUserAssignment.getUserId());
    }

    /**
     * Revokes the claimant's legal representative from the specified case and
     * updates the case data to mark the claimant as unrepresented.
     *
     * <p>The method first verifies that an existing claimant representative is
     * present. If no representative is found, a {@link GenericServiceException}
     * is thrown.</p>
     *
     * <p>Once the claimant representation has been revoked, the claimant is marked
     * as unrepresented based on the configured notice of change removal option.</p>
     *
     * @param userToken the authentication token used to perform the revocation
     * @param caseDetails the case containing the claimant representation details
     * @throws GenericServiceException if no existing claimant representative is found
     *         or if the representation cannot be revoked
     */
    public void revokeClaimantLegalRep(String userToken, CaseDetails caseDetails) throws GenericServiceException {
        final String methodName = "revokeClaimantLegalRep";
        CaseData caseData = caseDetails.getCaseData();
        // get existing rep and organisation details for sending emails
        RepresentedTypeC existingClaimantRep = caseData.getRepresentativeClaimantType();
        if (existingClaimantRep == null) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseDetails.getCaseId(), NocRemoveClaimantRepresentationService.class.getSimpleName(), methodName);
        }
        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        if (Strings.CS.equals(NOC_REMOVE_OPTION_ORGANISATION, caseDetails.getCaseData().getNocRemoveOption())) {
            ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseData);
        }
    }
}