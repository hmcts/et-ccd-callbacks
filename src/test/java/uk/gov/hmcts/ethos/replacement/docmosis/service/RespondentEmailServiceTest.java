package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class RespondentEmailServiceTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String RESPONDENT_ID_ONE = "respondent-one";
    private static final String RESPONDENT_ID_TWO = "respondent-two";
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

    private RespondentEmailService service;
    private CaseDetails caseDetails;
    private RespondentSumTypeItem firstRespondent;
    private RespondentSumTypeItem secondRespondent;

    @BeforeEach
    void setUp() {
        service = new RespondentEmailService(idamApi, adminUserService, ccdCaseAssignment);
        firstRespondent = respondent(RESPONDENT_ID_ONE, "First respondent", OLD_EMAIL, OLD_USER_ID, NO);
        secondRespondent = respondent(RESPONDENT_ID_TWO, "Second respondent",
                "second@example.com", "second-user-id", NO);

        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(List.of(firstRespondent, secondRespondent));
        caseDetails = createCaseDetails(caseData);
    }

    @Test
    void initialiseListsOnlyUnrepresentedRespondents() {
        secondRespondent.getValue().setRepresented(YES);

        assertThat(service.initialise(caseDetails.getCaseData())).isEmpty();
        assertThat(caseDetails.getCaseData().getRespondentEmailUpdateSelection().getListItems())
                .extracting("code", "label")
                .containsExactly(tuple(RESPONDENT_ID_ONE, "First respondent"));
        assertThat(caseDetails.getCaseData().getCurrentRespondentEmail()).isNull();
        assertThat(caseDetails.getCaseData().getNewRespondentEmail()).isNull();
    }

    @Test
    void initialiseReturnsErrorWhenNoUnrepresentedRespondentExists() {
        firstRespondent.getValue().setRepresented(YES);
        secondRespondent.getValue().setRepresented(YES);

        assertThat(service.initialise(caseDetails.getCaseData()))
                .containsExactly(RespondentEmailService.NO_UNREPRESENTED_RESPONDENTS_ERROR);
    }

    @Test
    void populateCurrentEmailUsesSelectedRespondentsResponseEmail() {
        firstRespondent.getValue().setResponseRespondentEmail("portal@example.com");
        selectRespondent(RESPONDENT_ID_ONE);

        assertThat(service.populateCurrentEmail(caseDetails.getCaseData())).isEmpty();
        assertThat(caseDetails.getCaseData().getCurrentRespondentEmail()).isEqualTo("portal@example.com");
    }

    @Test
    void validateRejectsInvalidEmailWithoutCallingIdam() {
        selectRespondent(RESPONDENT_ID_ONE);
        caseDetails.getCaseData().setCurrentRespondentEmail(OLD_EMAIL);
        caseDetails.getCaseData().setNewRespondentEmail("not-an-email");

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly("The email address entered is invalid.");
        verify(idamApi, never()).searchUsersByQuery(anyString(), anyString(), any(), any());
    }

    @Test
    void validateRejectsEmailWithoutAnExactIdamAccount() {
        prepareSelectedEmailFields();
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(idamApi.searchUsersByQuery("admin-token", NEW_EMAIL, 0, 50))
                .thenReturn(List.of(user("different@example.com", "different-user-id")));

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly(RespondentEmailService.IDAM_USER_NOT_FOUND_ERROR);
    }

    @Test
    void prepareUpdateChangesOnlySelectedRespondentAndSetsNewIdWhenAccessExists() throws IOException {
        prepareSelectedEmailFields();
        mockNewIdamUser();
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignmentsWithDefendant());

        assertThat(service.prepareUpdate(caseDetails)).isEmpty();
        assertThat(firstRespondent.getValue().getRespondentEmail()).isEqualTo(NEW_EMAIL);
        assertThat(firstRespondent.getValue().getResponseRespondentEmail()).isEqualTo(NEW_EMAIL);
        assertThat(firstRespondent.getValue().getIdamId()).isEqualTo(NEW_USER_ID);
        assertThat(secondRespondent.getValue().getRespondentEmail()).isEqualTo("second@example.com");
    }

    @Test
    void prepareUpdateChangesEmailOnlyWhenSelectedRespondentHasNoAccess() throws IOException {
        prepareSelectedEmailFields();
        mockNewIdamUser();
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID))
                .thenReturn(CaseUserAssignmentData.builder().caseUserAssignments(List.of()).build());

        assertThat(service.prepareUpdate(caseDetails)).isEmpty();
        assertThat(firstRespondent.getValue().getRespondentEmail()).isEqualTo(NEW_EMAIL);
        assertThat(firstRespondent.getValue().getIdamId()).isEqualTo(OLD_USER_ID);
    }

    @Test
    void reassignDefendantAccessRevokesOldAccessBeforeGrantingNewAccess() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);
        CallbackRequest callbackRequest = callbackRequest(beforeData, afterData);

        service.reassignDefendantAccess(callbackRequest);

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
    }

    @Test
    void reassignDefendantAccessRestoresOldAccessWhenGrantFails() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);
        CallbackRequest callbackRequest = callbackRequest(beforeData, afterData);
        doThrow(new IOException("grant failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));

        assertThatThrownBy(() -> service.reassignDefendantAccess(callbackRequest))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage("Failed to grant case access using the new respondent email");

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(OLD_USER_ID));
    }

    private void prepareSelectedEmailFields() {
        selectRespondent(RESPONDENT_ID_ONE);
        caseDetails.getCaseData().setCurrentRespondentEmail(OLD_EMAIL);
        caseDetails.getCaseData().setNewRespondentEmail(NEW_EMAIL);
    }

    private void selectRespondent(String respondentId) {
        DynamicFixedListType selection = DynamicFixedListType.from(
                respondentId,
                RESPONDENT_ID_ONE.equals(respondentId) ? "First respondent" : "Second respondent",
                true);
        caseDetails.getCaseData().setRespondentEmailUpdateSelection(selection);
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

    private RespondentSumTypeItem respondent(String id, String name, String email, String userId, String represented) {
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        item.setId(id);
        item.setValue(RespondentSumType.builder()
                .respondentName(name)
                .respondentEmail(email)
                .idamId(userId)
                .represented(represented)
                .build());
        return item;
    }

    private CaseDetails createCaseDetails(CaseData caseData) {
        CaseDetails details = new CaseDetails();
        details.setCaseId(CASE_ID);
        details.setCaseData(caseData);
        return details;
    }

    private CaseUserAssignmentData assignmentsWithDefendant() {
        CaseUserAssignment defendant = CaseUserAssignment.builder()
                .caseId(CASE_ID)
                .userId(OLD_USER_ID)
                .caseRole(CASE_USER_ROLE_DEFENDANT)
                .build();
        return CaseUserAssignmentData.builder().caseUserAssignments(List.of(defendant)).build();
    }

    private CaseData caseDataWithSelectedRespondent(String userId, String email) {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(List.of(
                respondent(RESPONDENT_ID_ONE, "First respondent", email, userId, NO),
                respondent(RESPONDENT_ID_TWO, "Second respondent", "second@example.com", "second-user-id", NO)));
        caseData.setRespondentEmailUpdateSelection(
                DynamicFixedListType.from(RESPONDENT_ID_ONE, "First respondent", true));
        return caseData;
    }

    private CallbackRequest callbackRequest(CaseData beforeData, CaseData afterData) {
        return CallbackRequest.builder()
                .caseDetails(createCaseDetails(afterData))
                .caseDetailsBefore(createCaseDetails(beforeData))
                .build();
    }

    private CaseAssignmentUserRolesRequest requestForUser(String userId) {
        return argThat(request -> request.getCaseAssignmentUserRoles().size() == 1
                && userId.equals(request.getCaseAssignmentUserRoles().getFirst().getUserId())
                && CASE_USER_ROLE_DEFENDANT.equals(request.getCaseAssignmentUserRoles().getFirst().getCaseRole())
                && CASE_ID.equals(request.getCaseAssignmentUserRoles().getFirst().getCaseDataId()));
    }
}
