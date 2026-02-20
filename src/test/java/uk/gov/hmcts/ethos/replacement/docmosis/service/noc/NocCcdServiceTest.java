package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
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
    private static final String RESPONDENT_REPRESENTATIVE_ID = "respondent_representative_id";
    private static final String CLAIMANT_REPRESENTATIVE_ID = "claimant_representative_id";
    private static final String ORGANISATION_ID_1 = "79ZRSOU";
    private static final String ORGANISATION_ID_2 = "80ZRSOU";
    private static final String ROLE_SOLICITORA = "SOLICITORA";
    private static final String ROLE_SOLICITORB = "SOLICITORB";
    private static final String OK = "Ok";

    private static final String EXPECTED_ERROR_UNABLE_TO_START_REMOVE_REP_ORG_POLICY_INVALID_CCD_REQUEST =
            "Unable to start update case submitted event to update representative role and organisation policy for "
                    + "case: " + CASE_ID + ", Reason: invalid ccd request";
    private static final String EXPECTED_ERROR_FAILED_TO_REMOVE_CLAIMANT_REP_AND_ORG_POLICY =
            "Failed to remove claimant representative and organisation policy for case " + CASE_ID + ". Exception: "
                    + "Something went wrong";
    private static final String EXPECTED_ERROR_UNABLE_TO_UPDATE_REVOKED_CLAIMANT_REP_AND_ORG_POLICY =
            "Claimant representative role assignment revoked but failed to update claimant representation and "
                    + "organisation policy for case ID: " + CASE_ID;

    private static final String EXCEPTION_DUMMY = "Something went wrong";

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
        // when user token is empty should do nothing and return false
        assertThat(nocCcdService.revokeClaimantRepresentation(StringUtils.EMPTY, caseDetails)).isFalse();
        // when claimant representative not has organisation identifier should do nothing and return false
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(CASE_TYPE);
        caseDetails.setJurisdiction(EMPLOYMENT);
        assertThat(nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails)).isFalse();
        // when no respondent representative found should do nothing and return false
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().myHmctsOrganisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_1).build()).build();
        caseData.setRepresentativeClaimantType(claimantRepresentative);
        assertThat(nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails)).isFalse();
        // when respondent representative not has matching organisation identifier should do nothing and return false
        RepresentedTypeRItem respondentRepresentative = RepresentedTypeRItem.builder().id(RESPONDENT_REPRESENTATIVE_ID)
                .value(RepresentedTypeR.builder().respondentOrganisation(Organisation.builder()
                                .organisationID(ORGANISATION_ID_2).build()).build()).build();
        caseData.setRepCollection(List.of(respondentRepresentative));
        assertThat(nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails)).isFalse();
        // when not able to find case user assignment with claimant solicitor role should do nothing and return false
        respondentRepresentative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(null);
        assertThat(nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails)).isFalse();
        // when case user assignment is found should revoke case assignment and return true
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().caseRole(ClaimantSolicitorRole
                .CLAIMANTSOLICITOR.getCaseRoleLabel()).build();
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(caseUserAssignment)).build();
        when(ccdClient.retrieveCaseAssignments(ADMIN_USER_TOKEN, CASE_ID)).thenReturn(caseUserAssignmentData);
        when(ccdClient.revokeCaseAssignments(ADMIN_USER_TOKEN, caseUserAssignmentData)).thenReturn(OK);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(ADMIN_USER_TOKEN, caseDetails.getCaseData(), CASE_TYPE, JURISDICTION,
                ccdRequest, CASE_ID)).thenReturn(new SubmitEvent());
        assertThat(nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails)).isTrue();
        assertThat(ccdRequest.getCaseDetails().getCaseData().getRepresentativeClaimantType()).isNull();
        assertThat(ccdRequest.getCaseDetails().getCaseData().getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(ccdRequest.getCaseDetails().getCaseData().getClaimantRepresentedQuestion()).isEqualTo(NO);
        assertThat(ccdRequest.getCaseDetails().getCaseData().getClaimantRepresentativeOrganisationPolicy())
                .isEqualTo(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(ClaimantSolicitorRole
                        .CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
        // when start event for case throws exception should log unable to update claimant rep and org policy error but
        // should return true as case assignment is revoked
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().myHmctsOrganisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_1).build()).build());
        caseData.setClaimantRepresentedQuestion(YES);
        doThrow(new IOException(EXCEPTION_DUMMY)).when(ccdClient).startEventForCase(
                ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID, EVENT_UPDATE_CASE_SUBMITTED);
        assertThat(nocCcdService.revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails)).isTrue();
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_ERROR_UNABLE_TO_UPDATE_REVOKED_CLAIMANT_REP_AND_ORG_POLICY);
    }

    @Test
    @SneakyThrows
    void theRemoveClaimantRepresentation() {
        CaseData caseData = new CaseData();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseTypeId(CASE_TYPE);
        caseDetails.setJurisdiction(EMPLOYMENT);
        // when start event for update case submitted returns null should log that and do nothing
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(null);
        nocCcdService.removeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails);
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_ERROR_UNABLE_TO_START_REMOVE_REP_ORG_POLICY_INVALID_CCD_REQUEST);
        // when start event for update case submitted returns valid CCD request should submit event for case
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().representativeId(CLAIMANT_REPRESENTATIVE_ID)
                .build());
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).organisation(
                        Organisation.builder().organisationID(ORGANISATION_ID_1).build()).build());
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID,
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class), eq(CASE_TYPE), eq(JURISDICTION),
                any(CCDRequest.class), eq(CASE_ID))).thenReturn(new SubmitEvent());
        nocCcdService.removeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails);
        assertThat(ccdRequest.getCaseDetails().getCaseData().getRepresentativeClaimantType()).isNull();
        assertThat(ccdRequest.getCaseDetails().getCaseData().getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(ccdRequest.getCaseDetails().getCaseData().getClaimantRepresentedQuestion()).isEqualTo(NO);
        assertThat(ccdRequest.getCaseDetails().getCaseData().getClaimantRepresentativeOrganisationPolicy())
                .isEqualTo(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(ClaimantSolicitorRole
                        .CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
        //when start event for update case submitted throws exception should log that exception
        doThrow(new IOException(EXCEPTION_DUMMY)).when(ccdClient).startEventForCase(
                ADMIN_USER_TOKEN, CASE_TYPE, JURISDICTION, CASE_ID, EVENT_UPDATE_CASE_SUBMITTED);
        nocCcdService.removeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails);
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_ERROR_FAILED_TO_REMOVE_CLAIMANT_REP_AND_ORG_POLICY);
    }
}