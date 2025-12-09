package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersIdamUser;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class RepresentativeServiceTest {

    private RepresentativeService representativeService;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private static final String DUMMY_ADMIN_USER_TOKEN = "dummy_admin_user_token";
    private static final String DUMMY_AUTHORISATION_TOKEN = "dummy_authorisation_token";
    private static final String VALID_REPRESENTATIVE_ID = "valid_representative_id";
    private static final String VALID_RESPONDENT_NAME = "valid_respondent_name";
    private static final String INVALID_REPRESENTATIVE_EMAIL_ADDRESS = "invalid_representative_email_address";
    private static final String DUMMY_ORGANISATION_ID = "dummy_organisation_id";
    private static final String VALID_REPRESENTATIVE_EMAIL_ADDRESS = "valid_representative_email_address";

    private static final String EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS = "Invalid representative exists.";
    private static final String EXPECTED_ERROR_REPRESENTATIVE_MISSING_EMAIL_ADDRESS =
            "Representative valid_representative_id is missing an email address.";
    private static final String EXPECTED_ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND =
            "Organisation not found for representative valid_representative_id.";
    private static final String EXPECTED_ERROR_ORGANISATION_USERS_NOT_FOUND =
            "Organisation users for organisation dummy_organisation_id not found.";
    private static final String EXPECTED_ERROR_REPRESENTATIVE_EMAIL_DOES_NOT_MATCH_ORGANISATION =
            "The email address invalid_representative_email_address was not found among the organisation’s users.";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        representativeService = new RepresentativeService(organisationClient, adminUserService, authTokenGenerator);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
    }

    @Test
    @SneakyThrows
    void theValidateRepresentativeEmailMatchesOrganisationUsers() {
        // when case data is empty should return empty list
        assertThat(representativeService.validateRepresentativeEmailMatchesOrganisationUsers(null)).isEmpty();

        // when case data representative collection is empty should return empty list
        CaseData caseData = new CaseData();
        caseData.setRepCollection(new ArrayList<>());
        assertThat(representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData)).isEmpty();

        // when case data has invalid representative should return error ERROR_INVALID_REPRESENTATIVE_EXISTS
        caseData.getRepCollection().add(RepresentedTypeRItem.builder().build());
        List<String> errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);

        // when representative not exists in hmcts organisation. (my Hmcts is selected as NO) should return empty list
        caseData.getRepCollection().getFirst().setId(VALID_REPRESENTATIVE_ID);
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(VALID_RESPONDENT_NAME);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.getRepCollection().getFirst().setValue(RepresentedTypeR.builder().myHmctsYesNo(NO)
                .dynamicRespRepName(dynamicFixedListType).build());
        assertThat(representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData)).isEmpty();

        // when representative does not have email address should return ERROR_REPRESENTATIVE_MISSING_EMAIL_ADDRESS
        caseData.getRepCollection().getFirst().getValue().setMyHmctsYesNo(YES);
        errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_REPRESENTATIVE_MISSING_EMAIL_ADDRESS);

        // when representative does not have organisation should return ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND
        caseData.getRepCollection().getFirst().getValue().setRepresentativeEmailAddress(
                INVALID_REPRESENTATIVE_EMAIL_ADDRESS);
        errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND);

        // when representative has organisation id is empty should return ERROR_ORGANISATION_USERS_NOT_FOUND
        caseData.getRepCollection().getFirst().getValue().setRespondentOrganisation(Organisation.builder().build());
        errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND);

        // when organisation users response is null should return ERROR_ORGANISATION_USERS_NOT_FOUND
        caseData.getRepCollection().getFirst().getValue().getRespondentOrganisation().setOrganisationID(
                DUMMY_ORGANISATION_ID);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(organisationClient.getOrganisationUsers(DUMMY_ADMIN_USER_TOKEN, DUMMY_AUTHORISATION_TOKEN,
                DUMMY_ORGANISATION_ID)).thenReturn(null);
        errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_ORGANISATION_USERS_NOT_FOUND);

        // when organisation users response body is empty should return ERROR_ORGANISATION_USERS_NOT_FOUND
        when(organisationClient.getOrganisationUsers(DUMMY_ADMIN_USER_TOKEN, DUMMY_AUTHORISATION_TOKEN,
                DUMMY_ORGANISATION_ID)).thenReturn(ResponseEntity.ok().body(null));
        errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_ORGANISATION_USERS_NOT_FOUND);

        // when organisation users response users collection is empty should return ERROR_ORGANISATION_USERS_NOT_FOUND
        OrganisationUsersResponse organisationUsersResponse = OrganisationUsersResponse.builder().users(
                new ArrayList<>()).build();
        when(organisationClient.getOrganisationUsers(DUMMY_ADMIN_USER_TOKEN, DUMMY_AUTHORISATION_TOKEN,
                DUMMY_ORGANISATION_ID)).thenReturn(ResponseEntity.ok().body(organisationUsersResponse));
        errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_ORGANISATION_USERS_NOT_FOUND);

        // when organisation users response users collection has valid email address but representative has
        // invalid email address should return EXPECTED_ERROR_REPRESENTATIVE_EMAIL_DOES_NOT_MATCH_ORGANISATION
        organisationUsersResponse.getUsers().add(OrganisationUsersIdamUser.builder().email(
                VALID_REPRESENTATIVE_EMAIL_ADDRESS).build());
        errors = representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData);
        assertThat(errors).isNotEmpty();
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_REPRESENTATIVE_EMAIL_DOES_NOT_MATCH_ORGANISATION);

        // when representative email address is found in organisation users' email addresses should return empty list.
        caseData.getRepCollection().getFirst().getValue().setRepresentativeEmailAddress(
                VALID_REPRESENTATIVE_EMAIL_ADDRESS);
        assertThat(representativeService.validateRepresentativeEmailMatchesOrganisationUsers(caseData)).isEmpty();
    }
}