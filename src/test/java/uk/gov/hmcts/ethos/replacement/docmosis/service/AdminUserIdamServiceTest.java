package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminUserIdamServiceTest {
    public static final String TOKEN = "ejk51xvk";

    public static final String EXPECTED_BEARER_TOKEN = "Bearer ejk51xvk";

    public static final String USER_ID = "111-222-333-444";

    @Mock
    private UserIdamService userIdamService;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userIdamService);
    }

    @Test
    void shouldReturnAdminUserToken() {
        when(userIdamService.getAccessToken(any(), any())).thenReturn(TOKEN);
        String adminUserToken = adminUserService.getAdminUserToken();
        assertThat(adminUserToken).isEqualTo(EXPECTED_BEARER_TOKEN);
    }

    @Test
    void shouldReturnUserId() {
        UserDetails userDetails = new UserDetails();
        when(userIdamService.getUserDetailsById(any(), eq(USER_ID))).thenReturn(userDetails);
        assertThat(adminUserService.getUserDetails(TOKEN, USER_ID)).isEqualTo(userDetails);
    }
}
