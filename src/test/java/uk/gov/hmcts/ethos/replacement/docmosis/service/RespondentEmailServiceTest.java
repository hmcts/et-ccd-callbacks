package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentEmailService.UPDATE_RESPONDENT_ACCESS_TRANSFER_STATE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class RespondentEmailServiceTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String CASE_TYPE_ID = "ET_EnglandWales";
    private static final String JURISDICTION = "EMPLOYMENT";
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
    @Mock
    private CcdClient ccdClient;

    private RespondentEmailService service;
    private CaseDetails caseDetails;
    private RespondentSumTypeItem firstRespondent;
    private RespondentSumTypeItem secondRespondent;

    @BeforeEach
    void setUp() {
        service = new RespondentEmailService(idamApi, adminUserService, ccdCaseAssignment, ccdClient);
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
    void initialiseExcludesRespondentLinkedToRepresentative() {
        firstRespondent.getValue().setRepresented(null);
        secondRespondent.getValue().setRepresented(null);
        caseDetails.getCaseData().setRepCollection(List.of(representativeFor(RESPONDENT_ID_ONE, "First respondent")));

        assertThat(service.initialise(caseDetails.getCaseData())).isEmpty();
        assertThat(caseDetails.getCaseData().getRespondentEmailUpdateSelection().getListItems())
                .extracting("code")
                .containsExactly(RESPONDENT_ID_TWO);
    }

    @Test
    void initialiseIncludesRespondentWhoseRepresentativeWasRemoved() {
        firstRespondent.getValue().setRepresented(YES);
        firstRespondent.getValue().setRepresentativeRemoved(YES);

        assertThat(service.initialise(caseDetails.getCaseData())).isEmpty();
        assertThat(caseDetails.getCaseData().getRespondentEmailUpdateSelection().getListItems())
                .extracting("code")
                .contains(RESPONDENT_ID_ONE);
    }

    @Test
    void populateCurrentEmailUsesSelectedRespondentsResponseEmail() {
        firstRespondent.getValue().setResponseRespondentEmail("portal@example.com");
        selectRespondent(RESPONDENT_ID_ONE);

        assertThat(service.populateCurrentEmail(caseDetails.getCaseData())).isEmpty();
        assertThat(caseDetails.getCaseData().getCurrentRespondentEmail()).isEqualTo("portal@example.com");
    }

    @Test
    void populateCurrentEmailRejectsMissingSelection() {
        caseDetails.getCaseData().setCurrentRespondentEmail(OLD_EMAIL);

        assertThat(service.populateCurrentEmail(caseDetails.getCaseData()))
                .containsExactly(RespondentEmailService.RESPONDENT_REQUIRED_ERROR);
        assertThat(caseDetails.getCaseData().getCurrentRespondentEmail()).isNull();
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
    void validateRejectsUnchangedEmail() {
        selectRespondent(RESPONDENT_ID_ONE);
        caseDetails.getCaseData().setCurrentRespondentEmail(OLD_EMAIL);
        caseDetails.getCaseData().setNewRespondentEmail("OLD@EXAMPLE.COM");

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly(RespondentEmailService.EMAIL_UNCHANGED_ERROR);
        verify(idamApi, never()).searchUsersByQuery(anyString(), anyString(), any(), any());
    }

    @Test
    void validateRejectsAmbiguousIdamAccount() {
        prepareSelectedEmailFields();
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(idamApi.searchUsersByQuery("admin-token", NEW_EMAIL, 0, 50))
                .thenReturn(List.of(user(NEW_EMAIL, "first-id"), user(NEW_EMAIL, "second-id")));

        assertThat(service.validateNewEmail(caseDetails.getCaseData()))
                .containsExactly(RespondentEmailService.IDAM_USER_AMBIGUOUS_ERROR);
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
        assertThat(caseDetails.getCaseData().getRespondentAccessTransferPending()).isEqualTo(YES);
        assertThat(caseDetails.getCaseData().getRespondentAccessPreviousIdamId()).isEqualTo(OLD_USER_ID);
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
        assertThat(caseDetails.getCaseData().getRespondentAccessTransferPending()).isNull();
        assertThat(caseDetails.getCaseData().getRespondentAccessPreviousIdamId()).isNull();
    }

    @Test
    void prepareUpdateReturnsErrorWhenAccessLookupFails() throws IOException {
        prepareSelectedEmailFields();
        mockNewIdamUser();
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenThrow(new IOException("lookup failed"));

        assertThat(service.prepareUpdate(caseDetails))
                .containsExactly(RespondentEmailService.ACCESS_LOOKUP_ERROR);
        assertThat(firstRespondent.getValue().getRespondentEmail()).isEqualTo(OLD_EMAIL);
    }

    @Test
    void reassignDefendantAccessRevokesOldAccessBeforeGrantingNewAccess() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);
        afterData.setRespondentAccessTransferPending(YES);
        afterData.setRespondentAccessPreviousIdamId(OLD_USER_ID);
        CallbackRequest callbackRequest = callbackRequest(beforeData, afterData);
        mockClearAccessTransferTracker();

        RespondentEmailService.Confirmation confirmation = service.reassignDefendantAccess(callbackRequest);

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        assertThat(confirmation.header()).isEqualTo(RespondentEmailService.CONFIRMATION_HEADER_SUCCESS);
        assertThat(confirmation.body()).isEqualTo(RespondentEmailService.CONFIRMATION_BODY_SUCCESS_WITH_ACCESS);
        assertThat(afterData.getRespondentAccessTransferPending()).isNull();
        assertThat(afterData.getRespondentAccessPreviousIdamId()).isNull();
        verify(ccdClient).submitEventForCase(anyString(), any(CaseData.class), anyString(), anyString(),
                any(CCDRequest.class), anyString());
    }

    @Test
    void reassignDefendantAccessRestoresOldAccessWhenGrantFails() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);
        afterData.setRespondentAccessTransferPending(YES);
        afterData.setRespondentAccessPreviousIdamId(OLD_USER_ID);
        CallbackRequest callbackRequest = callbackRequest(beforeData, afterData);
        doThrow(new IOException("grant failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));

        RespondentEmailService.Confirmation confirmation = service.reassignDefendantAccess(callbackRequest);

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(OLD_USER_ID));
        assertThat(confirmation.header()).isEqualTo(RespondentEmailService.CONFIRMATION_HEADER_PARTIAL_FAILURE);
        assertThat(confirmation.body()).isEqualTo(RespondentEmailService.CONFIRMATION_BODY_PARTIAL_FAILURE);
        assertThat(afterData.getRespondentAccessTransferPending()).isEqualTo(YES);
        assertThat(afterData.getRespondentAccessPreviousIdamId()).isEqualTo(OLD_USER_ID);
        verify(ccdClient, never()).submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void reassignDefendantAccessDoesNothingWithoutExistingOnlineAccess() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(null, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(null, NEW_EMAIL);

        RespondentEmailService.Confirmation confirmation =
                service.reassignDefendantAccess(callbackRequest(beforeData, afterData));

        verify(ccdCaseAssignment, never()).removeCaseUserRole(any());
        verify(ccdCaseAssignment, never()).addCaseUserRole(any());
        assertThat(confirmation.header()).isEqualTo(RespondentEmailService.CONFIRMATION_HEADER_SUCCESS);
        assertThat(confirmation.body()).isEqualTo(RespondentEmailService.CONFIRMATION_BODY_SUCCESS);
        verify(ccdClient, never()).submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void reassignDefendantAccessReportsRevokeFailure() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);
        afterData.setRespondentAccessTransferPending(YES);
        afterData.setRespondentAccessPreviousIdamId(OLD_USER_ID);
        CallbackRequest callbackRequest = callbackRequest(beforeData, afterData);
        doThrow(new IOException("revoke failed"))
                .when(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));

        RespondentEmailService.Confirmation confirmation = service.reassignDefendantAccess(callbackRequest);

        verify(ccdCaseAssignment, never()).addCaseUserRole(any());
        assertThat(confirmation.header()).isEqualTo(RespondentEmailService.CONFIRMATION_HEADER_PARTIAL_FAILURE);
        assertThat(confirmation.body()).isEqualTo(RespondentEmailService.CONFIRMATION_BODY_PARTIAL_FAILURE);
        assertThat(afterData.getRespondentAccessTransferPending()).isEqualTo(YES);
        assertThat(afterData.getRespondentAccessPreviousIdamId()).isEqualTo(OLD_USER_ID);
    }

    @Test
    void reassignDefendantAccessRejectsMissingPreviousCaseData() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        assertThatThrownBy(() -> service.reassignDefendantAccess(callbackRequest))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage("Missing case data required to update respondent access");
    }

    @Test
    void reassignDefendantAccessStillSucceedsWhenClearingTrackerFails() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);
        afterData.setRespondentAccessTransferPending(YES);
        afterData.setRespondentAccessPreviousIdamId(OLD_USER_ID);
        CallbackRequest callbackRequest = callbackRequest(beforeData, afterData);
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(ccdClient.startEventForCase(
                "admin-token", CASE_TYPE_ID, JURISDICTION, CASE_ID, UPDATE_RESPONDENT_ACCESS_TRANSFER_STATE))
                .thenThrow(new IOException("tracker clear failed"));

        RespondentEmailService.Confirmation confirmation = service.reassignDefendantAccess(callbackRequest);

        verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        assertThat(confirmation.header()).isEqualTo(RespondentEmailService.CONFIRMATION_HEADER_SUCCESS);
        assertThat(confirmation.body()).isEqualTo(RespondentEmailService.CONFIRMATION_BODY_SUCCESS_WITH_ACCESS);
        assertThat(afterData.getRespondentAccessTransferPending()).isNull();
        assertThat(afterData.getRespondentAccessPreviousIdamId()).isNull();
        verify(ccdClient, never()).submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void reassignDefendantAccessClearsTrackerWhenUserIdsUnchanged() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(OLD_USER_ID, NEW_EMAIL);
        afterData.setRespondentAccessTransferPending(YES);
        afterData.setRespondentAccessPreviousIdamId(OLD_USER_ID);

        RespondentEmailService.Confirmation confirmation =
                service.reassignDefendantAccess(callbackRequest(beforeData, afterData));

        verify(ccdCaseAssignment, never()).removeCaseUserRole(any());
        verify(ccdCaseAssignment, never()).addCaseUserRole(any());
        assertThat(confirmation.header()).isEqualTo(RespondentEmailService.CONFIRMATION_HEADER_SUCCESS);
        assertThat(confirmation.body()).isEqualTo(RespondentEmailService.CONFIRMATION_BODY_SUCCESS);
        assertThat(afterData.getRespondentAccessTransferPending()).isNull();
        assertThat(afterData.getRespondentAccessPreviousIdamId()).isNull();
    }

    @Test
    void reassignDefendantAccessReportsPartialFailureWhenRestoreAlsoFails() throws IOException {
        CaseData beforeData = caseDataWithSelectedRespondent(OLD_USER_ID, OLD_EMAIL);
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);
        afterData.setRespondentAccessTransferPending(YES);
        afterData.setRespondentAccessPreviousIdamId(OLD_USER_ID);
        CallbackRequest callbackRequest = callbackRequest(beforeData, afterData);
        doThrow(new IOException("grant failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        doThrow(new IOException("restore failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestForUser(OLD_USER_ID));

        RespondentEmailService.Confirmation confirmation = service.reassignDefendantAccess(callbackRequest);

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestForUser(OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(NEW_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestForUser(OLD_USER_ID));
        assertThat(confirmation.header()).isEqualTo(RespondentEmailService.CONFIRMATION_HEADER_PARTIAL_FAILURE);
        assertThat(confirmation.body()).isEqualTo(RespondentEmailService.CONFIRMATION_BODY_PARTIAL_FAILURE);
        assertThat(afterData.getRespondentAccessTransferPending()).isEqualTo(YES);
        verify(ccdClient, never()).submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void reassignDefendantAccessRejectsMissingRespondentInPreviousCaseData() {
        CaseData beforeData = new CaseData();
        beforeData.setRespondentCollection(List.of());
        CaseData afterData = caseDataWithSelectedRespondent(NEW_USER_ID, NEW_EMAIL);

        assertThatThrownBy(() -> service.reassignDefendantAccess(callbackRequest(beforeData, afterData)))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage("The selected respondent could not be found in the previous case data");
    }

    @Test
    void prepareUpdateSkipsAccessLookupWhenRespondentHasNoIdamId() throws IOException {
        firstRespondent.getValue().setIdamId(null);
        prepareSelectedEmailFields();
        mockNewIdamUser();

        assertThat(service.prepareUpdate(caseDetails)).isEmpty();
        assertThat(firstRespondent.getValue().getRespondentEmail()).isEqualTo(NEW_EMAIL);
        assertThat(firstRespondent.getValue().getIdamId()).isNull();
        assertThat(caseDetails.getCaseData().getRespondentAccessTransferPending()).isNull();
        verify(ccdCaseAssignment, never()).getCaseUserRoles(anyString());
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

    private RepresentedTypeRItem representativeFor(String respondentId, String respondentName) {
        RepresentedTypeRItem item = new RepresentedTypeRItem();
        item.setId("representative-id");
        item.setValue(RepresentedTypeR.builder()
                .respondentId(respondentId)
                .respRepName(respondentName)
                .build());
        return item;
    }

    private CaseDetails createCaseDetails(CaseData caseData) {
        CaseDetails details = new CaseDetails();
        details.setCaseId(CASE_ID);
        details.setCaseTypeId(CASE_TYPE_ID);
        details.setJurisdiction(JURISDICTION);
        details.setCaseData(caseData);
        return details;
    }

    private void mockClearAccessTransferTracker() throws IOException {
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        CaseData trackerCaseData = new CaseData();
        trackerCaseData.setRespondentAccessTransferPending(YES);
        trackerCaseData.setRespondentAccessPreviousIdamId(OLD_USER_ID);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(createCaseDetails(trackerCaseData));
        when(ccdClient.startEventForCase(
                "admin-token", CASE_TYPE_ID, JURISDICTION, CASE_ID, UPDATE_RESPONDENT_ACCESS_TRANSFER_STATE))
                .thenReturn(ccdRequest);
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
