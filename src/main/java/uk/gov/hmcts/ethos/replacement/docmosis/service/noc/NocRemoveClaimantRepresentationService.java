package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import java.util.List;

import static uk.gov.hmcts.et.common.model.hmc.ValidationError.INVALID_CASE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.STAGE_ABOUT_TO_START;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_NOT_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_LEAD_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_NO_SHARED_REPRESENTATIVE_TO_REMOVE_YOURSELF;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_LEAD;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_SHARED;

@Service
@RequiredArgsConstructor
public class NocRemoveClaimantRepresentationService {

    private final NocCcdService nocCcdService;
    private final FeatureToggleService featureToggleService;

    /**
     * Populates the NOC representative type, case-sharing status, and removal option
     * in the supplied case details for the authenticated user.
     *
     * <p>Validates the case details and retrieves validated user details before
     * updating the case data. Sets the representative type to
     * {@code NOC_REPRESENTATIVE_TYPE_LEAD} for the lead claimant representative,
     * or {@code NOC_REPRESENTATIVE_TYPE_SHARED} otherwise.
     *
     * <p>Sets the case-sharing status to {@code NOC_CASE_SHARING_STATUS_SHARED}
     * if a shared representative exists, or {@code NOC_CASE_SHARING_STATUS_NOT_SHARED}
     * otherwise.
     *
     * <p>Sets the removal option to {@code NOC_REMOVE_OPTION_YOURSELF} for non-lead
     * representatives, or clears it to {@code null} for the lead representative.
     * This method modifies the supplied case data.
     *
     * @param userDetails the validated user details
     * @param caseDetails the case details whose NOC removal fields are updated
     * @throws GenericServiceException if the supplied case details are invalid
     */
    public void populateNocRemovalDetails(UserDetails userDetails, CaseDetails caseDetails, String stage)
            throws GenericServiceException {
        final String methodName = "populateNocRemovalDetails";
        if (!CaseDataUtils.areCaseDetailsValid(caseDetails)) {
            throw new GenericServiceException(INVALID_CASE_DETAILS,
                    new Exception(INVALID_CASE_DETAILS),
                    INVALID_CASE_DETAILS,
                    INVALID_CASE_DETAILS,
                    NocRemoveClaimantRepresentationService.class.getSimpleName(),
                    methodName);
        }
        boolean isLeadRepresentative = ClaimantRepresentativeUtils.isLeadRepresentative(userDetails,
                caseDetails.getCaseData());
        caseDetails.getCaseData().setNocRepresentativeType(isLeadRepresentative
                ? NOC_REPRESENTATIVE_TYPE_LEAD : NOC_REPRESENTATIVE_TYPE_SHARED);
        boolean hasSharedRepresentative = hasRemainingClaimantRepresentative(caseDetails, userDetails);
        caseDetails.getCaseData().setNocCaseSharingStatus(hasSharedRepresentative
                ? NOC_CASE_SHARING_STATUS_SHARED : NOC_CASE_SHARING_STATUS_NOT_SHARED);
        if (!isLeadRepresentative && !hasSharedRepresentative) {
            String exceptionMessage = String.format(NOC_LEAD_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId());
            throw new GenericServiceException(exceptionMessage,
                    new Exception(exceptionMessage),
                    exceptionMessage,
                    exceptionMessage,
                    NocRemoveClaimantRepresentationService.class.getSimpleName(),
                    methodName);
        }
        if (STAGE_ABOUT_TO_START.equals(stage)) {
            caseDetails.getCaseData().setNocRemoveOption(null);
        }
        if (isLeadRepresentative && !hasSharedRepresentative) {
            caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_ORGANISATION);
        }
        if (!isLeadRepresentative) {
            caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_YOURSELF);
        }
    }

    /**
     * Checks whether the case has another claimant representative besides the specified user.
     *
     * <p>Retrieves the case's claimant solicitor assignments and looks up the user
     * details for the first remaining assignment after excluding the specified
     * user's assignment.</p>
     *
     * @param caseDetails the case whose claimant representatives are checked
     * @param userDetails the user whose assignment is excluded from the check
     * @return {@code true} if user details are found for the first remaining
     *         claimant representative; {@code false} if no assignment remains
     *         or the user details lookup returns {@code null}
     */
    public boolean hasRemainingClaimantRepresentative(CaseDetails caseDetails, UserDetails userDetails) {
        List<CaseUserAssignment> caseUserAssignments = nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(
                caseDetails.getCaseId(), ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        return nocCcdService.findRemainingClaimantRepresentative(caseUserAssignments, userDetails) != null;
    }

    /**
     * Revokes legal representation for a claimant as part of the Notice of Change (NoC) removal workflow.
     *
     * <p>This method executes the following business steps:
     * <ol>
     *   <li>Validates that an existing claimant representative ({@code RepresentedTypeC}) is present on the case;</li>
     *   <li>Determines whether other representatives from the same organisation remain assigned, recording
     *       the result in {@code nocCaseSharingStatus};</li>
     *   <li>Ensures that if the user chose to remove only themselves ({@code NOC_REMOVE_OPTION_YOURSELF}),
     *       at least one shared colleague remains to maintain continuous representation;</li>
     *   <li>Delegates case role revocation to {@link NocCcdService#revokeClaimantRepresentation};</li>
     *   <li>Marks the claimant as unrepresented via {@link ClaimantRepresentativeUtils#markClaimantAsUnrepresented}
     *       if the organisation-level removal option ({@code NOC_REMOVE_OPTION_ORGANISATION}) was chosen.</li>
     * </ol>
     *
     * @param userDetails the IDAM {@link UserDetails} of the solicitor performing the revocation
     * @param caseDetails the {@link CaseDetails} containing the case ID and {@link CaseData} to update
     * @throws GenericServiceException if no claimant representative exists on the case, or if the user attempts
     *                                 to remove themselves when no remaining shared representative exists
     */
    public void revokeClaimantLegalRep(UserDetails userDetails, CaseDetails caseDetails)
            throws GenericServiceException {
        final String methodName = "revokeClaimantLegalRep";
        CaseData caseData = caseDetails.getCaseData();
        // get existing rep and organisation details for sending emails
        RepresentedTypeC existingClaimantRep = caseData.getRepresentativeClaimantType();
        if (existingClaimantRep == null) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseDetails.getCaseId(), NocRemoveClaimantRepresentationService.class.getSimpleName(), methodName);
        }
        boolean hasSharedRepresentative = hasRemainingClaimantRepresentative(caseDetails, userDetails);
        caseDetails.getCaseData().setNocCaseSharingStatus(hasSharedRepresentative
                ? NOC_CASE_SHARING_STATUS_SHARED : NOC_CASE_SHARING_STATUS_NOT_SHARED);
        if (Strings.CS.equals(NOC_REMOVE_OPTION_YOURSELF, caseDetails.getCaseData().getNocRemoveOption())
                && !hasSharedRepresentative) {
            String exceptionMessage = String.format(NOC_NO_SHARED_REPRESENTATIVE_TO_REMOVE_YOURSELF,
                    caseDetails.getCaseId());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseDetails.getCaseId(), NocRemoveClaimantRepresentationService.class.getSimpleName(), methodName);
        }
        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userDetails, caseDetails);
        if (Strings.CS.equals(NOC_REMOVE_OPTION_ORGANISATION, caseDetails.getCaseData().getNocRemoveOption())) {
            ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseData,
                    featureToggleService.isCaseFlagsV2Enabled(caseDetails.getCaseTypeId()));
        }
    }
}