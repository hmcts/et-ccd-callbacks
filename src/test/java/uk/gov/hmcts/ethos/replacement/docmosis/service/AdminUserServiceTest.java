package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdminUserServiceTest {
    public static final String TOKEN = "ejk51xvk";

    public static final String EXPECTED_BEARER_TOKEN = "Bearer ejk51xvk";

    public static final String USER_ID = "111-222-333-444";

    @MockBean
    private UserService userService;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userService);
    }

    @Test
    void shouldReturnAdminUserToken() {
        when(userService.getAccessToken(any(), any())).thenReturn(TOKEN);
        String adminUserToken = adminUserService.getAdminUserToken();
        assertThat(adminUserToken).isEqualTo(EXPECTED_BEARER_TOKEN);
    }

    @Test
    void shouldReturnUserId() {
        UserDetails userDetails = new UserDetails();
        when(userService.getUserDetailsById(any(), eq(USER_ID))).thenReturn(userDetails);
        assertThat(adminUserService.getUserDetails(USER_ID)).isEqualTo(userDetails);
    }
}