package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.*;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshSharedUsersServiceTest {

    private static final String CASE_ID = "1234";
    private static final String DUMMY_ORG_ID = "dummyId";

    @Mock private AdminUserService adminUserService;
    @Mock private CcdCaseAssignment ccdCaseAssignment;
    @Mock private CaseUserAssignmentData mockUserRolesResponse;

    @InjectMocks private RefreshSharedUsersService service;

    private CaseDetails caseDetails;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseDetails = new CaseDetails();
        caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
    }

    @Test
    void shouldPopulateClaimantRepresentativeUsers() throws IOException {
        setupRepresentativeClaimantType();
        UserDetails userDetails = createUserDetails("test@example.com", "Test", "User");
        CaseUserAssignment assignment = createCaseUserAssignment("user1", ClaimantSolicitorRole.CLAIMANTSOLICITOR.toString());
        setupMockBehaviour("user1", userDetails, assignment);

        service.refreshSharedUsers(caseDetails);

        List<GenericTypeItem<OrganisationUsersIdamUser>> orgUsers = caseData.getRepresentativeClaimantType().getOrganisationUsers();
        assertThat(orgUsers).hasSize(1);
        assertThat(orgUsers.get(0).getValue().getEmail()).isEqualTo("test@example.com");
        assertThat(orgUsers.get(0).getValue().getFirstName()).isEqualTo("Test");
        assertThat(orgUsers.get(0).getValue().getLastName()).isEqualTo("User");
    }

    @Test
    void shouldPopulateMultipleClaimantRepresentativeUsers() throws IOException {
        setupRepresentativeClaimantType();

        UserDetails userDetails = createUserDetails("test@example.com", "Test", "User");
        UserDetails userDetails2 = createUserDetails("test2@example.com", "Test2", "User2");

        CaseUserAssignment assignment1 = createCaseUserAssignment("user1", ClaimantSolicitorRole.CLAIMANTSOLICITOR.toString());
        CaseUserAssignment assignment2 = createCaseUserAssignment("user2", ClaimantSolicitorRole.CLAIMANTSOLICITOR.toString());

        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(mockUserRolesResponse);
        when(mockUserRolesResponse.getCaseUserAssignments()).thenReturn(List.of(assignment1, assignment2));

        when(adminUserService.getUserDetails("user1")).thenReturn(userDetails);
        when(adminUserService.getUserDetails("user2")).thenReturn(userDetails2);

        service.refreshSharedUsers(caseDetails);

        List<GenericTypeItem<OrganisationUsersIdamUser>> orgUsers =
                caseData.getRepresentativeClaimantType().getOrganisationUsers();
        assertThat(orgUsers).hasSize(2);

        assertThat(orgUsers)
                .extracting(user -> user.getValue().getEmail())
                .containsExactlyInAnyOrder("test@example.com", "test2@example.com");

        assertThat(orgUsers)
                .extracting(user -> user.getValue().getFirstName())
                .containsExactlyInAnyOrder("Test", "Test2");

        assertThat(orgUsers)
                .extracting(user -> user.getValue().getLastName())
                .containsExactlyInAnyOrder("User", "User2");
    }


    @Test
    void shouldPopulateRespondentRepresentativeUsers() throws IOException {
        int index = SolicitorRole.SOLICITORA.getIndex();
        setupRespondentOrganisation(index);
        UserDetails userDetails = createUserDetails("resp@example.com", "Resp", "Solicitor");
        CaseUserAssignment assignment = createCaseUserAssignment("user1", SolicitorRole.SOLICITORA.getCaseRoleLabel());
        setupMockBehaviour("user1", userDetails, assignment);

        service.refreshSharedUsers(caseDetails);

        List<GenericTypeItem<OrganisationUsersIdamUser>> users =
                caseData.getRepCollection().get(index).getValue().getOrganisationUsers();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getValue().getEmail()).isEqualTo("resp@example.com");
        assertThat(users.get(0).getValue().getFirstName()).isEqualTo("Resp");
        assertThat(users.get(0).getValue().getLastName()).isEqualTo("Solicitor");
    }

    @Test
    void shouldPopulateRespondentRepresentativeMultipleUsers() throws IOException {
        UserDetails userDetails1 = createUserDetails("resp@example.com", "Resp", "Solicitor");
        UserDetails userDetails2 = createUserDetails("resp2@example.com", "Resp2", "Solicitor2");

        CaseUserAssignment assignment1 = createCaseUserAssignment("user1", SolicitorRole.SOLICITORA.getCaseRoleLabel());
        CaseUserAssignment assignment2 = createCaseUserAssignment("user2", SolicitorRole.SOLICITORB.getCaseRoleLabel());

        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(mockUserRolesResponse);
        when(mockUserRolesResponse.getCaseUserAssignments()).thenReturn(List.of(assignment1, assignment2));
        when(adminUserService.getUserDetails("user1")).thenReturn(userDetails1);
        when(adminUserService.getUserDetails("user2")).thenReturn(userDetails2);

        setupRespondentOrganisation(SolicitorRole.SOLICITORA.getIndex());
        setupRespondentOrganisation(SolicitorRole.SOLICITORB.getIndex());

        service.refreshSharedUsers(caseDetails);

        int indexA = SolicitorRole.SOLICITORA.getIndex();
        List<GenericTypeItem<OrganisationUsersIdamUser>> usersA =
                caseData.getRepCollection().get(indexA).getValue().getOrganisationUsers();
        assertThat(usersA).hasSize(1);
        assertThat(usersA.get(0).getValue().getEmail()).isEqualTo("resp@example.com");
        assertThat(usersA.get(0).getValue().getFirstName()).isEqualTo("Resp");
        assertThat(usersA.get(0).getValue().getLastName()).isEqualTo("Solicitor");

        int indexB = SolicitorRole.SOLICITORB.getIndex();
        List<GenericTypeItem<OrganisationUsersIdamUser>> usersB =
                caseData.getRepCollection().get(indexB).getValue().getOrganisationUsers();
        assertThat(usersB).hasSize(1);
        assertThat(usersB.get(0).getValue().getEmail()).isEqualTo("resp2@example.com");
        assertThat(usersB.get(0).getValue().getFirstName()).isEqualTo("Resp2");
        assertThat(usersB.get(0).getValue().getLastName()).isEqualTo("Solicitor2");
    }

    @Test
    void shouldNotFailIfNoClaimantOrRespondentUsers() throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(mockUserRolesResponse);
        when(mockUserRolesResponse.getCaseUserAssignments()).thenReturn(Collections.emptyList());

        service.refreshSharedUsers(caseDetails);

        assertThat(caseData.getRepresentativeClaimantType()).isNull();
        assertThat(caseData.getRepCollection()).isNull();
    }

    private void setupRepresentativeClaimantType() {
        RepresentedTypeC rep = new RepresentedTypeC();
        Organisation organisation = Organisation.builder()
                .organisationID(RefreshSharedUsersServiceTest.DUMMY_ORG_ID)
                .build();
        rep.setMyHmctsOrganisation(organisation);
        caseData.setRepresentativeClaimantType(rep);
    }

    private void setupRespondentOrganisation(int index) {
        List<RepresentedTypeRItem> repCollection = new ArrayList<>();
        for (int i = 0; i <= index; i++) {
            RepresentedTypeR rep = new RepresentedTypeR();
            Organisation organisation = Organisation.builder()
                    .organisationID(DUMMY_ORG_ID)
                    .build();
            rep.setRespondentOrganisation(organisation);

            RepresentedTypeRItem item = new RepresentedTypeRItem();
            item.setValue(rep);
            repCollection.add(item);
        }
        caseData.setRepCollection(repCollection);
    }

    private UserDetails createUserDetails(String email, String firstName, String lastName) {
        UserDetails userDetails = new UserDetails();
        userDetails.setEmail(email);
        userDetails.setFirstName(firstName);
        userDetails.setLastName(lastName);
        return userDetails;
    }

    private CaseUserAssignment createCaseUserAssignment(String userId, String caseRole) {
        CaseUserAssignment assignment = new CaseUserAssignment();
        assignment.setUserId(userId);
        assignment.setCaseRole(caseRole);
        return assignment;
    }

    private void setupMockBehaviour(String userId, UserDetails userDetails, CaseUserAssignment assignment) throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(mockUserRolesResponse);
        when(mockUserRolesResponse.getCaseUserAssignments()).thenReturn(List.of(assignment));
        when(adminUserService.getUserDetails(userId)).thenReturn(userDetails);
    }
}
