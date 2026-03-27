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
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class NocRepresentativeServiceTest {

    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @Mock
    private NocClaimantRepresentativeService nocClaimantRepresentativeService;
    @Mock
    private NocService nocService;
    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private NocRepresentativeService nocRepresentativeService;

    AutoCloseable closeable;

    private static final String CASE_ID = "1234567890123456";
    private static final String REPRESENTATIVE_ID = "test_representative_id";
    private static final String REPRESENTATIVE_NAME = "Representative Name";
    private static final String REPRESENTATIVE_EMAIL = "representative@hmcts.org";
    private static final String ORGANISATION_ID_1 = "test_organisation_id_1";
    private static final String ORGANISATION_ID_2 = "test_organisation_id_2";
    private static final String ADMIN_USER_TOKEN = "adminUserToken";
    private static final String DUMMY_EXCEPTION_MESSAGE = "An error occurred";
    private static final String USER_ID = "test_user_id";
    private static final String EXPECTED_ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES =
            "Representative Representative Name organisation does not match with selected organisation "
                    + "test_organisation_id_1";

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        nocRepresentativeService = new NocRepresentativeService(
                nocRespondentRepresentativeService,
                nocClaimantRepresentativeService,
                nocService,
                adminUserService
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

        CaseData result = nocRepresentativeService.updateRepresentation(caseDetails, "token");

        assertThat(result).isSameAs(caseData);
        verify(nocClaimantRepresentativeService).updateClaimantRepresentation(caseDetails, "token");
    }

    @Test
    void updateRepresentation_shouldCallRespondentService_whenNotClaimantSolicitorRole() throws Exception {
        CaseData caseData = new CaseData();
        ChangeOrganisationRequest change = buildChangeRequest("RESPONDENTSOLICITOR");
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        when(nocRespondentRepresentativeService.updateRespondentRepresentation(any(CaseDetails.class)))
                .thenReturn(caseData);
        when(nocRespondentRepresentativeService.prepopulateOrgAddress(any(CaseData.class), anyString()))
                .thenReturn(caseData);
        when(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(any(CaseDetails.class)))
                .thenReturn(caseData);
        CaseData result = nocRepresentativeService.updateRepresentation(caseDetails, "token");

        assertThat(result).isSameAs(caseData);
        verify(nocRespondentRepresentativeService).updateRespondentRepresentation(caseDetails);
        verify(nocRespondentRepresentativeService).prepopulateOrgAddress(caseData, "token");
        verifyNoInteractions(nocClaimantRepresentativeService);
    }

    @Test
    void updateRepresentation_shouldThrowException_whenChangeRequestInvalid() {
        CaseData caseData = new CaseData();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
    }

    @Test
    void updateRepresentation_shouldThrowException_whenChangeRequestIsNull() {
        CaseData caseData = new CaseData();
        caseData.setChangeOrganisationRequestField(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
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
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
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
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
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
    void theValidateRepresentativesOrganisation() {
        // when there is no representative in rep collection should return empty list
        CaseData caseData = new CaseData();
        caseData.setRepCollection(new ArrayList<>());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseData(caseData);
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isEmpty();
        // when representative in rep collection is not valid should return empty list
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        caseData.getRepCollection().add(representative);
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isEmpty();
        // when representative not has email address should return empty list
        representative.setId(REPRESENTATIVE_ID);
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isEmpty();
        // when representative not has my hmcts selection should return empty list
        representative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL);
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isEmpty();
        // when representative not has any organisation should return empty list
        representative.getValue().setMyHmctsYesNo(YES);
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isEmpty();
        // when organisation response and representative organisation not matches should return error
        representative.getValue().setNameOfRepresentative(REPRESENTATIVE_NAME);
        representative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1)
                .build());
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(USER_ID);
        when(nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL, CASE_ID))
                .thenReturn(accountIdByEmailResponse);
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID_2).build();
        when(nocService.findOrganisationByUserId(ADMIN_USER_TOKEN, USER_ID, CASE_ID)).thenReturn(organisationsResponse);
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isNotEmpty()
                .contains(EXPECTED_ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES);
        // when organisation response and representative organisation matches should return empty list
        organisationsResponse.setOrganisationIdentifier(ORGANISATION_ID_1);
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isEmpty();
        // when user response not found should return empty list
        when(nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL, CASE_ID))
                .thenThrow(new GenericServiceException(DUMMY_EXCEPTION_MESSAGE));
        assertThat(nocRepresentativeService.validateRepresentativesOrganisation(caseDetails)).isEmpty();

    }
}