package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import static uk.gov.hmcts.et.common.model.hmc.ValidationError.INVALID_CASE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRemoveRepresentationService {

    private final NocCcdService nocCcdService;
    private final AdminUserService adminUserService;

    /**
     * Sets the notice of change removal option based on the user's relationship
     * to the claimant representative organisation.
     * <p>
     * If the user is the lead claimant representative, the removal option is set
     * to remove the organisation. Otherwise, the option is set to remove only the
     * current representative.
     * </p>
     * <p>
     * The case details are validated before updating the case data. If the case
     * details are invalid, a {@link GenericServiceException} is thrown.
     * </p>
     *
     * @param userDetails the authenticated user's details used to determine the removal option
     * @param caseDetails the case details containing the claimant representative information
     * @throws GenericServiceException if the supplied case details are invalid
     */
    public void setNocRemoveOption(UserDetails userDetails, CaseDetails caseDetails) throws GenericServiceException {
        final String methodName = "setNocRemoveOption";
        if (!CaseDataUtils.areCaseDetailsValid(caseDetails)) {
            throw new GenericServiceException(INVALID_CASE_DETAILS,
                    new Exception(INVALID_CASE_DETAILS),
                    INVALID_CASE_DETAILS,
                    INVALID_CASE_DETAILS,
                    NocRemoveRepresentationService.class.getSimpleName(),
                    methodName);
        }
        caseDetails.getCaseData().setNocRemoveOption(
                UserUtils.isLeadClaimantRepresentative(userDetails,
                        caseDetails.getCaseData().getRepresentativeClaimantType())
                        ? NOC_REMOVE_OPTION_ORGANISATION
                        : NOC_REMOVE_OPTION_YOURSELF);
    }

    /**
     * Revokes the claimant's legal representative from the case and sends notification emails to all relevant parties.
     * This method performs the following actions:
     * - Retrieves the current claimant representative and organisation details.
     * - Revokes the claimant's legal representation in CCD.
     * - Marks the claimant as unrepresented in the case data.
     * - Sends notification emails to the organisation admin, removed legal representative, claimant, and all other
     *   respondents.
     *
     * @param caseDetails The case details containing the case data and ID.
     * @throws IllegalStateException if the claimant representative is missing in the case data.
     */
    public void revokeClaimantLegalRep(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        // get existing rep and organisation details for sending emails
        RepresentedTypeC existingClaimantRep = caseData.getRepresentativeClaimantType();
        if (existingClaimantRep == null) {
            throw new IllegalStateException(String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId()));
        }

        // revoke claimant legal rep
        final String adminUserToken = adminUserService.getAdminUserToken();
        nocCcdService.revokeClaimantRepresentation(adminUserToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseData, caseData.getNocRemoveOption());
    }
}