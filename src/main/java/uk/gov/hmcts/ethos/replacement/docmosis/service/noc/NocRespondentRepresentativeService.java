package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
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
import uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.NocUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_ALLOCATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.CASE_DETAILS_OR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SOLICITOR_ROLE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_MODIFY_REPRESENTATIVE_ACCESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_SET_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_ADDITION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_RETRIEVE_CASE_ASSIGNMENTS;
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
    private final UserIdamService userIdamService;
    private final OrganisationService organisationService;

    private static final String CLASS_NAME = NocRespondentRepresentativeService.class.getSimpleName();

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
                if (!RespondentRepresentativeUtils.hasOrganisation(representativeItem.getValue())) {
                    throw new GenericServiceException(EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);
                }
                nocWarnings.append(organisationService.checkRepresentativeAccountByEmail(
                        representativeItem.getValue().getNameOfRepresentative(),
                        representativeItem.getValue().getRepresentativeEmailAddress()));
            }
        }
        caseData.setNocWarning(nocWarnings.toString());
    }

    public void updateRepresentativesAccess(CallbackRequest callbackRequest, String userToken) {
        revokeOldRepresentatives(callbackRequest, userToken);
        addNewRepresentatives(callbackRequest);
        resetOrganisationPolicies(callbackRequest.getCaseDetails());
    }

    /**
     * Identifies respondent representatives removed between the previous and current case states,
     * sends removal notifications, and revokes their access by updating the case data.
     *
     * <p>This method compares the representative collections from {@code caseDetailsBefore}
     * and {@code caseDetails} to determine which representatives are no longer present.
     * For any identified representatives:
     * <ul>
     *     <li>Attempts to send representation removal notifications.</li>
     *     <li>Revokes their access to the case.</li>
     *     <li>Stores the revoked representatives in {@code repCollectionToRemove} on the updated case data.</li>
     * </ul>
     *
     * <p>If no representatives are identified for removal, the method exits without performing any action.
     * Failures during notification are logged but do not interrupt the revocation process.
     *
     * <p><b>Assumptions:</b>
     * <ul>
     *     <li>{@link CallbackRequest} contains both {@code caseDetailsBefore} and {@code caseDetails}.</li>
     *     <li>Both case details include non-null {@code CaseData} with representative collections.</li>
     *     <li>The representative collections accurately reflect the state before and after the update.</li>
     *     <li>{@code userToken} is valid and authorised to perform revocation operations.</li>
     * </ul>
     *
     * @param callbackRequest the callback request containing previous and updated case details
     * @param userToken the user authorisation token used to revoke representative access
     */
    public void revokeOldRepresentatives(CallbackRequest callbackRequest,
                                                               String userToken) {
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
        callbackRequest.getCaseDetails().getCaseData().setRepCollectionToRemove(
                revokeOldRespondentRepresentativeAccess(callbackRequest, userToken, representativesToRemove));
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
        ResponseEntity<AccountIdByEmailResponse> response =
                organisationClient.getAccountIdByEmail(
                        adminUserService.getAdminUserToken(),
                        authTokenGenerator.generate(),
                        email
                );
        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            return false;
        }
        AccountIdByEmailResponse body = response.getBody();
        if (body == null) {
            return false;
        }
        return StringUtils.isNotBlank(body.getUserIdentifier());
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
            return Collections.emptyList();
        }
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetailsBefore();
        CaseUserAssignmentData caseUserAssignments = nocCcdService.retrieveCaseUserAssignments(
                adminUserService.getAdminUserToken(), oldCaseDetails.getCaseId());
        if (ObjectUtils.isEmpty(caseUserAssignments)
                || CollectionUtils.isEmpty(caseUserAssignments.getCaseUserAssignments())) {
            return Collections.emptyList();
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
        try {
            revokeCaseAssignments(userToken, caseUserAssignmentsToRevoke);
        } catch (GenericRuntimeException e) {
            log.error(ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL, oldCaseDetails.getCaseId(), e.getMessage(), e);
            return new ArrayList<>();
        }
        return representativesToRevoke;
    }

    /**
     * Revokes respondent representatives that belong to the same organisation as the claimant's representative.
     *
     * <p>The method first validates that the required {@link CaseDetails} and associated case data are present.
     * If the claimant representative has a valid HMCTS organisation ID, the method searches for respondent
     * representatives linked to the same organisation. Any matching respondent representatives are then revoked
     * and their associated organisation policies are reset.</p>
     *
     * <p><b>Assumptions:</b></p>
     * <ul>
     *     <li>The {@code caseDetails} object may be null or incomplete; in such cases the method exits without action.
     *     </li>
     *     <li>The claimant representative contains a valid HMCTS organisation ID used to identify related
     *     representatives.</li>
     *     <li>Respondent representatives belonging to the same organisation should not remain active.</li>
     *     <li>Organisation policies must be reset after representatives are revoked.</li>
     * </ul>
     *
     * @param caseDetails the case details containing case data and representative information
     */
    public void revokeRespondentRepresentativesWithSameOrganisationAsClaimant(CaseDetails caseDetails) {
        if (ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isEmpty(caseDetails.getCaseId())
                || ObjectUtils.isEmpty(caseDetails.getCaseData())) {
            log.error(CASE_DETAILS_OR_CASE_DATA_NOT_FOUND);
            return;
        }
        if (ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())) {
            return;
        }
        String organisationId = ClaimantRepresentativeUtils.getHmctsOrganisationIdOrEmpty(caseDetails.getCaseData()
                .getRepresentativeClaimantType());
        if (StringUtils.isEmpty(organisationId)) {
            return;
        }
        revokeAndRemoveRepresentativesByOrganisation(caseDetails, organisationId);
    }

    /**
     * Finds respondent representatives associated with the given organisation and
     * delegates revocation/removal to
     * {@link #revokeAndRemoveRespondentRepresentatives(CaseDetails, List)}.
     *
     * <p>If no matching representatives are found, this method makes no changes.</p>
     *
     * <p>Representatives are removed from case data only when their access
     * revocation succeeds.</p>
     *
     * @param caseDetails the case details containing the case data to update
     * @param organisationId the organisation identifier used to find matching
     *                       respondent representatives
     */
    public void revokeAndRemoveRepresentativesByOrganisation(CaseDetails caseDetails, String organisationId) {
        List<RepresentedTypeRItem> respondentRepresentativesToRevoke = RespondentRepresentativeUtils
                .findRepresentativesByOrganisationId(caseDetails.getCaseData(), organisationId);
        if (CollectionUtils.isEmpty(respondentRepresentativesToRevoke)) {
            return;
        }
        for (RepresentedTypeRItem representative : respondentRepresentativesToRevoke) {
            RespondentSumTypeItem respondent = RespondentRepresentativeUtils.findRespondentByRepresentative(
                    caseDetails.getCaseData(), representative);
            nocNotificationService.notifyRespondentOfRepresentativeUpdate(caseDetails, respondent);
        }
        revokeAndRemoveRespondentRepresentatives(caseDetails, respondentRepresentativesToRevoke);
    }

    /**
     * Attempts to revoke access for the specified respondent representatives and
     * removes from case data only those representatives whose access was
     * successfully revoked.
     *
     * <p>Processing order:</p>
     * <ol>
     *   <li>Attempt to revoke access for the supplied representatives.</li>
     *   <li>Reset organisation policies for the representatives whose access was
     *       successfully revoked.</li>
     *   <li>Remove from case data only the representatives whose access was
     *       successfully revoked.</li>
     * </ol>
     *
     * <p>If no access revocations succeed, this method leaves respondent
     * representatives in case data unchanged.</p>
     *
     * @param caseDetails the case details containing the case data to update;
     *                    must not be {@code null}
     * @param representatives the respondent representatives for which access
     *                        revocation should be attempted; may be {@code null}
     *                        or empty
     * @throws IllegalArgumentException if {@code caseDetails} or its
     *                                  {@code caseData} is invalid
     */
    public void revokeAndRemoveRespondentRepresentatives(CaseDetails caseDetails,
                                                         List<RepresentedTypeRItem> representatives) {
        List<RepresentedTypeRItem> revokedRepresentatives = revokeRespondentRepresentatives(caseDetails,
                representatives);
        NocUtils.resetOrganisationPolicies(caseDetails.getCaseData(), revokedRepresentatives);
        RespondentRepresentativeUtils.removeRespondentRepresentatives(caseDetails.getCaseData(), representatives);
    }

    /**
     * Revokes respondent representative case assignments for the specified representatives.
     *
     * <p>This method retrieves all current case user assignments for the given case and
     * identifies those associated with respondent representative roles. If a representative
     * corresponding to the assignment exists in the provided list of representatives to revoke,
     * the assignment is marked for revocation.</p>
     *
     * <p>Only assignments linked to respondent representative roles are considered. Matching
     * representatives are resolved using
     * {@link RespondentRepresentativeUtils#findRepresentativeInListByRoleOrRespondentName(CaseData, String, List)}.</p>
     *
     * <p>If no case user assignments are found for the case, the method exits without performing
     * any revocation.</p>
     *
     * <h3>Assumptions</h3>
     * <ul>
     *     <li>{@code caseDetails} contains valid case data and a case identifier.</li>
     *     <li>The list {@code representativesToRevoke} contains representatives whose access
     *     should be removed from the case.</li>
     *     <li>Only assignments associated with respondent representative roles are eligible
     *     for revocation.</li>
     *     <li>Role validation is performed using {@link RoleUtils#isRespondentRepresentativeRole(String)}.</li>
     *     <li>Case assignments are revoked using an administrative user token.</li>
     * </ul>
     *
     * @param caseDetails the case details containing the case identifier and associated case data
     * @param representativesToRevoke the list of respondent representatives whose case assignments
     *                                should be revoked
     */
    public List<RepresentedTypeRItem> revokeRespondentRepresentatives(
            CaseDetails caseDetails, List<RepresentedTypeRItem> representativesToRevoke) {
        CaseUserAssignmentData caseUserAssignmentsData = nocCcdService.retrieveCaseUserAssignments(
                adminUserService.getAdminUserToken(), caseDetails.getCaseId());
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        if (ObjectUtils.isEmpty(caseUserAssignmentsData)
                || CollectionUtils.isEmpty(caseUserAssignmentsData.getCaseUserAssignments())) {
            return  representativesToRemove;
        }
        List<CaseUserAssignment> caseUserAssignmentsToRevoke = new ArrayList<>();
        for (CaseUserAssignment caseUserAssignment : caseUserAssignmentsData.getCaseUserAssignments()) {
            if (!RoleUtils.isRespondentRepresentativeRole(caseUserAssignment.getCaseRole())) {
                continue;
            }
            RepresentedTypeRItem representedTypeRItem = RespondentRepresentativeUtils
                    .findRepresentativeInListByRoleOrRespondentName(caseDetails.getCaseData(),
                            caseUserAssignment.getCaseRole(), representativesToRevoke);
            if (ObjectUtils.isNotEmpty(representedTypeRItem)) {
                caseUserAssignmentsToRevoke.add(caseUserAssignment);
                representativesToRemove.add(representedTypeRItem);
            }
        }
        try {
            revokeCaseAssignments(adminUserService.getAdminUserToken(), caseUserAssignmentsToRevoke);
        } catch (GenericRuntimeException e) {
            log.error(ERROR_UNABLE_TO_MODIFY_REPRESENTATIVE_ACCESS, caseDetails.getCaseId(), e.getMessage(), e);
            return new ArrayList<>();
        }
        return representativesToRemove;
    }

    private void revokeCaseAssignments(String userToken,
                                       List<CaseUserAssignment> caseUserAssignmentsToRevoke) {
        if (CollectionUtils.isNotEmpty(caseUserAssignmentsToRevoke)) {
            try {
                ccdClient.revokeCaseAssignments(userToken, CaseUserAssignmentData.builder().caseUserAssignments(
                        caseUserAssignmentsToRevoke).build());
            } catch (IOException exception) {
                throw new GenericRuntimeException(exception);
            }
        }
    }

    /**
     * Resets organisation policies for a case by removing or updating the representative
     * roles associated with the provided revoked representatives.
     *
     * <p>This method performs the following steps:
     * <ul>
     *     <li>Validates the input {@link CaseDetails} and revoked representatives list.</li>
     *     <li>Obtains an admin user token to perform privileged operations.</li>
     *     <li>Starts a CCD event to update organisation policy representative roles.</li>
     *     <li>Builds updated case data with the revoked representatives removed or updated.</li>
     *     <li>Submits the event to persist the changes in CCD.</li>
     * </ul>
     *
     * <p>If any required input is missing or invalid, the method exits without performing any action.
     *
     * @param caseDetails the case details containing case ID, type, jurisdiction, and existing case data
     * @throws GenericRuntimeException if an error occurs while communicating with CCD services
     */
    public void resetOrganisationPolicies(CaseDetails caseDetails) {
        if (ObjectUtils.isEmpty(caseDetails)
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || StringUtils.isBlank(caseDetails.getCaseTypeId())
                || StringUtils.isBlank(caseDetails.getJurisdiction())
                || StringUtils.isEmpty(caseDetails.getCaseId())
                || (CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollectionToAdd())
                && CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollectionToRemove()))) {
            return;
        }
        String adminUserToken = adminUserService.getAdminUserToken();
        try {
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken,
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    caseDetails.getCaseId(),
                    CaseEvent.UPDATE_RESP_ORG_POLICY.name());
            CaseData ccdRequestCaseData = getCCDRequestCaseData(caseDetails, ccdRequest);
            ccdClient.submitEventForCase(adminUserToken, ccdRequestCaseData, caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(), ccdRequest, caseDetails.getCaseId());
        } catch (IOException | GenericServiceException exception) {
            log.error(ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES, caseDetails.getCaseId(),
                    exception.getMessage());
            throw new GenericRuntimeException(exception);
        }
    }

    private static @NonNull CaseData getCCDRequestCaseData(CaseDetails caseDetails, CCDRequest ccdRequest)
            throws GenericServiceException {
        final String methodName = "getCCDRequestCaseData";
        if (ObjectUtils.isEmpty(ccdRequest)
                || ObjectUtils.isEmpty(ccdRequest.getCaseDetails())
                || StringUtils.isBlank(ccdRequest.getCaseDetails().getCaseId())) {
            String exceptionMessage = String.format(GenericConstants.EXCEPTION_UNABLE_TO_START_EVENT_WITHOUT_CASE_ID,
                    CaseEvent.UPDATE_RESP_ORG_POLICY.name());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    NOT_ALLOCATED, CLASS_NAME, methodName);
        }
        if (ObjectUtils.isEmpty(ccdRequest.getCaseDetails().getCaseData())) {
            String exceptionMessage = String.format(GenericConstants.EXCEPTION_UNABLE_TO_START_EVENT_WITH_CASE_ID,
                    CaseEvent.UPDATE_RESP_ORG_POLICY.name(), ccdRequest.getCaseDetails().getCaseId());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    NOT_ALLOCATED, CLASS_NAME, methodName);
        }
        CaseData ccdRequestCaseData = ccdRequest.getCaseDetails().getCaseData();
        // Sets representatives to remove their remaining organisation policies, roles and
        // changeOrganisationRequestField from case data
        ccdRequestCaseData.setRepCollectionToRemove(caseDetails.getCaseData().getRepCollectionToRemove());
        ccdRequestCaseData.setRepCollectionToAdd(caseDetails.getCaseData().getRepCollectionToAdd());
        return ccdRequestCaseData;
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
        nocNotificationService.sendRespondentRepresentationUpdateNotifications(newCaseDetails,
                newOrUpdatedRepresentatives, NOC_TYPE_ADDITION);
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
        caseDetails.getCaseData().setRepCollectionToAdd(new ArrayList<>());
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
                    RepresentedTypeRItem caseRepresentative = RespondentRepresentativeUtils.findRepresentativeById(
                            caseDetails.getCaseData(), representative.getId());
                    if (RespondentRepresentativeUtils.isValidRepresentative(caseRepresentative)) {
                        representative.getValue().setRole(role);
                        caseDetails.getCaseData().getRepCollectionToAdd().add(caseRepresentative);
                    } else {
                        log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND,
                                caseDetails.getCaseId());
                        return;
                    }
                } catch (GenericServiceException gse) {
                    log.error(ERROR_UNABLE_TO_SET_ROLE, role, caseDetails.getCaseId(), gse.getMessage());
                }
            }
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
     * Finds all respondent representatives linked to the given user for a specific case.
     *
     * <p>The method performs a series of validations on the input parameters. If the user token,
     * case details, case ID, case data, or representative collection is missing or invalid,
     * an empty list is returned.</p>
     *
     * <p>It retrieves the user details using the provided token and then looks up the case user
     * assignments. From these assignments, it filters those where the user has a respondent
     * representative role. For each matching assignment, the corresponding representative
     * is located within the case data and added to the result.</p>
     *
     * <p>If any step fails (e.g. user details not found, assignments not available, or no matching
     * representatives), the method safely returns an empty list.</p>
     *
     * @param userToken   the IDAM user authentication token
     * @param caseDetails the case details containing case ID and case data
     * @return a list of {@link RepresentedTypeRItem} representing the respondent representatives
     *         associated with the user; returns an empty list if none are found or inputs are invalid
     */
    public List<RepresentedTypeRItem> findRepresentativesByToken(String userToken, CaseDetails caseDetails) {
        if (StringUtils.isBlank(userToken)
                || ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isBlank(caseDetails.getCaseId())
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())) {
            return Collections.emptyList();
        }
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        if (ObjectUtils.isEmpty(userDetails) || StringUtils.isBlank(userDetails.getUid())) {
            return Collections.emptyList();
        }
        CaseUserAssignmentData caseUserAssignmentData = null;
        try {
            caseUserAssignmentData = nocCcdService.retrieveCaseUserAssignments(userToken, caseDetails.getCaseId());
        } catch (CcdInputOutputException exception) {
            log.warn(WARNING_FAILED_TO_RETRIEVE_CASE_ASSIGNMENTS, caseDetails.getCaseId(),
                    exception.getMessage());
        }
        if (ObjectUtils.isEmpty(caseUserAssignmentData)
                || CollectionUtils.isEmpty(caseUserAssignmentData.getCaseUserAssignments())) {
            return Collections.emptyList();
        }
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        for (CaseUserAssignment caseUserAssignment : caseUserAssignmentData.getCaseUserAssignments()) {
            if (!RoleUtils.isRespondentRepresentativeRole(caseUserAssignment.getCaseRole())
                    || !userDetails.getUid().equals(caseUserAssignment.getUserId())) {
                continue;
            }
            RepresentedTypeRItem representative =
                    RespondentRepresentativeUtils.findRepresentativeByRoleOrRespondentName(caseDetails.getCaseData(),
                            caseUserAssignment.getCaseRole());
            if (ObjectUtils.isNotEmpty(representative)) {
                representatives.add(representative);
            }
        }
        return representatives;
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

    /**
     * Validates that each eligible representative's selected respondent organisation matches
     * the organisation associated with that representative's user account.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>{@code caseDetails}, {@code caseDetails.getCaseData()}, and the representative entries
     *       in {@code repCollection} are non-null.</li>
     *   <li>{@code RespondentRepresentativeUtils.isValidRepresentative(...)} safely determines
     *       whether a representative has the minimum required data for validation.</li>
     *   <li>A representative should only be organisation-validated when they have a non-blank
     *       email address, {@code myHmctsYesNo} is {@code YES}, and a respondent organisation
     *       has been selected.</li>
     *   <li>If a user cannot be found in IDAM or their organisation cannot be retrieved,
     *       the method treats that representative as not valid for organisation comparison
     *       and does not add a validation error.</li>
     *   <li>{@code OrganisationUtils.hasMatchingOrganisationId(...)} performs the authoritative
     *       comparison between the selected organisation and the organisation returned for the user.</li>
     * </ul>
     *
     * <p>The validation is performed only for representatives that:
     * <ul>
     *   <li>are considered valid representatives,</li>
     *   <li>have a non-blank representative email address,</li>
     *   <li>have MyHMCTS enabled, and</li>
     *   <li>have a respondent organisation present.</li>
     * </ul>
     *
     * <p>For each such representative, the method looks up the user by email address and then
     * retrieves the organisation linked to that user. If the selected respondent organisation
     * does not match the organisation returned for the user, a validation error is added.
     *
     * <p>If the representative cannot be found or their organisation cannot be retrieved
     * (for example, due to a {@code GenericServiceException}), organisation matching is skipped
     * for that representative and no error is added.
     *
     * @param caseDetails the case details containing representative information to validate
     * @return a list of validation error messages; empty if there are no representatives to
     *     validate or no organisation mismatches are found
     */
    public List<String> validateRespondentRepresentativesOrganisationMatch(CaseDetails caseDetails) {
        List<String> errors = new ArrayList<>();
        if (CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())) {
            return errors;
        }
        for (RepresentedTypeRItem representative : caseDetails.getCaseData().getRepCollection()) {
            if (RespondentRepresentativeUtils.isValidRepresentative(representative)
                    && StringUtils.isNotBlank(representative.getValue().getRepresentativeEmailAddress())
                    && YES.equals(representative.getValue().getMyHmctsYesNo())
                    && ObjectUtils.isNotEmpty(representative.getValue().getRespondentOrganisation())) {
                AccountIdByEmailResponse userResponse;
                OrganisationsResponse organisationsResponse = null;
                boolean isValidUserAndOrganisation = true;
                try {
                    String accessToken = adminUserService.getAdminUserToken();
                    userResponse = nocService.findUserByEmail(accessToken,
                            representative.getValue().getRepresentativeEmailAddress(), caseDetails.getCaseId());
                    organisationsResponse = nocService.findOrganisationByUserId(accessToken,
                            userResponse.getUserIdentifier(), caseDetails.getCaseId());
                } catch (GenericServiceException e) {
                    // if user is not defined on idam should not check for organisation.
                    isValidUserAndOrganisation = false;
                }
                if (isValidUserAndOrganisation
                        && !OrganisationUtils.hasMatchingOrganisationId(
                        representative.getValue().getRespondentOrganisation(), organisationsResponse)) {
                    errors.add(String.format(ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES,
                            representative.getValue().getNameOfRepresentative(),
                            representative.getValue().getRespondentOrganisation().getOrganisationID()));
                }
            }
        }
        return errors;
    }
}
