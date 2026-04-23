package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
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
    void isClaimantNonSystemUserReturnsTrueWhenCaseDataIsNull() {
        assertTrue(ClaimantUtil.isClaimantNonSystemUser(null));
    }

    @Test
    void isClaimantNonSystemUserReturnsFalseForRepresentedClaimantWithMyHmctsCase() {
        CaseData representedCaseData = new CaseData();
        representedCaseData.setMigratedFromEcm(YES);
        representedCaseData.setClaimantRepresentedQuestion(YES);
        representedCaseData.setRepresentativeClaimantType(new RepresentedTypeC());
        representedCaseData.getRepresentativeClaimantType().setMyHmctsOrganisation(
            Organisation.builder().organisationID("org-1").organisationName("Org").build()
        );

        assertFalse(ClaimantUtil.isClaimantNonSystemUser(representedCaseData));
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
        CaseData updatedCaseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        assertThat(updatedCaseData.getClaimantId()).isNull();
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

        assertThat(exception.getMessage())
            .isEqualTo("Unable to add idam id because case has already been assigned caseId, " + TEST_CASE_ID);
    }

    @Test
    void validateClaimantAssignmentDoesNotOverwriteExistingHubLinksStatuses() {
        HubLinksStatuses existingStatuses = new HubLinksStatuses();
        existingStatuses.setPersonalDetails("completed");
        caseData.setHubLinksStatuses(existingStatuses);
        caseDetails = createCaseDetailsWithCaseData(caseData);

        ClaimantUtil.validateClaimantAssignment(caseDetails, null, TEST_USER_ID);

        CaseData updatedCaseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        assertThat(updatedCaseData.getHubLinksStatuses().getPersonalDetails()).isEqualTo("completed");
    }

    @Test
    void validateClaimantAssignmentDoesNotModifyCaseDataWhenNoAssignmentsExist() {
        caseData.setClaimantId(TEST_USER_ID);
        caseDetails = createCaseDetailsWithCaseData(caseData);

        boolean result = ClaimantUtil.validateClaimantAssignment(caseDetails, null, TEST_USER_ID);

        assertFalse(result);
        CaseData updatedCaseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        assertThat(updatedCaseData.getClaimantId()).isEqualTo(TEST_USER_ID);
    }

    @Test
    void validateClaimantAssignmentReturnsFalseForEmptyCaseData() {
        CaseDetails emptyCaseDetails = CaseDetails.builder().id(TEST_CASE_ID).data(null).build();

        boolean result = ClaimantUtil.validateClaimantAssignment(emptyCaseDetails, null, TEST_USER_ID);

        assertFalse(result);
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

    @Test
    void genericTseApplicationTypeCanBeSerialized() {
        GenericTseApplicationType respondentApp = GenericTseApplicationType.builder()
            .applicant(RESPONDENT_TITLE)
            .type("Amend response")
            .build();
        GenericTseApplicationTypeItem appItem = GenericTseApplicationTypeItem.builder()
            .id("1")
            .value(respondentApp)
            .build();
        caseData.setGenericTseApplicationCollection(List.of(appItem));

        Map<String, Object> map = EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData);
        CaseData deserialized = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(map);

        assertThat(deserialized.getGenericTseApplicationCollection()).isNotEmpty();
        assertThat(deserialized.getGenericTseApplicationCollection().get(0).getValue().getApplicant())
            .isEqualTo(RESPONDENT_TITLE);
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
