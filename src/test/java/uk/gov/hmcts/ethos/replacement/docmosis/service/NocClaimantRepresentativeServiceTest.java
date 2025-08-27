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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocClaimantHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private NocHelper nocHelper;

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
                nocHelper,
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

        Organisation oldOrganisation =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();

        Organisation newOrganisation =
                Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_2).build();

        ChangeOrganisationRequest changeOrganisationRequest =
                createChangeOrganisationRequest(newOrganisation, oldOrganisation);

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
                .isEqualTo(newOrganisation);
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
}
