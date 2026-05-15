package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EMPTY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_FAILED_TO_ASSIGN_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_INVALID_GRANT_ACCESS_PARAMETER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL_WITH_IO_EXCEPTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_USER_AND_SELECTED_ORGANISATIONS_NOT_MATCH;

@RequiredArgsConstructor
@Component
@Slf4j
public class NocService {
    private final NocCcdService nocCcdService;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment caseAssignment;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;

    private static final String CLASS_NAME = "NocService";

    /**
     * Revokes access from all users of an organisation being replaced or removed.
     * @param caseId - case id of case to apply update to
     * @param changeOrganisationRequest - containing case role and id of organisation to remove
     * @throws IOException - thrown if no CCD service is inaccessible
     */
    public void removeOrganisationRepresentativeAccess(String caseId,
                                                       ChangeOrganisationRequest changeOrganisationRequest)
            throws IOException {
        String roleOfRemovedOrg = changeOrganisationRequest.getCaseRoleId().getSelectedCode();
        String orgId = changeOrganisationRequest.getOrganisationToRemove().getOrganisationID();
        CaseUserAssignmentData caseAssignments =
                nocCcdService.retrieveCaseUserAssignments(adminUserService.getAdminUserToken(), caseId);

        List<CaseUserAssignment> usersToRevoke = caseAssignments.getCaseUserAssignments().stream()
                .filter(caseUserAssignment -> caseUserAssignment.getCaseRole().equals(roleOfRemovedOrg))
                .map(caseUserAssignment ->
                        CaseUserAssignment.builder().userId(caseUserAssignment.getUserId())
                                .organisationId(orgId)
                                .caseRole(roleOfRemovedOrg)
                                .caseId(caseId)
                                .build()
                ).toList();

        if (CollectionUtils.isNotEmpty(usersToRevoke)) {
            nocCcdService.revokeCaseAssignments(adminUserService.getAdminUserToken(),
                    CaseUserAssignmentData.builder().caseUserAssignments(usersToRevoke).build());
        }
    }

    /**
     * Grants case access to a representative user for the specified case and role.
     * <p>
     * The method:
     * <ul>
     *     <li>Validates the provided access token, email, case reference,
     *     organisation, and role</li>
     *     <li>Finds the user associated with the supplied email address</li>
     *     <li>Verifies that the user belongs to the selected organisation</li>
     *     <li>Grants the requested case role access to the user</li>
     * </ul>
     *
     * <p><strong>Assumptions:</strong></p>
     * <ul>
     *     <li>The email address belongs to an existing user account</li>
     *     <li>The user is associated with the supplied organisation</li>
     *     <li>The role value is a valid CCD case role</li>
     *     <li>The submission reference represents a valid case identifier</li>
     *     <li>The caller has sufficient permissions to grant case access</li>
     * </ul>
     *
     * @param accessToken authentication token used to perform user and case access operations
     * @param email the email address of the representative user
     * @param submissionReference the case reference identifier
     * @param organisationToAdd the organisation expected to be associated with the user
     * @param role the CCD case role to grant
     * @return the identifier(idam id) of the user who was granted access
     * @throws GenericServiceException if validation fails, the user or organisation
     *                                 cannot be found, the organisation does not match,
     *                                 or the case access assignment fails
     */
    public String grantRepresentativeAccess(String accessToken, String email,
                                          String submissionReference, Organisation organisationToAdd,
                                          String role) throws GenericServiceException {
        if (StringUtils.isBlank(accessToken)
                || StringUtils.isBlank(email)
                || StringUtils.isBlank(submissionReference)
                || ObjectUtils.isEmpty(organisationToAdd)
                || StringUtils.isBlank(organisationToAdd.getOrganisationID())
                || !RoleUtils.isValidRole(role)) {
            String tmpCaseId = StringUtils.isEmpty(submissionReference) ? EMPTY_LOWERCASE : submissionReference;
            String tmpRole = StringUtils.isBlank(role) ? EMPTY_LOWERCASE : role;
            String exceptionMessage = String.format(EXCEPTION_INVALID_GRANT_ACCESS_PARAMETER, tmpCaseId, tmpRole);
            throw new GenericServiceException(exceptionMessage);
        }
        AccountIdByEmailResponse userResponse;
        try {
            userResponse = findUserByEmail(accessToken, email,
                    submissionReference);
            OrganisationsResponse organisationsResponse = findOrganisationByUserId(accessToken,
                    userResponse.getUserIdentifier(), submissionReference);
            if (!OrganisationUtils.hasMatchingOrganisationId(organisationToAdd, organisationsResponse)) {
                String exceptionMessage = String.format(EXCEPTION_USER_AND_SELECTED_ORGANISATIONS_NOT_MATCH,
                        submissionReference);
                throw new GenericServiceException(exceptionMessage);
            }
            grantCaseAccess(userResponse.getUserIdentifier(), submissionReference, role);
        } catch (GenericServiceException exception) {
            String exceptionMessage = String.format(EXCEPTION_FAILED_TO_ASSIGN_ROLE, role, submissionReference,
                    exception.getMessage());
            throw new GenericServiceException(exceptionMessage, new Exception(exception),
                    EXCEPTION_FAILED_TO_ASSIGN_ROLE, submissionReference, CLASS_NAME, "grantRepresentativeAccess");
        }
        return userResponse.getUserIdentifier();
    }

    /**
     * Retrieves organisation details associated with a given user identifier.
     * <p>
     * Calls the Organisation service to obtain organisation information for the specified user.
     * If the response is null, contains no-body, or does not include an organisation identifier,
     * a {@link GenericServiceException} is thrown.
     *
     * @param accessToken the access token used to authorise the request to the Organisation service
     * @param userId the unique identifier of the user whose organisation details are to be retrieved
     * @param submissionReference the submission reference used to provide context in error messages
     * @return the {@link OrganisationsResponse} containing the organisation details for the user
     * @throws GenericServiceException if the organisation cannot be found or the response is invalid
     */
    public OrganisationsResponse findOrganisationByUserId(String accessToken,
                                                          String userId,
                                                          String submissionReference)
            throws GenericServiceException {
        ResponseEntity<OrganisationsResponse> organisationsResponseEntity =
                organisationClient.retrieveOrganisationDetailsByUserId(accessToken, authTokenGenerator.generate(),
                        userId);
        var body = organisationsResponseEntity != null ? organisationsResponseEntity.getBody() : null;
        var organisationId = body != null ? body.getOrganisationIdentifier() : null;
        if (StringUtils.isBlank(organisationId)) {
            throw new GenericServiceException(
                    String.format(EXCEPTION_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID, submissionReference)
            );
        }
        return organisationsResponseEntity.getBody();
    }

    /**
     * Retrieves a user account by email address.
     * <p>
     * Calls the Organisation service to look up a user using the provided email address.
     * If the response is null, contains no-body, or does not include a user identifier,
     * a {@link GenericServiceException} is thrown.
     *
     * @param accessToken the access token used to authorise the request to the Organisation service
     * @param email the email address of the user to be looked up
     * @param submissionReference the submission reference used to provide context in error messages
     * @return the {@link AccountIdByEmailResponse} containing the user's account identifier
     * @throws GenericServiceException if the user cannot be found or the response is invalid
     */
    public AccountIdByEmailResponse findUserByEmail(String accessToken,
                                                    String email,
                                                    String submissionReference)  throws GenericServiceException {
        ResponseEntity<AccountIdByEmailResponse> userResponseEntity;
        try {
            userResponseEntity =
                    organisationClient.getAccountIdByEmail(accessToken, authTokenGenerator.generate(), email);
        } catch (RuntimeException re) {
            String exceptionMessage = String.format(EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL_WITH_IO_EXCEPTION,
                    submissionReference, re);
            throw new GenericServiceException(exceptionMessage);
        }
        var body = userResponseEntity != null ? userResponseEntity.getBody() : null;
        var userIdentifier = body != null ? body.getUserIdentifier() : null;

        if (StringUtils.isBlank(userIdentifier)) {
            throw new GenericServiceException(
                    String.format(EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL, submissionReference)
            );
        }
        return userResponseEntity.getBody();
    }

    /**
     * Grants a case role to a user for the specified case.
     * <p>
     * This method creates a case user role assignment for the given user and case,
     * and submits the assignment to CCD.
     *
     * @param userId the unique identifier of the user to be granted access
     * @param caseId the identifier of the case to which access is being granted
     * @param caseRole the case role to assign to the user
     */
    public void grantCaseAccess(String userId, String caseId, String caseRole) {
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
                .userId(userId)
                .caseDataId(caseId)
                .caseRole(caseRole)
                .build();
        try {
            caseAssignment.addCaseUserRole(CaseAssignmentUserRolesRequest.builder()
                    .caseAssignmentUserRoles(List.of(caseAssignmentUserRole))
                    .build());
        } catch (IOException e) {
            log.error("Failed to add case assignment user role", e);
        }
    }
}
