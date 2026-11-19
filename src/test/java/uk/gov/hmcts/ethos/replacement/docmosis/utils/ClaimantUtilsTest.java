package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ClaimantUtilsTest {

    private static final String CLAIMANT_EMAIL = "claimant@hmcts.org";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String CLAIMANT_TITLE = "Mr";
    private static final String CLAIMANT_FIRST_NAMES = "Claimant First";
    private static final String CLAIMANT_LASTNAME = "Name";
    private static final String CLAIMANT_FULL_NAME = "Mr Claimant First Name";

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
    void theGetClaimantEmailAddressWithoutException() {
        // when case data does not have claimant should return empty string
        CaseData caseData = new CaseData();
        assertThat(ClaimantUtils.getClaimantEmailAddressWithoutException(caseData)).isEmpty();
        // when case data has claimant email address should return address
        caseData.setClaimantType(new ClaimantType());
        caseData.getClaimantType().setClaimantEmailAddress(CLAIMANT_EMAIL);
        assertThat(ClaimantUtils.getClaimantEmailAddressWithoutException(caseData)).isEqualTo(CLAIMANT_EMAIL);
    }

    @Test
    void theResolveClaimantName() {
        // when claimant ind type and claimant is empty should return empty string
        CaseData caseData = new CaseData();
        assertThat(ClaimantUtils.resolveClaimantName(caseData)).isEmpty();
        // when claimant is not empty should return it
        caseData.setClaimant(CLAIMANT_NAME);
        assertThat(ClaimantUtils.resolveClaimantName(caseData)).isEqualTo(CLAIMANT_NAME);
        // claimant full name is empty should return claimant name
        caseData.setClaimantIndType(new ClaimantIndType());
        assertThat(ClaimantUtils.resolveClaimantName(caseData)).isEqualTo(CLAIMANT_NAME);
        // when claimant full name exists should return claimant full name
        caseData.getClaimantIndType().setClaimantTitle(CLAIMANT_TITLE);
        caseData.getClaimantIndType().setClaimantFirstNames(CLAIMANT_FIRST_NAMES);
        caseData.getClaimantIndType().setClaimantLastName(CLAIMANT_LASTNAME);
        assertThat(ClaimantUtils.resolveClaimantName(caseData)).isEqualTo(CLAIMANT_FULL_NAME);
    }
}
