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

    /**
     * Determines whether the claimant representative's organisation is linked to
     * any of the respondent representatives' organisations.
     *
     * <p>The method checks both the claimant's direct organisation ID and, if present,
     * the associated MyHMCTS organisation ID. A match is considered valid if either
     * of these IDs equals any non-blank organisation ID in the provided respondent list.</p>
     *
     * <p>If the claimant representative is {@code null}, the respondent organisation
     * list is {@code null} or empty, or both claimant organisation identifiers are
     * blank, the method returns {@code false}.</p>
     *
     * @param claimantRepresentative              the claimant's representation details,
     *                                            containing organisation identifiers
     * @param respondentRepresentativeOrganisationIds
     *                                            a list of organisation IDs associated
     *                                            with respondent representatives
     * @return {@code true} if the claimant organisation ID or MyHMCTS organisation ID
     *         matches any respondent organisation ID; {@code false} otherwise
     */
    public static boolean isClaimantOrganisationLinkedToRespondents(
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

    /**
     * Determines whether the given representative has a valid organisation identifier.
     *
     * <p>This method returns {@code true} if the representative is not {@code null} and
     * at least one of the following conditions is met:
     * <ul>
     *     <li>The representative has a non-null MyHMCTS organisation with a non-blank organisation ID, or</li>
     *     <li>The representative has a non-blank direct organisation ID.</li>
     * </ul>
     * </p>
     *
     * <p>If the representative is {@code null}, or both organisation identifiers are
     * absent or blank, the method returns {@code false}.</p>
     *
     * @param representative the {@link RepresentedTypeC} instance to evaluate
     * @return {@code true} if a valid organisation identifier is present;
     *         {@code false} otherwise
     */
    public static boolean hasOrganisationIdentifier(RepresentedTypeC representative) {
        return ObjectUtils.isNotEmpty(representative)
                && (ObjectUtils.isNotEmpty(representative.getMyHmctsOrganisation())
                && StringUtils.isNotBlank(representative.getMyHmctsOrganisation().getOrganisationID())
                || StringUtils.isNotBlank(representative.getOrganisationId()));
    }
}
