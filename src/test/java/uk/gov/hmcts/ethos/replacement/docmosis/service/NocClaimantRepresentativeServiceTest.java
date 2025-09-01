package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocClaimantHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NocClaimantRepresentativeServiceTest {
    private static final String CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String USER_EMAIL = "test@hmcts.net";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Brown";
    private static final String ORGANISATION_ID_OLD = "ORG_OLD";
    private static final String ORGANISATION_ID_NEW = "ORG3_NEW";
    private static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";

    private NocClaimantRepresentativeService nocClaimantRepresentativeService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private OrganisationClient organisationClient;
    @MockBean
    private AdminUserService adminUserService;
    @MockBean
    private NocCcdService nocCcdService;
    @MockBean
    private NocNotificationService nocNotificationService;
    @MockBean
    private CcdCaseAssignment ccdCaseAssignment;
    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private NocClaimantHelper nocClaimantHelper;
    @MockBean
    private NocService nocService;

    private CaseData caseData;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
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

        caseData = new CaseData();
        ClaimantType claimantType = new ClaimantType();
        caseData.setClaimantType(claimantType);
        caseData.setClaimantFirstName("John");
        caseData.setClaimantLastName("Doe");

        caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("12345");
    }

    @Test
    void updateClaimantRepresentation_shouldUpdateClaimantRepresentation() throws IOException {
        RepresentedTypeC claimantRep = new RepresentedTypeC();
        claimantRep.setNameOfRepresentative(USER_FIRST_NAME + " " + USER_LAST_NAME);

        ChangeOrganisationRequest changeOrganisationRequest =
                createChangeOrganisationRequest();

        caseData.setChangeOrganisationRequestField(changeOrganisationRequest);
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(
                Optional.of(mockAuditEvent()));
        UserDetails mockUser = getMockUser();
        when(adminUserService.getUserDetails(any())).thenReturn(mockUser);

        nocClaimantRepresentativeService.updateClaimantRepresentation(
                caseDetails, "Some Token"
        );

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

    private ChangeOrganisationRequest createChangeOrganisationRequest() {
        Organisation organisationToRemove =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();

        Organisation organisationToAdd =
                Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_2).build();

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
    void updateRepresentativesAccess() throws IOException {
        CCDRequest ccdRequest = getCCDRequest();

        when(adminUserService.getAdminUserToken()).thenReturn("AUTH_TOKEN");
        when(nocCcdService.updateCaseRepresentation(any(), any(), any(), any())).thenReturn(ccdRequest);
        when(nocCcdService.getCaseAssignments(any(), any())).thenReturn(
                mockCaseAssignmentData());
        when(ccdCaseAssignment.applyNocAsAdmin(any())).thenReturn(CCDCallbackResponse.builder()
                .data(caseData)
                .build());
        when(nocClaimantHelper.createChangeRequest(any(), any())).thenReturn(createChangeOrganisationRequest());

        nocClaimantRepresentativeService.updateClaimantRepAccess(getCallBackCallbackRequest(), "test@test.com");

        verify(nocCcdService, times(1))
                .updateCaseRepresentation(any(), any(), any(), any());

        verify(nocNotificationService, times(1))
                .sendNotificationOfChangeEmails(any(), any(), any(), anyString());

        verify(ccdClient, times(1))
                .submitUpdateRepEvent(any(), any(), any(), any(), any(), any());
    }

    private CallbackRequest getCallBackCallbackRequest() {
        CallbackRequest callbackRequest = new CallbackRequest();
        CaseDetails caseDetailsBefore = new CaseDetails();
        caseDetailsBefore.setCaseData(getCaseDataBefore());
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        callbackRequest.setCaseDetails(caseDetailsAfter);
        return callbackRequest;
    }

    private CCDRequest getCCDRequest() {
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        ccdRequest.setCaseDetails(caseDetailsAfter);
        return ccdRequest;
    }

    private CaseData getCaseDataAfter() {
        CaseData caseDataAfter = new CaseData();
        caseDataAfter.setRespondentCollection(new ArrayList<>());

        //Organisation
        Organisation org1 =
                Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy1 =
                OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(CLAIMANT_SOLICITOR).build();
        caseDataAfter.setRespondentOrganisationPolicy0(orgPolicy1);


        // Claimant Representative
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("John Brown");
        representedTypeC.setRepresentativeEmailAddress("claimantrep@test.com");
        representedTypeC.setMyHmctsOrganisation(org1);
        caseDataAfter.setRepresentativeClaimantType(representedTypeC);

        caseDataAfter.setChangeOrganisationRequestField(createChangeOrganisationRequest());
        return caseDataAfter;
    }

    private CaseData getCaseDataBefore() {
        CaseData caseDataBefore = new CaseData();

        caseDataBefore.setRespondentCollection(new ArrayList<>());
        caseDataBefore.setClaimant("claimant");
        caseDataBefore.setEthosCaseReference("caseRef");

        //Organisation
        Organisation org1 =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
                OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(CLAIMANT_SOLICITOR).build();
        caseDataBefore.setRespondentOrganisationPolicy0(orgPolicy1);

        // Claimant Representative
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("James Brown");
        representedTypeC.setRepresentativeEmailAddress("james@test.com");
        representedTypeC.setMyHmctsOrganisation(org1);
        caseDataBefore.setRepresentativeClaimantType(representedTypeC);

        return caseDataBefore;
    }

    private CaseUserAssignmentData mockCaseAssignmentData() {
        List<CaseUserAssignment> caseUserAssignments = List.of(CaseUserAssignment.builder().userId("USER_ID_ONE")
                        .organisationId(ET_ORG_1)
                        .caseRole(CLAIMANT_SOLICITOR)
                        .caseId("CASE_ID_ONE")
                        .build(),
                CaseUserAssignment.builder().userId("USER_ID_TWO")
                        .organisationId(ET_ORG_2)
                        .caseRole(CLAIMANT_SOLICITOR)
                        .caseId("CASE_ID_ONE")
                        .build());

        return CaseUserAssignmentData.builder().caseUserAssignments(caseUserAssignments).build();
    }
}