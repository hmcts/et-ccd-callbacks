package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ClaimantUtilsTest {

    private static final String CLAIMANT_EMAIL = "claimant@hmcts.org";

    private static final String EXPECTED_EXCEPTION_NO_CLAIMANT_FOUND = "Could not find claimant email address.";

    @Test
    void theGetClaimantEmailAddress() {
        // when case data is empty should throw no claimant found exception
        NotFoundException nfe = assertThrows(NotFoundException.class, () -> ClaimantUtils
                .getClaimantEmailAddress(null));
        assertThat(nfe.getMessage()).isEqualTo(EXPECTED_EXCEPTION_NO_CLAIMANT_FOUND);
        // when claimant type is not found should throw exception
        CaseData caseData = new CaseData();
        nfe = assertThrows(NotFoundException.class, () -> ClaimantUtils
                .getClaimantEmailAddress(caseData));
        assertThat(nfe.getMessage()).isEqualTo(EXPECTED_EXCEPTION_NO_CLAIMANT_FOUND);
        // when claimant type does not have claimant email address should throw exception
        caseData.setClaimantType(new ClaimantType());
        nfe = assertThrows(NotFoundException.class, () -> ClaimantUtils
                .getClaimantEmailAddress(caseData));
        assertThat(nfe.getMessage()).isEqualTo(EXPECTED_EXCEPTION_NO_CLAIMANT_FOUND);
        // when claimant has email address should return that address
        caseData.getClaimantType().setClaimantEmailAddress(CLAIMANT_EMAIL);
        assertThat(ClaimantUtils.getClaimantEmailAddress(caseData)).isEqualTo(CLAIMANT_EMAIL);
    }
}
