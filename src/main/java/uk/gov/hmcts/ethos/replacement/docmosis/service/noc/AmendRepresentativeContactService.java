package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.MyHmctsService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UserUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole.CLAIMANTSOLICITOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils.getOrganisationAddressAsText;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils.mapOrganisationAddressToAddress;

@Slf4j
@Service("amendRepresentativeContactService")
@RequiredArgsConstructor
public class AmendRepresentativeContactService {

    private final MyHmctsService myHmctsService;
    private final NocRepresentativeService nocRepresentativeService;

    /**
     * Updates the contact details (phone number and address) of the representatives for all represented respondents
     * within the given {@link CaseData} object.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates that the {@code caseData} object and {@code userToken} are not null or empty.</li>
     *     <li>Retrieves the indexes of respondents that are represented using the {@code userToken} and CCD ID from
     *     the case data.</li>
     *     <li>Iterates over the list of represented respondent indexes and updates each representative's phone number
     *     and address with the values stored in {@code caseData.getEt3ResponsePhone()} and
     *     {@code caseData.getEt3ResponseAddress()}.</li>
     * </ul>
     * <p>
     * If any validation fails, or if there are no represented respondents or the representative data is missing, a
     * {@link GenericServiceException} is thrown with relevant context.
     *
     * @param userToken           The authorization token used to authenticate service calls.
     * @param caseData            The case data containing respondent and representative information, including
     *                            ET3 response contact details.
     * @param submissionReference A unique identifier for the current case submission, used for error tracking
     *                            and logging.
     * @throws GenericServiceException if:
     *      <ul>
     *          <li>{@code caseData} is null or empty.</li>
     *          <li>{@code userToken} is blank.</li>
     *          <li>An error occurs while retrieving represented respondent indexes.</li>
     *          <li>No represented respondents are found or the representative collection is empty.</li>
     *      </ul>
     */
    public void updateRepresentativeContactDetails(String userToken, CaseData caseData, String submissionReference)
            throws GenericServiceException {
        CaseDataUtils.validateCaseData(caseData, submissionReference);
        UserUtils.validateToken(userToken, submissionReference);
        List<String> roles = nocRepresentativeService
                .getValidatedRepresentativeRolesByUserToken(userToken, submissionReference);
        if (REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS.equals(
                caseData.getRepresentativeContactChangeOption())) {
            setRepresentativeMyHmctsContactAddress(userToken, caseData);
        }
        if (roles.contains(CLAIMANTSOLICITOR.getCaseRoleLabel())) {
            ClaimantRepresentativeUtils.updateRepresentativeContactDetails(caseData, submissionReference);
            return;
        }
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
    }

    /**
     * Populates representative contact address details in the case data using
     * the authenticated user's MyHMCTS organisation address.
     * <p>
     * The method retrieves the organisation address associated with the user token,
     * maps it to the ET3 response address structure, and stores both:
     * <ul>
     *     <li>the structured address object</li>
     *     <li>the formatted address text representation</li>
     * </ul>
     * in the supplied case data.
     *
     * <p><strong>Assumptions:</strong></p>
     * <ul>
     *     <li>The user token belongs to an authenticated user</li>
     *     <li>The user is associated with a MyHMCTS organisation</li>
     *     <li>The organisation has valid contact address information configured</li>
     *     <li>The supplied case data instance is already initialised</li>
     * </ul>
     *
     * @param userToken the authenticated user token used to retrieve organisation details
     * @param caseData the case data to update with representative address information
     * @throws GenericServiceException if the organisation address cannot be retrieved
     *                                 or organisation details are missing
     */
    public void setRepresentativeMyHmctsContactAddress(String userToken, CaseData caseData)
            throws GenericServiceException {
        OrganisationAddress organisationAddress = myHmctsService.getUserOrganisationAddress(userToken);
        caseData.setEt3ResponseAddress(mapOrganisationAddressToAddress(organisationAddress));
        caseData.setMyHmctsAddressText(getOrganisationAddressAsText(organisationAddress));
    }

    /**
     * Updates the ET3 response contact address and phone details based on the
     * representative roles associated with the authenticated user.
     * <p>
     * This method:
     * <ul>
     *     <li>Validates the provided case data.</li>
     *     <li>Validates the user authentication token.</li>
     *     <li>Retrieves the representative roles associated with the user.</li>
     *     <li>Updates the ET3 response contact details using claimant representative
     *     details when the user has the claimant solicitor role.</li>
     *     <li>Otherwise, updates the ET3 response contact details using the
     *     respondent representative details associated with the user's roles.</li>
     * </ul>
     * <p>
     * Assumptions:
     * <ul>
     *     <li>The user token belongs to a valid authenticated representative.</li>
     *     <li>The submission reference uniquely identifies a case.</li>
     *     <li>The representative roles accurately determine which contact details
     *     should populate the ET3 response fields.</li>
     *     <li>The ET3 response address and phone fields within the case data
     *     are mutable.</li>
     * </ul>
     *
     * @param userToken the authentication token associated with the user
     * @param caseData the case data containing ET3 response and representative details
     * @param submissionReference the unique reference identifying the submission/case
     * @throws GenericServiceException if:
     *         <ul>
     *             <li>the case data is invalid,</li>
     *             <li>the user token is invalid, or</li>
     *             <li>the representative roles cannot be retrieved or validated</li>
     *         </ul>
     */
    public void setEt3ResponseContactAddress(String userToken, CaseData caseData, String submissionReference)
            throws GenericServiceException {
        CaseDataUtils.validateCaseData(caseData, submissionReference);
        UserUtils.validateToken(userToken, submissionReference);
        List<String> roles = nocRepresentativeService
                .getValidatedRepresentativeRolesByUserToken(userToken, submissionReference);
        if (roles.contains(CLAIMANTSOLICITOR.getCaseRoleLabel())) {
            ClaimantRepresentativeUtils.updateET3ResponseContactDetails(caseData);
            return;
        }
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
    }
}
