package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class NocService {
    private final NocCcdService nocCcdService;
    private final AdminUserService adminUserService;
    private final UserIdamService userIdamService;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdCaseAssignment caseAssignment;

    /**
     * Revokes access from all users of an organisation being replaced or removed.
     * @param caseId - case id of case to apply update to
     * @param changeOrganisationRequest - containing case role and id of organisation to remove
     * @throws IOException - thrown if no ccd service is inaccessible
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

    public void grantClaimantRepAccess(String accessToken, String email,
                                       String caseId, Organisation organisationToAdd) {
        try {
            List<UserDetails> users = userIdamService.getUsersByQuery(accessToken, "email:" + email, null, null);
            for (UserDetails user : users) {
                OrganisationsResponse organisationsResponse = organisationClient.retrieveOrganisationDetailsByUserId(
                        accessToken, authTokenGenerator.generate(), user.getUid()).getBody();

                if (organisationsResponse != null
                        && organisationToAdd.getOrganisationID()
                        .equals(organisationsResponse.getOrganisationIdentifier())) {
                    grantCaseAccess(user.getUid(), caseId, ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
                }
            }
        } catch (IOException e) {
            log.error("Representative search failed");
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
