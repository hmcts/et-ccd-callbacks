package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.WARNING_CLAIMANT_EMAIL_NOT_FOUND;

@Slf4j
public final class ClaimantRepresentativeUtils {

    private ClaimantRepresentativeUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Resolves the email address to be used for claimant Notice of Change (NoC) notifications.
     * <p>
     * The method first attempts to return the claimant representative’s email address,
     * if a representative is present and an email address is provided. If no representative
     * email is available, it falls back to the claimant’s own email address held in the
     * case data.
     * </p>
     * <p>
     * The method never throws an exception and never returns {@code null}. If the case
     * details are incomplete or no claimant email address can be resolved, an empty
     * string is returned and a warning is logged.
     * </p>
     *
     * @param caseDetails the case details containing claimant and representative information
     * @return the resolved claimant email address for NoC notifications, or an empty
     *         string if no email address is available
     */
    public static String getClaimantNocNotificationEmail(CaseDetails caseDetails) {
        if (ObjectUtils.isEmpty(caseDetails)
                || ObjectUtils.isEmpty(caseDetails.getCaseData())) {
            String caseId = ObjectUtils.isEmpty(caseDetails) ? StringUtils.EMPTY : caseDetails.getCaseId();
            log.warn(WARNING_CLAIMANT_EMAIL_NOT_FOUND, caseId);
            return StringUtils.EMPTY;
        }
        if (ObjectUtils.isNotEmpty(caseDetails.getCaseData().getRepresentativeClaimantType())
                && StringUtils.isNotBlank(caseDetails.getCaseData().getRepresentativeClaimantType()
                .getRepresentativeEmailAddress())) {
            return caseDetails.getCaseData().getRepresentativeClaimantType().getRepresentativeEmailAddress();
        }
        String email = StringUtils.EMPTY;
        try {
            email = ClaimantUtils.getClaimantEmailAddress(caseDetails.getCaseData());
        } catch (NotFoundException e) {
            log.warn(e.getMessage());
        }
        return email;
    }

    public static boolean isClaimantRepresentativeOrganisationInRespondentOrganisations(
            RepresentedTypeC claimantRepresentative,
            List<String> respondentRepresentativeOrganisationIds) {
        if (ObjectUtils.isEmpty(claimantRepresentative)
                || CollectionUtils.isEmpty(respondentRepresentativeOrganisationIds)) {
            return false;
        }
        String claimantOrgId = claimantRepresentative.getOrganisationId();
        String myHmctsOrgId = claimantRepresentative.getMyHmctsOrganisation() != null
                ? claimantRepresentative.getMyHmctsOrganisation().getOrganisationID()
                : null;

        if (StringUtils.isBlank(claimantOrgId) && StringUtils.isBlank(myHmctsOrgId)) {
            return false;
        }
        return respondentRepresentativeOrganisationIds.stream()
                .filter(StringUtils::isNotBlank)
                .anyMatch(id -> id.equals(claimantOrgId) || id.equals(myHmctsOrgId));
    }
}
