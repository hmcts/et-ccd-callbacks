package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class UserUtilsTest {

    private static final String DUMMY_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String DUMMY_USER_ID = "user123";
    private static final String DUMMY_EMAIL_1 = "user123@example.com";

    private static final String EXPECTED_EXCEPTION_INVALID_USER_TOKEN = "Invalid user token";

    @Test
    @SneakyThrows
    void theValidateToken() {
        // When token is empty should throw exception.
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> UserUtils.validateToken(null, DUMMY_SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_INVALID_USER_TOKEN);

        // When token is not empty, should not throw exception.
        assertDoesNotThrow(() -> UserUtils.validateToken("valid_token", DUMMY_SUBMISSION_REFERENCE));
    }

    @Test
    void theHasRequiredUserDetails() {
        // When userDetails is null, should return false.
        assertThat(UserUtils.hasRequiredUserDetails(null)).isFalse();
        // When userDetails has blank uid and email, should return false.
        UserDetails userDetails = new UserDetails();
        assertThat(UserUtils.hasRequiredUserDetails(userDetails)).isFalse();
        // When userDetails has blank email, should return false.
        userDetails.setUid(DUMMY_USER_ID);
        assertThat(UserUtils.hasRequiredUserDetails(userDetails)).isFalse();
        // When userDetails has non-blank uid and email should return true.
        userDetails.setEmail(DUMMY_EMAIL_1);
        assertThat(UserUtils.hasRequiredUserDetails(userDetails)).isTrue();
    }

    @ParameterizedTest
    @CsvSource(value = {"'John Smith', 'John', 'Smith', 'John Smith'",
        "null, 'John', 'Smith', 'John Smith'",
        "null, 'John', null, 'John'",
        "null, null, 'Smith', 'Smith'",
        "null, null,   null, ''",
        "' ', 'John', 'Smith', 'John Smith'",
        "null, ' John  ', '  Smith  ', 'John Smith'"},
        nullValues = "null")
    void theResolveUserDisplayName(String name, String firstName, String lastName, String expected) {
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getName()).thenReturn(name);
        when(userDetails.getFirstName()).thenReturn(firstName);
        when(userDetails.getLastName()).thenReturn(lastName);

        assertThat(UserUtils.resolveUserDisplayName(userDetails)).isEqualTo(expected);
    }

    @Test
    void theResolveUserDisplayName_null() {
        assertThat(UserUtils.resolveUserDisplayName(null)).isEmpty();
    }
}