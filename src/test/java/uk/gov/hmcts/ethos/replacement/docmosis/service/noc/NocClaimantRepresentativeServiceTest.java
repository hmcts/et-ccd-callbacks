package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.NocUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.REPRESENTATIVE_EMAIL_1;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.createCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.createCaseDetailsWithCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.createChangeOrganisationRequest;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.getCCDRequest;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.getCallBackCallbackRequest;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.getCaseDataAfter;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.mockCaseAssignmentData;

class NocClaimantRepresentativeServiceTest {
    private static final String USER_EMAIL = "test@hmcts.net";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Brown";
    private static final String DUMMY_ADMIN_USER_TOKEN = "dummyAdminUserToken";
    private static final String DUMMY_ORGANISATION_USER_ID = "dummyOrganisationUserId";
    private static final String ORGANISATION_ID_NEW = "ORG3_NEW";
    private static final String REPRESENTATIVE_NAME_1 = "Representative Name 1";
    private static final String S2S_TOKEN = "s2sToken";
    private static final String EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Representative 'Representative Name 1' could not be found using representative_1@hmcts.org. Case access "
                    + "will not be defined for this representative.\n";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private CcdCaseAssignment ccdCaseAssignment;
    @Mock
    private NocService nocService;
    @Mock
    private OrganisationService organisationService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private OrganisationsResponse organisationsResponse;

    @InjectMocks
    private NocClaimantRepresentativeService nocClaimantRepresentativeService;

    private AutoCloseable closeable;
    private MockedStatic<NocUtils> mockedNocUtils;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        nocClaimantRepresentativeService = new NocClaimantRepresentativeService(
                authTokenGenerator,
                organisationClient,
                adminUserService,
                nocCcdService,
                nocNotificationService,
                nocService,
                organisationService
        );
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        organisationsResponse = OrganisationsResponse.builder().organisationIdentifier(ORGANISATION_ID_NEW).build();
        caseData = createCaseData();
        caseDetails = createCaseDetailsWithCaseData(caseData);
        mockedNocUtils = mockStatic(NocUtils.class, Mockito.CALLS_REAL_METHODS);
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        closeable.close();
        if (!mockedNocUtils.isClosed()) {
            mockedNocUtils.close();
        }
    }

    @Test
    void updateClaimantRepresentation_shouldUpdateClaimantRepresentation() throws IOException {
        RepresentedTypeC claimantRep = new RepresentedTypeC();
        claimantRep.setNameOfRepresentative(USER_FIRST_NAME + " " + USER_LAST_NAME);
        ChangeOrganisationRequest changeOrganisationRequest = createChangeOrganisationRequest();
        caseData.setChangeOrganisationRequestField(changeOrganisationRequest);
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(mockAuditEvent()));
        UserDetails mockUser = getMockUser();
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
        when(adminUserService.getUserDetails(eq(DUMMY_ADMIN_USER_TOKEN), anyString())).thenReturn(mockUser);
        nocClaimantRepresentativeService.updateClaimantRepresentation(caseDetails, DUMMY_ADMIN_USER_TOKEN);
        assertThat(caseData.getRepresentativeClaimantType().getNameOfRepresentative())
                .isEqualTo(claimantRep.getNameOfRepresentative());
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress())
                .isEqualTo(mockUser.getEmail());
        assertThat(caseData.getRepresentativeClaimantType().getMyHmctsOrganisation())
                .isEqualTo(changeOrganisationRequest.getOrganisationToAdd());
    }

    private UserDetails getMockUser() {
        final UserDetails userDetails = new UserDetails();
        userDetails.setEmail(USER_EMAIL);
        userDetails.setFirstName(USER_FIRST_NAME);
        userDetails.setLastName(USER_LAST_NAME);
        return userDetails;
    }

    private AuditEvent mockAuditEvent() {
        return AuditEvent.builder()
                .id("123")
                .userId("54321")
                .userFirstName(USER_FIRST_NAME)
                .userLastName(USER_LAST_NAME)
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    void updateRepresentativesAccess() throws IOException {
        CCDRequest ccdRequest = getCCDRequest();
        when(adminUserService.getAdminUserToken()).thenReturn("AUTH_TOKEN");
        when(nocCcdService.startEventForUpdateRepresentation(any(), any(), any(), any())).thenReturn(ccdRequest);
        when(nocCcdService.retrieveCaseUserAssignments(any(), any())).thenReturn(
                mockCaseAssignmentData());
        when(ccdCaseAssignment.applyNocAsAdmin(any())).thenReturn(CCDCallbackResponse.builder()
                .data(caseData)
                .build());
        when(NocUtils.buildApprovedChangeOrganisationRequest(any(), any(), any()))
                .thenReturn(createChangeOrganisationRequest());
        nocClaimantRepresentativeService.updateClaimantRepAccess(getCallBackCallbackRequest());
        verify(nocNotificationService, times(1))
                .sendNotificationOfChangeEmails(any(), any(), any());
    }

    @Test
    void updateClaimantRepAccess_shouldGrantAndRemoveAccessAndSendNotification() throws IOException {
        AccountIdByEmailResponse userResponse = new AccountIdByEmailResponse();
        userResponse.setUserIdentifier(DUMMY_ORGANISATION_USER_ID);
        userResponse.setIdamStatus(DUMMY_ORGANISATION_USER_ID);
        CallbackRequest callbackRequest = getCallBackCallbackRequest();
        callbackRequest.getCaseDetails().getCaseData().setClaimantRepresentedQuestion(YES);
        CCDRequest ccdRequest = getCCDRequest();
        ChangeOrganisationRequest changeRequest = createChangeOrganisationRequest();
        when(nocCcdService.startEventForUpdateRepresentation(any(), any(), any(), any())).thenReturn(ccdRequest);
        when(ccdCaseAssignment.applyNocAsAdmin(any())).thenReturn(CCDCallbackResponse.builder()
                .data(getCaseDataAfter()).build());
        when(NocUtils.buildApprovedChangeOrganisationRequest(any(), any(), any())).thenReturn(changeRequest);
        doReturn(ResponseEntity.ok(userResponse)).when(organisationClient).getAccountIdByEmail(
                DUMMY_ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_1);
        doReturn(ResponseEntity.ok(organisationsResponse)).when(organisationClient)
                .retrieveOrganisationDetailsByUserId(DUMMY_ADMIN_USER_TOKEN, S2S_TOKEN, DUMMY_ORGANISATION_USER_ID);
        // Act
        nocClaimantRepresentativeService.updateClaimantRepAccess(callbackRequest);
        // Assert
        verify(nocNotificationService, times(NumberUtils.INTEGER_ONE)).sendNotificationOfChangeEmails(
                any(), any(), any());
        verify(nocService, times(NumberUtils.INTEGER_ONE)).removeOrganisationRepresentativeAccess(
                anyString(), any(ChangeOrganisationRequest.class));
    }

    @Test
    void shouldReturnChangeRequestWhenOrganisationChanged() {
        CaseData after = new CaseData();
        CaseData before = new CaseData();
        after.setRepresentativeClaimantType(new RepresentedTypeC());
        before.setRepresentativeClaimantType(new RepresentedTypeC());
        Organisation newOrg = Organisation.builder().organisationID("NEW").build();
        after.getRepresentativeClaimantType().setMyHmctsOrganisation(newOrg);
        Organisation oldOrg = Organisation.builder().organisationID("OLD").build();
        before.getRepresentativeClaimantType().setMyHmctsOrganisation(oldOrg);

        ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder().build();
        when(NocUtils.buildApprovedChangeOrganisationRequest(newOrg, oldOrg,
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(expected);

        ChangeOrganisationRequest result =
                nocClaimantRepresentativeService.identifyRepresentationChanges(after, before);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void shouldReturnChangeRequestWhenOrganisationUnchanged() {
        Organisation org = Organisation.builder().organisationID("SAME").build();
        CaseData after = new CaseData();
        CaseData before = new CaseData();
        after.setRepresentativeClaimantType(new RepresentedTypeC());
        before.setRepresentativeClaimantType(new RepresentedTypeC());
        after.getRepresentativeClaimantType().setMyHmctsOrganisation(org);
        before.getRepresentativeClaimantType().setMyHmctsOrganisation(org);

        ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder().build();
        when(NocUtils.buildApprovedChangeOrganisationRequest(org, null,
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(expected);

        ChangeOrganisationRequest result =
                nocClaimantRepresentativeService.identifyRepresentationChanges(after, before);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void shouldHandleNullRepresentativeClaimantType() {
        CaseData after = new CaseData();
        CaseData before = new CaseData();

        ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder().build();
        ChangeOrganisationRequest result =
                nocClaimantRepresentativeService.identifyRepresentationChanges(after, before);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void theValidateRepresentativeOrganisationAndEmail() {
        // when representative claimant type is empty should not set noc warning
        CaseData tmpCaseData = new CaseData();
        nocClaimantRepresentativeService.validateRepresentativeOrganisationAndEmail(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isNull();
        // when representative does not have email should not set noc warning
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().build();
        tmpCaseData.setRepresentativeClaimantType(claimantRepresentative);
        nocClaimantRepresentativeService.validateRepresentativeOrganisationAndEmail(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isNull();
        // when representative does not have hmcts organisation id should not set noc warning
        claimantRepresentative.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        nocClaimantRepresentativeService.validateRepresentativeOrganisationAndEmail(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isNull();
        // when representative has hmcts organisation id and email should set noc warning
        claimantRepresentative.setMyHmctsOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_NEW)
                .build());
        claimantRepresentative.setNameOfRepresentative(REPRESENTATIVE_NAME_1);
        when(organisationService.checkRepresentativeAccountByEmail(REPRESENTATIVE_NAME_1, REPRESENTATIVE_EMAIL_1))
                .thenReturn(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
        nocClaimantRepresentativeService.validateRepresentativeOrganisationAndEmail(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
    }
}