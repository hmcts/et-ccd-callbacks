package uk.gov.hmcts.ethos.replacement.docmosis.service;

import ch.qos.logback.classic.Level;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse.SuperUser;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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
    private static final String REPRESENTATIVE_IDAM_ID = "representative_idam_id";
    private static final String ORGANISATION_ID_1 = "organisation_id";
    private static final String ORGANISATION_ID = "organisation_id";
    private static final String ORGANISATION_ADMIN_EMAIL = "organisation_admin@gmail.com";
    private static final String URL_GET_ACCOUNT_ID_BY_EMAIL =
            "http://localhost:8765/refdata/external/v1/organisations/users/accountId";
    private static final String URL_FIND_ORGANISATION_BY_IDAM_USER_ID =
            "http://localhost:8765/refdata/internal/v1/organisations/orgDetails/" + REPRESENTATIVE_IDAM_ID;
    private static final String URL_FIND_ORGANISATION_SUPERUSER_BY_ID =
            "http://localhost:8765/refdata/internal/v1/organisations?id=" + ORGANISATION_ID;
    private static final String FEIGN_EXCEPTION_USER_NOT_FOUND = "status 404 reading UserClient#getUser(String)";
    private static final String FEIGN_EXCEPTION_ORGANISATION_NOT_FOUND =
            "status 404 reading OrganisationClient#retrieveOrganisationDetailsByUserId(String)";
    private static final String FEIGN_EXCEPTION_SUPER_USER_NOT_FOUND =
            "status 404 reading OrganisationClient#getSuperUser(String)";

    private static final String EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "We have been unable to assign 'Representative Name 1' access to this case via MyHMCTS. They must "
                    + "check with their organisation administrator to ensure they have a valid MyHMCTS account, who "
                    + "will need to assign the case to them. To continue, please click Ignore and Continue.";
    private static final String EXPECTED_WARNING_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID =
            "Failed to find organisation by user id. Exception is status 404 reading "
                    + "OrganisationClient#retrieveOrganisationDetailsByUserId(String)";

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(OrganisationService.class);
    }

    @Test
    void theCheckRepresentativeAccountByEmail() {
        // when user response not has user identifier should return warning message
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);
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
    void theFindOrganisationByIdamUserId() {
        // when organisation response not has organisation identifier should return null
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenReturn(null);
        assertThat(organisationService.findOrganisationByIdamUserId(REPRESENTATIVE_IDAM_ID)).isNull();
        // whe organisation response has organisation identifier should return that organisation response
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID_1).build();
        ResponseEntity<OrganisationsResponse> organisationsResponseEntity = ResponseEntity.ok(organisationsResponse);
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenReturn(organisationsResponseEntity);
        assertThat(organisationService.findOrganisationByIdamUserId(REPRESENTATIVE_IDAM_ID))
                .isEqualTo(organisationsResponse);
        // when feign exception is thrown should return null
        Request request = Request.create(
                Request.HttpMethod.GET,
                URL_FIND_ORGANISATION_BY_IDAM_USER_ID,
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                new RequestTemplate()
        );

        FeignException.NotFound notFound = new FeignException.NotFound(
                FEIGN_EXCEPTION_ORGANISATION_NOT_FOUND,
                request,
                new byte[0],
                Collections.emptyMap()
        );
        when(organisationClient.retrieveOrganisationDetailsByUserId(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN,
                REPRESENTATIVE_IDAM_ID)).thenThrow(notFound);
        assertThat(organisationService.findOrganisationByIdamUserId(REPRESENTATIVE_IDAM_ID))
                .isNull();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID);
    }

    @Test
    void theFindSuperUserByOrganisationId() {
        // when organisation id is empty should return null
        assertThat(organisationService.findSuperUserByOrganisationId(ORGANISATION_ID)).isNull();
        // when organisation response does not have superuser should return null
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);
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
                URL_FIND_ORGANISATION_SUPERUSER_BY_ID,
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
}
