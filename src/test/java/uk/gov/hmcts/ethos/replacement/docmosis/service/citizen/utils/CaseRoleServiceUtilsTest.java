package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils.CaseRoleServiceUtils.createCaseUserRoleRequest;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_LONG;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_USER_ROLE_CLAIMANT_SOLICITOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CLAIMANT_SOLICITOR_IDAM_ID;

final class CaseRoleServiceUtilsTest {

    @Test
    void theCreateCaseUserRoleRequest() {
        CaseAssignmentUserRolesRequest expectedCaseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder()
                        .caseDataId(TEST_CASE_ID_STRING)
                        .userId(TEST_CLAIMANT_SOLICITOR_IDAM_ID)
                        .caseRole(TEST_CASE_USER_ROLE_CLAIMANT_SOLICITOR).build())).build();
        CaseAssignmentUserRolesRequest actualCaseAssignmentUserRolesRequest = createCaseUserRoleRequest(
                        TEST_CLAIMANT_SOLICITOR_IDAM_ID,
                        CaseDetails.builder().id(TEST_CASE_ID_LONG).build(),
                        TEST_CASE_USER_ROLE_CLAIMANT_SOLICITOR);
        assertThat(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getCaseDataId())
                .isEqualTo(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles()
                        .getFirst().getCaseDataId());
        assertThat(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getCaseRole())
                .isEqualTo(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getCaseRole());
        assertThat(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getUserId())
                .isEqualTo(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getUserId());
    }

}
