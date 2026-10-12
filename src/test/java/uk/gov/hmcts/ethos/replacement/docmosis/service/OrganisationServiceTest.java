package uk.gov.hmcts.ethos.replacement.docmosis.service;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse.SuperUser;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    OrganisationClient organisationClient;

    @InjectMocks
    OrganisationService organisationService;

    private static final String ADMIN_USER_TOKEN = "admin_user_token";
    private static final String AUTHORISATION_TOKEN = "authorisation_token";
    private static final String REPRESENTATIVE_NAME_1 = "Representative Name 1";
    private static final String REPRESENTATIVE_EMAIL_1 = "representative_1@hmcts.org";
    private static final String REPRESENTATIVE_ID_1 = "representative_id_1";
    private static final String REPRESENTATIVE_IDAM_ID = "5c3384d4-55e5-428a-ac37-cb6d42bc561c";
    private static final String ORGANISATION_ID = "organisation_id";
    private static final String ORGANISATION_NAME = "Organisation Name";
    private static final String ORGANISATION_ADMIN_EMAIL = "organisation_admin@gmail.com";
    private static final String URL_GET_ACCOUNT_ID_BY_EMAIL =
            "http://localhost:8765/refdata/external/v1/organisations/users/accountId";
    private static final String URL_GET_ORGANISATION_BY_ID =
            "http://localhost:8765/refdata/internal/v1/organisations?id=" + ORGANISATION_ID;
    private static final String FEIGN_EXCEPTION_USER_NOT_FOUND = "status 404 reading UserClient#getUser(String)";
    private static final String FEIGN_EXCEPTION_SUPER_USER_NOT_FOUND =
            "status 404 reading OrganisationClient#getSuperUser(String)";

    private static final String EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "We have been unable to assign 'Representative Name 1' access to this case via MyHMCTS. They must "
                    + "check with their organisation administrator to ensure they have a valid MyHMCTS account, who "
                    + "will need to assign the case to them. To continue, please click Ignore and Continue.";

    @BeforeEach
    void setUp() {
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);
    }

    @Test
    void theCheckRepresentativeAccountByEmail() {
        // when user response not has user identifier should return warning message
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(null);
        assertThat(organisationService.checkRepresentativeAccountByEmail(REPRESENTATIVE_NAME_1, REPRESENTATIVE_EMAIL_1))
                .isEqualTo(List.of(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL));
        // when user response has user identifier should return empty string
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID_1);
        ResponseEntity<AccountIdByEmailResponse> userResponse = ResponseEntity.ok(accountIdByEmailResponse);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(userResponse);
        assertThat(organisationService.checkRepresentativeAccountByEmail(REPRESENTATIVE_NAME_1, REPRESENTATIVE_EMAIL_1))
                .isEmpty();
        // when feign exception 404 is thrown should return warning message
        Request request = Request.create(
                Request.HttpMethod.GET,
                URL_GET_ACCOUNT_ID_BY_EMAIL,
                Collections.emptyMap(),
                new byte[0],
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        FeignException.NotFound notFound = new FeignException.NotFound(
                FEIGN_EXCEPTION_USER_NOT_FOUND,
                request,
                new byte[0],
                Collections.emptyMap()
        );
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenThrow(notFound);
        assertThat(organisationService.checkRepresentativeAccountByEmail(REPRESENTATIVE_NAME_1, REPRESENTATIVE_EMAIL_1))
                .isEqualTo(List.of(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL));

    }

    @Test
    void theFindSuperUserByOrganisationId() {
        // when organisation id is empty should return null
        assertThat(organisationService.findSuperUserByOrganisationId(ORGANISATION_ID)).isNull();
        // when organisation response does not have superuser should return null
        ResponseEntity<RetrieveOrgByIdResponse> organisationResponse =
                new ResponseEntity<>(RetrieveOrgByIdResponse.builder().build(), HttpStatus.OK);
        when(organisationClient.getOrganisationById(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, ORGANISATION_ID))
                .thenReturn(organisationResponse);
        assertThat(organisationService.findSuperUserByOrganisationId(ORGANISATION_ID)).isNull();
        // when organisation response returns superuser should return that superuser
        SuperUser superUser = SuperUser.builder().email(ORGANISATION_ADMIN_EMAIL).build();
        organisationResponse = new ResponseEntity<>(RetrieveOrgByIdResponse.builder().superUser(superUser).build(),
                HttpStatus.OK);
        when(organisationClient.getOrganisationById(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, ORGANISATION_ID))
                .thenReturn(organisationResponse);
        assertThat(organisationService.findSuperUserByOrganisationId(ORGANISATION_ID)).isEqualTo(superUser);
        // when organisation client throws exception should return null
        Request request = Request.create(
                Request.HttpMethod.GET,
                URL_GET_ORGANISATION_BY_ID,
                Collections.emptyMap(),
                new byte[0],
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        FeignException.NotFound notFound = new FeignException.NotFound(
                FEIGN_EXCEPTION_SUPER_USER_NOT_FOUND,
                request,
                new byte[0],
                Collections.emptyMap()
        );
        when(organisationClient.getOrganisationById(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, ORGANISATION_ID))
                .thenThrow(notFound);
        assertThat(organisationService.findSuperUserByOrganisationId(ORGANISATION_ID)).isNull();
    }

    @Test
    void theFindOrganisationByIdamId() {
        // when not have valid organisation response should return null
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenReturn(null);
        assertThat(organisationService.findOrganisationByIdamId(REPRESENTATIVE_IDAM_ID)).isNull();
        // when has a valid organisation response should return that organisation
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID).name(ORGANISATION_NAME).build();
        ResponseEntity<OrganisationsResponse> organisationsResponseEntity = new ResponseEntity<>(organisationsResponse,
                HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenReturn(organisationsResponseEntity);
        assertThat(organisationService.findOrganisationByIdamId(REPRESENTATIVE_IDAM_ID))
                .isEqualTo(organisationsResponse);
    }

    @Test
    void theFindClaimantRepresentativeOrganisationName() {
        // when claimant representative has organisation name should return that name
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().nameOfOrganisation(ORGANISATION_NAME)
                .build();
        UserDetails userDetails = new UserDetails();
        assertThat(organisationService.resolveClaimantRepresentativeOrganisationName(claimantRepresentative,
                userDetails)).isEqualTo(ORGANISATION_NAME);
        // when claimant representative does not have organisation name and user does not have an id should return
        // empty string
        claimantRepresentative.setNameOfOrganisation(StringUtils.EMPTY);
        assertThat(organisationService.resolveClaimantRepresentativeOrganisationName(claimantRepresentative,
                userDetails)).isEmpty();
        // when organisation is empty should return empty string
        userDetails.setUid(REPRESENTATIVE_IDAM_ID);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenReturn(null);
        assertThat(organisationService.resolveClaimantRepresentativeOrganisationName(claimantRepresentative,
                userDetails)).isEmpty();
        // when organisation does not have name should return empty string
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID).build();
        ResponseEntity<OrganisationsResponse> organisationsResponseEntity = new ResponseEntity<>(organisationsResponse,
                HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenReturn(organisationsResponseEntity);
        assertThat(organisationService.resolveClaimantRepresentativeOrganisationName(claimantRepresentative,
                userDetails)).isEmpty();
        // when organisation has name should return that name
        organisationsResponse.setName(ORGANISATION_NAME);
        organisationsResponseEntity = new ResponseEntity<>(organisationsResponse, HttpStatus.OK);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenReturn(organisationsResponseEntity);
        assertThat(organisationService.resolveClaimantRepresentativeOrganisationName(claimantRepresentative,
                userDetails)).isEqualTo(ORGANISATION_NAME);
    }
}
