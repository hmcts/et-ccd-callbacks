package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

public final class CaseRoleServiceUtils {

    private CaseRoleServiceUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Builds a {@link CaseAssignmentUserRolesRequest} using the provided UserInfo,
     * {@link CaseDetails}, and case role.
     *
     *  <p>
     *      This method constructs a single {@link CaseAssignmentUserRole} that associates the given user
     *      with the specified case and role. The constructed role assignment is wrapped in a
     *      {@link CaseAssignmentUserRolesRequest} and returned.
     *  </p>
     *
     * @param userIdamId  the user IDAM ID
     * @param caseDetails the case details, including the case ID
     * @param caseRole    the role to assign to the user for the given case (e.g., "[CLAIMANT]", "[DEFENDANT]")
     * @return a {@link CaseAssignmentUserRolesRequest} containing the role assignment
     */
    public static CaseAssignmentUserRolesRequest createCaseUserRoleRequest(
            String userIdamId, CaseDetails caseDetails, String caseRole) {
        List<CaseAssignmentUserRole> caseAssignmentUserRoles = new ArrayList<>();
        caseAssignmentUserRoles.add(CaseAssignmentUserRole
                .builder()
                .caseDataId(String.valueOf(caseDetails.getId()))
                .userId(userIdamId)
                .caseRole(caseRole)
                .build());
        return CaseAssignmentUserRolesRequest.builder().caseAssignmentUserRoles(caseAssignmentUserRoles).build();
    }
}
