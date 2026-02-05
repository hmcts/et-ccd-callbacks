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
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.NocUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SOLICITOR_ROLE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_SET_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS;

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
            nocNotificationService.sendRespondentRepresentationRemovalNotifications(oldCaseDetails, newCaseDetails,
                    representativesToRemove);
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
        if (ObjectUtils.isEmpty(callbackRequest) || StringUtils.isBlank(userToken)) {
            return new  ArrayList<>();
        }
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetailsBefore();
        if (ObjectUtils.isEmpty(oldCaseDetails)
                || StringUtils.isEmpty(oldCaseDetails.getCaseId())
                || ObjectUtils.isEmpty(oldCaseDetails.getCaseData())
                || CollectionUtils.isEmpty(representativesToRemove)) {
            return new ArrayList<>();
        }
        CaseUserAssignmentData caseUserAssignments = nocCcdService.getCaseAssignments(
                adminUserService.getAdminUserToken(), oldCaseDetails.getCaseId());
        if (ObjectUtils.isEmpty(caseUserAssignments)
                || CollectionUtils.isEmpty(caseUserAssignments.getCaseUserAssignments())) {
            return new ArrayList<>();
        }
        // to get list of representatives whose assignment is revoked
        List<RepresentedTypeRItem> representativesToRevoke = new ArrayList<>();
        List<CaseUserAssignment> caseUserAssignmentsToRevoke = new ArrayList<>();
        for (CaseUserAssignment caseUserAssignment : caseUserAssignments.getCaseUserAssignments()) {
            if (!RoleUtils.isRespondentRepresentativeRole(caseUserAssignment.getCaseRole())) {
                continue;
            }
            for (RepresentedTypeRItem representative : representativesToRemove) {
                if (!RespondentRepresentativeUtils.isValidRepresentative(representative)) {
                    continue;
                }
                String respondentName = RoleUtils.findRespondentNameByRole(oldCaseDetails.getCaseData(),
                        caseUserAssignment.getCaseRole());
                if (RespondentRepresentativeUtils.canModifyAccess(representative)
                        && (StringUtils.isNotBlank(respondentName)
                        && respondentName.equals(representative.getValue().getRespRepName())
                        || StringUtils.isNotBlank(caseUserAssignment.getCaseRole())
                        && caseUserAssignment.getCaseRole().equals(representative.getValue().getRole()))) {
                    representativesToRevoke.add(representative);
                    caseUserAssignmentsToRevoke.add(caseUserAssignment);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(caseUserAssignmentsToRevoke)) {
            try {
                ccdClient.revokeCaseAssignments(userToken, CaseUserAssignmentData.builder().caseUserAssignments(
                        caseUserAssignmentsToRevoke).build());
            } catch (IOException exception) {
                log.info(exception.getMessage(), exception);
            }
        }
        return representativesToRevoke;
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
            ccdClient.submitEventForCase(adminUserToken, ccdRequestCaseData, caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(), ccdRequest, caseDetails.getCaseId());
        } catch (IOException exception) {
            log.info(ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES, caseDetails.getCaseId(),
                    exception.getMessage());
        }
    }

    public void addNewRepresentatives(CallbackRequest callbackRequest) {
        if (ObjectUtils.isEmpty(callbackRequest)
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetailsBefore())
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetails())
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetailsBefore().getCaseData())
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetails().getCaseData())
                || ObjectUtils.isEmpty(callbackRequest.getCaseDetails().getCaseData().getRepCollection())) {
            return;
        }
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetailsBefore();
        CaseDetails newCaseDetails = callbackRequest.getCaseDetails();
        // finds both new and organisation or e-mail changed representatives
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
        CaseUserAssignmentData caseUserAssignments = nocCcdService.getCaseAssignments(
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
                updateCaseForNewRepresentative(caseDetails, representative.getId(), role);
            } catch (GenericServiceException gse) {
                log.info(ERROR_UNABLE_TO_SET_ROLE, role, caseDetails.getCaseId(), gse.getMessage());
            }
        }
    }

    public void updateCaseForNewRepresentative(CaseDetails caseDetails,
                                               String representativeId,
                                               String role) {
        if (ObjectUtils.isEmpty(caseDetails)) {
            log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS);
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
            RepresentedTypeRItem representative = RespondentRepresentativeUtils.findRepresentativeById(
                    ccdRequestCaseData, representativeId);
            if (!RespondentRepresentativeUtils.isValidRepresentative(representative)) {
                log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND, caseDetails.getCaseId());
                return;
            }
            assert representative != null;
            representative.getValue().setRole(role);
            NocUtils.applyRespondentOrganisationPolicyForRole(caseDetails.getCaseData(), representative);
            ccdClient.submitEventForCase(adminUserToken, ccdRequestCaseData, caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(), ccdRequest, caseDetails.getCaseId());
        } catch (IOException exception) {
            log.error(ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES, caseDetails.getCaseId(),
                    exception.getMessage());
        }
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
     * Gets the case data before and after and checks respondent org policies for differences.
     * For each difference creates a change organisation request to remove old organisation and add new.
     * For each change request trigger the updateRepresentation event against CCD.
     * Notifications are sent to Tribunal, Claimant, Respondent, New Rep, Old Rep (if there is existing org).
     * Previous Representative's access is revoked.
     * @param callbackRequest - containing case details before event and after the event
     * @throws IOException - exception thrown by CCD
     */
    public void updateRespondentRepresentativesAccess(CallbackRequest callbackRequest)
            throws IOException, GenericServiceException {
        NocUtils.validateCallbackRequest(callbackRequest);

        CaseDetails newCaseDetails = callbackRequest.getCaseDetails();
        CaseDetails oldCaseDetails = callbackRequest.getCaseDetailsBefore();
        // Identify changes in representation of respondents
        List<UpdateRespondentRepresentativeRequest> updateRespondentRepresentativeRequests =
                identifyRepresentationChanges(oldCaseDetails.getCaseData(), newCaseDetails.getCaseData());

        for (UpdateRespondentRepresentativeRequest updateRespondentRepresentativeRequest
                : updateRespondentRepresentativeRequests) {
            try {
                nocNotificationService.sendNotificationOfChangeEmails(oldCaseDetails,
                        newCaseDetails,
                        updateRespondentRepresentativeRequest.getChangeOrganisationRequest());
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
            }

            if (updateRespondentRepresentativeRequest != null
                    && updateRespondentRepresentativeRequest.getChangeOrganisationRequest()
                    .getOrganisationToRemove() != null) {
                try {
                    // this service only removes organisation representative access to the case and removes
                    // respondent representative from cas_users
                    nocService.removeOrganisationRepresentativeAccess(newCaseDetails.getCaseId(),
                            updateRespondentRepresentativeRequest.getChangeOrganisationRequest());
                } catch (IOException e) {
                    throw new CcdInputOutputException("Failed to remove organisation representative access", e);
                }
            }
            // We use "update case submitted" instead of an update event for respondent representatives,
            // because there is not always a direct relationship between organisation ID and representatives.
            // The updates performed are:
            //   • Remove changeOrganisationRequestField to prevent re-processing
            //   • Clears the changeOrganisationRequestField to prevent errors in the existing representative process
            //     and to allow further changes to be made
            //   • Mark the respondent as no longer represented, so this is reflected on the respondent page
            String adminUserToken = adminUserService.getAdminUserToken();
            CCDRequest ccdRequest = ccdClient.startEventForCase(adminUserToken,
                    newCaseDetails.getCaseTypeId(),
                    newCaseDetails.getJurisdiction(),
                    newCaseDetails.getCaseId(),
                    EVENT_UPDATE_CASE_SUBMITTED);

            CaseData ccdRequestCaseData = ccdRequest.getCaseDetails().getCaseData();

            // Clears the changeOrganisationRequestField to prevent errors in the existing representative process
            // and to allow further changes to be made
            ccdRequestCaseData.setChangeOrganisationRequestField(null);

            // Removes organisation policy for the respondent
            // It is assumed that there is always a notice of change answer for each organisation policy
            OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(
                    ccdRequestCaseData,
                    updateRespondentRepresentativeRequest
            );

            // Sets representative removed to YES for the respondent
            // It is assumed that there is always one respondent with the name in the request
            RespondentUtils.markRespondentRepresentativeRemoved(ccdRequestCaseData,
                    updateRespondentRepresentativeRequest
            );

            ccdClient.submitEventForCase(adminUserToken, ccdRequestCaseData, newCaseDetails.getCaseTypeId(),
                    newCaseDetails.getJurisdiction(), ccdRequest, newCaseDetails.getCaseId());
        }
    }

    /**
     * Identifies differences in representation.
     * @param after - case data after event
     * @param before - case data before event was triggered
     * @return list of change organisation requests for any changes detected
     */
    public List<UpdateRespondentRepresentativeRequest> identifyRepresentationChanges(CaseData  after,
                                                                                     CaseData before) {
        final List<RespondentSumTypeItem> newRespondents =
            getIfNull(after.getRespondentCollection(), new ArrayList<>());
        final Map<String, Organisation> newRespondentsOrganisations =
            nocRespondentHelper.getRespondentOrganisations(after);
        final Map<String, Organisation> oldRespondentsOrganisations =
            nocRespondentHelper.getRespondentOrganisations(before);
        final List<UpdateRespondentRepresentativeRequest> updateRespondentRepresentativeOrganisationRequests
                = new ArrayList<>();

        for (int i = 0; i < newRespondents.size(); i++) {
            SolicitorRole solicitorRole = Arrays.asList(SolicitorRole.values()).get(i);
            RespondentSumTypeItem respondentSumTypeItem = newRespondents.get(i);
            String respondentId = respondentSumTypeItem.getId();

            Organisation newOrganisation = newRespondentsOrganisations.get(respondentId);
            Organisation oldOrganisation = oldRespondentsOrganisations.get(respondentId);

            if (!Objects.equals(newOrganisation, oldOrganisation)) {
                String respondentName = StringUtils.EMPTY;
                if (ObjectUtils.isNotEmpty(respondentSumTypeItem)
                        && ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue())
                        && StringUtils.isNotBlank(respondentSumTypeItem.getValue().getRespondentName())) {
                    respondentName = respondentSumTypeItem.getValue().getRespondentName();
                }
                UpdateRespondentRepresentativeRequest updateRespondentRepresentativeOrganisationRequest =
                        UpdateRespondentRepresentativeRequest.builder().changeOrganisationRequest(
                                nocRespondentHelper.createChangeRequest(newOrganisation, oldOrganisation,
                                        solicitorRole)).respondentName(respondentName).build();
                // Sets representation removed when representative is removed for the respondent
                if (ObjectUtils.isNotEmpty(oldOrganisation) && ObjectUtils.isEmpty(newOrganisation)) {
                    updateRespondentRepresentativeOrganisationRequest.setRepresentativeRemoved(YES);
                }
                updateRespondentRepresentativeOrganisationRequests
                        .add(updateRespondentRepresentativeOrganisationRequest);
            }
        }

        return updateRespondentRepresentativeOrganisationRequests;
    }

    /**
     * Revokes access from all users of an organisation being replaced or removed.
     * @param caseId - case id of case to apply update to
     * @param changeOrganisationRequest - containing case role and id of organisation to remove
     */
    public void removeOrganisationRepresentativeAccess(String caseId,
                                                       ChangeOrganisationRequest changeOrganisationRequest) {
        String orgId = changeOrganisationRequest.getOrganisationToRemove().getOrganisationID();
        String roleOfRemovedOrg = changeOrganisationRequest.getCaseRoleId().getSelectedCode();
        CaseUserAssignmentData caseAssignments =
            nocCcdService.getCaseAssignments(adminUserService.getAdminUserToken(), caseId);

        List<CaseUserAssignment> usersToRevoke = caseAssignments.getCaseUserAssignments().stream()
            .filter(caseUserAssignment -> caseUserAssignment.getCaseRole().equals(roleOfRemovedOrg))
            .map(caseUserAssignment ->
                CaseUserAssignment.builder().userId(caseUserAssignment.getUserId())
                    .organisationId(orgId)
                    .caseRole(roleOfRemovedOrg)
                    .caseId(caseId)
                    .build()
            ).toList();

        if (!CollectionUtils.isEmpty(usersToRevoke)) {
            nocCcdService.revokeCaseAssignments(adminUserService.getAdminUserToken(),
                CaseUserAssignmentData.builder().caseUserAssignments(usersToRevoke).build());
        }
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
     * Validates that each representative marked as a myHMCTS representative has an email address
     * that corresponds to a user within the selected organisation.
     *
     * <p>The method performs the following validations for each representative in the
     * {@code repCollection}:</p>
     *
     * <ul>
     *     <li><strong>Organisation selection requirement:</strong>
     *         If a representative is marked as a myHMCTS representative, an organisation must be selected.
     *         If no organisation is present, a {@link GenericServiceException} is thrown.</li>
     *
     *     <li><strong>Email address presence:</strong>
     *         If the representative does not have an email address, a warning message is added.</li>
     *
     *     <li><strong>Email-to-organisation user match:</strong>
     *         The representative's email address is checked against the organisation's registered users.
     *         If the email cannot be matched to any organisation user, a warning is added.</li>
     * </ul>
     *
     * <p><strong>Important:</strong></p>
     * <ul>
     *     <li>No validation error is not implemented if the organisation cannot be found in the organisation list, when
     *     user is a myHmcts organisation user. because all organisations are selected from existing organisation data
     *     and are therefore assumed valid.</li>
     *     <li>All representatives included in {@code caseData.getRepCollection()} are assumed to be structurally valid
     *     and eligible for validation.</li>
     * </ul>
     *
     * @param caseData the case data containing the representatives to validate
     * @param submissionReference a reference identifier used for error tracking during submission
     * @throws GenericServiceException if a myHMCTS representative does not have a selected organisation
     */
    public void validateRepresentativeOrganisationAndEmail(CaseData caseData,
                                                                   String submissionReference)
            throws GenericServiceException {
        if (ObjectUtils.isEmpty(caseData)
                || CollectionUtils.isEmpty(caseData.getRepCollection())) {
            return;
        }
        StringBuilder nocWarnings = new StringBuilder(StringUtils.EMPTY);
        for (RepresentedTypeRItem representativeItem :  caseData.getRepCollection()) {
            RepresentedTypeR representative = representativeItem.getValue();
            // checking if representative organisation is a hmcts organisation
            if (!YES.equals(representative.getMyHmctsYesNo())) {
                continue;
            }
            final String representativeName = representative.getNameOfRepresentative();
            // Checking if representative has an organisation
            if (ObjectUtils.isEmpty(representative.getRespondentOrganisation())
                    || StringUtils.isBlank(representative.getRespondentOrganisation().getOrganisationID())) {
                String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND,
                        representativeName);
                throw new GenericServiceException(exceptionMessage, new Exception(), exceptionMessage,
                        submissionReference, "NocRespondentRepresentativeService",
                        "validateRepresentativeOrganisationAndEmail");
            }
            // checking if representative has an email address
            final String representativeEmail = representative.getRepresentativeEmailAddress();
            if (StringUtils.isBlank(representativeEmail)) {
                String warningMessage = String.format(WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS, representativeName);
                nocWarnings.append(warningMessage).append('\n');
                continue;
            }

            String accessToken = adminUserService.getAdminUserToken();
            try {
                ResponseEntity<AccountIdByEmailResponse> userResponse =
                        organisationClient.getAccountIdByEmail(accessToken, authTokenGenerator.generate(),
                                representativeEmail);
                // checking if representative email address exists in organisation users
                if (ObjectUtils.isEmpty(userResponse)
                        || ObjectUtils.isEmpty(userResponse.getBody())
                        || StringUtils.isBlank(userResponse.getBody().getUserIdentifier())) {
                    String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                            representativeName, representativeEmail);
                    nocWarnings.append(warningMessage).append('\n');
                }
            } catch (Exception e) {
                // for localhost if e-mail is not entered same as wiremock request
                String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                        representativeName, representativeEmail);
                nocWarnings.append(warningMessage).append('\n');
            }
        }
        caseData.setNocWarning(nocWarnings.toString());
    }
}
