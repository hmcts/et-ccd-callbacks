package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ClaimantUtils;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
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
     * Determines whether the claimant representative's email address matches
     * any valid respondent representative's email address.
     *
     * <p>The method retrieves the claimant representative's email from the case data
     * and compares it against the email addresses of respondent representatives,
     * considering only those deemed valid.</p>
     *
     * <p>If the claimant representative details are missing or the email address
     * is blank, the method returns {@code false}.</p>
     *
     * <p><strong>Assumptions:</strong> It is assumed that {@code caseData} is not
     * {@code null} and that it contains a respondent representative collection.
     * These values are not explicitly validated within this method.</p>
     *
     * @param caseData the {@link CaseData} containing claimant and respondent
     *                 representative information; assumed to be non-null and
     *                 populated with a respondent collection
     * @return {@code true} if the claimant representative's email matches any
     *         valid respondent representative email; {@code false} otherwise
     */
    public static boolean isClaimantRepresentativeEmailMatchedWithRespondents(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData.getRepresentativeClaimantType())
                || StringUtils.isBlank(caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress())) {
            return false;
        }
        String claimantEmail = caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress();

        return caseData.getRepCollection().stream()
                .filter(RespondentRepresentativeUtils::isValidRepresentative)
                .map(item -> item.getValue().getRepresentativeEmailAddress())
                .anyMatch(claimantEmail::equals);
    }

    /**
     * Updates the given {@link CaseData} to reflect that the claimant
     * is no longer represented.
     *
     * <p>This method performs the following state changes:
     * <ul>
     *     <li>Clears the claimant representative type.</li>
     *     <li>Marks the claimant representative as removed.</li>
     *     <li>Sets the claimant represented question flag to {@code NO}.</li>
     *     <li>Resets the claimant representative organisation policy with the
     *         {@link ClaimantSolicitorRole#CLAIMANTSOLICITOR} case role label.</li>
     * </ul>
     *
     * <h3>Assumptions</h3>
     * <ul>
     *     <li>{@code caseData} is not {@code null}.</li>
     *     <li>The {@code YES} and {@code NO} constants are valid flag values
     *         expected by the {@link CaseData} model.</li>
     * </ul>
     *
     * <p>If {@code caseData} is {@code null}, a {@link NullPointerException}
     * will be thrown.
     *
     * @param caseData the case data to update to an unrepresented claimant state;
     *                 must not be {@code null}
     */
    public static void markClaimantAsUnrepresented(CaseData caseData) {
        caseData.setRepresentativeClaimantType(null);
        caseData.setClaimantRepresentativeRemoved(YES);
        caseData.setClaimantRepresentedQuestion(NO);
        caseData.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
    }
}
