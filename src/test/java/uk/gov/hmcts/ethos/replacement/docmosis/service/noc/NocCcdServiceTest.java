package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.AuditEventsResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EVENT_UPDATE_CASE_SUBMITTED;

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

    @MockBean
    private CcdClient ccdClient;

    private NocCcdService nocCcdService;

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(NocCcdService.class);
        nocCcdService = new NocCcdService(ccdClient);
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
    void theFindCaseUserAssignmentByRole() {
        // when user token is empty should return null
        assertThat(nocCcdService.findCaseUserAssignmentByRole(StringUtils.EMPTY, CASE_ID, ROLE_SOLICITORA)).isNull();
        // when case id is empty should return null
        assertThat(nocCcdService.findCaseUserAssignmentByRole(ADMIN_USER_TOKEN, StringUtils.EMPTY,
                ROLE_SOLICITORA)).isNull();
        // when role is empty should return null
        assertThat(nocCcdService.findCaseUserAssignmentByRole(ADMIN_USER_TOKEN, CASE_ID, StringUtils.EMPTY)).isNull();
        // when case user assignment data is empty should return null
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        assertThat(nocCcdService.findCaseUserAssignmentByRole(ADMIN_USER_TOKEN, CASE_ID, ROLE_SOLICITORA)).isNull();
        // when case user assignment data not has any assignment should return null
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().build();
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        assertThat(nocCcdService.findCaseUserAssignmentByRole(ADMIN_USER_TOKEN, CASE_ID, ROLE_SOLICITORA)).isNull();
        // when case user assignment data has assignment with blank role should return null
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment));
        assertThat(nocCcdService.findCaseUserAssignmentByRole(ADMIN_USER_TOKEN, CASE_ID, ROLE_SOLICITORA)).isNull();
        // when case user assignment data has assignment with different role than checked role should return null
        caseUserAssignment.setCaseRole(ROLE_SOLICITORB);
        assertThat(nocCcdService.findCaseUserAssignmentByRole(ADMIN_USER_TOKEN, CASE_ID, ROLE_SOLICITORA)).isNull();
        // when case user assignment data has assignment role same as checked role should return case user assingment
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        assertThat(nocCcdService.findCaseUserAssignmentByRole(ADMIN_USER_TOKEN, CASE_ID, ROLE_SOLICITORA)).isNotNull()
                .isEqualTo(caseUserAssignment);
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
        // when user token is empty should not throw exception
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(StringUtils.EMPTY, caseDetails));
        // when claimant representative not has organisation identifier should not throw exception
        caseDetails.setCaseId(CASE_ID);
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails));
        // when not able to find case user assignment with claimant solicitor role should not throw exception
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails));
        // when case user assignment is found should not throw exception and revoke case assignment
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().caseRole(ClaimantSolicitorRole
                .CLAIMANTSOLICITOR.getCaseRoleLabel()).build();
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(caseUserAssignment)).build();
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        when(ccdClient.revokeCaseAssignments(ADMIN_USER_TOKEN, caseUserAssignmentData)).thenReturn(OK);
        assertDoesNotThrow(() -> nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).revokeCaseAssignments(ADMIN_USER_TOKEN,
                caseUserAssignmentData);
    }
}