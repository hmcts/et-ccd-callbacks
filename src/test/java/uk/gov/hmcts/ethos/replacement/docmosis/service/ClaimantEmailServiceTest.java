package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;

@ExtendWith(MockitoExtension.class)
class ClaimantEmailServiceTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String OLD_EMAIL = "old@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String OLD_USER_ID = "old-user-id";
    private static final String NEW_USER_ID = "new-user-id";

    @Mock
    private IdamApi idamApi;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdCaseAssignment ccdCaseAssignment;

    private ClaimantEmailService service;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        service = new ClaimantEmailService(idamApi, adminUserService, ccdCaseAssignment);
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(OLD_EMAIL);
        CaseData caseData = new CaseData();
        caseData.setClaimantType(claimantType);
        caseData.setCurrentClaimantEmail(OLD_EMAIL);
        caseData.setNewClaimantEmail(NEW_EMAIL);
        caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseData(caseData);
    }

    @Test
    void initialisePrepopulatesCurrentEmailAndClearsNewEmail() {
        service.initialise(caseDetails.getCaseData());

        assertThat(caseDetails.getCaseData().getCurrentClaimantEmail()).isEqualTo(OLD_EMAIL);
        assertThat(caseDetails.getCaseData().getNewClaimantEmail()).isNull();
    }

    @Test
    void initialiseHandlesMissingClaimantDetails() {
        caseDetails.getCaseData().setClaimantType(null);

        service.initialise(caseDetails.getCaseData());

        assertThat(caseDetails.getCaseData().getCurrentClaimantEmail()).isNull();
        assertThat(caseDetails.getCaseData().getNewClaimantEmail()).isNull();
    }

    @Test
    void validateRejectsInvalidEmailWithoutCallingIdam() {
        caseDetails.getCaseData().setNewClaimantEmail("not-an-email");

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly("The email address entered is invalid.");
        verify(idamApi, never()).searchUsersByQuery(anyString(), anyString(), any(), any());
    }

    @Test
    void validateRejectsEmailWithoutAnExactIdamAccount() {
        UserDetails differentUser = user("different@example.com", "different-user-id");
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(idamApi.searchUsersByQuery("admin-token", NEW_EMAIL, 0, 50))
                .thenReturn(List.of(differentUser));

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly(ClaimantEmailService.IDAM_USER_NOT_FOUND_ERROR);
    }

    @Test
    void validateRejectsUnchangedEmailIgnoringCase() {
        caseDetails.getCaseData().setNewClaimantEmail(OLD_EMAIL.toUpperCase(Locale.ROOT));

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly(ClaimantEmailService.EMAIL_UNCHANGED_ERROR);
        verify(idamApi, never()).searchUsersByQuery(anyString(), anyString(), any(), any());
    }

    @Test
    void validateAcceptsOneExactIdamAccountIgnoringCase() {
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(idamApi.searchUsersByQuery("admin-token", NEW_EMAIL, 0, 50))
                .thenReturn(List.of(user(NEW_EMAIL.toUpperCase(Locale.ROOT), NEW_USER_ID)));

        assertThat(service.validateNewEmail(caseDetails.getCaseData())).isEmpty();
    }

    @Test
    void validateRejectsMultipleExactIdamAccounts() {
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(idamApi.searchUsersByQuery("admin-token", NEW_EMAIL, 0, 50))
                .thenReturn(List.of(
                        user(NEW_EMAIL, NEW_USER_ID),
                        user(NEW_EMAIL.toUpperCase(Locale.ROOT), "another-user-id")));

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly(ClaimantEmailService.IDAM_USER_AMBIGUOUS_ERROR);
    }

    @Test
    void prepareUpdateReturnsInputErrorsWithoutCallingIdam() {
        caseDetails.getCaseData().setNewClaimantEmail(OLD_EMAIL);

        assertThat(service.prepareUpdate(caseDetails))
                .containsExactly(ClaimantEmailService.EMAIL_UNCHANGED_ERROR);
        verify(idamApi, never()).searchUsersByQuery(anyString(), anyString(), any(), any());
        assertThat(caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress()).isEqualTo(OLD_EMAIL);
    }

    @Test
    void prepareUpdateReturnsIdamLookupErrorsWithoutChangingCaseData() {
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(idamApi.searchUsersByQuery("admin-token", NEW_EMAIL, 0, 50)).thenReturn(List.of());

        assertThat(service.prepareUpdate(caseDetails))
                .containsExactly(ClaimantEmailService.IDAM_USER_NOT_FOUND_ERROR);
        assertThat(caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress()).isEqualTo(OLD_EMAIL);
    }

    @Test
    void prepareUpdateSetsEmailAndNewClaimantIdWhenCreatorAccessExists() throws IOException {
        mockNewIdamUser();
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignmentsWithCreator());

        assertThat(service.prepareUpdate(caseDetails)).isEmpty();
        assertThat(caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress()).isEqualTo(NEW_EMAIL);
        assertThat(caseDetails.getCaseData().getClaimantId()).isEqualTo(NEW_USER_ID);
        assertThat(caseDetails.getCaseData().getCurrentClaimantEmail()).isNull();
        assertThat(caseDetails.getCaseData().getNewClaimantEmail()).isNull();
    }

    @Test
    void prepareUpdateChangesEmailOnlyWhenNoCreatorAccessExists() throws IOException {
        caseDetails.getCaseData().setClaimantId("existing-value");
        mockNewIdamUser();
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID))
                .thenReturn(CaseUserAssignmentData.builder().caseUserAssignments(List.of()).build());

        assertThat(service.prepareUpdate(caseDetails)).isEmpty();
        assertThat(caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress()).isEqualTo(NEW_EMAIL);
        assertThat(caseDetails.getCaseData().getClaimantId()).isEqualTo("existing-value");
    }

    @Test
    void prepareUpdateCreatesMissingClaimantDetails() throws IOException {
        caseDetails.getCaseData().setClaimantType(null);
        mockNewIdamUser();
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(null);

        assertThat(service.prepareUpdate(caseDetails)).isEmpty();
        assertThat(caseDetails.getCaseData().getClaimantType()).isNotNull();
        assertThat(caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress()).isEqualTo(NEW_EMAIL);
    }

    @Test
    void prepareUpdateReturnsErrorWhenCreatorAccessCannotBeChecked() throws IOException {
        mockNewIdamUser();
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenThrow(new IOException("lookup failed"));

        assertThat(service.prepareUpdate(caseDetails))
                .containsExactly(ClaimantEmailService.ACCESS_LOOKUP_ERROR);
        assertThat(caseDetails.getCaseData().getClaimantType().getClaimantEmailAddress()).isEqualTo(OLD_EMAIL);
    }

    @Test
    void reassignCreatorAccessRevokesOldAccessBeforeGrantingNewAccess() throws IOException {
        caseDetails.getCaseData().setClaimantId(NEW_USER_ID);
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignmentsWithCreator());

        service.reassignCreatorAccess(caseDetails);

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
    }

    @Test
    void reassignCreatorAccessRestoresOldAccessWhenGrantFails() throws IOException {
        caseDetails.getCaseData().setClaimantId(NEW_USER_ID);
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignmentsWithCreator());
        doThrow(new IOException("grant failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));

        assertThatThrownBy(() -> service.reassignCreatorAccess(caseDetails))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage("Failed to grant case access using the new claimant email");

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(OLD_USER_ID));
    }

    @Test
    void reassignCreatorAccessReportsLookupFailure() throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenThrow(new IOException("lookup failed"));

        assertThatThrownBy(() -> service.reassignCreatorAccess(caseDetails))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage("Failed to retrieve claimant case access")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void reassignCreatorAccessDoesNothingWhenUserIdIsUnchanged() throws IOException {
        caseDetails.getCaseData().setClaimantId(OLD_USER_ID);
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignmentsWithCreator());

        service.reassignCreatorAccess(caseDetails);

        verify(ccdCaseAssignment, never()).removeCaseUserRole(any());
        verify(ccdCaseAssignment, never()).addCaseUserRole(any());
    }

    @Test
    void reassignCreatorAccessReportsRevokeFailure() throws IOException {
        caseDetails.getCaseData().setClaimantId(NEW_USER_ID);
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignmentsWithCreator());
        doThrow(new IOException("revoke failed"))
                .when(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));

        assertThatThrownBy(() -> service.reassignCreatorAccess(caseDetails))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage("Failed to revoke access linked to the previous claimant email")
                .hasCauseInstanceOf(IOException.class);
        verify(ccdCaseAssignment, never()).addCaseUserRole(any());
    }

    @Test
    void reassignCreatorAccessPreservesRestoreFailureAsSuppressed() throws IOException {
        caseDetails.getCaseData().setClaimantId(NEW_USER_ID);
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignmentsWithCreator());
        doThrow(new IOException("grant failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        doThrow(new IOException("restore failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestForUser(OLD_USER_ID));

        assertThatThrownBy(() -> service.reassignCreatorAccess(caseDetails))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage("Failed to grant case access using the new claimant email")
                .satisfies(exception -> assertThat(exception.getCause().getSuppressed())
                        .singleElement()
                        .satisfies(suppressed -> assertThat(suppressed).hasMessage("restore failed")));
    }

    @Test
    void reassignCreatorAccessDoesNothingWhenNoCreatorExists() throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID))
                .thenReturn(CaseUserAssignmentData.builder().caseUserAssignments(List.of()).build());

        service.reassignCreatorAccess(caseDetails);

        verify(ccdCaseAssignment, never()).removeCaseUserRole(any());
        verify(ccdCaseAssignment, never()).addCaseUserRole(any());
    }

    @Test
    void reassignCreatorAccessHandlesMissingAssignmentsList() throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID))
                .thenReturn(CaseUserAssignmentData.builder().caseUserAssignments(null).build());

        service.reassignCreatorAccess(caseDetails);

        verify(ccdCaseAssignment, never()).removeCaseUserRole(any());
        verify(ccdCaseAssignment, never()).addCaseUserRole(any());
    }

    private void mockNewIdamUser() {
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(idamApi.searchUsersByQuery("admin-token", NEW_EMAIL, 0, 50))
                .thenReturn(List.of(user(NEW_EMAIL, NEW_USER_ID)));
    }

    private UserDetails user(String email, String uid) {
        UserDetails user = new UserDetails();
        user.setEmail(email);
        user.setUid(uid);
        return user;
    }

    private CaseUserAssignmentData assignmentsWithCreator() {
        CaseUserAssignment creator = CaseUserAssignment.builder()
                .caseId(CASE_ID)
                .userId(OLD_USER_ID)
                .caseRole(CASE_USER_ROLE_CREATOR)
                .build();
        return CaseUserAssignmentData.builder().caseUserAssignments(List.of(creator)).build();
    }

    private CaseAssignmentUserRolesRequest requestForUser(String userId) {
        return argThat(request -> request.getCaseAssignmentUserRoles().size() == 1
                && userId.equals(request.getCaseAssignmentUserRoles().getFirst().getUserId())
                && CASE_USER_ROLE_CREATOR.equals(request.getCaseAssignmentUserRoles().getFirst().getCaseRole())
                && CASE_ID.equals(request.getCaseAssignmentUserRoles().getFirst().getCaseDataId()));
    }
}
