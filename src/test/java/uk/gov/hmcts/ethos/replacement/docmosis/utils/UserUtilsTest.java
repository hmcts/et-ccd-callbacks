package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class UserUtilsTest {

    private static final String DUMMY_SUBMISSION_REFERENCE = "1234567890123456";

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
}
