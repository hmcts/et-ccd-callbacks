package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    private UserService userService;
    @MockitoBean
    private UserIdamService userIdamService;

    private static final String USER_TOKEN = "dummy user token";
    private static final String SUBMISSION_REFERENCE = "dummy submission reference";
    private static final String USER_ID = "dummy user id";
    private static final String EXPECTED_EXCEPTION_USER_NOT_FOUND = "User not found";
    private static final String EXPECTED_EXCEPTION_USER_ID_NOT_FOUND = "User ID not found";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        userService = new UserService(userIdamService);
    }

    @Test
    @SneakyThrows
    void theGetValidatedUserDetails() {
        // when user idam service returns empty user details should throw GenericServiceException
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(null);
        GenericServiceException gse = assertThrows(GenericServiceException.class, () ->
                userService.getValidatedUserDetails(USER_TOKEN, SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_USER_NOT_FOUND);
        // when user idam service returns user details without user id should throw GenericServiceException
        UserDetails userDetails = new UserDetails();
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        gse = assertThrows(GenericServiceException.class, () ->
                userService.getValidatedUserDetails(USER_TOKEN, SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_USER_ID_NOT_FOUND);
        // when user idam service returns correct user details should return the same user details
        userDetails.setUid(USER_ID);
        assertThat(userService.getValidatedUserDetails(USER_TOKEN, SUBMISSION_REFERENCE))
                .isEqualTo(userDetails);
    }
}
