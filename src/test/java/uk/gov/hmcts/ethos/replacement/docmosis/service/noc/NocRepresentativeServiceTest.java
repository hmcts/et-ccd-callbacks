package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NocRepresentativeServiceTest {

    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @Mock
    private NocClaimantRepresentativeService nocClaimantRepresentativeService;
    @Mock
    private CcdCaseAssignment ccdCaseAssignment;
    @Mock
    private UserService userService;

    @InjectMocks
    private NocRepresentativeService nocRepresentativeService;

    AutoCloseable closeable;

    private static final String RESPONDENT_SOLICITOR_ROLE_A = "[SOLICITORA]";
    private static final String USER_TOKEN = "userToken";
    private static final String SUBMISSION_REFERENCE = "Dummy Submission Reference";
    private static final String REPRESENTATIVE_ID_1 = "Dummy Representative 1";
    private static final String REPRESENTATIVE_ID_2 = "Dummy Representative 2";
    private static final String ORGANISATION_ID = "Dummy Organisation ID";
    private static final String EXPECTED_EXCEPTION_CASE_ROLES_NOT_FOUND = "Case roles not found";
    private static final String EXPECTED_EXCEPTION_SYSTEM_ERROR = "A system error occurred. Please try again later!";

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        nocRepresentativeService = new NocRepresentativeService(
                nocRespondentRepresentativeService,
                nocClaimantRepresentativeService,
                ccdCaseAssignment,
                userService
        );
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        closeable.close();
    }

    @Test
    void updateRepresentation_shouldCallClaimantService_whenClaimantSolicitorRole() throws Exception {
        CaseData caseData = new CaseData();
        ChangeOrganisationRequest change =
                buildChangeRequest(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        when(nocClaimantRepresentativeService.updateClaimantRepresentation(any(), any()))
                .thenReturn(caseData);

        CaseData result = nocRepresentativeService.updateRepresentation(caseDetails, USER_TOKEN);

        assertThat(result).isSameAs(caseData);
        verify(nocClaimantRepresentativeService).updateClaimantRepresentation(caseDetails, USER_TOKEN);
    }

    @Test
    void updateRepresentation_shouldCallRespondentService_whenNotClaimantSolicitorRole() throws Exception {
        CaseData caseData = new CaseData();
        ChangeOrganisationRequest change = buildChangeRequest(RESPONDENT_SOLICITOR_ROLE_A);
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        when(nocRespondentRepresentativeService.updateRespondentRepresentation(any(CaseDetails.class)))
                .thenReturn(caseData);
        when(nocRespondentRepresentativeService.prepopulateOrgAddress(any(CaseData.class), anyString()))
                .thenReturn(caseData);
        when(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(any(CaseDetails.class)))
                .thenReturn(caseData);
        CaseData result = nocRepresentativeService.updateRepresentation(caseDetails, USER_TOKEN);

        assertThat(result).isSameAs(caseData);
        verify(nocRespondentRepresentativeService).updateRespondentRepresentation(caseDetails);
        verify(nocRespondentRepresentativeService).prepopulateOrgAddress(caseData, USER_TOKEN);
        verifyNoInteractions(nocClaimantRepresentativeService);
    }

    @Test
    void updateRepresentation_shouldThrowException_whenChangeRequestInvalid() {
        CaseData caseData = new CaseData();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, USER_TOKEN));
    }

    @Test
    void updateRepresentation_shouldThrowException_whenChangeRequestIsNull() {
        CaseData caseData = new CaseData();
        caseData.setChangeOrganisationRequestField(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, USER_TOKEN));
    }

    @Test
    void updateRepresentation_shouldThrowException_whenCaseRoleIdIsNull() {
        ChangeOrganisationRequest change = ChangeOrganisationRequest.builder()
                .caseRoleId(null)
                .organisationToAdd(mock(uk.gov.hmcts.et.common.model.ccd.types.Organisation.class))
                .build();
        CaseData caseData = new CaseData();
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, USER_TOKEN));
    }

    @Test
    void updateRepresentation_shouldThrowException_whenOrganisationToAddIsNull() {
        ChangeOrganisationRequest change = ChangeOrganisationRequest.builder()
                .caseRoleId(new DynamicFixedListType())
                .organisationToAdd(null)
                .build();
        CaseData caseData = new CaseData();
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, USER_TOKEN));
    }

    private ChangeOrganisationRequest buildChangeRequest(String caseRoleLabel) {
        DynamicFixedListType caseRole = new DynamicFixedListType();
        DynamicValueType value = new DynamicValueType();
        value.setCode(caseRoleLabel);
        value.setLabel(caseRoleLabel);
        caseRole.setValue(value);

        return ChangeOrganisationRequest.builder()
                .caseRoleId(caseRole)
                .organisationToAdd(mock(uk.gov.hmcts.et.common.model.ccd.types.Organisation.class))
                .build();
    }

    @Test
    @SneakyThrows
    void theGetValidatedCaseUserAssignments() {
        // when case user assignments data is empty should throw generic service exception
        when(ccdCaseAssignment.getCaseUserRoles(SUBMISSION_REFERENCE)).thenReturn(null);
        GenericServiceException gse = assertThrows(GenericServiceException.class, () -> nocRepresentativeService
                .getValidatedCaseUserAssignments(SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_ROLES_NOT_FOUND);
        // when case user assignments is empty should throw generic service exception
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().build();
        when(ccdCaseAssignment.getCaseUserRoles(SUBMISSION_REFERENCE)).thenReturn(caseUserAssignmentData);
        gse = assertThrows(GenericServiceException.class, () -> nocRepresentativeService
                .getValidatedCaseUserAssignments(SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_ROLES_NOT_FOUND);
        // when case user assignments has valid value(s) should return that list
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment));
        assertThat(nocRepresentativeService.getValidatedCaseUserAssignments(SUBMISSION_REFERENCE))
                .isEqualTo(List.of(caseUserAssignment));
        // when ccdCaseAssignment, getCaseUserRole method throws exception should throw generic service exception
        when(ccdCaseAssignment.getCaseUserRoles(SUBMISSION_REFERENCE))
                .thenThrow(new IOException(EXPECTED_EXCEPTION_SYSTEM_ERROR));
        gse = assertThrows(GenericServiceException.class, () -> nocRepresentativeService
                .getValidatedCaseUserAssignments(SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_SYSTEM_ERROR);
    }

    @Test
    @SneakyThrows
    void theGetValidatedRepresentativeRolesByUserToken() {
        List<CaseUserAssignment> caseUserAssignments = new ArrayList<>();
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().userId(REPRESENTATIVE_ID_1)
                .caseRole(RESPONDENT_SOLICITOR_ROLE_A).caseId(SUBMISSION_REFERENCE).organisationId(ORGANISATION_ID)
                .build();
        caseUserAssignments.add(caseUserAssignment);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(caseUserAssignments).build();
        when(ccdCaseAssignment.getCaseUserRoles(SUBMISSION_REFERENCE)).thenReturn(caseUserAssignmentData);
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(REPRESENTATIVE_ID_2);
        when(userService.getValidatedUserDetails(USER_TOKEN, SUBMISSION_REFERENCE)).thenReturn(userDetails);
        // when no role found should throw generic service exception
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(USER_TOKEN,
                        SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_ROLES_NOT_FOUND);
        // when role(s) found should return that role(s) in a list
        userDetails.setUid(REPRESENTATIVE_ID_1);
        assertThat(nocRepresentativeService.getValidatedRepresentativeRolesByUserToken(USER_TOKEN,
                SUBMISSION_REFERENCE)).isEqualTo(List.of(RESPONDENT_SOLICITOR_ROLE_A));

    }
}