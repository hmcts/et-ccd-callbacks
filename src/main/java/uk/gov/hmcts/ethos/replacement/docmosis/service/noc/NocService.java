package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EMPTY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_FAILED_TO_ASSIGN_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_INVALID_GRANT_ACCESS_PARAMETER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL;

@RequiredArgsConstructor
@Component
@Slf4j
public class NocService {
    private final NocCcdService nocCcdService;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment caseAssignment;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;

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

    public void grantRepresentativeAccess(String accessToken, String email,
                                          String submissionReference, Organisation organisationToAdd,
                                          String role) throws GenericServiceException {
        if (StringUtils.isBlank(accessToken)
                || StringUtils.isBlank(email)
                || StringUtils.isBlank(submissionReference)
                || ObjectUtils.isEmpty(organisationToAdd)
                || StringUtils.isBlank(organisationToAdd.getOrganisationID())
                || !RoleUtils.isValidRole(role)) {
            String tmpAccessToken = StringUtils.isEmpty(accessToken) ? EMPTY_LOWERCASE : accessToken;
            String tmpEmail = StringUtils.isEmpty(email) ? EMPTY_LOWERCASE : email;
            String tmpCaseId = StringUtils.isEmpty(submissionReference) ? EMPTY_LOWERCASE : submissionReference;
            String tmpOrganisationId = ObjectUtils.isEmpty(organisationToAdd) ? EMPTY_LOWERCASE
                    : StringUtils.isBlank(organisationToAdd.getOrganisationID()) ? EMPTY_LOWERCASE
                    : organisationToAdd.getOrganisationID();
            String tmpRole = StringUtils.isBlank(role) ? EMPTY_LOWERCASE : role;
            String exceptionMessage = String.format(EXCEPTION_INVALID_GRANT_ACCESS_PARAMETER, tmpAccessToken, tmpEmail,
                    tmpCaseId, tmpOrganisationId, tmpRole);
            throw new GenericServiceException(exceptionMessage);
        }
        try {
            ResponseEntity<AccountIdByEmailResponse> userResponseEntity =
                    organisationClient.getAccountIdByEmail(accessToken, authTokenGenerator.generate(), email);
            if (ObjectUtils.isEmpty(userResponseEntity)
                    || ObjectUtils.isEmpty(userResponseEntity.getBody())
                    || StringUtils.isBlank(userResponseEntity.getBody().getUserIdentifier())) {
                String exceptionMessage = String.format(EXCEPTION_UNABLE_TO_GET_ACCOUNT_ID_BY_EMAIL, email,
                        submissionReference);
                throw new GenericServiceException(exceptionMessage);
            }
            AccountIdByEmailResponse userResponse = userResponseEntity.getBody();
            ResponseEntity<OrganisationsResponse> organisationsResponseEntity =
                    organisationClient.retrieveOrganisationDetailsByUserId(accessToken, authTokenGenerator.generate(),
                            userResponse.getUserIdentifier());
            if (ObjectUtils.isEmpty(organisationsResponseEntity)
                    || ObjectUtils.isEmpty(organisationsResponseEntity.getBody())
                    || StringUtils.isBlank(organisationsResponseEntity.getBody().getOrganisationIdentifier())
                    || !Strings.CS.equals(organisationsResponseEntity.getBody().getOrganisationIdentifier(),
                    organisationToAdd.getOrganisationID())) {
                String exceptionMessage = String.format(EXCEPTION_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID,
                        userResponse.getUserIdentifier(), submissionReference);
                throw new GenericServiceException(exceptionMessage);
            }
            grantCaseAccess(userResponse.getUserIdentifier(), submissionReference, role);
        } catch (IOException e) {
            log.error(EXCEPTION_FAILED_TO_ASSIGN_ROLE, role, email, submissionReference);
            throw new GenericServiceException(e);
        }
    }

    public void grantCaseAccess(String userId, String caseId, String caseRole) throws IOException {
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
                .userId(userId)
                .caseDataId(caseId)
                .caseRole(caseRole)
                .build();
        caseAssignment.addCaseUserRole(CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRoles(List.of(caseAssignmentUserRole))
                .build());
    }
}
