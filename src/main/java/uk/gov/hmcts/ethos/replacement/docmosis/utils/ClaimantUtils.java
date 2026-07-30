package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CLAIMANT_NOT_FOUND;

public final class ClaimantUtils {

    private ClaimantUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Retrieves the claimant email address from the provided case data.
     *
     * <p>If the case data or claimant type is missing, a {@link NotFoundException}
     * is thrown. If the claimant email address is {@code null} or empty, an empty
     * string is returned.</p>
     *
     * <p>Assumptions:</p>
     * <ul>
     *     <li>The claimant email address is held under {@code caseData.getClaimantType()}.</li>
     *     <li>A missing {@code CaseData} or claimant type means the claimant details are not available.</li>
     *     <li>A {@code null} or empty claimant email address is not treated as an exception and is returned as an
     *     empty string.</li>
     *     <li>This method only retrieves the email address; it does not validate the email format or check portal
     *     access.</li>
     * </ul>
     *
     * @param caseData the case data containing claimant information
     * @return the claimant email address, or an empty string if it is not present
     * @throws NotFoundException if the case data or claimant type is missing
     */
    public static String getClaimantEmailAddress(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)
                || ObjectUtils.isEmpty(caseData.getClaimantType())) {
            throw new NotFoundException(EXCEPTION_CLAIMANT_NOT_FOUND);
        }
        String claimantEmailAddress = caseData.getClaimantType().getClaimantEmailAddress();
        return StringUtils.isBlank(claimantEmailAddress) ? StringUtils.EMPTY : claimantEmailAddress;
    }

    public static String getClaimant(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)
                || StringUtils.isBlank(caseData.getClaimant())) {
            return StringUtils.EMPTY;
        }
        return caseData.getClaimant();
    }
}
