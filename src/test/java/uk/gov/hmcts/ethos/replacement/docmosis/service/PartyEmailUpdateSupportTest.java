package uk.gov.hmcts.ethos.replacement.docmosis.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class PartyEmailUpdateSupportTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String EMAIL = "new@example.com";
    private static final String USER_TOKEN = "Bearer exui-token";
    private static final String EMAIL_QUERY = "email:" + EMAIL;
    private static final String OLD_USER_ID = "old-user-id";
    private static final String NEW_USER_ID = "new-user-id";

    @Mock
    private IdamApi idamApi;
    @Mock
    private CcdCaseAssignment ccdCaseAssignment;

    private PartyEmailUpdateSupport support;

    @BeforeEach
    void setUp() {
        support = new PartyEmailUpdateSupport(idamApi, ccdCaseAssignment);
    }

    @Test
    void findCitizenUserByEmailReturnsExactCitizenMatchIgnoringCase() {
        when(idamApi.searchUsersByQuery(USER_TOKEN, EMAIL_QUERY, 0, 50))
                .thenReturn(List.of(user(EMAIL.toUpperCase(Locale.ROOT), NEW_USER_ID)));

        List<String> errors = new ArrayList<>();
        Optional<UserDetails> result = support.findCitizenUserByEmail(
                EMAIL, USER_TOKEN, PartyEmailMessages.claimant(), errors);

        assertThat(result).isPresent();
        assertThat(result.get().getUid()).isEqualTo(NEW_USER_ID);
        assertThat(errors).isEmpty();
    }

    @Test
    void findCitizenUserByEmailAddsLookupErrorWhenIdamSearchFails() {
        when(idamApi.searchUsersByQuery(USER_TOKEN, EMAIL_QUERY, 0, 50))
                .thenThrow(forbiddenIdamSearchException());

        List<String> errors = new ArrayList<>();
        assertThat(support.findCitizenUserByEmail(EMAIL, USER_TOKEN, PartyEmailMessages.respondent(), errors))
                .isEmpty();
        assertThat(errors).containsExactly(PartyEmailMessages.respondent().idamUserLookupError());
    }

    @Test
    void findCitizenUserByEmailRejectsNonCitizenAndAmbiguousMatches() {
        when(idamApi.searchUsersByQuery(USER_TOKEN, EMAIL_QUERY, 0, 50))
                .thenReturn(List.of(user(EMAIL, NEW_USER_ID, List.of("caseworker"))))
                .thenReturn(List.of(user(EMAIL, NEW_USER_ID), user(EMAIL, "other-id")));

        List<String> nonCitizenErrors = new ArrayList<>();
        assertThat(support.findCitizenUserByEmail(EMAIL, USER_TOKEN, PartyEmailMessages.claimant(), nonCitizenErrors))
                .isEmpty();
        assertThat(nonCitizenErrors).containsExactly(PartyEmailMessages.claimant().idamUserNotCitizenError());

        List<String> ambiguousErrors = new ArrayList<>();
        assertThat(support.findCitizenUserByEmail(EMAIL, USER_TOKEN, PartyEmailMessages.claimant(), ambiguousErrors))
                .isEmpty();
        assertThat(ambiguousErrors).containsExactly(PartyEmailMessages.claimant().idamUserAmbiguousError());
    }

    @Test
    void ensureCaseAccessGrantsWhenClaimantHasNoCreator() throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID))
                .thenReturn(CaseUserAssignmentData.builder().caseUserAssignments(List.of()).build());

        AccessOutcome outcome = support.ensureCaseAccess(
                CASE_ID, null, NEW_USER_ID, PartyEmailUpdateSpec.claimant());

        assertThat(outcome).isEqualTo(AccessOutcome.GRANTED);
        verify(ccdCaseAssignment).addCaseUserRole(requestFor(CASE_USER_ROLE_CREATOR, NEW_USER_ID));
        verify(ccdCaseAssignment, never()).removeCaseUserRole(any());
    }

    @Test
    void ensureCaseAccessReassignsClaimantCreator() throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID)).thenReturn(assignments(CASE_USER_ROLE_CREATOR, OLD_USER_ID));

        AccessOutcome outcome = support.ensureCaseAccess(
                CASE_ID, null, NEW_USER_ID, PartyEmailUpdateSpec.claimant());

        assertThat(outcome).isEqualTo(AccessOutcome.REASSIGNED);
        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestFor(CASE_USER_ROLE_CREATOR, OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestFor(CASE_USER_ROLE_CREATOR, NEW_USER_ID));
    }

    @Test
    void ensureCaseAccessSkipsLookupWhenRespondentHasNoPreviousIdamId() throws IOException {
        AccessOutcome outcome = support.ensureCaseAccess(
                CASE_ID, null, NEW_USER_ID, PartyEmailUpdateSpec.respondent());

        assertThat(outcome).isEqualTo(AccessOutcome.GRANTED);
        verify(ccdCaseAssignment, never()).getCaseUserRoles(any());
        verify(ccdCaseAssignment).addCaseUserRole(requestFor(CASE_USER_ROLE_DEFENDANT, NEW_USER_ID));
    }

    @Test
    void ensureCaseAccessRestoresOldAccessWhenGrantFailsDuringReassignment() throws IOException {
        when(ccdCaseAssignment.getCaseUserRoles(CASE_ID))
                .thenReturn(assignments(CASE_USER_ROLE_DEFENDANT, OLD_USER_ID));
        doThrow(new IOException("grant failed"))
                .when(ccdCaseAssignment).addCaseUserRole(requestFor(CASE_USER_ROLE_DEFENDANT, NEW_USER_ID));

        assertThatThrownBy(() -> support.ensureCaseAccess(
                CASE_ID, OLD_USER_ID, NEW_USER_ID, PartyEmailUpdateSpec.respondent()))
                .isInstanceOf(CcdInputOutputException.class)
                .hasMessage(PartyEmailMessages.respondent().accessGrantError());

        InOrder inOrder = inOrder(ccdCaseAssignment);
        inOrder.verify(ccdCaseAssignment).removeCaseUserRole(requestFor(CASE_USER_ROLE_DEFENDANT, OLD_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestFor(CASE_USER_ROLE_DEFENDANT, NEW_USER_ID));
        inOrder.verify(ccdCaseAssignment).addCaseUserRole(requestFor(CASE_USER_ROLE_DEFENDANT, OLD_USER_ID));
    }

    @Test
    void emailUpdateFailureMessageMapsAccessOutcome() {
        PartyEmailMessages messages = PartyEmailMessages.claimant();
        assertThat(support.emailUpdateFailureMessage(AccessOutcome.REASSIGNED, messages))
                .isEqualTo(messages.emailUpdateAfterReassignError());
        assertThat(support.emailUpdateFailureMessage(AccessOutcome.GRANTED, messages))
                .isEqualTo(messages.emailUpdateAfterGrantError());
        assertThat(support.emailUpdateFailureMessage(AccessOutcome.UNCHANGED, messages))
                .isEqualTo(messages.emailUpdateError());
    }

    private UserDetails user(String email, String uid) {
        return user(email, uid, List.of("citizen"));
    }

    private UserDetails user(String email, String uid, List<String> roles) {
        UserDetails user = new UserDetails();
        user.setEmail(email);
        user.setUid(uid);
        user.setRoles(roles);
        return user;
    }

    private CaseUserAssignmentData assignments(String role, String userId) {
        CaseUserAssignment assignment = CaseUserAssignment.builder()
                .caseId(CASE_ID)
                .userId(userId)
                .caseRole(role)
                .build();
        return CaseUserAssignmentData.builder().caseUserAssignments(List.of(assignment)).build();
    }

    private CaseAssignmentUserRolesRequest requestFor(String role, String userId) {
        return argThat(request -> request.getCaseAssignmentUserRoles().size() == 1
                && userId.equals(request.getCaseAssignmentUserRoles().getFirst().getUserId())
                && role.equals(request.getCaseAssignmentUserRoles().getFirst().getCaseRole())
                && CASE_ID.equals(request.getCaseAssignmentUserRoles().getFirst().getCaseDataId()));
    }

    private FeignException forbiddenIdamSearchException() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "https://idam-api.aat.platform.hmcts.net/api/v1/users",
                Map.of(),
                null,
                new RequestTemplate());
        return new FeignException.Forbidden(
                "[403 Forbidden] during [GET] to [/api/v1/users]",
                request,
                "{\"status\":403,\"error\":\"Forbidden\"}".getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap());
    }
}
