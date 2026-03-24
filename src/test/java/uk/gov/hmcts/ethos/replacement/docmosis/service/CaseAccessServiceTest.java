package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService.CREATOR_ROLE;

@ExtendWith(SpringExtension.class)
class CaseAccessServiceTest {

    private List<String> errors;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private CaseAccessService caseAccessService;
    @MockBean
    private CcdCaseAssignment caseAssignment;
    @Mock
    private RestTemplate restTemplate;
    @MockBean
    private AdminUserService adminUserService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private OrganisationClient organisationClient;

    @BeforeEach
    void setUp() {
        caseAccessService = new CaseAccessService(caseAssignment, organisationClient,
                authTokenGenerator, adminUserService);
        caseData = new CaseData();
        caseData.setLinkedCaseCT("http://example.com/1234567890123456");
        caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1111111111111111");
        when(adminUserService.getAdminUserToken()).thenReturn("userToken");
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");

    }

    @Test
    void assignExistingCaseRoles() throws IOException {
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder()
                .userId(UUID.randomUUID().toString())
                .caseId("1234567890123456")
                .caseRole(CREATOR_ROLE)
                .build();
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(caseUserAssignment))
                .build();

        when(caseAssignment.getCaseUserRoles("1234567890123456")).thenReturn(caseUserAssignmentData);
        CaseAssignmentUserRolesResponse expected = CaseAssignmentUserRolesResponse.builder()
                .statusMessage("Case-User-Role assignments created successfully")
                .build();
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CaseAssignmentUserRolesResponse.class)
        )).thenReturn(ResponseEntity.ok(expected));

        errors = caseAccessService.assignExistingCaseRoles(caseDetails);

        assertEquals(0, errors.size());
        verify(caseAssignment).addCaseUserRole(any(CaseAssignmentUserRolesRequest.class));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("invalidCaseId")
    void invalidCaseId(String linkedCaseCT) {
        caseData.setLinkedCaseCT(linkedCaseCT);
        caseDetails.setCaseData(caseData);
        errors = caseAccessService.assignExistingCaseRoles(caseDetails);
        assertEquals("Error getting original case id", errors.get(0));

    }

    private static Stream<Arguments> invalidCaseId() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("https://example.com/123456789012345") //15 digits
        );
    }

    @Test
    void noCaseRolesReturnedFromOriginalCase() throws IOException {
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(Collections.emptyList())
                .build();
        when(caseAssignment.getCaseUserRoles("1234567890123456")).thenReturn(caseUserAssignmentData);
        errors = caseAccessService.assignExistingCaseRoles(caseDetails);
        assertEquals("Case assigned user roles list is empty", errors.get(0));
    }

    @Test
    void noUserIdReturnedFromOriginalCase() throws IOException {
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(Collections.singletonList(
                        CaseUserAssignment.builder()
                                .caseId("1234567890123456")
                                .caseRole(CREATOR_ROLE)
                                .build()))
                .build();
        when(caseAssignment.getCaseUserRoles("1234567890123456")).thenReturn(caseUserAssignmentData);
        errors = caseAccessService.assignExistingCaseRoles(caseDetails);
        assertEquals("User ID is null or empty", errors.get(0));
    }

    @Test
    void errorAssigningCaseAccess() throws IOException {
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(Collections.singletonList(
                        CaseUserAssignment.builder()
                                .caseId("1234567890123456")
                                .userId(UUID.randomUUID().toString())
                                .caseRole(CREATOR_ROLE)
                                .build()))
                .build();
        when(caseAssignment.getCaseUserRoles("1234567890123456")).thenReturn(caseUserAssignmentData);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CaseAssignmentUserRolesResponse.class)
                )).thenThrow(new RestClientResponseException("Error", 500, "Internal Server Error", null, null, null));
        doCallRealMethod().when(caseAssignment).addCaseUserRole(any(CaseAssignmentUserRolesRequest.class));

        errors = caseAccessService.assignExistingCaseRoles(caseDetails);
        assertEquals("Error assigning case access for case 1234567890123456 on behalf of 1111111111111111",
                errors.get(0));
    }

    @Test
    void getCaseUserAssignmentsById_returnsAssignments() throws IOException {
        String caseId = "1234567890123456";
        CaseUserAssignment assignment = CaseUserAssignment.builder()
                .userId("user1")
                .caseId(caseId)
                .caseRole(CREATOR_ROLE)
                .build();
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(assignment))
                .build();
        when(caseAssignment.getCaseUserRoles(caseId)).thenReturn(assignmentData);

        List<CaseUserAssignment> result = caseAccessService.getCaseUserAssignmentsById(caseId);

        assertEquals(1, result.size());
        assertEquals("user1", result.getFirst().getUserId());
        assertEquals(CREATOR_ROLE, result.getFirst().getCaseRole());
    }

    @Test
    void getCaseUserAssignmentsById_rolesNull_returnsEmptyList() throws IOException {
        String caseId = "1234567890123456";
        when(caseAssignment.getCaseUserRoles(caseId)).thenReturn(null);

        List<CaseUserAssignment> result = caseAccessService.getCaseUserAssignmentsById(caseId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCaseUserAssignmentsById_assignmentsNull_returnsEmptyList() throws IOException {
        String caseId = "1234567890123456";
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(null)
                .build();
        when(caseAssignment.getCaseUserRoles(caseId)).thenReturn(assignmentData);

        List<CaseUserAssignment> result = caseAccessService.getCaseUserAssignmentsById(caseId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCaseUserAssignmentsById_throwsUncheckedIOExceptionOnIOException() throws IOException {
        String caseId = "1234567890123456";
        when(caseAssignment.getCaseUserRoles(caseId)).thenThrow(new IOException("IO error"));

        UncheckedIOException exception = assertThrows(
                UncheckedIOException.class,
                () -> caseAccessService.getCaseUserAssignmentsById(caseId)
        );
        assertEquals("Failed to retrieve user assignments for case " + caseId, exception.getMessage());
        assertEquals("IO error", exception.getCause().getMessage());
    }
}
