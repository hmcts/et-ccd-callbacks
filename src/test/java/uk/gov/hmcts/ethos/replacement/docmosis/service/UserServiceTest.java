package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.OAuth2Configuration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    private UserDetails userDetails;

    private TokenResponse tokenResponse;
    private OAuth2Configuration oauth2Configuration;

    @BeforeEach
    public void setUp() {
        userDetails = HelperTest.getUserDetails();
        tokenResponse = HelperTest.getUserToken();
        IdamApi idamApi = new IdamApi() {
            @Override
            public UserDetails retrieveUserDetails(String authorisation) {
                return HelperTest.getUserDetails();
            }

            @Override
            public UserDetails getUserByUserId(String authorisation, String userId) {
                return HelperTest.getUserDetails();
            }

            @Override
            public TokenResponse generateOpenIdToken(TokenRequest tokenRequest) {
                return tokenResponse;
            }
        };

        mockOauth2Configuration();

        userService = new UserService(idamApi, oauth2Configuration);
    }

    @Test
    public void shouldHaveUserDetails() {
        assertEquals(userService.getUserDetails("TOKEN"), userDetails);
    }

    @Test
    public void shouldCheckAllUserDetails() {
        assertEquals(userDetails, userService.getUserDetails("TOKEN"));
        assertEquals("mail@mail.com", userService.getUserDetails("TOKEN").getEmail());
        assertEquals("Mike", userService.getUserDetails("TOKEN").getFirstName());
        assertEquals("Jordan", userService.getUserDetails("TOKEN").getLastName());
        assertEquals(Collections.singletonList("role"), userService.getUserDetails("TOKEN").getRoles());
        assertEquals(userDetails.toString(), userService.getUserDetails("TOKEN").toString());
    }

    @Test
    public void shouldGetUserById() {
        assertEquals(userDetails, userService.getUserDetailsById("TOKEN", "id"));
    }

    @Test
    public void shouldGetAccessToken() {
        assertEquals("abcefg", userService.getAccessToken("John@email.com", "abc123"));
    }

    @Test
    public void shouldReturnAccessTokenResponse() {
        assertEquals(tokenResponse, userService.getAccessTokenResponse("John@email.com", "abc123"));
    }

    private void mockOauth2Configuration() {
        oauth2Configuration = mock(OAuth2Configuration.class);
        when(oauth2Configuration.getClientId()).thenReturn("111");
        when(oauth2Configuration.getClientSecret()).thenReturn("AAAAA");
        when(oauth2Configuration.getRedirectUri()).thenReturn("http://localhost:8080/test");
        when(oauth2Configuration.getClientScope()).thenReturn("roles");
    }
}