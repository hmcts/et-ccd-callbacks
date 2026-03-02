package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.NocUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_INPUTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SOLICITOR_ROLE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_SET_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_START_EVENT_TO_UPDATE_REPRESENTATIVE_AND_ORGANISATION_POLICY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_EMAIL_ADDRESS_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocRespondentRepresentativeService {

    private final NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;
    private final CaseConverter caseConverter;
    private final NocCcdService nocCcdService;
    private final AdminUserService adminUserService;
    private final NocRespondentHelper nocRespondentHelper;
    private final NocNotificationService nocNotificationService;
    private final CcdClient ccdClient;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final NocService nocService;

    /**
     * Validates that each representative marked as an HMCTS organisation user
     * has a valid organisation and a resolvable email address within the
     * organisation service.
     *
     * <p>The validation performs the following checks for each representative in the case data:</p>
     * <ul>
     *     <li>If the representative is marked as an HMCTS organisation user.</li>
     *     <li>Ensures the representative has a non-null organisation with a valid organisation ID.
     *         If not, a {@link GenericServiceException} is thrown.</li>
     *     <li>Checks that the representative has a non-blank email address.
     *         If missing, a warning message is added to the case data.</li>
     *     <li>Attempts to resolve the representative’s account via the organisation service
     *         using the provided email address. If no matching account is found or the lookup
     *         fails, a warning message is added to the case data.</li>
     * </ul>
     *
     * <p>If {@code caseData} or its representative collection is null or empty,
     * the method exits without performing any validation.</p>
     *
     * <p>All warnings generated during validation are aggregated and stored in
     * {@code caseData.setNocWarning(...)}. The method does not fail on missing
     * email accounts but records them as warnings instead.</p>
     *
     * @param caseData the case data containing representative details to validate
     * @throws GenericServiceException if a representative marked as an HMCTS
     *         organisation user does not have a valid organisation or organisation ID
     */
    public void validateRepresentativesOrganisationsAndEmails(CaseData caseData)
            throws GenericServiceException {
        if (!RespondentRepresentativeUtils.hasRepresentatives(caseData)) {
            return;
        }
        StringBuilder nocWarnings = new StringBuilder(StringUtils.EMPTY);
        for (RepresentedTypeRItem representativeItem :  caseData.getRepCollection()) {
            if (RespondentRepresentativeUtils.isValidRepresentative(representativeItem)
                    && YES.equals(representativeItem.getValue().getMyHmctsYesNo())) {
                if (StringUtils.isBlank(representativeItem.getValue().getRepresentativeEmailAddress())) {
                    nocWarnings.append(WARNING_REPRESENTATIVE_EMAIL_ADDRESS_NOT_FOUND).append('\n');
                    continue;
                }
                nocWarnings.append(validateRepresentativeOrganisationAndEmail(representativeItem));
            }
        }
        caseData.setNocWarning(nocWarnings.toString());
    }

    private String validateRepresentativeOrganisationAndEmail(RepresentedTypeRItem representativeItem)
            throws GenericServiceException {
        StringBuilder nocWarnings = new StringBuilder(StringUtils.EMPTY);
        RepresentedTypeR representative = representativeItem.getValue();
        final String representativeName = representative.getNameOfRepresentative();
        // Checking if representative has an organisation
        if (!RespondentRepresentativeUtils.hasOrganisation(representative)) {
            throw new GenericServiceException(EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);
        }
        String accessToken = adminUserService.getAdminUserToken();
        try {
            ResponseEntity<AccountIdByEmailResponse> userResponse =
                    organisationClient.getAccountIdByEmail(accessToken, authTokenGenerator.generate(),
                            representative.getRepresentativeEmailAddress());
            // checking if representative email address exists in organisation users
            if (!OrganisationUtils.hasUserIdentifier(userResponse)) {
                String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                        representativeName, representative.getRepresentativeEmailAddress());
                nocWarnings.append(warningMessage).append('\n');
            }
        } catch (Exception e) {
            String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                    representativeName, representative.getRepresentativeEmailAddress());
            nocWarnings.append(warningMessage).append('\n');
        }
        return nocWarnings.toString();
    }

    /**
     * Identifies and removes respondent representatives that have been deleted between
     * the previous and current versions of a case.
     * <p>
     * The method compares the representative collections from the case details before
     * and after the callback, and for any representatives that have been removed it:
     * <ul>
     *   <li>sends representation removal notifications</li>
     *   <li>revokes the representatives' access to the case</li>
     *   <li>resets associated organisation policies</li>
     * </ul>
     * <p>
     * If no representatives are identified for removal, or if revocation produces no
     * revoked representatives, the method exits without further action.
     * <p>
     * Any exceptions raised while sending notifications are caught and logged to prevent
     * disruption to the removal process.
     *
     * @param callbackRequest the callback request containing both the previous and current
     *                        case details
     * @param userToken       the user authentication token used to revoke representative access
     */
    public void removeOldRepresentatives(CallbackRequest callbackRequest, String userToken) {
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetailsBefore();
        CaseDetails newCaseDetails = callbackRequest.getCaseDetails();

        List<RepresentedTypeRItem> oldRepresentatives = oldCaseDetails.getCaseData().getRepCollection();
        List<RepresentedTypeRItem> newRepresentatives = newCaseDetails.getCaseData().getRepCollection();
        List<RepresentedTypeRItem> representativesToRemove =
                findRepresentativesToRemove(oldRepresentatives, newRepresentatives);
        if (CollectionUtils.isEmpty(representativesToRemove)) {
            return;
        }
        try {
            nocNotificationService.sendRespondentRepresentationUpdateNotifications(oldCaseDetails,
                    representativesToRemove, NOC_TYPE_REMOVAL);
        } catch (Exception e) {
            log.info(ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL, oldCaseDetails.getCaseId(), e.getMessage());
        }
        List<RepresentedTypeRItem> revokedRepresentatives = revokeOldRespondentRepresentativeAccess(callbackRequest,
                userToken, representativesToRemove);
        if (CollectionUtils.isEmpty(revokedRepresentatives)) {
            return;
        }
        resetOrganisationPolicies(callbackRequest.getCaseDetails(), revokedRepresentatives);
    }

    /**
     * Identifies representatives from the existing list that should be treated as changed
     * for the same respondent when compared against a new list of representatives.
     * <p>
     * A representative from {@code oldRepresentatives} is included in the returned list if:
     * <ul>
     *     <li>it is a valid representative, and</li>
     *     <li>no matching representative exists in {@code newRepresentatives} for the same respondent, or</li>
     *     <li>a matching representative exists but the organisation or email address has changed</li>
     * </ul>
     * <p>
     * Only valid representatives are considered during the comparison. If
     * {@code oldRepresentatives} is {@code null} or empty, an empty list is returned.
     *
     * @param oldRepresentatives the existing representatives to compare against
     * @param newRepresentatives the updated representatives to compare with
     * @return a list of representatives from {@code oldRepresentatives} that are either
     *         no longer present or have updated organisation or email details
     */
    public List<RepresentedTypeRItem> findRepresentativesToRemove(
            List<RepresentedTypeRItem> oldRepresentatives, List<RepresentedTypeRItem> newRepresentatives) {
        if (CollectionUtils.isEmpty(oldRepresentatives)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(newRepresentatives)) {
            return oldRepresentatives;
        }
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        for (RepresentedTypeRItem oldRepresentative : oldRepresentatives) {
            if (!RespondentRepresentativeUtils.isValidRepresentative(oldRepresentative)) {
                continue;
            }
            // to check if representative exists but its organisation or email is changed or not
            boolean hasRespondentRepresentativeOrganisationChanged = false;
            // to check if representative exists or not
            boolean isMatchingValidRepresentative = false;
            boolean hmctsRepresentativeEmailChanged = false;
            for (RepresentedTypeRItem newRepresentative : newRepresentatives) {
                if (RespondentRepresentativeUtils.isMatchingValidRepresentative(oldRepresentative, newRepresentative)) {
                    isMatchingValidRepresentative = true;
                    // representative already exists but its organisation or email is changed
                    hasRespondentRepresentativeOrganisationChanged =
                            RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(
                                    oldRepresentative.getValue(), newRepresentative.getValue());
                    // when representative email changed and new representative has account on HMCTS should
                    // remove old representative and assign new representative access with new email address.
                    hmctsRepresentativeEmailChanged = hasHmctsRepresentativeEmailChanged(oldRepresentative,
                            newRepresentative);
                }
            }
            if (RespondentRepresentativeUtils.canRemoveRepresentative(isMatchingValidRepresentative,
                    hasRespondentRepresentativeOrganisationChanged,
                    hmctsRepresentativeEmailChanged)) {
                representativesToRemove.add(oldRepresentative);
            }
        }
        return representativesToRemove;
    }

    /**
     * Determines whether the representative's email address has changed and the
     * updated email belongs to a registered HMCTS organisation user.
     *
     * <p>This method returns {@code true} only if:</p>
     * <ul>
     *     <li>The representative email address differs between the old and new
     *         {@link RepresentedTypeRItem} instances (case-insensitive comparison), and</li>
     *     <li>The new representative email address is associated with a valid
     *         HMCTS organisation user.</li>
     * </ul>
     *
     * <p><strong>Assumption:</strong> Both {@code oldRepresentative} and
     * {@code newRepresentative}, including their underlying values and email
     * addresses, are non-null and non-empty. This method does not perform
     * null-safety validation.</p>
     *
     * @param oldRepresentative the original representative item (assumed non-null and populated)
     * @param newRepresentative the updated representative item (assumed non-null and populated)
     * @return {@code true} if the email has changed and the new email belongs to an
     *         HMCTS organisation user; {@code false} otherwise
     */
    public boolean hasHmctsRepresentativeEmailChanged(RepresentedTypeRItem oldRepresentative,
                                                      RepresentedTypeRItem newRepresentative) {
        return RespondentRepresentativeUtils.isRepresentativeEmailChanged(oldRepresentative.getValue(),
                newRepresentative.getValue())
                && isHmctsOrganisationUser(newRepresentative.getValue().getRepresentativeEmailAddress());
    }

    /**
     * Checks whether the given email address belongs to a registered HMCTS organisation user.
     *
     * <p>This method performs a lookup against the organisation service using
     * an administrative access token. An email is considered associated with an
     * HMCTS organisation user if:</p>
     * <ul>
     *     <li>A non-null response is returned,</li>
     *     <li>The HTTP status code is present and indicates a successful (2xx) response,</li>
     *     <li>The response body is not null, and</li>
     *     <li>The returned user identifier is non-blank.</li>
     * </ul>
     *
     * @param email the email address to validate; must not be {@code null} or blank
     * @return {@code true} if the email corresponds to a valid HMCTS organisation user;
     *         {@code false} otherwise
     */
    public boolean isHmctsOrganisationUser(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        ResponseEntity<AccountIdByEmailResponse> accountIdByEmailResponseResponseEntity = organisationClient
                .getAccountIdByEmail(adminUserService.getAdminUserToken(), authTokenGenerator.generate(), email);
        return ObjectUtils.isNotEmpty(accountIdByEmailResponseResponseEntity)
                && ObjectUtils.isNotEmpty(accountIdByEmailResponseResponseEntity.getStatusCode())
                && accountIdByEmailResponseResponseEntity.getStatusCode().is2xxSuccessful()
                && ObjectUtils.isNotEmpty(accountIdByEmailResponseResponseEntity.getBody())
                && StringUtils.isNotBlank(accountIdByEmailResponseResponseEntity.getBody().getUserIdentifier());
    }

    /**
     * Revokes CCD case role assignments for respondent representatives that have been removed
     * from the case data.
     * <p>
     * The method compares the respondent representative roles currently assigned on the
     * existing (pre-callback) case with the list of representatives marked for removal.
     * For each matching respondent representative role that is eligible for modification,
     * the corresponding case role assignment is revoked via the CCD API.
     * </p>
     * <p>
     * If any required input data is missing or invalid (for example, callback request,
     * user token, case details, assignments, or representatives to remove), no action
     * is taken and an empty list is returned.
     * </p>
     *
     * @param callbackRequest the callback request containing the case details before the update
     * @param userToken the IDAM user token used to revoke case assignments
     * @param representativesToRemove the list of respondent representatives whose access
     *                                should be revoked
     * @return a list of {@link RepresentedTypeRItem} instances for which case role assignments
     *         were successfully identified and revoked; an empty list if no revocations occur
     */
    public List<RepresentedTypeRItem> revokeOldRespondentRepresentativeAccess(
            CallbackRequest callbackRequest, String userToken, List<RepresentedTypeRItem> representativesToRemove) {
        if (!NocUtils.canRevokeRepresentativeAccess(callbackRequest, userToken, representativesToRemove)) {
            return new  ArrayList<>();
        }
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetailsBefore();
        CaseUserAssignmentData caseUserAssignments = nocCcdService.retrieveCaseUserAssignments(
                adminUserService.getAdminUserToken(), oldCaseDetails.getCaseId());
        if (ObjectUtils.isEmpty(caseUserAssignments)
                || CollectionUtils.isEmpty(caseUserAssignments.getCaseUserAssignments())) {
            return new ArrayList<>();
        }
        // finds list of representatives whose assignment is revoked
        List<RepresentedTypeRItem> representativesToRevoke = new ArrayList<>();
        List<CaseUserAssignment> caseUserAssignmentsToRevoke = new ArrayList<>();
        for (CaseUserAssignment caseUserAssignment : caseUserAssignments.getCaseUserAssignments()) {
            if (!RoleUtils.isRespondentRepresentativeRole(caseUserAssignment.getCaseRole())) {
                continue;
            }
            for (RepresentedTypeRItem representative : representativesToRemove) {
                String respondentName = RoleUtils.findRespondentNameByRole(oldCaseDetails.getCaseData(),
                        caseUserAssignment.getCaseRole());
                if (!RespondentRepresentativeUtils.isEligibleForAccessRevocation(representative, caseUserAssignment,
                        respondentName)) {
                    continue;
                }
                representativesToRevoke.add(representative);
                caseUserAssignmentsToRevoke.add(caseUserAssignment);
            }
        }
        revokeCaseAssignments(userToken, caseUserAssignmentsToRevoke);
        return representativesToRevoke;
    }

    private void revokeCaseAssignments(String userToken, List<CaseUserAssignment> caseUserAssignmentsToRevoke) {
        if (CollectionUtils.isNotEmpty(caseUserAssignmentsToRevoke)) {
            try {
                ccdClient.revokeCaseAssignments(userToken, CaseUserAssignmentData.builder().caseUserAssignments(
                        caseUserAssignmentsToRevoke).build());
            } catch (IOException exception) {
                log.info(exception.getMessage(), exception);
            }
        }
    }

    /**
     * Resets respondent organisation policies and Notice of Change (NoC) answers for the given case
     * based on the provided list of revoked representatives.
     * <p>
     * This method:
     * <ul>
     *   <li>starts an {@code UPDATE_CASE_SUBMITTED} event as an admin user</li>
     *   <li>clears the {@code changeOrganisationRequestField} to avoid conflicts with existing
     *       representative changes</li>
     *   <li>removes organisation policies and related NoC answers for each revoked representative</li>
     *   <li>submits the updated case data back to CCD</li>
     * </ul>
     * <p>
     * If any required case details are missing (case data, case type, jurisdiction, or case ID),
     * or if the list of revoked representatives is {@code null} or empty, the method performs
     * no action.
     * <p>
     * Any {@link IOException} encountered while communicating with CCD is caught and logged.
     *
     * @param caseDetails           the CCD case details for which organisation policies should be reset
     * @param revokedRepresentatives the representatives whose organisation policies and NoC
     *                               answers should be removed
     */
    public void resetOrganisationPolicies(CaseDetails caseDetails,
                                          List<RepresentedTypeRItem> revokedRepresentatives) {
        if (ObjectUtils.isEmpty(caseDetails)
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || StringUtils.isBlank(caseDetails.getCaseTypeId())
                || StringUtils.isBlank(caseDetails.getJurisdiction())
                || StringUtils.isEmpty(caseDetails.getCaseId())
                || CollectionUtils.isEmpty(revokedRepresentatives)) {
            return;
        }
        String adminUserToken = adminUserService.getAdminUserToken();
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken,
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    caseDetails.getCaseId(),
                    EVENT_UPDATE_CASE_SUBMITTED);
            CaseData ccdRequestCaseData = ccdRequest.getCaseDetails().getCaseData();
            // Clears the changeOrganisationRequestField to prevent errors in the existing representative process
            // and to allow further changes to be made
            ccdRequestCaseData.setChangeOrganisationRequestField(null);
            // Removes organisation policies & notice of change answers
            NocUtils.resetOrganisationPolicies(ccdRequestCaseData, revokedRepresentatives);
            RespondentRepresentativeUtils.clearRolesForRepresentatives(ccdRequestCaseData, revokedRepresentatives);
            ccdClient.submitEventForCase(adminUserToken, ccdRequestCaseData, caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(), ccdRequest, caseDetails.getCaseId());
        } catch (IOException exception) {
            log.error(ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES, caseDetails.getCaseId(),
                    exception.getMessage());
        }
    }

    /**
     * Identifies and assigns access to newly added or updated respondent representatives
     * based on the differences between the previous and current case details.
     *
     * <p>This method performs the following steps:
     * <ol>
     *     <li>Validates that the {@link CallbackRequest}, its case details (before and after),
     *     associated case data, and the current representative collection are present.</li>
     *     <li>Compares the previous and current representative collections to determine
     *     which representatives are either newly added or have had key details updated
     *     (e.g. organisation or email address).</li>
     *     <li>Filters the identified representatives to determine which require access
     *     to be assigned.</li>
     *     <li>Grants the appropriate access permissions to those representatives.</li>
     * </ol>
     *
     * <p>If any required data is missing or empty, the method exits without performing
     * any processing.
     *
     * @param callbackRequest the callback request containing both the previous
     *                        ({@code caseDetailsBefore}) and current ({@code caseDetails})
     *                        case details used to determine newly added or updated
     *                        respondent representatives
     */
    public void addNewRepresentatives(CallbackRequest callbackRequest) {
        if (ObjectUtils.isEmpty(callbackRequest)
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetailsBefore())
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetails())
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetailsBefore().getCaseData())
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetails().getCaseData())
                || CollectionUtils.isEmpty(callbackRequest.getCaseDetails().getCaseData().getRepCollection())) {
            return;
        }
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetailsBefore();
        CaseDetails newCaseDetails = callbackRequest.getCaseDetails();
        // finds both new, and organisation or e-mail changed representatives
        List<RepresentedTypeRItem> newOrUpdatedRepresentatives = RespondentRepresentativeUtils
                .findNewOrUpdatedRepresentatives(newCaseDetails.getCaseData().getRepCollection(),
                        oldCaseDetails.getCaseData().getRepCollection());
        List<RepresentedTypeRItem> representativesToAssign = findRepresentativesToAssign(newCaseDetails,
                newOrUpdatedRepresentatives);
        grantRespondentRepresentativesAccess(newCaseDetails, representativesToAssign);
    }

    /**
     * Determines which respondent representatives can be assigned to the given case.
     *
     * <p>The method first filters the provided representatives to include only those
     * whose access can be modified. It then removes any representatives that are
     * already assigned to the case as respondent representatives.</p>
     *
     * <p>If the input list is {@code null} or empty, an empty list is returned.
     * If case details are missing or incomplete, only the access-modifiable filtering
     * is applied.</p>
     *
     * <p>The returned list is a mutable list and the input list is not modified.</p>
     *
     * @param caseDetails the case details used to determine existing respondent assignments
     * @param representatives the list of respondent representatives to evaluate
     * @return a list of representatives that can be assigned to the case
     */
    public List<RepresentedTypeRItem> findRepresentativesToAssign(CaseDetails caseDetails,
                                                                  List<RepresentedTypeRItem> representatives) {
        List<RepresentedTypeRItem> assignableRepresentatives = RespondentRepresentativeUtils
                .filterModifiableRepresentatives(representatives);
        if (!RespondentRepresentativeUtils.hasValidAssignmentContext(assignableRepresentatives, caseDetails)) {
            return assignableRepresentatives;
        }
        CaseUserAssignmentData caseUserAssignments = nocCcdService.retrieveCaseUserAssignments(
                adminUserService.getAdminUserToken(), caseDetails.getCaseId());
        if (ObjectUtils.isEmpty(caseUserAssignments)
                || CollectionUtils.isEmpty(caseUserAssignments.getCaseUserAssignments())) {
            return assignableRepresentatives;
        }
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        for (RepresentedTypeRItem representative : assignableRepresentatives) {
            if (StringUtils.isBlank(representative.getValue().getRespRepName())) {
                continue;
            }
            for (CaseUserAssignment caseUserAssignment : caseUserAssignments.getCaseUserAssignments()) {
                if (!RoleUtils.isRespondentRepresentativeRole(caseUserAssignment.getCaseRole())) {
                    continue;
                }
                String respondentName = RoleUtils.findRespondentNameByRole(caseDetails.getCaseData(),
                        caseUserAssignment.getCaseRole());
                if (representative.getValue().getRespRepName().equals(respondentName)) {
                    representativesToRemove.add(representative);
                }
            }
        }
        List<RepresentedTypeRItem> representativesToAssign = new ArrayList<>(assignableRepresentatives);
        representativesToAssign.removeAll(representativesToRemove);
        return representativesToAssign;
    }

    /**
     * Grants case access to valid respondent representatives by assigning them the next available
     * respondent solicitor role via Notice of Change (NoC).
     * <p>
     * The method iterates over the provided list of representatives and, for each valid representative:
     * <ul>
     *     <li>Determines the next available respondent solicitor role on the case</li>
     *     <li>Grants access to the case using the representative's email and organisation details</li>
     * </ul>
     * </p>
     * <p>
     * Processing will stop if no respondent solicitor roles are available on the case.
     * Invalid representatives are skipped. Any failures when granting access are logged and do not
     * prevent processing of subsequent representatives.
     * </p>
     * <p>
     * The method performs no action if:
     * <ul>
     *     <li>{@code caseDetails} or its case data is {@code null}</li>
     *     <li>The case ID is blank</li>
     *     <li>The representatives list is {@code null} or empty</li>
     * </ul>
     * </p>
     *
     * @param caseDetails     the case details containing the case ID and case data
     * @param representatives a list of respondent representatives to be granted access
     */
    public void grantRespondentRepresentativesAccess(CaseDetails caseDetails,
                                                     List<RepresentedTypeRItem> representatives) {
        if (ObjectUtils.isEmpty(caseDetails)
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || StringUtils.isBlank(caseDetails.getCaseId())
                || CollectionUtils.isEmpty(representatives)) {
            return;
        }
        for (RepresentedTypeRItem representative : representatives) {
            if (RespondentRepresentativeUtils.isValidRepresentative(representative)) {
                String role = RoleUtils.deriveSolicitorRoleToAssign(caseDetails.getCaseData(), representative);
                if (StringUtils.isBlank(role)) {
                    log.error(ERROR_SOLICITOR_ROLE_NOT_FOUND, caseDetails.getCaseId());
                    break;
                }
                try {
                    nocService.grantRepresentativeAccess(adminUserService.getAdminUserToken(),
                            representative.getValue().getRepresentativeEmailAddress(), caseDetails.getCaseId(),
                            representative.getValue().getRespondentOrganisation(), role);
                    updateRepresentativeRoleAndOrganisationPolicy(caseDetails, representative.getId(), role);
                } catch (GenericServiceException gse) {
                    log.error(ERROR_UNABLE_TO_SET_ROLE, role, caseDetails.getCaseId(), gse.getMessage());
                }
            }
        }
    }

    /**
     * Updates the role of an existing representative on a case and reapplies the
     * respondent organisation policy based on the updated role.
     * <p>
     * This method performs an admin-initiated case update which:
     * <ul>
     *   <li>Validates the supplied case details and input parameters</li>
     *   <li>Starts a CCD {@code UPDATE_CASE_SUBMITTED} event</li>
     *   <li>Finds the representative using the provided identifier</li>
     *   <li>Updates the representative's role</li>
     *   <li>Applies the appropriate respondent organisation policy for the updated role</li>
     *   <li>Submits the updated case data back to CCD</li>
     * </ul>
     * <p>
     * If validation fails, the representative cannot be found, or the CCD event cannot be
     * started or submitted, the method logs an error and exits without persisting any changes.
     * No exceptions are propagated to the caller.
     *
     * @param caseDetails       the case details containing the identifiers and data required
     *                          to update the representative and organisation policy
     * @param representativeId the unique identifier of the representative whose role is to be updated
     * @param role              the new role to assign to the representative
     */
    public void updateRepresentativeRoleAndOrganisationPolicy(CaseDetails caseDetails,
                                                              String representativeId,
                                                              String role) {
        if (ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isBlank(caseDetails.getCaseId())
                || StringUtils.isBlank(caseDetails.getCaseTypeId())
                || StringUtils.isBlank(caseDetails.getJurisdiction())) {
            log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS);
            return;
        }
        if (StringUtils.isBlank(representativeId) || StringUtils.isBlank(role)) {
            log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_INPUTS, caseDetails.getCaseId());
            return;
        }
        String adminUserToken = adminUserService.getAdminUserToken();
        try {
            CCDRequest ccdRequest = nocCcdService.startEventForUpdateCaseSubmitted(adminUserToken,
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    caseDetails.getCaseId());
            if (ObjectUtils.isEmpty(ccdRequest)) {
                log.error(ERROR_UNABLE_TO_START_EVENT_TO_UPDATE_REPRESENTATIVE_AND_ORGANISATION_POLICY,
                        caseDetails.getCaseId());
                return;
            }
            CaseDetails ccdRequestCaseDetails = ccdRequest.getCaseDetails();
            RepresentedTypeRItem representative = RespondentRepresentativeUtils.findRepresentativeById(
                    ccdRequestCaseDetails.getCaseData(), representativeId);
            if (!RespondentRepresentativeUtils.isValidRepresentative(representative)) {
                log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId());
                return;
            }
            assert representative != null;
            representative.getValue().setRole(role);
            NocUtils.applyRespondentOrganisationPolicyForRole(ccdRequestCaseDetails.getCaseData(), representative);
            ccdClient.submitEventForCase(adminUserToken,
                    ccdRequestCaseDetails.getCaseData(),
                    ccdRequestCaseDetails.getCaseTypeId(),
                    ccdRequestCaseDetails.getJurisdiction(),
                    ccdRequest,
                    ccdRequestCaseDetails.getCaseId());
        } catch (IOException exception) {
            log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES, caseDetails.getCaseId(),
                    exception.getMessage());
        }
    }

    /**
     * Removes the claimant's legal representation where a conflict exists with a respondent's
     * representative.
     *
     * <p>This method performs a series of validation checks to ensure the supplied
     * {@link CaseDetails} contains the minimum required information (case ID,
     * case type ID, jurisdiction, case data, representative collection, and that
     * the claimant is currently marked as represented). If any of these checks fail,
     * no action is taken and the existing {@link CaseData} is returned (or {@code null}
     * if {@code caseDetails} itself is {@code null}).</p>
     *
     * <p>If validation passes, the method:
     * <ol>
     *     <li>Obtains an admin user token.</li>
     *     <li>Attempts to revoke the claimant's representation via CCD.</li>
     *     <li>If revocation fails and the claimant representative’s email matches
     *         a respondent representative, forcefully removes the claimant representation.</li>
     *     <li>Sends a notification to the claimant if the representation is successfully removed.</li>
     * </ol>
     *
     * <p>If representation removal is unsuccessful, the original {@link CaseData}
     * is returned unchanged.</p>
     *
     * @param caseDetails the case details containing identifiers and case data;
     *                    may be {@code null}
     * @return the updated {@link CaseData} if representation was successfully removed,
     *         the existing {@link CaseData} if no action was taken or removal failed,
     *         or {@code null} if {@code caseDetails} is {@code null}
     */
    public CaseData removeConflictingClaimantRepresentation(CaseDetails caseDetails) {
        if (ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isEmpty(caseDetails.getCaseId())
                || StringUtils.isBlank(caseDetails.getCaseTypeId())
                || StringUtils.isBlank(caseDetails.getJurisdiction())
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())
                || !YES.equals(caseDetails.getCaseData().getClaimantRepresentedQuestion())) {
            return ObjectUtils.isEmpty(caseDetails) ? null : caseDetails.getCaseData();
        }
        List<String> respondentRepresentativeOrganisationIds = RespondentRepresentativeUtils
                .extractValidRespondentRepresentativeOrganisationIds(caseDetails.getCaseData());
        boolean claimantRepresentativeOrganisationMatches = ClaimantRepresentativeUtils
                .isClaimantOrganisationLinkedToRespondents(
                        caseDetails.getCaseData().getRepresentativeClaimantType(),
                        respondentRepresentativeOrganisationIds);
        boolean claimantRepresentativeEmailMatches = ClaimantRepresentativeUtils
                .isClaimantRepresentativeEmailMatchedWithRespondents(caseDetails.getCaseData());
        if (!claimantRepresentativeOrganisationMatches && !claimantRepresentativeEmailMatches) {
            return caseDetails.getCaseData();
        }
        final String adminUserToken = adminUserService.getAdminUserToken();
        nocCcdService.revokeClaimantRepresentation(adminUserToken, caseDetails);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseDetails.getCaseData());
        nocNotificationService.notifyClaimantOfRepresentationRemoval(caseDetails);
        return caseDetails.getCaseData();
    }

    /**
     * Add respondent organisation policy and notice of change answer fields to the case data.
     * @param caseData case data
     * @return modified case data
     */
    public CaseData prepopulateOrgPolicyAndNoc(CaseData caseData) {
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> generatedContent =
            noticeOfChangeFieldPopulator.generate(caseData);
        caseDataAsMap.putAll(generatedContent);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    /**
     * Replace the organisation policy and relevant respondent representative mapping with
     * new respondent representative details.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case
     */
    public CaseData updateRespondentRepresentation(CaseDetails caseDetails) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        resetRespondentRepresentativeRemovedField(caseData);
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> repCollection = updateRepresentationMap(caseData, caseDetails.getCaseId());
        caseDataAsMap.putAll(repCollection);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    private static void resetRespondentRepresentativeRemovedField(CaseData caseData) {
        ChangeOrganisationRequest change = findChangeOrganisationRequest(caseData);
        SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getSelectedCode()).orElseThrow();
        RespondentSumTypeItem respondent = caseData.getRespondentCollection().get(role.getIndex());
        respondent.getValue().setRepresentativeRemoved(NO);
    }

    private static ChangeOrganisationRequest findChangeOrganisationRequest(CaseData caseData) {
        ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();
        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }
        return change;
    }

    private Map<String, Object> updateRepresentationMap(CaseData caseData, String caseId) throws IOException {

        final ChangeOrganisationRequest change = findChangeOrganisationRequest(caseData);
        String accessToken = adminUserService.getAdminUserToken();
        Optional<AuditEvent> auditEvent =
            nocCcdService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);
        Optional<UserDetails> userDetails = auditEvent
            .map(event -> adminUserService.getUserDetails(accessToken, event.getUserId()));
        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getSelectedCode()).orElseThrow();
        RespondentSumTypeItem respondent = caseData.getRespondentCollection().get(role.getIndex());
        RepresentedTypeR addedSolicitor = nocRespondentHelper.generateNewRepDetails(change, userDetails, respondent);
        addedSolicitor.setRole(role.getCaseRoleLabel());
        addedSolicitor.setRespondentId(respondent.getId());
        List<RepresentedTypeRItem> repCollection = getIfNull(caseData.getRepCollection(), new ArrayList<>());
        int repIndex = nocRespondentHelper.getIndexOfRep(respondent, repCollection);
        if (repIndex >= 0) {
            repCollection.get(repIndex).setValue(addedSolicitor);
        } else {
            //assumption is NOC will take care of replacing value in org policy
            RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
            representedTypeRItem.setValue(addedSolicitor);
            repCollection.add(representedTypeRItem);
        }
        return Map.of(SolicitorRole.CASE_FIELD, repCollection);
    }

    /**
     * Add respondent representative organisation address to the case data.
     * @param caseData case data
     * @return modified case data
     */
    public CaseData prepopulateOrgAddress(CaseData caseData, String userToken) {
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();

        if (CollectionUtils.isEmpty(repCollection)
                || repCollection.stream()
                        .noneMatch(r -> r.getValue() != null && YES.equals(r.getValue().getMyHmctsYesNo()))) {
            return caseData;
        }
        // get all Organisation Details
        List<OrganisationsResponse> organisationList = organisationClient.getOrganisations(
                userToken, authTokenGenerator.generate());
        for (RepresentedTypeRItem representative : repCollection) {
            RepresentedTypeR representativeDetails = representative.getValue();
            if (representativeDetails != null && YES.equals(representativeDetails.getMyHmctsYesNo())) {
                Organisation repOrg = representativeDetails.getRespondentOrganisation();
                if (repOrg != null && repOrg.getOrganisationID() != null) {
                    representativeDetails.setNonMyHmctsOrganisationId(StringUtils.EMPTY);
                    // get organisation details
                    Optional<OrganisationsResponse> organisation =
                            organisationList
                                    .stream()
                                    .filter(o -> o.getOrganisationIdentifier().equals(repOrg.getOrganisationID()))
                                    .findFirst();
                    organisation.ifPresent(orgResponse -> updateRepDetails(orgResponse, representativeDetails));
                }
            }
        }
        return caseData;
    }

    private void updateRepDetails(OrganisationsResponse orgRes, RepresentedTypeR repDetails) {
        repDetails.setNameOfOrganisation(orgRes.getName());
        if (!CollectionUtils.isEmpty(orgRes.getContactInformation())) {
            Address repAddress = repDetails.getRepresentativeAddress();
            if (AddressUtils.isNullOrEmpty(repAddress)) {
                repAddress = AddressUtils.createIfNull(repDetails.getRepresentativeAddress());
                OrganisationAddress orgAddress = orgRes.getContactInformation().getFirst();
                // update Representative Address with Org Address
                repAddress.setAddressLine1(orgAddress.getAddressLine1());
                repAddress.setAddressLine2(orgAddress.getAddressLine2());
                repAddress.setAddressLine3(orgAddress.getAddressLine3());
                repAddress.setPostTown(orgAddress.getTownCity());
                repAddress.setCounty(orgAddress.getCounty());
                repAddress.setCountry(orgAddress.getCountry());
                repAddress.setPostCode(orgAddress.getPostCode());
                repDetails.setRepresentativeAddress(repAddress);
            }
        }
    }
}
