package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NocServiceTest {
    private static final String CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String ORGANISATION_ID_OLD = "ORG_OLD";
    private static final String ORGANISATION_ID_NEW = "ORG3_NEW";
    private static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";

    private static final String ROLE_SOLICITOR_A = "[SOLICITORA]";
    private static final String ROLE_INVALID = "[INVLAIDROLE]";
    private static final String ADMIN_USER_TOKEN = "adminUserToken";
    private static final String AUTH_TOKEN = "authToken";
    private static final String REPRESENTATIVE_EMAIL = "representative@email.com";
    private static final String SUBMISSION_REFERENCE = "1234567890123456";
    private static final String ORGANISATION_ID_1 = "79ZRSOU";
    private static final String ORGANISATION_ID_INVALID = "INVALID";
    private static final String REPRESENTATIVE_ID = "112b0bdd-5e58-32f1-8c51-6e71f5f90bc5";

    private static final int INTEGER_THREE = 3;
    private static final int INTEGER_FOUR = 4;
    private static final int INTEGER_FIVE = 5;
    private static final int INTEGER_SIX = 6;
    private static final int INTEGER_SEVEN = 7;
    private static final int INTEGER_EIGHT = 8;
    private static final int INTEGER_NINE = 9;

    private static final String EXPECTED_EXCEPTION_ACCESS_TOKEN_EMPTY =
            "There are missing parameters; accessToken: empty, email: representative@email.com, "
                    + "submission reference: 1234567890123456, organisationId: 79ZRSOU, role: [SOLICITORA].";
    private static final String EXPECTED_EXCEPTION_EMAIL_EMPTY =
            "There are missing parameters; accessToken: adminUserToken, email: empty, "
                    + "submission reference: 1234567890123456, organisationId: 79ZRSOU, role: [SOLICITORA].";
    private static final String EXPECTED_EXCEPTION_SUBMISSION_REFERENCE_EMPTY =
            "There are missing parameters; accessToken: adminUserToken, email: representative@email.com, "
                    + "submission reference: empty, organisationId: 79ZRSOU, role: [SOLICITORA].";
    private static final String EXPECTED_EXCEPTION_ORGANISATION_ID_EMPTY =
            "There are missing parameters; accessToken: adminUserToken, email: representative@email.com, "
                    + "submission reference: 1234567890123456, organisationId: empty, role: [SOLICITORA].";
    private static final String EXPECTED_EXCEPTION_ROLE_INVALID =
            "There are missing parameters; accessToken: adminUserToken, email: representative@email.com, "
                    + "submission reference: 1234567890123456, organisationId: 79ZRSOU, role: [INVLAIDROLE].";
    private static final String EXPECTED_EXCEPTION_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Failed to assign role [SOLICITORA], to user with email representative@email.com, for case "
                    + "1234567890123456. Exception message: Unable to get account id by email representative@email.com "
                    + "for case 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND_BY_USER_ID =
            "Failed to assign role [SOLICITORA], to user with email representative@email.com, for case "
                    + "1234567890123456. Exception message: Unable to find organisation by user id "
                    + "112b0bdd-5e58-32f1-8c51-6e71f5f90bc5 for case 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_EXCEPTION_USER_AND_SELECTED_ORGANISATIONS_NOT_MATCH =
            "Failed to assign role [SOLICITORA], to user with email representative@email.com, for case "
                    + "1234567890123456. Exception message: User's organisation and selected organisation does not "
                    + "match user id 112b0bdd-5e58-32f1-8c51-6e71f5f90bc5, selected organisation id INVALID, for case "
                    + "1234567890123456.";

    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private CcdCaseAssignment caseAssignment;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private NocService nocService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        nocService = new NocService(nocCcdService, adminUserService, caseAssignment, organisationClient,
                authTokenGenerator);
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        closeable.close();
    }

    @Test
    void removeOrganisationRepresentativeAccess_shouldRevokeAccessIfRoleMatches() throws IOException {
        String caseId = "case123";
        Organisation organisationToRemove =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();
        ChangeOrganisationRequest changeRequest = createChangeOrganisationRequest(null, organisationToRemove);

        CaseUserAssignment assignment = CaseUserAssignment.builder()
                .userId("user1")
                .caseRole(CLAIMANT_SOLICITOR)
                .organisationId(ORGANISATION_ID_OLD)
                .caseId(caseId)
                .build();
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(assignment))
                .build();

        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(nocCcdService.getCaseAssignments(anyString(), eq(caseId))).thenReturn(assignmentData);

        nocService.removeOrganisationRepresentativeAccess(caseId, changeRequest);

        verify(nocCcdService, times(1)).revokeCaseAssignments(anyString(), any(CaseUserAssignmentData.class));
    }

    @Test
    void removeOrganisationRepresentativeAccess_shouldNotRevokeAccessIfNoMatchingRole() throws IOException {
        Organisation organisationToRemove =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();

        Organisation organisationToAdd =
                Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_2).build();

        ChangeOrganisationRequest changeRequest =
                createChangeOrganisationRequest(organisationToAdd, organisationToRemove);

        CaseUserAssignment assignment = CaseUserAssignment.builder()
                .userId("user1")
                .caseRole("role1")
                .build();
        CaseUserAssignmentData assignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(assignment))
                .build();

        String caseId = "case123";
        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(nocCcdService.getCaseAssignments(anyString(), eq(caseId))).thenReturn(assignmentData);

        nocService.removeOrganisationRepresentativeAccess(caseId, changeRequest);

        verify(nocCcdService, never()).revokeCaseAssignments(anyString(), any(CaseUserAssignmentData.class));
    }

    @Test
    void grantCaseAccess_shouldAddCaseUserRole() throws IOException {
        String userId = "userId";
        String caseId = "case123";
        String caseRole = "[CLAIMANTSOLICITOR]";

        doNothing().when(caseAssignment).addCaseUserRole(any(CaseAssignmentUserRolesRequest.class));

        nocService.grantCaseAccess(userId, caseId, caseRole);

        ArgumentCaptor<CaseAssignmentUserRolesRequest> captor =
                ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);
        verify(caseAssignment, times(1)).addCaseUserRole(captor.capture());
        CaseAssignmentUserRolesRequest request = captor.getValue();
        assertEquals(1, request.getCaseAssignmentUserRoles().size());
        CaseAssignmentUserRole role = request.getCaseAssignmentUserRoles().getFirst();
        assertEquals(userId, role.getUserId());
        assertEquals(caseId, role.getCaseDataId());
        assertEquals(caseRole, role.getCaseRole());
    }

    private ChangeOrganisationRequest createChangeOrganisationRequest(Organisation organisationToAdd,
                                                                      Organisation organisationToRemove) {
        DynamicFixedListType caseRole = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(CLAIMANT_SOLICITOR);
        dynamicValueType.setLabel(CLAIMANT_SOLICITOR);
        caseRole.setValue(dynamicValueType);

        return ChangeOrganisationRequest.builder()
                .organisationToAdd(organisationToAdd)
                .organisationToRemove(organisationToRemove)
                .caseRoleId(caseRole)
                .build();
    }

    @Test
    @SneakyThrows
    void theGrantRepresentativeAccess() {
        Organisation organisationToAdd = Organisation.builder().organisationID(ORGANISATION_ID_1).build();
        // when admin user token is empty should not throw any exception
        GenericServiceException gse = assertThrows(GenericServiceException.class, () ->
                nocService.grantRepresentativeAccess(StringUtils.EMPTY, REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE,
                        organisationToAdd, ROLE_SOLICITOR_A));
        verifyNoInteractions(organisationClient);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ACCESS_TOKEN_EMPTY);
        // when representative email is empty should not throw any exception
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                StringUtils.EMPTY, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verifyNoInteractions(organisationClient);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_EMAIL_EMPTY);
        // when submission reference is empty should not throw any exception
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, StringUtils.EMPTY, organisationToAdd, ROLE_SOLICITOR_A));
        verifyNoInteractions(organisationClient);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_SUBMISSION_REFERENCE_EMPTY);
        // when organisation to add is empty should not throw any exception
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, null, ROLE_SOLICITOR_A));
        verifyNoInteractions(organisationClient);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ORGANISATION_ID_EMPTY);
        // when organisation to add id is empty should not throw any exception
        organisationToAdd.setOrganisationID(StringUtils.EMPTY);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verifyNoInteractions(organisationClient);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ORGANISATION_ID_EMPTY);
        // when role value is not valid should not throw any exception
        organisationToAdd.setOrganisationID(ORGANISATION_ID_1);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_INVALID));
        verifyNoInteractions(organisationClient);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ROLE_INVALID);
        // when organisationClient.getAccountIdByEmail returns null value should not throw any exception
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(null);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(NumberUtils.INTEGER_ONE))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when organisationClient.getAccountIdByEmail returns null body should not throw any exception
        ResponseEntity<AccountIdByEmailResponse> accountIdByEmailResponseWithoutBody =
                new ResponseEntity<>(HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(accountIdByEmailResponseWithoutBody);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(NumberUtils.INTEGER_TWO))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when organisationClient.getAccountIdByEmail returns body without user identifier should not throw any
        // exception
        AccountIdByEmailResponse accountIdByEmailResponseWithoutUserIdentifier = new AccountIdByEmailResponse();
        accountIdByEmailResponseWithoutUserIdentifier.setUserIdentifier(StringUtils.EMPTY);
        ResponseEntity<AccountIdByEmailResponse> accountIdByEmailResponseWithoutUserIdentifierEntity =
                new ResponseEntity<>(accountIdByEmailResponseWithoutUserIdentifier, HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(accountIdByEmailResponseWithoutUserIdentifierEntity);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(INTEGER_THREE))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when organisationClient.retrieveOrganisationDetailsByUserId returns null should not throw any exception
        AccountIdByEmailResponse validAccountIdByEmailResponse = new AccountIdByEmailResponse();
        validAccountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID);
        ResponseEntity<AccountIdByEmailResponse> validAccountIdByEmailResponseEntity =
                new ResponseEntity<>(validAccountIdByEmailResponse, HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(validAccountIdByEmailResponseEntity);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(null);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(INTEGER_FOUR))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        verify(organisationClient, times(NumberUtils.INTEGER_ONE))
                .retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND_BY_USER_ID);
        // when organisationClient.retrieveOrganisationDetailsByUserId returns null body should not throw any exception
        ResponseEntity<OrganisationsResponse> organisationsResponseWithoutBodyEntity =
                new ResponseEntity<>(HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(organisationsResponseWithoutBodyEntity);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(INTEGER_FIVE))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        verify(organisationClient, times(NumberUtils.INTEGER_TWO))
                .retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND_BY_USER_ID);
        // when organisationClient.retrieveOrganisationDetailsByUserId returns empty organisation identifier
        // should not throw any exception
        OrganisationsResponse organisationsResponseWithoutIdentifier = OrganisationsResponse.builder().build();
        ResponseEntity<OrganisationsResponse> organisationsResponseWithoutIdentifierEntity =
                new ResponseEntity<>(organisationsResponseWithoutIdentifier, HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(organisationsResponseWithoutIdentifierEntity);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(INTEGER_SIX))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        verify(organisationClient, times(INTEGER_THREE))
                .retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND_BY_USER_ID);
        // when organisationClient.retrieveOrganisationDetailsByUserId returns invalid organisation identifier should
        // not throw any exception
        OrganisationsResponse organisationsResponseInvalidIdentifier = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID_INVALID).build();
        ResponseEntity<OrganisationsResponse> organisationsResponseInvalidIdentifierEntity =
                new ResponseEntity<>(organisationsResponseInvalidIdentifier, HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(organisationsResponseInvalidIdentifierEntity);
        gse = assertThrows(GenericServiceException.class, () -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(INTEGER_SEVEN))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        verify(organisationClient, times(INTEGER_FOUR))
                .retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_EXCEPTION_USER_AND_SELECTED_ORGANISATIONS_NOT_MATCH);
        // when caseAssignment.addCaseUserRole throws exception should log that exception
        OrganisationsResponse organisationsResponseValid = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID_1).build();
        ResponseEntity<OrganisationsResponse> organisationsResponseValidEntity =
                new ResponseEntity<>(organisationsResponseValid, HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(organisationsResponseValidEntity);
        doThrow(new IOException()).when(caseAssignment).addCaseUserRole(any());
        nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A);
        verify(organisationClient, times(INTEGER_EIGHT))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        verify(organisationClient, times(INTEGER_FIVE))
                .retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID);
        verify(caseAssignment, times(NumberUtils.INTEGER_ONE))
                .addCaseUserRole(any());
        // when caseAssignment.addCaseUserRole does not throw exception should add representative access
        doNothing().when(caseAssignment).addCaseUserRole(any());
        assertDoesNotThrow(() -> nocService.grantRepresentativeAccess(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE, organisationToAdd, ROLE_SOLICITOR_A));
        verify(organisationClient, times(INTEGER_NINE))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        verify(organisationClient, times(INTEGER_SIX))
                .retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID);
        verify(caseAssignment, times(NumberUtils.INTEGER_TWO))
                .addCaseUserRole(any());
    }

    @Test
    @SneakyThrows
    void theFindUserByEmail() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);

        // when organisation client returns empty response should throw exception
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(null);
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE));
        verify(organisationClient, times(NumberUtils.INTEGER_ONE))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        assertThat(EXPECTED_EXCEPTION_ACCOUNT_NOT_FOUND_BY_EMAIL).contains(gse.getMessage());

        // when organisation client returns no-body response should throw exception
        ResponseEntity<AccountIdByEmailResponse> userResponse = new ResponseEntity<>(HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(userResponse);
        gse = assertThrows(GenericServiceException.class,
                () -> nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE));
        verify(organisationClient, times(NumberUtils.INTEGER_TWO))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        assertThat(EXPECTED_EXCEPTION_ACCOUNT_NOT_FOUND_BY_EMAIL).contains(gse.getMessage());

        // when organisation client returns user response without identifier should throw exception
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        userResponse = new ResponseEntity<>(accountIdByEmailResponse, HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(userResponse);
        gse = assertThrows(GenericServiceException.class,
                () -> nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE));
        verify(organisationClient, times(INTEGER_THREE))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        assertThat(EXPECTED_EXCEPTION_ACCOUNT_NOT_FOUND_BY_EMAIL).contains(gse.getMessage());

        // when organisation client returns valid user response should return that response body
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID);
        userResponse = new ResponseEntity<>(accountIdByEmailResponse, HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(userResponse);
        AccountIdByEmailResponse actualAccountIdByEmailResponse = nocService.findUserByEmail(ADMIN_USER_TOKEN,
                REPRESENTATIVE_EMAIL, SUBMISSION_REFERENCE);
        verify(organisationClient, times(INTEGER_FOUR))
                .getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL);
        assertThat(actualAccountIdByEmailResponse).isEqualTo(userResponse.getBody());
    }

    @Test
    @SneakyThrows
    void theFindOrganisationByUserId() {
        when(authTokenGenerator.generate()).thenReturn(AUTH_TOKEN);
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID);
        ResponseEntity<AccountIdByEmailResponse> userResponse = new ResponseEntity<>(accountIdByEmailResponse,
                HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_EMAIL))
                .thenReturn(userResponse);
        // when organisation client returns empty organisation details should throw exception
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(null);
        GenericServiceException gse = assertThrows(GenericServiceException.class, () -> nocService
                .findOrganisationByUserId(ADMIN_USER_TOKEN, REPRESENTATIVE_ID, SUBMISSION_REFERENCE));
        verify(organisationClient, times(NumberUtils.INTEGER_ONE))
                .retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID);
        assertThat(EXPECTED_EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND_BY_USER_ID).contains(gse.getMessage());

        // when organisation client returns empty body response for organisation detils should throw exception
        ResponseEntity<OrganisationsResponse> organisationsResponseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(organisationsResponseEntity);
        gse = assertThrows(GenericServiceException.class, () -> nocService.findOrganisationByUserId(ADMIN_USER_TOKEN,
                REPRESENTATIVE_ID, SUBMISSION_REFERENCE));
        verify(organisationClient, times(NumberUtils.INTEGER_TWO)).retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN,
                AUTH_TOKEN, REPRESENTATIVE_ID);
        assertThat(EXPECTED_EXCEPTION_ORGANISATION_DETAILS_NOT_FOUND_BY_USER_ID).contains(gse.getMessage());

        // when organisation client returns valid organisation response should return that response body
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder().organisationIdentifier(
                ORGANISATION_ID_1).build();
        organisationsResponseEntity = new ResponseEntity<>(organisationsResponse, HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTH_TOKEN, REPRESENTATIVE_ID))
                .thenReturn(organisationsResponseEntity);
        OrganisationsResponse actualOrganisationResponse = nocService.findOrganisationByUserId(ADMIN_USER_TOKEN,
                REPRESENTATIVE_ID, SUBMISSION_REFERENCE);
        verify(organisationClient, times(INTEGER_THREE)).retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN,
                AUTH_TOKEN, REPRESENTATIVE_ID);
        assertThat(actualOrganisationResponse).isEqualTo(organisationsResponse);
    }
}
