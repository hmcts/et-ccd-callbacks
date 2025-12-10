package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersIdamUser;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersResponse;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NocUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_ORGANISATION_USERS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_INVALID_REPRESENTATIVE_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_EMAIL_DOES_NOT_MATCH_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_MISSING_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REQUEST;

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
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseDataBefore = caseDetailsBefore.getCaseData();
        CaseData caseData = caseDetails.getCaseData();
        // Identify changes in representation of respondents
        List<UpdateRespondentRepresentativeRequest> updateRespondentRepresentativeRequests =
                identifyRepresentationChanges(caseData, caseDataBefore);

        for (UpdateRespondentRepresentativeRequest updateRespondentRepresentativeRequest
                : updateRespondentRepresentativeRequests) {
            try {
                nocNotificationService.sendNotificationOfChangeEmails(caseDetailsBefore,
                        caseDetails,
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
                    nocService.removeOrganisationRepresentativeAccess(caseDetails.getCaseId(),
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
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    caseDetails.getCaseId(),
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

            ccdClient.submitEventForCase(adminUserToken, ccdRequestCaseData, caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(), ccdRequest, caseDetails.getCaseId());
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
     * Validates that each NOC representative in the given {@link CaseData} has a valid email address
     * and that the email belongs to a user within the representative's associated organisation.
     *
     * <p>The method performs the following checks for each representative:
     * <ul>
     *     <li>Representative data is valid according to {@code NocUtils#isValidNocRepresentative}.</li>
     *     <li>If the representative is marked as a MyHMCTS user, an email address must be present.</li>
     *     <li>The representative must have a valid respondent organisation with a non-blank organisation ID.</li>
     *     <li>The organisation must contain at least one user.</li>
     *     <li>The representative's email must match one of the organisation's registered users.</li>
     * </ul>
     *
     * <p>If any validation fails, the method returns a list containing a single error message
     * describing the issue. If all representatives are valid, an empty list is returned.
     *
     * @param caseData the case data containing representatives to validate
     * @return a list of validation error messages, or an empty list if all representatives are valid
     */
    public List<String> validateRepresentativeEmailMatchesOrganisationUsers(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)
                || org.apache.commons.collections4.CollectionUtils.isEmpty(caseData.getRepCollection())) {
            return Collections.emptyList();
        }
        for (RepresentedTypeRItem representativeItem :  caseData.getRepCollection()) {
            if (!NocUtils.isValidNocRepresentative(representativeItem)) {
                return List.of(ERROR_INVALID_REPRESENTATIVE_EXISTS);
            }
            RepresentedTypeR representative = representativeItem.getValue();
            if (!YES.equals(representative.getMyHmctsYesNo())) {
                continue;
            }
            final String representativeEmail = representative.getRepresentativeEmailAddress();
            if (StringUtils.isBlank(representativeEmail)) {
                return List.of(String.format(ERROR_REPRESENTATIVE_MISSING_EMAIL_ADDRESS, representativeItem.getId()));
            }
            if (ObjectUtils.isEmpty(representative.getRespondentOrganisation())
                    || StringUtils.isBlank(representative.getRespondentOrganisation().getOrganisationID())) {
                return List.of(String.format(ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND,  representativeItem.getId()));
            }
            final String organisationId = representative.getRespondentOrganisation().getOrganisationID();
            final ResponseEntity<OrganisationUsersResponse> organisationUsersResponse =
                    organisationClient.getOrganisationUsers(adminUserService.getAdminUserToken(),
                            authTokenGenerator.generate(), organisationId);

            if (ObjectUtils.isEmpty(organisationUsersResponse)
                    || ObjectUtils.isEmpty(organisationUsersResponse.getBody())
                    || org.apache.commons.collections4.CollectionUtils.isEmpty(
                            organisationUsersResponse.getBody().getUsers())) {
                return List.of(String.format(ERROR_ORGANISATION_USERS_NOT_FOUND, organisationId));
            }
            final List<OrganisationUsersIdamUser> organisationUsers = organisationUsersResponse.getBody().getUsers();
            boolean representativeExistsInOrganisation = organisationUsers.stream()
                    .anyMatch(user -> representativeEmail.equals(user.getEmail()));
            if (!representativeExistsInOrganisation) {
                return List.of(String.format(
                        ERROR_REPRESENTATIVE_EMAIL_DOES_NOT_MATCH_ORGANISATION, representativeEmail));
            }
        }
        return Collections.emptyList();
    }
}
