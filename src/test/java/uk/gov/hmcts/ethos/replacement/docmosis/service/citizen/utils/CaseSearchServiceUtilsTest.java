package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.CREATOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.DEFENDANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_LONG;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_STRING_NOT_MATCH;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_DUMMY_ROLE;

final class CaseSearchServiceUtilsTest {

    @Test
    void theDetermineCaseUserRole() {
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder().build();
        // returns empty string if caseDetails is null
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(null,
                caseAssignmentUserRole)).isEmpty();

        // returns empty string if case details id is empty
        CaseDetails caseDetails = CaseDetails.builder().build();
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(caseDetails, caseAssignmentUserRole)).isEmpty();

        // returns empty string if caseAssignmentUserRole is null
        caseDetails.setId(TEST_CASE_ID_LONG);
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(caseDetails, null))
                .isEmpty();

        // returns empty string if caseAssignmentUserRole caseDataId is empty
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(caseDetails, caseAssignmentUserRole))
                .isEmpty();

        // returns empty string if caseDetails id and caseAssignmentUserRole caseDataId do not match
        CaseAssignmentUserRole caseAssignmentUserRoleCaseDataIdNotMatch = CaseAssignmentUserRole.builder()
                .caseDataId(TEST_CASE_ID_STRING_NOT_MATCH).build();
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(caseDetails,
                caseAssignmentUserRoleCaseDataIdNotMatch)).isEmpty();

        // returns empty string if caseAssignmentUserRole caseRole is not CREATOR or DEFENDANT
        CaseAssignmentUserRole caseAssignmentUserRoleCaseRoleNotMatch = CaseAssignmentUserRole.builder()
                .caseDataId(TEST_CASE_ID_STRING).caseRole(TEST_DUMMY_ROLE).build();
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(caseDetails,
                caseAssignmentUserRoleCaseRoleNotMatch)).isEmpty();

        // returns CREATOR if caseDetails id and caseAssignmentUserRole caseDataId match and caseRole is CREATOR
        CaseAssignmentUserRole caseAssignmentUserRoleCaseRoleCreator = CaseAssignmentUserRole.builder()
                .caseDataId(TEST_CASE_ID_STRING).caseRole(CREATOR).build();
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(caseDetails,
                caseAssignmentUserRoleCaseRoleCreator)).isEqualTo(CREATOR);

        // returns DEFENDANT if caseDetails id and caseAssignmentUserRole caseDataId match and caseRole is DEFENDANT
        CaseAssignmentUserRole caseAssignmentUserRoleCaseRoleDefendant = CaseAssignmentUserRole.builder()
                .caseDataId(TEST_CASE_ID_STRING).caseRole(DEFENDANT).build();
        assertThat(CitizenCaseSearchServiceUtils.determineCaseUserRole(caseDetails,
                caseAssignmentUserRoleCaseRoleDefendant)).isEqualTo(DEFENDANT);

    }

    @Test
    void theFilterCaseDetailsByUserRole() {
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
                .caseDataId(TEST_CASE_ID_STRING).caseRole(TEST_DUMMY_ROLE).build();
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_LONG).build();
        // returns empty list if caseDetailsList is empty
        assertThat(CitizenCaseSearchServiceUtils.filterCaseDetailsByUserRole(
                null, List.of(caseAssignmentUserRole), DEFENDANT)).isEmpty();
        assertThat(CitizenCaseSearchServiceUtils.filterCaseDetailsByUserRole(
                List.of(caseDetails), null, DEFENDANT)).isEmpty();
        assertThat(CitizenCaseSearchServiceUtils.filterCaseDetailsByUserRole(
                List.of(caseDetails), List.of(caseAssignmentUserRole), StringUtils.EMPTY)).isEmpty();
        // returns empty list if no match found with case details and case assignment user role not has CREATOR or
        // DEFENDANT role
        assertThat(CitizenCaseSearchServiceUtils.filterCaseDetailsByUserRole(
                List.of(caseDetails), List.of(caseAssignmentUserRole), DEFENDANT)).isEmpty();
        // returns empty list if case assignment user role is CREATOR but case user role parameter is DEFENDANT
        CaseAssignmentUserRole caseAssignmentUserRoleCaseRoleCreator = CaseAssignmentUserRole.builder()
                .caseDataId(TEST_CASE_ID_STRING).caseRole(CREATOR).build();
        assertThat(CitizenCaseSearchServiceUtils.filterCaseDetailsByUserRole(
                List.of(caseDetails), List.of(caseAssignmentUserRoleCaseRoleCreator), DEFENDANT)).isEmpty();
        // returns list with one case details if case assignment user role is DEFENDANT and case user role is DEFENDANT
        CaseAssignmentUserRole caseAssignmentUserRoleCaseRoleDefendant = CaseAssignmentUserRole.builder()
                .caseDataId(TEST_CASE_ID_STRING).caseRole(DEFENDANT).build();
        assertThat(CitizenCaseSearchServiceUtils.filterCaseDetailsByUserRole(
                List.of(caseDetails), List.of(caseAssignmentUserRoleCaseRoleDefendant), DEFENDANT))
                .hasSize(NumberUtils.INTEGER_ONE).contains(caseDetails);

    }
}
