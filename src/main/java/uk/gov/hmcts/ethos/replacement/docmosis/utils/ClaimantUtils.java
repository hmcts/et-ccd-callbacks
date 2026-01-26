package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CLAIMANT_EMAIL_NOT_FOUND;

public final class ClaimantUtils {

    private ClaimantUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Retrieves the claimant email address from the given case data.
     * <p>
     * If the claimant email address is not present or is blank, this method returns
     * an empty string. If the case data or claimant details are missing, a
     * {@link NotFoundException} is thrown.
     * </p>
     *
     * @param caseData the case data containing claimant details
     * @return the claimant email address, or an empty string if no email address is provided
     * @throws NotFoundException if the case data or claimant information is not available
     */
    public static String getClaimantEmailAddress(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)
                || ObjectUtils.isEmpty(caseData.getClaimantType())
                || StringUtils.isBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            throw new NotFoundException(EXCEPTION_CLAIMANT_EMAIL_NOT_FOUND);
        }
        return caseData.getClaimantType().getClaimantEmailAddress();
    }

}
