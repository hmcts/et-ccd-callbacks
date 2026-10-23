package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    NocCcdService.class,
    CcdClient.class
})
class NocCcdServiceTest {
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ADMIN_USER_TOKEN = "eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String CASE_ID = "1234567890123456";
    private static final String ROLE_SOLICITORA = "SOLICITORA";
    private static final String ROLE_SOLICITORB = "SOLICITORB";
    private static final String OK = "Ok";
    private static final String IDAM_ID_1 = "85d97996-22a5-40d7-882e-3a382c8ae1b4";
    private static final String IDAM_ID_2 = "85d97996-22a5-40d7-882e-3a382c8ae1b5";
    private static final String USER_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ORGANISATION_ID = "ORG123456";
    private static final String ORGANISATION_NAME = "ORGANISATION NAME";
    private static final String REPRESENTATIVE_EMAIL = "representative@hmcts.org";
    private static final String REPRESENTATIVE_NAME = "Representative Name";
    private static final String REPRESENTATIVE_ID = "REPRESENTATIVE123";

    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND_BY_TOKEN =
            "Representative not found by token for case ID " + CASE_ID + ".";

    @MockitoBean
    private CcdClient ccdClient;
    @MockitoBean
    private UserIdamService userIdamService;
    @MockitoBean
    private AdminUserService adminUserService;

    private NocCcdService nocCcdService;

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(NocCcdService.class);
        nocCcdService = new NocCcdService(ccdClient, userIdamService, adminUserService);
    }

    private AuditEventsResponse getAuditEventsResponse() {
        return AuditEventsResponse.builder().auditEvents(List.of(AuditEvent.builder()
                .userId("128")
                .userFirstName("John")
                .userLastName("Smith")
                .id("nocRequest")
                .createdDate(LocalDateTime.of(2022, 9, 10, 8, 0, 0))
                .build(),
            AuditEvent.builder()
                .userId("967")
                .userFirstName("Kate")
                .userLastName("Johnson")
                .id("nocRequest")
                .createdDate(LocalDateTime.of(2022, 10, 1, 0, 0, 0))
                .build(),
            AuditEvent.builder()
                .userId("774")
                .userFirstName("Patrick")
                .userLastName("Fitzgerald")
                .id("amendCase")
                .createdDate(LocalDateTime.of(2022, 11, 22, 0, 0, 0))
                .build())).build();
    }

    @Test
    void shouldGetLatestAuditEventByName() throws IOException {
        AuditEventsResponse auditEventsResponse = getAuditEventsResponse();
        when(ccdClient.retrieveCaseEvents(AUTH_TOKEN, CASE_ID)).thenReturn(auditEventsResponse);
        Optional<AuditEvent> event = nocCcdService.getLatestAuditEventByName(AUTH_TOKEN, CASE_ID, "nocRequest");
        assertThat(event).isPresent().hasValue(getAuditEventsResponse().getAuditEvents().get(1));
    }

    @Test
    void shouldThrowExceptionRetrievingCaseAssignments() throws IOException {
        when(ccdClient.retrieveCaseAssignments(AUTH_TOKEN, CASE_ID)).thenThrow(new IOException());
        CcdInputOutputException exception = assertThrows(
                CcdInputOutputException.class, () ->
                nocCcdService.retrieveCaseUserAssignments(AUTH_TOKEN, CASE_ID));

        assertThat(exception.getMessage()).isEqualTo("Failed to retrieve case assignments");
    }

    @Test
    void shouldThrowExceptionRevokingCaseAssignments() throws IOException {
        CaseUserAssignmentData data = new CaseUserAssignmentData();
        when(ccdClient.revokeCaseAssignments(AUTH_TOKEN, data)).thenThrow(new IOException());
        CcdInputOutputException exception = assertThrows(
                CcdInputOutputException.class, () ->
                        nocCcdService.revokeCaseAssignments(AUTH_TOKEN, data));

        assertThat(exception.getMessage()).isEqualTo("Failed to revoke case assignments");
    }

    @Test
    void shouldCallCcdStartEventForUpdateRepresentation() throws IOException {
        CCDRequest request = new CCDRequest();
        when(ccdClient.startEventForUpdateRep(AUTH_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID)).thenReturn(request);
        when(ccdClient.submitUpdateRepEvent(eq(AUTH_TOKEN), any(), eq(CASE_TYPE), eq(JURISDICTION),
            eq(request), eq(CASE_ID))).thenReturn(new SubmitEvent());
        nocCcdService.startEventForUpdateRepresentation(AUTH_TOKEN, JURISDICTION, CASE_TYPE,
            CASE_ID);
        verify(ccdClient, times(1)).startEventForUpdateRep(AUTH_TOKEN, CASE_TYPE, JURISDICTION,
                CASE_ID);
    }

    @Test
    @SneakyThrows
    void theFindCaseUserAssignmentsByCaseId() {
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        // when case id is empty should return null
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseId(StringUtils.EMPTY)).isEmpty();
        // when case user assignment data is empty should return null
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseId(CASE_ID)).isEmpty();
        // when case user assignment data not has any assignment should return null
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().build();
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseId(CASE_ID)).isEmpty();
        // when case user assignment data has assignment should return case user assignment data
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment));
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseId(CASE_ID)).isEqualTo(List.of(caseUserAssignment));
    }

    @Test
    @SneakyThrows
    void theFindCaseUserAssignmentsByRole() {
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        // when case id is empty should return empty list
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(StringUtils.EMPTY, ROLE_SOLICITORA)).isEmpty();
        // when role is empty should return empty list
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(CASE_ID, StringUtils.EMPTY)).isEmpty();
        // when case user assignment data is empty should return empty list
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(CASE_ID, ROLE_SOLICITORA)).isEmpty();
        // when case user assignment data not has any assignment should return empty list
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().build();
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(CASE_ID, ROLE_SOLICITORA)).isEmpty();
        // when case user assignment data has assignment with blank role should return empty list
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment));
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(CASE_ID, ROLE_SOLICITORA)).isEmpty();
        // when case user assignment data has assignment with different role than checked role should return empty list
        caseUserAssignment.setCaseRole(ROLE_SOLICITORB);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(CASE_ID, ROLE_SOLICITORA)).isEmpty();
        // when case user assignment data has assignment role same as checked role should return case user assignment
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(CASE_ID, ROLE_SOLICITORA)).isNotNull()
                .isEqualTo(List.of(caseUserAssignment));
    }

    @Test
    @SneakyThrows
    void theFindCaseUserAssignmentsByIdamIdAndRole() {
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        // when case id is empty should return empty list
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdIdamIdAndRole(StringUtils.EMPTY, IDAM_ID_1,
                ROLE_SOLICITORA)).isEmpty();
        // when idam id is empty should return empty list
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdIdamIdAndRole(CASE_ID, StringUtils.EMPTY,
                ROLE_SOLICITORA)).isEmpty();
        // when role is empty should return empty list
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdIdamIdAndRole(CASE_ID, IDAM_ID_1, StringUtils.EMPTY))
                .isEmpty();
        // when no case assignment found should return empty list
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdIdamIdAndRole(CASE_ID, IDAM_ID_1, ROLE_SOLICITORA))
                .isEmpty();
        // when case assignment found but no assignment with matching idam id should return empty list
        CaseUserAssignment caseUserAssignment = new CaseUserAssignment();
        caseUserAssignment.setUserId(IDAM_ID_2);
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID))
                .thenReturn(new CaseUserAssignmentData(List.of(caseUserAssignment)));
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdIdamIdAndRole(CASE_ID, IDAM_ID_1, ROLE_SOLICITORA))
                .isEmpty();
        // when case assignment found with matching idam id but different role should return empty list
        caseUserAssignment.setUserId(IDAM_ID_1);
        caseUserAssignment.setCaseRole(ROLE_SOLICITORB);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdIdamIdAndRole(CASE_ID, IDAM_ID_1, ROLE_SOLICITORA))
                .isEmpty();
        // when case assignment found with matching idam id and role should return case user assignment
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        assertThat(nocCcdService.findCaseUserAssignmentsByCaseIdIdamIdAndRole(CASE_ID, IDAM_ID_1, ROLE_SOLICITORA))
                .isEqualTo(List.of(caseUserAssignment));
    }

    @Test
    @SneakyThrows
    void theStartEventForUpdateCaseSubmitted() {
        // when user token is empty should return null
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(StringUtils.EMPTY, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNull();
        // when case type is empty should return null
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, StringUtils.EMPTY, JURISDICTION,
                CASE_ID)).isNull();
        // when jurisdiction is empty should return null
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, StringUtils.EMPTY,
                CASE_ID)).isNull();
        // when case id is empty should return null
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION,
                StringUtils.EMPTY)).isNull();
        // when CCD request is empty should return null
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(null);
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNull();
        // when CCD request does not have case details should return null
        CCDRequest ccdRequest = new CCDRequest();
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNull();
        // case details does not have case id should return null
        CaseDetails caseDetails = new CaseDetails();
        ccdRequest.setCaseDetails(caseDetails);
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNull();
        // case details does not have jurisdiction should return null
        ccdRequest.getCaseDetails().setCaseId(CASE_ID);
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNull();
        // case details does not have case type id should return null
        ccdRequest.getCaseDetails().setJurisdiction(EMPLOYMENT);
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNull();
        // case details does not have case data should return null
        ccdRequest.getCaseDetails().setCaseTypeId(CASE_TYPE);
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNull();
        // case details has case data should return CCD request
        ccdRequest.getCaseDetails().setCaseData(new CaseData());
        assertThat(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID))
                .isNotNull().isEqualTo(ccdRequest);
    }

    @Test
    @SneakyThrows
    void theRevokeClaimantRepresentation() {
        CaseDetails caseDetails = new CaseDetails();
        // when user token is empty should not throw exception and should not interact with CCD client
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(StringUtils.EMPTY, caseDetails));
        verifyNoInteractions(ccdClient);
        // when remove option is yourself should revoke only that representative's claimant representation
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseData(new CaseData());
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_YOURSELF);
        caseDetails.getCaseData().setRepresentativeClaimantType(RepresentedTypeC.builder()
                .organisationId(ORGANISATION_ID).nameOfOrganisation(ORGANISATION_NAME)
                .nameOfRepresentative(REPRESENTATIVE_NAME).representativeEmailAddress(REPRESENTATIVE_EMAIL)
                .representativeId(REPRESENTATIVE_ID).build());
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(IDAM_ID_1);
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().userId(IDAM_ID_1)
                .caseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build();
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().caseUserAssignments(
                List.of(caseUserAssignment)).build();
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        when(ccdClient.revokeCaseAssignments(ADMIN_USER_TOKEN, caseUserAssignmentData)).thenReturn(OK);
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(USER_TOKEN, caseDetails));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID);
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).revokeCaseAssignments(ADMIN_USER_TOKEN,
                caseUserAssignmentData);
        // when remove option is not yourself but case assignments not found should not interact with revoke case
        // assignments
        caseDetails.getCaseData().setNocRemoveOption(null);
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(USER_TOKEN, caseDetails));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_TWO)).retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID);
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).revokeCaseAssignments(ADMIN_USER_TOKEN,
                caseUserAssignmentData);
        // when remove option is not yourself and case assignments found should revoke all case assignments
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(USER_TOKEN, caseDetails));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_THREE)).retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID);
        verify(ccdClient, times(LoggerTestUtils.INTEGER_TWO)).revokeCaseAssignments(ADMIN_USER_TOKEN,
                caseUserAssignmentData);
    }

    @Test
    @SneakyThrows
    void theRevokeUserClaimantRepresentation() {
        // when user token is empty should not throw exception
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        assertDoesNotThrow(() -> nocCcdService.revokeUserClaimantRepresentation(StringUtils.EMPTY, caseDetails));
        verifyNoInteractions(ccdClient);
        // when user details is empty should throw representative not found generic service exception
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(null);
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocCcdService.revokeUserClaimantRepresentation(USER_TOKEN, caseDetails));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND_BY_TOKEN);
        verifyNoInteractions(ccdClient);
        // when user details does not have user idam id should throw representative not found generic service exception
        UserDetails userDetails = new UserDetails();
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        gse = assertThrows(GenericServiceException.class, () ->
                nocCcdService.revokeUserClaimantRepresentation(USER_TOKEN, caseDetails));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND_BY_TOKEN);
        verifyNoInteractions(ccdClient);
        // when no case user assignments found by case id, IDAM id and claimant solicitor role should not
        // throw exception and should not revoke case assignments.
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        userDetails.setUid(IDAM_ID_1);
        assertDoesNotThrow(() -> nocCcdService.revokeUserClaimantRepresentation(USER_TOKEN, caseDetails));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID);
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(ADMIN_USER_TOKEN),
                any(CaseUserAssignmentData.class));
        // when case user assignment(s) found by case id, IDAM id and claimant solicitor role should revoke that case
        // assignment(s)
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().userId(IDAM_ID_1)
                .caseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build();
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(caseUserAssignment)).build();
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        assertDoesNotThrow(() -> nocCcdService.revokeUserClaimantRepresentation(USER_TOKEN, caseDetails));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_TWO)).retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID);
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).revokeCaseAssignments(ADMIN_USER_TOKEN,
                caseUserAssignmentData);
    }

}