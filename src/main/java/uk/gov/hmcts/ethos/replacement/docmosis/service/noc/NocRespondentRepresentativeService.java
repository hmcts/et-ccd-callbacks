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
import uk.gov.hmcts.ethos.replacement.docmosis.domain.noc.RepresentativesCaseAssignments;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.MyHmctsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.CASE_DETAILS_OR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SOLICITOR_ROLE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_MODIFY_REPRESENTATIVE_ACCESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_REVOKE_RESPONDENT_REPRESENTATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_SET_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_ADDITION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_FIND_ORGANISATION_BY_EMAIL_SYSTEM_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_RETRIEVE_CASE_ASSIGNMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_EMAIL_ADDRESS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils.getOrganisationAddressAsText;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils.mapOrganisationAddressToAddress;

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
    private final MyHmctsService myHmctsService;

    private static final String CLASS_NAME = NocRespondentRepresentativeService.class.getSimpleName();

    /**
     * Validates respondent representatives' organisation and email details and returns
     * any warning messages identified during validation.
     *
     * <p>This method iterates through the respondent representatives in the case data and
     * checks only those representatives who are valid and marked as MyHMCTS users.
     *
     * <p>For each applicable representative:
     * <ul>
     *   <li>if the representative email address is missing, a warning is added and
     *       validation continues with the next representative;</li>
     *   <li>if the representative organisation is missing, a
     *       {@link GenericServiceException} is thrown;</li>
     *   <li>otherwise, the representative account is checked by email and any warnings
     *       returned by the organisation service are added to the result.</li>
     * </ul>
     *
     * @param caseData the case data containing the respondent representatives to validate
     * @return a list of warning messages for representatives whose email address is missing
     *     or whose account could not be confirmed by email; an empty list if no warnings
     *     are identified
     * @throws GenericServiceException if a valid MyHMCTS representative does not have an
     *     associated organisation
     */
    public List<String> validateRepresentativesOrganisationsAndEmails(CaseData caseData)
            throws GenericServiceException {
        List<String> warnings = new ArrayList<>();
        if (!RespondentRepresentativeUtils.hasRepresentatives(caseData)) {
            return warnings;
        }
        for (RepresentedTypeRItem representativeItem :  caseData.getRepCollection()) {
            if (RespondentRepresentativeUtils.isValidRepresentative(representativeItem)
                    && YES.equals(representativeItem.getValue().getMyHmctsYesNo())) {
                if (StringUtils.isBlank(representativeItem.getValue().getRepresentativeEmailAddress())) {
                    warnings.add(WARNING_REPRESENTATIVE_EMAIL_ADDRESS_NOT_FOUND);
                    continue;
                }
                if (!RespondentRepresentativeUtils.hasOrganisation(representativeItem.getValue())) {
                    throw new GenericServiceException(EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);
                }
                warnings.addAll(organisationService.checkRepresentativeAccountByEmail(
                        representativeItem.getValue().getNameOfRepresentative(),
                        representativeItem.getValue().getRepresentativeEmailAddress()));
            }
        }
        return warnings;
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
                // Assignments automatically created by CCD for representatives linked to other respondents.
                // These are not recorded in the representative collection, but are added to support same company's
                // representatives. This auto assignments business is still in architectural design check.
                List<CaseUserAssignment> remainingCaseUserAssignments = new ArrayList<>(caseUserAssignments
                        .getCaseUserAssignments());
                remainingCaseUserAssignments.remove(caseUserAssignment);
                ClaimantRepresentativeUtils.removeClaimantRepresentativeAssignment(remainingCaseUserAssignments);
                List<CaseUserAssignment> otherAssignmentsToRemove =
                        findCaseAssignmentsToRevokeForRep(callbackRequest.getCaseDetails().getCaseId(),
                                remainingCaseUserAssignments, representative, caseUserAssignment.getCaseRole());
                if (CollectionUtils.isNotEmpty(otherAssignmentsToRemove)) {
                    caseUserAssignmentsToRevoke.addAll(otherAssignmentsToRemove);
                }
            }
        }
        try {
            revokeCaseAssignments(userToken, caseUserAssignmentsToRevoke);
        } catch (GenericRuntimeException e) {
            log.error(ERROR_UNABLE_TO_REVOKE_RESPONDENT_REPRESENTATION, oldCaseDetails.getCaseId(), e.getMessage(), e);
            return new ArrayList<>();
        }
        return representativesToRevoke;
    }

    /**
     * Finds the case user assignments that should be revoked for the supplied representative.
     *
     * <p>The method validates that the case ID, case user assignments and representative details
     * are present. The representative must have either an IDAM ID or a representative email
     * address that can be used to resolve the IDAM ID. If the required data is missing, or the
     * representative IDAM ID cannot be resolved, an empty list is returned.</p>
     *
     * <p>The method first finds the representative's existing assignments by IDAM ID. It then
     * identifies assignments for revocation where the representative has an assignment for a
     * case role that is also assigned to more than one user. These assignments are removed from
     * the supplied {@code caseUserAssignments} list before any remaining assignments for the
     * supplied {@code role} are also added to the revocation list.</p>
     *
     * <p><strong>Note:</strong> this method mutates the supplied {@code caseUserAssignments}
     * list by removing assignments that have already been identified for revocation.</p>
     *
     * @param caseId the ID of the case, also used as the submission reference when resolving
     *               the representative's IDAM ID
     * @param caseUserAssignments the current case user assignments for the case
     * @param representative the representative whose assignments should be checked for revocation
     * @param role the case role whose remaining assignments should also be revoked
     * @return a list of {@link CaseUserAssignment} entries to revoke, or an empty list if the
     *         required data is missing or the representative IDAM ID cannot be resolved
     */
    public List<CaseUserAssignment> findCaseAssignmentsToRevokeForRep(String caseId,
                                                                      List<CaseUserAssignment> caseUserAssignments,
                                                                      RepresentedTypeRItem representative,
                                                                      String role) {
        if (StringUtils.isBlank(caseId)
                || CollectionUtils.isEmpty(caseUserAssignments)
                || ObjectUtils.isEmpty(representative)
                || ObjectUtils.isEmpty(representative.getValue())
                || (StringUtils.isBlank(representative.getValue().getRepresentativeEmailAddress())
                && StringUtils.isBlank(representative.getValue().getIdamId()))) {
            return new ArrayList<>();
        }
        String idamId = resolveRepresentativeIdamId(representative, caseId);
        if (StringUtils.isBlank(idamId)) {
            return new ArrayList<>();
        }
        List<CaseUserAssignment> representativeRemainingAssignments = RespondentRepresentativeUtils
                .findCaseUserAssignmentsByRepresentativeIdamId(caseUserAssignments, idamId);
        List<CaseUserAssignment> caseUserAssignmentsToRevoke = new ArrayList<>();
        // finds representative's other case assignments
        for (CaseUserAssignment caseUserAssignment : representativeRemainingAssignments) {
            if (NocUtils.countAssignmentsByRole(caseUserAssignments, caseUserAssignment.getCaseRole()) > 1) {
                caseUserAssignmentsToRevoke.add(caseUserAssignment);
            }
        }
        caseUserAssignments.removeAll(caseUserAssignmentsToRevoke);
        // finds role's other case assignments
        List<CaseUserAssignment> caseUserAssignmentsByRole =
                NocUtils.filterCaseUserAssignmentsByRole(caseUserAssignments, role);
        if (CollectionUtils.isNotEmpty(caseUserAssignmentsByRole)) {
            caseUserAssignmentsToRevoke.addAll(caseUserAssignmentsByRole);
        }
        return caseUserAssignmentsToRevoke;
    }

    /**
     * Resolves the IDAM ID for the supplied representative.
     *
     * <p>The method first attempts to use the IDAM ID already held on the representative.
     * If the IDAM ID is blank, it attempts to retrieve the user identifier by looking up
     * the representative's email address through the NOC service.</p>
     *
     * <p>If the lookup fails with a {@link GenericServiceException}, the exception message
     * is logged as a warning and the unresolved IDAM ID value is returned.</p>
     *
     * <p>Assumptions:</p>
     * <ul>
     *     <li>{@code representative} is not {@code null}.</li>
     *     <li>{@code representative.getValue()} is not {@code null}.</li>
     *     <li>The representative value contains an email address that can be used for lookup
     *     when the IDAM ID is missing.</li>
     *     <li>{@code submissionReference} identifies the submission context for the lookup.</li>
     * </ul>
     *
     * @param representative the representative whose IDAM ID should be resolved
     * @param submissionReference the submission reference used when looking up the user by email
     * @return the existing or resolved representative IDAM ID, or the original blank value if
     *         the IDAM ID cannot be resolved
     */
    public String resolveRepresentativeIdamId(RepresentedTypeRItem representative, String submissionReference) {
        String idamId = representative.getValue().getIdamId();
        if (StringUtils.isBlank(idamId)) {
            try {
                AccountIdByEmailResponse accountIdByEmailResponse = nocService.findUserByEmail(
                        adminUserService.getAdminUserToken(),
                        representative.getValue().getRepresentativeEmailAddress(),
                        submissionReference);
                idamId = accountIdByEmailResponse.getUserIdentifier();
            } catch (GenericServiceException e) {
                log.warn(e.getMessage());
            }
        }
        return idamId;
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
        if (!CaseDataUtils.areCaseDetailsValid(caseDetails)) {
            log.error(CASE_DETAILS_OR_CASE_DATA_NOT_FOUND);
            return;
        }
        if (ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRespondentCollection())) {
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
     * Revokes and removes all respondent representatives associated with the specified organisation
     * from the provided case details.
     * <p>
     * If no respondent representatives are found for the given organisation ID, this method returns
     * without making any changes.
     *
     * @param caseDetails the case details containing the case data to search and update
     * @param organisationId the organisation ID used to identify respondent representatives to revoke
     */
    public void revokeAndRemoveRepresentativesByOrganisation(CaseDetails caseDetails, String organisationId) {
        List<RepresentedTypeRItem> respondentRepresentativesToRevoke = RespondentRepresentativeUtils
                .findRepresentativesByOrganisationId(caseDetails.getCaseData(), organisationId);
        if (CollectionUtils.isEmpty(respondentRepresentativesToRevoke)) {
            return;
        }
        revokeAndRemoveRespondentRepresentatives(caseDetails, respondentRepresentativesToRevoke);
    }

    /**
     * Revokes and removes respondent representatives from the case, assuming that
     * revocation must be attempted before any representative is removed from case data.
     *
     * <p>Assumptions:</p>
     * <ul>
     *   <li>{@code caseDetails} is not {@code null} and contains valid case data.</li>
     *   <li>{@code representatives} is not {@code null} and contains the respondent
     *       representatives being processed.</li>
     *   <li>{@link #revokeRespondentRepresentatives(CaseDetails, List)} returns a non-null
     *       {@code RepresentativesCaseAssignments} result.</li>
     *   <li>If revoked case-user assignments are present, the corresponding representatives in
     *       {@code getRepresentativesToRemove()} are considered successfully revoked and should
     *       have their organisation policies reset.</li>
     *   <li>If no revoked case-user assignments are present, the representatives returned in
     *       {@code getRepresentativesToRemove()} should not be removed from case data in this call.</li>
     *   <li>Resetting organisation policies is only required for representatives whose assignments
     *       were successfully revoked.</li>
     * </ul>
     *
     * <p>Behaviour:</p>
     * <ul>
     *   <li>Attempts to revoke case-user assignments for the supplied representatives.</li>
     *   <li>Builds the final list of representatives to remove from case data based on the revocation result.</li>
     *   <li>Resets organisation policies for revoked representatives.</li>
     *   <li>Removes the remaining applicable representatives from the case data.</li>
     * </ul>
     *
     * @param caseDetails the case details containing the case data to update
     * @param representatives the respondent representatives to be revoked and removed
     */
    public void revokeAndRemoveRespondentRepresentatives(CaseDetails caseDetails,
                                                         List<RepresentedTypeRItem> representatives) {
        RepresentativesCaseAssignments representativesCaseAssignments = revokeRespondentRepresentatives(caseDetails,
                representatives);
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>(representatives);
        if (CollectionUtils.isNotEmpty(representativesCaseAssignments.getRevokedCaseUserAssignments())) {
            NocUtils.resetOrganisationPolicies(caseDetails.getCaseData(),
                    representativesCaseAssignments.getRepresentativesToRemove());
        } else {
            representativesToRemove.removeAll(representativesCaseAssignments.getRepresentativesToRemove());
        }
        RespondentRepresentativeUtils.removeRespondentRepresentatives(caseDetails.getCaseData(),
                representativesToRemove);
    }

    /**
     * Revokes respondent representative case-user assignments for the given case and
     * returns the representatives and assignments identified for removal.
     *
     * <p>The method retrieves all case-user assignments for the case, filters them to
     * respondent representative roles, and attempts to match each assignment against the
     * supplied list of representatives to revoke. Where a match is found, both the
     * corresponding {@link CaseUserAssignment} and {@link RepresentedTypeRItem} are collected
     * into the returned {@link RepresentativesCaseAssignments}.
     *
     * <p>If no case-user assignments exist for the case, an empty
     * {@link RepresentativesCaseAssignments} is returned.
     *
     * <p>Once matching assignments have been identified, the method attempts to revoke them.
     * If revocation fails with a {@link GenericRuntimeException}, the error is logged and the
     * returned {@code revokedCaseUserAssignments} list is reset to empty. The matched
     * {@code representativesToRemove} list remains unchanged.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>{@code caseDetails} is non-null and contains a valid case ID and case data.</li>
     *   <li>{@code representativesToRevoke} is expected to contain respondent representatives
     *       relevant to the case.</li>
     *   <li>{@link RespondentRepresentativeUtils#findRepresentativeInListByRoleOrRespondentName}
     *       can safely evaluate the provided representative list for matching by case role or
     *       respondent name.</li>
     *   <li>Only case-user assignments with respondent representative roles are eligible
     *       for revocation.</li>
     * </ul>
     *
     * @param caseDetails the case whose respondent representative assignments are to be evaluated
     * @param representativesToRevoke the list of respondent representatives that should be matched
     *                                and revoked where corresponding case-user assignments exist
     * @return a {@link RepresentativesCaseAssignments} containing the matched representatives to remove
     *         and the case-user assignments identified for revocation
     */
    public RepresentativesCaseAssignments revokeRespondentRepresentatives(
            CaseDetails caseDetails, List<RepresentedTypeRItem> representativesToRevoke) {
        RepresentativesCaseAssignments representativesCaseAssignments = RepresentativesCaseAssignments.builder()
                .representativesToRemove(new ArrayList<>()).revokedCaseUserAssignments(new ArrayList<>()).build();
        if (ObjectUtils.isEmpty(caseDetails)
                || StringUtils.isBlank(caseDetails.getCaseId())
                || ObjectUtils.isEmpty(caseDetails.getCaseData())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())
                || CollectionUtils.isEmpty(caseDetails.getCaseData().getRespondentCollection())
                || CollectionUtils.isEmpty(representativesToRevoke)) {
            return representativesCaseAssignments;
        }
        CaseUserAssignmentData caseUserAssignmentsData = nocCcdService.retrieveCaseUserAssignments(
                adminUserService.getAdminUserToken(), caseDetails.getCaseId());
        if (ObjectUtils.isEmpty(caseUserAssignmentsData)
                || CollectionUtils.isEmpty(caseUserAssignmentsData.getCaseUserAssignments())) {
            return  representativesCaseAssignments;
        }
        List<CaseUserAssignment> caseUserAssignments =
                new ArrayList<>(caseUserAssignmentsData.getCaseUserAssignments());
        List<CaseUserAssignment> manualAssignments = new ArrayList<>();
        for (RepresentedTypeRItem representative : representativesToRevoke) {
            manualAssignments.addAll(RespondentRepresentativeUtils.findManualAssignments(
                    caseDetails.getCaseData(), caseUserAssignments, representative));
            if (CollectionUtils.isNotEmpty(manualAssignments)) {
                representativesCaseAssignments.getRevokedCaseUserAssignments().addAll(manualAssignments);
                representativesCaseAssignments.getRepresentativesToRemove().add(representative);
                caseUserAssignments.removeAll(manualAssignments);
            }
            List<CaseUserAssignment> autoAssignments = RespondentRepresentativeUtils.findAutoAssignments(
                    representative, manualAssignments, caseUserAssignments);
            if (CollectionUtils.isNotEmpty(autoAssignments)) {
                representativesCaseAssignments.getRevokedCaseUserAssignments().addAll(autoAssignments);
            }
        }
        try {
            revokeCaseAssignments(adminUserService.getAdminUserToken(),
                    representativesCaseAssignments.getRevokedCaseUserAssignments());
        } catch (GenericRuntimeException e) {
            log.error(ERROR_UNABLE_TO_MODIFY_REPRESENTATIVE_ACCESS, caseDetails.getCaseId(), e.getMessage(), e);
            if (CollectionUtils.isEmpty(manualAssignments)) {
                representativesCaseAssignments.setRepresentativesToRemove(Collections.emptyList());
            }
            representativesCaseAssignments.setRevokedCaseUserAssignments(Collections.emptyList());
        }
        return representativesCaseAssignments;
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
        if (!CaseDataUtils.areCaseDetailsValid(caseDetails)
                || StringUtils.isBlank(caseDetails.getCaseTypeId())
                || StringUtils.isBlank(caseDetails.getJurisdiction())
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
     * Grants MyHMCTS access to the supplied respondent representatives and records the
     * successfully processed representatives for later organisation policy updates.
     *
     * <p>This method first validates the input and returns immediately if the case details,
     * case data, case ID, or representatives list is missing. It then resets
     * {@code repCollectionToAdd} on the case data and processes each supplied representative.
     *
     * <p>For each valid representative, the method:
     * <ul>
     *   <li>derives the solicitor role to assign;</li>
     *   <li>grants representative access through the NOC service;</li>
     *   <li>locates the corresponding representative in the case data; and</li>
     *   <li>adds that representative to {@code repCollectionToAdd} after setting the derived role.</li>
     * </ul>
     *
     * <p>If no role can be derived for a representative, an error is logged and processing
     * stops for any remaining representatives. If access cannot be granted for a specific
     * representative, the error is logged and processing continues with the next one. If the
     * matching representative cannot be found in the case data after access is granted, an
     * error is logged and the method exits.
     *
     * @param caseDetails the case details containing the case ID and case data to update
     * @param representatives the respondent representatives to be granted access
     */
    public void grantRespondentRepresentativesAccess(CaseDetails caseDetails,
                                                     List<RepresentedTypeRItem> representatives) {
        if (!CaseDataUtils.areCaseDetailsValid(caseDetails)
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
                    String representativeIdamId = nocService.grantRepresentativeAccess(
                            adminUserService.getAdminUserToken(),
                            representative.getValue().getRepresentativeEmailAddress(), caseDetails.getCaseId(),
                            representative.getValue().getRespondentOrganisation(), role);
                    RepresentedTypeRItem caseRepresentative = RespondentRepresentativeUtils.findRepresentativeById(
                            caseDetails.getCaseData(), representative.getId());
                    if (RespondentRepresentativeUtils.isValidRepresentative(caseRepresentative)) {
                        representative.getValue().setRole(role);
                        representative.getValue().setIdamId(representativeIdamId);
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
     * Removes claimant representation when it conflicts with respondent representative details.
     *
     * <p>This method checks whether the claimant representative is linked to any respondent
     * representative either by organisation or by email address. If a conflict is found, the
     * claimant representation is revoked in CCD and the claimant is marked as unrepresented
     * in the case data.</p>
     *
     * <p>If the supplied {@link CaseDetails} is missing required information, or if the claimant
     * is not represented, the method does not perform any revocation and returns the existing
     * case data unchanged. If {@code caseDetails} is {@code null} or empty, {@code null} is
     * returned.</p>
     *
     * <p><strong>Assumptions:</strong></p>
     * <ul>
     *     <li>A claimant representation conflict exists when the claimant representative
     *     organisation matches one of the respondent representative organisations, or when
     *     the claimant representative email matches a respondent representative email.</li>
     *     <li>The claimant is considered represented only when
     *     {@code claimantRepresentedQuestion} is equal to {@code YES}.</li>
     *     <li>{@code caseId}, {@code caseTypeId}, {@code jurisdiction}, {@code caseData}, and
     *     {@code repCollection} are required before attempting to revoke representation.</li>
     *     <li>The CCD revocation is performed using an admin user token.</li>
     *     <li>If the CCD revocation service call fails, the exception is expected to propagate
     *     to the caller, as no rollback or local error handling is performed in this method.</li>
     *     <li>The returned {@link CaseData} is the same mutable instance contained in the
     *     supplied {@link CaseDetails}.</li>
     * </ul>
     *
     * @param caseDetails the case details containing case metadata and case data to validate
     *                    and update
     * @return the updated {@link CaseData}; the original case data if no conflict is found or
     *         validation fails; or {@code null} when {@code caseDetails} is {@code null} or empty
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
        CaseUserAssignmentData caseUserAssignmentData = getCaseUserAssignmentData(adminUserService.getAdminUserToken(),
                caseDetails.getCaseId()).orElse(null);
        if (ObjectUtils.isEmpty(caseUserAssignmentData)
                || CollectionUtils.isEmpty(caseUserAssignmentData.getCaseUserAssignments())) {
            return Collections.emptyList();
        }
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        for (CaseUserAssignment caseUserAssignment : caseUserAssignmentData.getCaseUserAssignments()) {
            if (userDetails.getUid().equals(caseUserAssignment.getUserId())) {
                RepresentedTypeRItem representative = RespondentRepresentativeUtils.findRepresentativeByIdamIdOrRole(
                        caseDetails.getCaseData(), caseUserAssignment.getUserId(), caseUserAssignment.getCaseRole());
                if (!RespondentRepresentativeUtils.isValidRepresentative(representative)) {
                    continue;
                }
                representative.getValue().setIdamId(userDetails.getUid());
                if (checkRepresentativeAssignment(caseDetails, representative, caseUserAssignment)) {
                    representatives.add(representative);
                }
            }
        }
        return representatives;
    }

    public boolean checkRepresentativeAssignment(CaseDetails caseDetails,
                                                  RepresentedTypeRItem representative,
                                                  CaseUserAssignment caseUserAssignment) {
        return RespondentRepresentativeUtils.isValidRepresentative(representative)
                && (matchesRepresentativeIdamId(caseDetails.getCaseId(), representative, caseUserAssignment)
                || RespondentRepresentativeUtils.isCaseUserAssignmentForRepresentativeByRespondentName(
                        caseDetails.getCaseData(), representative, caseUserAssignment)
                || RespondentRepresentativeUtils.isCaseUserAssignmentForRepresentativeByRespondentId(
                        caseDetails.getCaseData(), representative, caseUserAssignment));
    }

    /**
     * Determines whether the supplied case user assignment belongs to the supplied representative.
     *
     * <p>The method resolves the representative's IDAM ID, using the existing IDAM ID where
     * available or the representative's email address as a fallback. If both the representative
     * email address and IDAM ID are blank, or the IDAM ID cannot be resolved, the method returns
     * {@code false}.</p>
     *
     * <p>The resolved representative IDAM ID is compared with the user ID on the supplied
     * {@link CaseUserAssignment}.</p>
     *
     * <p>Assumptions:</p>
     * <ul>
     *     <li>{@code representative} is not {@code null}.</li>
     *     <li>{@code representative.getValue()} is not {@code null}.</li>
     *     <li>{@code caseUserAssignment} is not {@code null}.</li>
     *     <li>{@code submissionReference} identifies the submission context used when resolving
     *     the representative's IDAM ID by email.</li>
     * </ul>
     *
     * @param submissionReference the submission reference used when resolving the representative's IDAM ID
     * @param representative the representative to compare against the case user assignment
     * @param caseUserAssignment the case user assignment to check
     * @return {@code true} if the resolved representative IDAM ID matches the assignment user ID;
     *         otherwise {@code false}
     */
    public boolean matchesRepresentativeIdamId(String submissionReference,
                                               RepresentedTypeRItem representative,
                                               CaseUserAssignment  caseUserAssignment) {
        if (StringUtils.isBlank(representative.getValue().getRepresentativeEmailAddress())
                && StringUtils.isBlank(representative.getValue().getIdamId())) {
            return false;
        }
        String idamId = resolveRepresentativeIdamId(representative, submissionReference);
        if (StringUtils.isBlank(idamId)) {
            return false;
        }
        return idamId.equals(caseUserAssignment.getUserId());
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
        auditEvent.ifPresent(event -> addedSolicitor.setIdamId(event.getUserId()));
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
     *   <li>have My HMCTS enabled, and</li>
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
                    log.warn(WARNING_FAILED_TO_FIND_ORGANISATION_BY_EMAIL_SYSTEM_ERROR, e.getMessage());
                    // if user is not defined on idam should not check for organisation.
                    isValidUserAndOrganisation = false;
                }
                if (isValidUserAndOrganisation
                        && !OrganisationUtils.hasMatchingOrganisationId(
                        representative.getValue().getRespondentOrganisation(), organisationsResponse)) {
                    errors.add(String.format(ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES,
                            representative.getValue().getNameOfRepresentative(),
                            representative.getValue().getRespondentOrganisation().getOrganisationName()));
                }
            }
        }
        return errors;
    }

    /**
     * Retrieves case user assignment data for the given case ID using the supplied user token.
     *
     * <p>If the case user assignments cannot be retrieved due to a CCD input/output exception,
     * the exception is logged and an empty {@link Optional} is returned.
     *
     * @param caseId the ID of the case for which user assignment data is requested
     * @param userToken the user token used to authenticate the request
     * @return an {@link Optional} containing the retrieved {@link CaseUserAssignmentData},
     *     or {@link Optional#empty()} if no data is returned or the retrieval fails
     */
    private Optional<CaseUserAssignmentData> getCaseUserAssignmentData(String userToken, String caseId) {
        try {
            return Optional.ofNullable(nocCcdService.retrieveCaseUserAssignments(userToken, caseId));
        } catch (CcdInputOutputException exception) {
            log.warn(WARNING_FAILED_TO_RETRIEVE_CASE_ASSIGNMENTS, caseId, exception.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Pre-fills the amend contact details form with the respondent representative's current phone and address.
     *
     * <p>Finds the representative(s) for the authenticated user and copies their
     * {@code representativePhoneNumber} and {@code representativeAddress} into the top-level
     * {@code et3ResponsePhone} and {@code et3ResponseAddress} fields for display in the UI.
     *
     * @param userToken   the IDAM authentication token of the logged-in legal rep
     * @param caseDetails the case details containing the representative collection
     */
    public void loadRespondentRepresentativeContactDetails(String userToken, CaseDetails caseDetails) {
        List<RepresentedTypeRItem> representatives = findRepresentativesByToken(userToken, caseDetails);
        if (CollectionUtils.isEmpty(representatives)) {
            return;
        }
        RepresentedTypeR rep = representatives.getFirst().getValue();
        caseDetails.getCaseData().setEt3ResponsePhone(rep.getRepresentativePhoneNumber());
        caseDetails.getCaseData().setEt3ResponseAddress(rep.getRepresentativeAddress());
    }

    /**
     * Fetches the MyHMCTS organisation address for the authenticated user and sets it on the case data
     * so it can be displayed as a read-only field on the "Use MyHMCTS details" page.
     *
     * @param userToken the IDAM authentication token of the logged-in legal rep
     * @param caseData  the case data to update with the organisation address text
     * @throws GenericServiceException if the organisation address cannot be retrieved
     */
    public void populateMyHmctsOrganisationAddress(String userToken, CaseData caseData)
            throws GenericServiceException {
        OrganisationAddress organisationAddress = myHmctsService.getUserOrganisationAddress(userToken);
        caseData.setEt3ResponseAddress(mapOrganisationAddressToAddress(organisationAddress));
        caseData.setMyHmctsAddressText(getOrganisationAddressAsText(organisationAddress));
    }

    /**
     * Saves the amended contact details (phone and address) back to all respondent representatives
     * associated with the authenticated user.
     *
     * <p>If the user selected "Use MyHMCTS details", the organisation address is first fetched and
     * applied to the case data before being persisted to the representative collection.
     *
     * @param userToken   the IDAM authentication token of the logged-in legal rep
     * @param caseDetails the case details containing the representative collection and form values
     * @throws GenericServiceException if the MyHMCTS organisation address cannot be retrieved
     */
    public void saveRespondentRepresentativeContactDetails(String userToken, CaseDetails caseDetails)
            throws GenericServiceException {
        CaseData caseData = caseDetails.getCaseData();
        if (REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS.equals(
                caseData.getRepresentativeContactChangeOption())) {
            populateMyHmctsOrganisationAddress(userToken, caseData);
        }
        List<RepresentedTypeRItem> representatives = findRepresentativesByToken(userToken, caseDetails);
        for (RepresentedTypeRItem item : representatives) {
            if (ObjectUtils.isEmpty(item) || ObjectUtils.isEmpty(item.getValue())) {
                continue;
            }
            item.getValue().setRepresentativePhoneNumber(caseData.getEt3ResponsePhone());
            item.getValue().setRepresentativeAddress(caseData.getEt3ResponseAddress());
        }
        caseData.setMyHmctsAddressText(null);
    }
}
