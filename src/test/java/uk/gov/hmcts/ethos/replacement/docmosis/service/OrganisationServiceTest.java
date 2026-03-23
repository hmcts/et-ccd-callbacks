package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

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

    private static final String EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Representative 'Representative Name 1' could not be found using representative_1@hmcts.org. Case access "
                    + "will not be defined for this representative.\n";

    @Test
    void theCheckRepresentativeAccountByEmail() {
        // when user response not has user identifier should return warning message
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(AUTHORISATION_TOKEN);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(null);
        assertThat(organisationService.checkRepresentativeAccountByEmail(REPRESENTATIVE_NAME_1, REPRESENTATIVE_EMAIL_1))
                .isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when user response has user identifier should return empty string
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID_1);
        ResponseEntity<AccountIdByEmailResponse> userResponse = ResponseEntity.ok(accountIdByEmailResponse);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(userResponse);
        assertThat(organisationService.checkRepresentativeAccountByEmail(REPRESENTATIVE_NAME_1, REPRESENTATIVE_EMAIL_1))
                .isEmpty();
        // when exception is thrown should return warning message
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, AUTHORISATION_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenThrow(new RuntimeException());
        assertThat(organisationService.checkRepresentativeAccountByEmail(REPRESENTATIVE_NAME_1, REPRESENTATIVE_EMAIL_1))
                .isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
    }
}
