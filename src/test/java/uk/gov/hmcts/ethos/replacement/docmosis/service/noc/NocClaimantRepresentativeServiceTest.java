package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ecm.common.client.CcdClient;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocClaimantHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.test.utils.NocClaimantRepresentativeServiceTestUtils.DUMMY_CASE_ID;
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
    private static final String S2S_TOKEN = "s2sToken";

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
    private CcdClient ccdClient;
    @Mock
    private NocClaimantHelper nocClaimantHelper;
    @Mock
    private NocService nocService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private OrganisationsResponse organisationsResponse;

    @InjectMocks
    private NocClaimantRepresentativeService nocClaimantRepresentativeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nocClaimantRepresentativeService = new NocClaimantRepresentativeService(
                authTokenGenerator,
                organisationClient,
                adminUserService,
                nocCcdService,
                nocNotificationService,
                ccdCaseAssignment,
                ccdClient,
                nocService,
                nocClaimantHelper
        );
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        organisationsResponse = OrganisationsResponse.builder().organisationIdentifier(ORGANISATION_ID_NEW).build();
        caseData = createCaseData();
        caseDetails = createCaseDetailsWithCaseData(caseData);
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
        when(nocCcdService.getCaseAssignments(any(), any())).thenReturn(
                mockCaseAssignmentData());
        when(ccdCaseAssignment.applyNocAsAdmin(any())).thenReturn(CCDCallbackResponse.builder()
                .data(caseData)
                .build());
        when(nocClaimantHelper.createChangeRequest(any(), any())).thenReturn(createChangeOrganisationRequest());

        nocClaimantRepresentativeService.updateClaimantRepAccess(getCallBackCallbackRequest());

        verify(nocCcdService, times(1))
                .startEventForUpdateRepresentation(any(), any(), any(), any());

        verify(nocNotificationService, times(1))
                .sendNotificationOfChangeEmails(any(), any(), any());

        verify(ccdClient, times(1))
                .submitUpdateRepEvent(any(), any(), any(), any(), any(), any());
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
        when(nocClaimantHelper.createChangeRequest(any(), any())).thenReturn(changeRequest);
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
        verify(ccdClient, times(NumberUtils.INTEGER_ONE)).submitUpdateRepEvent(
                eq(DUMMY_ADMIN_USER_TOKEN),
                anyMap(),
                anyString(),
                anyString(),
                eq(ccdRequest),
                eq(DUMMY_CASE_ID));
    }

    @Test
    void shouldReturnChangeRequestWhenOrganisationChanged() {
        CaseData after = new CaseData();
        CaseData before = new CaseData();
        after.setRepresentativeClaimantType(new uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC());
        before.setRepresentativeClaimantType(new uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC());
        Organisation newOrg = Organisation.builder().organisationID("NEW").build();
        after.getRepresentativeClaimantType().setMyHmctsOrganisation(newOrg);
        Organisation oldOrg = Organisation.builder().organisationID("OLD").build();
        before.getRepresentativeClaimantType().setMyHmctsOrganisation(oldOrg);

        ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder().build();
        when(nocClaimantHelper.createChangeRequest(newOrg, oldOrg)).thenReturn(expected);

        ChangeOrganisationRequest result =
                nocClaimantRepresentativeService.identifyRepresentationChanges(after, before);

        assertThat(result).isSameAs(expected);
        verify(nocClaimantHelper).createChangeRequest(newOrg, oldOrg);
    }

    @Test
    void shouldReturnChangeRequestWhenOrganisationUnchanged() {
        Organisation org = Organisation.builder().organisationID("SAME").build();
        CaseData after = new CaseData();
        CaseData before = new CaseData();
        after.setRepresentativeClaimantType(new uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC());
        before.setRepresentativeClaimantType(new uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC());
        after.getRepresentativeClaimantType().setMyHmctsOrganisation(org);
        before.getRepresentativeClaimantType().setMyHmctsOrganisation(org);

        ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder().build();
        when(nocClaimantHelper.createChangeRequest(org, null)).thenReturn(expected);

        ChangeOrganisationRequest result =
                nocClaimantRepresentativeService.identifyRepresentationChanges(after, before);

        assertThat(result).isSameAs(expected);
        verify(nocClaimantHelper).createChangeRequest(org, null);
    }

    @Test
    void shouldHandleNullRepresentativeClaimantType() {
        CaseData after = new CaseData();
        CaseData before = new CaseData();

        ChangeOrganisationRequest expected = ChangeOrganisationRequest.builder().build();
        when(nocClaimantHelper.createChangeRequest(null, null)).thenReturn(expected);

        ChangeOrganisationRequest result =
                nocClaimantRepresentativeService.identifyRepresentationChanges(after, before);

        assertThat(result).isSameAs(expected);
        verify(nocClaimantHelper).createChangeRequest(null, null);
    }

    @Test
    void grantClaimantRepAccess_shouldGrantAccessIfOrgMatches() throws IOException {
        String accessToken = "access";
        String email = "user@test.com";
        AccountIdByEmailResponse userResponse = new AccountIdByEmailResponse();
        userResponse.setUserIdentifier("userId");

        OrganisationsResponse orgResponse = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID_NEW)
                .build();

        when(organisationClient.getAccountIdByEmail(eq(accessToken), anyString(), eq(email)))
                .thenReturn(ResponseEntity.ok(userResponse));
        when(organisationClient.retrieveOrganisationDetailsByUserId(eq(accessToken), anyString(), eq("userId")))
                .thenReturn(ResponseEntity.ok(orgResponse));

        doNothing().when(nocService).grantCaseAccess(anyString(), anyString(), anyString());

        Organisation orgToAdd = Organisation.builder().organisationID(ORGANISATION_ID_NEW).build();
        String caseId = "case123";
        nocClaimantRepresentativeService.grantClaimantRepAccess(accessToken, email, caseId, orgToAdd,
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());

        verify(nocService, times(1)).grantCaseAccess(anyString(), anyString(), anyString());
    }

    @Test
    void grantClaimantRepAccess_shouldNotGrantAccessIfOrgDoesNotMatch() throws IOException {
        String accessToken = "access";
        String email = "user@test.com";
        AccountIdByEmailResponse userResponse = new AccountIdByEmailResponse();
        userResponse.setUserIdentifier("userId");

        OrganisationsResponse orgResponse = OrganisationsResponse.builder()
                .organisationIdentifier("otherOrgId")
                .build();

        when(organisationClient.getAccountIdByEmail(eq(accessToken), anyString(), eq(email)))
                .thenReturn(ResponseEntity.ok(userResponse));
        when(organisationClient.retrieveOrganisationDetailsByUserId(eq(accessToken), anyString(), eq("userId")))
                .thenReturn(ResponseEntity.ok(orgResponse));
        when(authTokenGenerator.generate()).thenReturn("serviceToken");

        String caseId = "case123";
        Organisation orgToAdd = Organisation.builder().organisationID("orgId").build();
        nocClaimantRepresentativeService.grantClaimantRepAccess(accessToken, email, caseId, orgToAdd,
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());

        verify(nocService, never()).grantCaseAccess(anyString(), anyString(), anyString());
    }

}