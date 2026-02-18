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
        List<RepresentedTypeRItem> representativesToRemove = RespondentRepresentativeUtils
                .findRepresentativesToRemove(oldRepresentatives, newRepresentatives);
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
        if (CollectionUtils.isEmpty(representatives)) {
            return new ArrayList<>();
        }
        List<RepresentedTypeRItem> assignableRepresentatives = RespondentRepresentativeUtils
                .filterModifiableRepresentatives(representatives);
        if (CollectionUtils.isEmpty(assignableRepresentatives)
                || ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isEmpty(caseDetails.getCaseId())) {
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
        assignableRepresentatives.removeAll(representativesToRemove);
        return assignableRepresentatives;
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
            if (!RespondentRepresentativeUtils.isValidRepresentative(representative)) {
                continue;
            }
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
     * Removes the claimant representative from the case if the same organisation
     * already exists as a respondent representative.
     *
     * <p>This method performs a series of validation checks before attempting removal:
     * <ul>
     *     <li>Ensures {@code caseDetails}, {@code caseId}, and {@code caseData} are not null or empty.</li>
     *     <li>Verifies that a claimant is marked as represented ({@code YES}).</li>
     *     <li>Ensures claimant representative details and organisation identifiers are present
     *     (either via {@code MyHmctsOrganisation.organisationID} or {@code organisationId}).</li>
     *     <li>Ensures that respondent representatives exist on the case.</li>
     * </ul>
     *
     * <p>If the claimant representative's organisation ID matches any organisation ID
     * in the respondent representatives list, the claimant representation is removed
     * using {@code nocCcdService.removeClaimantRepresentation(...)} with an admin user token.
     *
     * <p>No action is taken if:
     * <ul>
     *     <li>Mandatory case data is missing,</li>
     *     <li>The claimant is not represented,</li>
     *     <li>The claimant representative has no valid organisation identifier, or</li>
     *     <li>No matching organisation is found among respondent representatives.</li>
     * </ul>
     *
     * @param caseDetails the {@link CaseDetails} containing the case data and representation details
     */
    public void removeClaimantRepresentativeIfOrganisationExistsInRespondent(CaseDetails caseDetails) {
        if (ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isEmpty(caseDetails.getCaseId())
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())
                || !YES.equals(caseDetails.getCaseData().getClaimantRepresentedQuestion())
                || ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType())
                || (ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType()
                .getMyHmctsOrganisation())
                || StringUtils.isBlank(caseDetails.getCaseData().getRepresentativeClaimantType()
                .getMyHmctsOrganisation().getOrganisationID()))
                && StringUtils.isBlank(caseDetails.getCaseData().getRepresentativeClaimantType().getOrganisationId())) {
            return;
        }
        List<String> respondentRepresentativeOrganisationIds = RespondentRepresentativeUtils
                .extractValidRespondentRepresentativeOrganisationIds(caseDetails.getCaseData());
        if (CollectionUtils.isEmpty(respondentRepresentativeOrganisationIds)) {
            return;
        }
        boolean claimantRepresentativeExists = ClaimantRepresentativeUtils
                .isClaimantRepresentativeOrganisationInRespondentOrganisations(
                        caseDetails.getCaseData().getRepresentativeClaimantType(),
                        respondentRepresentativeOrganisationIds);
        if (!claimantRepresentativeExists) {
            return;
        }
        nocCcdService.removeClaimantRepresentation(adminUserService.getAdminUserToken(), caseDetails);
        nocNotificationService.notifyClaimantOfRepresentationRemoval(caseDetails);
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
