package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UserUtilsTest {

    private static final String DUMMY_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String DUMMY_USER_ID = "user123";
    private static final String DUMMY_EMAIL_1 = "user123@example.com";
    private static final String FIRST_NAME = "Mehmet Tahir";
    private static final String LAST_NAME = "Dede";
    private static final String FULL_NAME = FIRST_NAME + " " + LAST_NAME;

    private static final String EXPECTED_EXCEPTION_INVALID_USER_TOKEN = "Invalid user token";

    @Test
    @SneakyThrows
    void theValidateToken() {
        // When token is empty should throw exception.
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> UserUtils.validateToken(null, DUMMY_SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_INVALID_USER_TOKEN);

        // When token is not empty, should not throw exception.
        final CaseData caseData = new CaseData();
        assertDoesNotThrow(() -> CaseDataUtils.validateCaseData(caseData, DUMMY_SUBMISSION_REFERENCE));
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

    @Test
    void theGetFullName() {
        // When userDetails has first and last name, should return full name.
        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName(FIRST_NAME);
        userDetails.setLastName(LAST_NAME);
        assertThat(UserUtils.getFullName(userDetails)).isEqualTo(FULL_NAME);

        // When userDetails has only first name, should return first name.
        userDetails.setLastName(null);
        assertThat(UserUtils.getFullName(userDetails)).isEqualTo(FIRST_NAME);

        // When userDetails has only last name, should return last name.
        userDetails.setFirstName(null);
        userDetails.setLastName(LAST_NAME);
        assertThat(UserUtils.getFullName(userDetails)).isEqualTo(LAST_NAME);

        // When userDetails has no first or last name, should return empty string.
        userDetails.setFirstName(null);
        userDetails.setLastName(null);
        assertThat(UserUtils.getFullName(userDetails)).isEqualTo(StringUtils.EMPTY);
    }
}