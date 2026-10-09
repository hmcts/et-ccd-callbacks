package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
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
                    caseDetails.getCaseId(), NocRemoveRepresentationService.class.getSimpleName(), methodName);
        }
        // revoke claimant legal rep
        nocCcdService.revokeClaimantRepresentation(userToken, caseDetails);
        if (Strings.CS.equals(NOC_REMOVE_OPTION_ORGANISATION, caseDetails.getCaseData().getNocRemoveOption())) {
            ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseData);
        }
    }
}