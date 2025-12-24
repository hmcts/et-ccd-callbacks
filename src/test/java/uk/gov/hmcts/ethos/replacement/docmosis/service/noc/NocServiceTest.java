package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NocServiceTest {
    private static final String CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String ORGANISATION_ID_OLD = "ORG_OLD";
    private static final String ORGANISATION_ID_NEW = "ORG3_NEW";
    private static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";

    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdCaseAssignment caseAssignment;

    @InjectMocks
    private NocService nocService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nocService = new NocService(nocCcdService, adminUserService, caseAssignment);
    }

    @Test
    void removeOrganisationRepresentativeAccess_shouldRevokeAccessIfRoleMatches() throws IOException {
        String caseId = "case123";
        Organisation organisationToRemove =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();
        ChangeOrganisationRequest changeRequest = createChangeOrganisationRequest(null, organisationToRemove);

        CaseUserAssignment assignment = CaseUserAssignment.builder()
                .userId("user1")
                .caseRole(CLAIMANT_SOLICITOR)
                .organisationId(ORGANISATION_ID_OLD)
                .caseId(caseId)
                .build();
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(assignment))
                .build();

        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(nocCcdService.getCaseAssignments(anyString(), eq(caseId))).thenReturn(assignmentData);

        nocService.removeOrganisationRepresentativeAccess(caseId, changeRequest);

        verify(nocCcdService, times(1)).revokeCaseAssignments(anyString(), any(CaseUserAssignmentData.class));
    }

    @Test
    void removeOrganisationRepresentativeAccess_shouldNotRevokeAccessIfNoMatchingRole() throws IOException {
        Organisation organisationToRemove =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();

        Organisation organisationToAdd =
                Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_2).build();

        ChangeOrganisationRequest changeRequest =
                createChangeOrganisationRequest(organisationToAdd, organisationToRemove);

        CaseUserAssignment assignment = CaseUserAssignment.builder()
                .userId("user1")
                .caseRole("role1")
                .build();
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(assignment))
                .build();

        String caseId = "case123";
        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(nocCcdService.getCaseAssignments(anyString(), eq(caseId))).thenReturn(assignmentData);

        nocService.removeOrganisationRepresentativeAccess(caseId, changeRequest);

        verify(nocCcdService, never()).revokeCaseAssignments(anyString(), any(CaseUserAssignmentData.class));
    }

    @Test
    void grantCaseAccess_shouldAddCaseUserRole() throws IOException {
        String userId = "userId";
        String caseId = "case123";
        String caseRole = "[CLAIMANTSOLICITOR]";

        doNothing().when(caseAssignment).addCaseUserRole(any(CaseAssignmentUserRolesRequest.class));

        nocService.grantCaseAccess(userId, caseId, caseRole);

        ArgumentCaptor<CaseAssignmentUserRolesRequest> captor =
                ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);
        verify(caseAssignment, times(1)).addCaseUserRole(captor.capture());
        CaseAssignmentUserRolesRequest request = captor.getValue();
        assertEquals(1, request.getCaseAssignmentUserRoles().size());
        CaseAssignmentUserRole role = request.getCaseAssignmentUserRoles().getFirst();
        assertEquals(userId, role.getUserId());
        assertEquals(caseId, role.getCaseDataId());
        assertEquals(caseRole, role.getCaseRole());
    }

    private ChangeOrganisationRequest createChangeOrganisationRequest(Organisation organisationToAdd,
                                                                      Organisation organisationToRemove) {
        DynamicFixedListType caseRole = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(CLAIMANT_SOLICITOR);
        dynamicValueType.setLabel(CLAIMANT_SOLICITOR);
        caseRole.setValue(dynamicValueType);

        return ChangeOrganisationRequest.builder()
                .organisationToAdd(organisationToAdd)
                .organisationToRemove(organisationToRemove)
                .caseRoleId(caseRole)
                .build();
    }
}
