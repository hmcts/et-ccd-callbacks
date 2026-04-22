package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleConflictException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

class ClaimantUtilTest {
    private static final String TEST_USER_ID = "test-user-id-12345";
    private static final String DIFFERENT_USER_ID = "different-user-id-67890";
    private static final Long TEST_CASE_ID = 1_646_225_213_651_590L;

    private CaseData caseData;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        caseDetails = createCaseDetailsWithCaseData(caseData);
    }

    @Test
    void isClaimantNonSystemUserTest_BothNull() {
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(null);
        assertTrue(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_SubmissionYes() {
        caseData.setEt1OnlineSubmission("Yes");
        caseData.setHubLinksStatuses(null);
        assertFalse(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_HubLinksStatuses() {
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        assertFalse(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_AllYes() {
        caseData.setEt1OnlineSubmission(YES);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        caseData.setMigratedFromEcm(YES);
        assertTrue(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void validateClaimantAssignmentReturnsFalseWhenNoAssignmentsExist() {
        boolean result = ClaimantUtil.validateClaimantAssignment(caseDetails, null, TEST_USER_ID);

        assertFalse(result);
    }

    @Test
    void validateClaimantAssignmentReturnsTrueWhenSameCreatorAlreadyAssigned() {
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                .userId(TEST_USER_ID)
                .caseRole(CASE_USER_ROLE_CREATOR)
                .build()))
            .build();

        boolean result = ClaimantUtil.validateClaimantAssignment(caseDetails, assignmentData, TEST_USER_ID);

        assertTrue(result);
    }

    @Test
    void validateClaimantAssignmentThrowsWhenDifferentCreatorAlreadyAssigned() {
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                .userId(DIFFERENT_USER_ID)
                .caseRole(CASE_USER_ROLE_CREATOR)
                .build()))
            .build();

        CaseUserRoleConflictException exception = assertThrows(
            CaseUserRoleConflictException.class,
            () -> ClaimantUtil.validateClaimantAssignment(caseDetails, assignmentData, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains("case has already been assigned");
    }

    @Test
    void validateClaimantAssignmentReturnsFalseWhenAssignmentsContainNoCreatorRole() {
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                .userId(DIFFERENT_USER_ID)
                .caseRole(CASE_USER_ROLE_DEFENDANT)
                .build()))
            .build();

        boolean result = ClaimantUtil.validateClaimantAssignment(caseDetails, assignmentData, TEST_USER_ID);

        assertThat(result).isFalse();
    }

    private CaseDetails createCaseDetailsWithCaseData(CaseData inputCaseData) {
        Map<String, Object> caseDataMap = EmployeeObjectMapper.mapCaseDataToLinkedHashMap(inputCaseData);
        if (caseDataMap == null) {
            caseDataMap = new HashMap<>();
        }
        return CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .build();
    }
}
