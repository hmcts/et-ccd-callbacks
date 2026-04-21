package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.NocUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_FAILED_TO_FIND_ORGANISATION_BY_EMAIL_SYSTEM_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocClaimantRepresentativeService {
    public static final String NOC_REQUEST = "nocRequest";

    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
    private final NocService nocService;
    private final OrganisationService organisationService;

    /**
     * Validates whether the claimant representative has the minimum details required for
     * an organisation account check and, where so, performs the check using the
     * representative's email address.
     *
     * <p>If the claimant representative is missing, or does not have an email address or
     * HMCTS organisation ID, no validation is performed and an empty list is returned.
     *
     * <p>Where all required representative details are present, this method delegates to
     * the organisation service to check whether the representative account can be resolved
     * by email and returns any warning messages produced by that check.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>{@code caseData} is not {@code null}.</li>
     *   <li>{@code caseData.getRepresentativeClaimantType()} may be absent, and this is
     *       treated as a non-validation scenario rather than an error.</li>
     *   <li>The organisation service returns a non-null list of warning messages.</li>
     *   <li>An empty returned list means either the required representative details were
     *       not present or no warnings were identified by the account check.</li>
     * </ul>
     *
     * @param caseData the case data containing the claimant representative details
     * @return a list of warning messages, or an empty list if the representative details
     *     are incomplete or no warnings are identified
     */
    public List<String> validateRepresentativeOrganisationAndEmail(CaseData caseData) {
        List<String> warnings = new ArrayList<>();
        if (!ClaimantRepresentativeUtils.hasRepresentative(caseData.getRepresentativeClaimantType())
                || !ClaimantRepresentativeUtils.hasRepresentativeEmail(caseData.getRepresentativeClaimantType())
                || !ClaimantRepresentativeUtils.hasHmctsOrganisationId(caseData.getRepresentativeClaimantType())) {
            return warnings;
        }
        return organisationService.checkRepresentativeAccountByEmail(
                caseData.getRepresentativeClaimantType().getNameOfRepresentative(),
                caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress());
    }

    /**
     * Validates that the selected My HMCTS organisation for the claimant representative
     * matches the organisation associated with the representative's My HMCTS account.
     *
     * <p>If the claimant representative details are incomplete, no validation is performed
     * and an empty string is returned.
     *
     * <p>Where the representative email address and selected organisation details are present,
     * this method attempts to:
     * <ol>
     *   <li>find the representative's user account by email address, and</li>
     *   <li>retrieve the organisation associated with that user account.</li>
     * </ol>
     *
     * <p>If both lookups succeed, the selected organisation is compared with the
     * representative's registered organisation. If they do not match, a formatted error
     * message is returned. If the lookups fail, no organisation mismatch validation is
     * applied and an empty string is returned.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>{@code caseDetails} is not {@code null}.</li>
     *   <li>{@code caseDetails.getCaseData()} is not {@code null}.</li>
     *   <li>{@code caseDetails.getCaseId()} contains a valid case identifier for logging and service calls.</li>
     *   <li>{@code adminUserService.getAdminUserToken()} returns a valid access token.</li>
     *   <li>If the representative cannot be found in IDAM, organisation validation is skipped.</li>
     *   <li>{@code nocService.findUserByEmail(...)} returns a response with a non-null user identifier when successful.
     *   </li>
     *   <li>{@code OrganisationUtils.hasMatchingOrganisationId(...)} safely handles the returned
     *       {@code organisationsResponse} when present.</li>
     *   <li>An empty return value means either:
     *     <ul>
     *       <li>the representative details required for validation were missing,</li>
     *       <li>the representative account and organisation could not be resolved, or</li>
     *       <li>the selected organisation matched the representative's organisation.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param caseDetails the case details containing the claimant representative information
     * @return a formatted error message if the selected organisation does not match the
     *     representative's organisation, or an empty string if no validation error is found
     */
    public String validateClaimantRepresentativeOrganisationMatch(CaseDetails caseDetails) {
        String error = StringUtils.EMPTY;
        if (ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType())
                || StringUtils.isBlank(caseDetails.getCaseData().getRepresentativeClaimantType()
                .getRepresentativeEmailAddress())
                || ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType()
                .getMyHmctsOrganisation())
                || ObjectUtils.isEmpty(caseDetails.getCaseData().getRepresentativeClaimantType()
                .getMyHmctsOrganisation().getOrganisationID())) {
            return error;
        }
        AccountIdByEmailResponse userResponse;
        OrganisationsResponse organisationsResponse = null;
        boolean isValidUserAndOrganisation = true;
        RepresentedTypeC representative = caseDetails.getCaseData().getRepresentativeClaimantType();
        try {
            String accessToken = adminUserService.getAdminUserToken();
            userResponse = nocService.findUserByEmail(accessToken,
                    representative.getRepresentativeEmailAddress(), caseDetails.getCaseId());
            organisationsResponse = nocService.findOrganisationByUserId(accessToken,
                    userResponse.getUserIdentifier(), caseDetails.getCaseId());
        } catch (GenericServiceException e) {
            // if user is not defined on idam should not check for organisation.
            log.warn(WARNING_FAILED_TO_FIND_ORGANISATION_BY_EMAIL_SYSTEM_ERROR, e.getMessage());
            isValidUserAndOrganisation = false;
        }
        if (isValidUserAndOrganisation
                && !OrganisationUtils.hasMatchingOrganisationId(
                representative.getMyHmctsOrganisation(), organisationsResponse)) {
            error = String.format(ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES,
                    representative.getNameOfRepresentative(),
                    representative.getMyHmctsOrganisation().getOrganisationID());
        }
        return error;
    }

    /**
     * Update claimant representation based on NoC request.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case data
     * @throws IOException if CCD operation fails
     */
    public CaseData updateClaimantRepresentation(CaseDetails caseDetails, String userToken) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        updateClaimantRepMap(caseData, caseId);
        prepopulateOrgAddress(caseData, userToken);

        return caseData;
    }

    private void updateClaimantRepMap(CaseData caseData, String caseId) throws IOException {
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.setClaimantRepresentativeRemoved(NO);
        RepresentedTypeC claimantRep = createRepresentedTypeC(caseId, caseData.getChangeOrganisationRequestField());
        caseData.setRepresentativeClaimantType(claimantRep);
    }

    private RepresentedTypeC createRepresentedTypeC(String caseId, ChangeOrganisationRequest change)
            throws IOException {
        String accessToken = adminUserService.getAdminUserToken();

        Optional<AuditEvent> auditEvent =
                nocCcdService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);
        Optional<UserDetails> userDetailsOptional = auditEvent
                .map(event -> adminUserService.getUserDetails(adminUserService.getAdminUserToken(),
                        event.getUserId()));

        UserDetails userDetails = userDetailsOptional.orElseThrow();

        RepresentedTypeC claimantRep = new RepresentedTypeC();
        claimantRep.setNameOfRepresentative(userDetails.getFirstName() + " " + userDetails.getLastName());
        claimantRep.setRepresentativeEmailAddress(userDetails.getEmail());
        claimantRep.setMyHmctsOrganisation(change.getOrganisationToAdd());

        return claimantRep;
    }

    private void prepopulateOrgAddress(CaseData caseData, String userToken) {
        List<OrganisationsResponse> organisationList = organisationClient.getOrganisations(
                userToken, authTokenGenerator.generate());

        RepresentedTypeC claimantRep = caseData.getRepresentativeClaimantType();
        if (claimantRep != null && claimantRep.getMyHmctsOrganisation() != null) {
            String orgId = claimantRep.getMyHmctsOrganisation().getOrganisationID();
            Optional<OrganisationsResponse> organisationResponse = organisationList.stream()
                    .filter(org -> org.getOrganisationIdentifier().equals(orgId))
                    .findFirst();

            if (organisationResponse.isPresent()) {
                OrganisationsResponse organisation = organisationResponse.get();
                claimantRep.setRepresentativeAddress(OrganisationUtils.getOrganisationAddress(organisation));
                claimantRep.setNameOfOrganisation(organisation.getName());
                caseData.setRepresentativeClaimantType(claimantRep);
            }
        }
    }

    public void updateClaimantRepAccess(CallbackRequest callbackRequest)
            throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseDataBefore = caseDetailsBefore.getCaseData();
        CaseData caseData = caseDetails.getCaseData();
        ChangeOrganisationRequest changeRequest = identifyRepresentationChanges(caseData,
                caseDataBefore);
        try {
            nocNotificationService.sendNotificationOfChangeEmails(caseDetailsBefore, caseDetails, changeRequest);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
        if (changeRequest != null
                && changeRequest.getOrganisationToRemove() != null) {
            try {
                nocService.removeOrganisationRepresentativeAccess(caseDetails.getCaseId(), changeRequest);
            } catch (IOException e) {
                throw new CcdInputOutputException("Failed to remove organisation representative access", e);
            }
        }
        String accessToken = adminUserService.getAdminUserToken();
        if (YES.equals(caseData.getClaimantRepresentedQuestion())) {
            RepresentedTypeC claimantRep = caseData.getRepresentativeClaimantType();
            if (claimantRep != null && claimantRep.getRepresentativeEmailAddress() != null) {
                try {
                    nocService.grantRepresentativeAccess(accessToken,
                            caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress(),
                            caseDetails.getCaseId(),
                            changeRequest.getOrganisationToAdd(),
                            ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
                } catch (GenericServiceException genericServiceException) {
                    log.error(genericServiceException.getMessage(), genericServiceException);
                }
            }
        }
    }

    public ChangeOrganisationRequest identifyRepresentationChanges(CaseData  after, CaseData before) {
        Organisation newRepOrg = after.getRepresentativeClaimantType() != null
                ? after.getRepresentativeClaimantType().getMyHmctsOrganisation() : null;
        Organisation oldRepOrg = before.getRepresentativeClaimantType() != null
                ? before.getRepresentativeClaimantType().getMyHmctsOrganisation() : null;
        ChangeOrganisationRequest changeRequests;

        if (!Objects.equals(newRepOrg, oldRepOrg)) {
            changeRequests = NocUtils.buildApprovedChangeOrganisationRequest(newRepOrg, oldRepOrg,
                    ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        } else {
            changeRequests = NocUtils.buildApprovedChangeOrganisationRequest(newRepOrg, null,
                    ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        }

        return changeRequests;
    }
}
