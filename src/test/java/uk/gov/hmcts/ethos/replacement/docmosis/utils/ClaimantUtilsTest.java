package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ClaimantUtilsTest {

    private static final String CLAIMANT_REPRESENTATIVE_EMAIL = "claimant_representative@hmcts.org";
    private static final String CLAIMANT_EMAIL = "claimant@hmcts.org";

    private static final String EXPECTED_EXCEPTION_CLAIMANT_NOT_FOUND = "Could not find claimant.";

    @Test
    void theGetClaimantEmailAddress() {
        // when case data is empty should throw no claimant found exception
        NotFoundException nfe = assertThrows(NotFoundException.class, () -> ClaimantUtils
                .getClaimantEmailAddress(null));
        assertThat(nfe.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CLAIMANT_NOT_FOUND);
        // when claimant type is not found should throw exception
        CaseData caseData = new CaseData();
        nfe = assertThrows(NotFoundException.class, () -> ClaimantUtils
                .getClaimantEmailAddress(caseData));
        assertThat(nfe.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CLAIMANT_NOT_FOUND);
        // when claimant type does not have claimant email address should throw exception
        caseData.setClaimantType(new ClaimantType());
        assertThat(ClaimantUtils.getClaimantEmailAddress(caseData)).isEmpty();
        // when claimant has email address should return that address
        caseData.getClaimantType().setClaimantEmailAddress(CLAIMANT_EMAIL);
        assertThat(ClaimantUtils.getClaimantEmailAddress(caseData)).isEqualTo(CLAIMANT_EMAIL);
    }

    @Test
    void theResolveClaimantEmailAddress() {
        // when both representative claimant type and claimant types are empty should return null
        CaseData caseData = new CaseData();
        assertThat(ClaimantUtils.resolveClaimantEmailAddress(caseData)).isNull();
        // when both representative claimant type and claimant types not have email address should return null
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().build();
        caseData.setRepresentativeClaimantType(claimantRepresentative);
        ClaimantType claimantType = new ClaimantType();
        caseData.setClaimantType(claimantType);
        assertThat(ClaimantUtils.resolveClaimantEmailAddress(caseData)).isNull();
        // when claimant has email address should return that email address
        caseData.getClaimantType().setClaimantEmailAddress(CLAIMANT_EMAIL);
        assertThat(ClaimantUtils.resolveClaimantEmailAddress(caseData)).isEqualTo(CLAIMANT_EMAIL);
        // when claimant representative has email address should return that email address
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL);
        assertThat(ClaimantUtils.resolveClaimantEmailAddress(caseData)).isEqualTo(CLAIMANT_REPRESENTATIVE_EMAIL);
    }
}
