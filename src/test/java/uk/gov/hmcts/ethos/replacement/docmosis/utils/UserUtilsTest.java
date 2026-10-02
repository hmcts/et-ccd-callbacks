package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UserUtilsTest {

    private static final String DUMMY_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String DUMMY_USER_ID = "user123";
    private static final String DUMMY_CLAIMANT_REPRESENTATIVE_ID = "rep123";
    private static final String DUMMY_EMAIL_1 = "user123@example.com";
    private static final String DUMMY_EMAIL_2 = "user456@example.com";

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
    void theIsLeadClaimantRepresentative() {
        // When userDetails is null, should return false.
        assertThat(UserUtils.isLeadClaimantRepresentative(null, null)).isFalse();
        // When claimant representative is null, should return false.
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(DUMMY_USER_ID);
        userDetails.setEmail(DUMMY_EMAIL_1);
        assertThat(UserUtils.isLeadClaimantRepresentative(userDetails, null)).isFalse();
        // When userDetails and claimant representative email do not match, should return false.
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder()
                .representativeId(DUMMY_CLAIMANT_REPRESENTATIVE_ID)
                .representativeEmailAddress(DUMMY_EMAIL_2).build();
        assertThat(UserUtils.isLeadClaimantRepresentative(userDetails, claimantRepresentative)).isFalse();
        // When userDetails and claimant representative emails match, should return true.
        claimantRepresentative.setRepresentativeEmailAddress(DUMMY_EMAIL_1);
        assertThat(UserUtils.isLeadClaimantRepresentative(userDetails, claimantRepresentative)).isTrue();
    }
}