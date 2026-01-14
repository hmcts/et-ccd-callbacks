package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.util.List;
import java.util.stream.IntStream;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.MAX_NOC_ANSWERS;

public final class RoleUtils {

    private RoleUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Determines whether the given role represents a valid respondent solicitor role.
     * <p>
     * A role is considered a respondent representative role if it is non-blank and
     * can be successfully mapped to a {@link SolicitorRole}.
     * </p>
     *
     * @param role the role identifier to evaluate
     * @return {@code true} if the role is non-blank and corresponds to a respondent
     *         solicitor role; {@code false} otherwise
     */
    public static boolean isRespondentRepresentativeRole(String role) {
        return StringUtils.isNotBlank(role) && SolicitorRole.from(role).isPresent();
    }

    /**
     * Determines whether the given role represents a claimant's legal representative.
     * <p>
     * A role is considered a claimant representative role if it is not blank and
     * matches the case role label defined for {@link ClaimantSolicitorRole#CLAIMANTSOLICITOR}.
     *
     * @param role the role string to evaluate; may be {@code null} or blank
     * @return {@code true} if the role is non-blank and equals the claimant solicitor
     *         case role label, otherwise {@code false}
     */
    public static boolean isClaimantRepresentativeRole(String role) {
        return StringUtils.isNotBlank(role) && ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel().equals(role);
    }

    /**
     * Determines whether the given role is a valid legal representative role.
     * <p>
     * A role is considered valid if it represents either a claimant's legal
     * representative or a respondent's legal representative.
     *
     * @param role the role string to evaluate; may be {@code null} or blank
     * @return {@code true} if the role matches a claimant or respondent
     *         representative role, otherwise {@code false}
     */
    public static boolean isValidRole(String role) {
        return  isClaimantRepresentativeRole(role) || isRespondentRepresentativeRole(role);
    }

    /**
     * Finds the index of the first Notice of Change answer associated with the given
     * respondent name.
     * <p>
     * The respondent name is compared using a case-insensitive, null-safe comparison.
     * The search is performed in index order over the Notice of Change answers derived
     * from the supplied {@link CaseData}.
     * </p>
     *
     * <p>
     * If the respondent name is blank, the case data is {@code null}, or no matching
     * Notice of Change answer is found, this method returns {@code -1}.
     * </p>
     *
     * @param caseData the case data containing Notice of Change answers
     * @param respondentName the respondent name to match against
     * @return the zero-based index of the matching Notice of Change answer, or
     *         {@code -1} if no match is found or the input is invalid
     */
    public static int findSolicitorRoleIndexByRespondentName(CaseData caseData, String respondentName) {
        if (ObjectUtils.isEmpty(caseData) || StringUtils.isBlank(respondentName)) {
            return NumberUtils.INTEGER_MINUS_ONE;
        }
        List<NoticeOfChangeAnswers> noticeOfChangeAnswers = getNoticeOfChangeAnswers(caseData);
        for (int i = 0; i < noticeOfChangeAnswers.size(); i++) {
            var answer = noticeOfChangeAnswers.get(i);
            if (ObjectUtils.isNotEmpty(answer) && Strings.CI.equals(answer.getRespondentName(), respondentName)) {
                return i;
            }
        }
        return NumberUtils.INTEGER_MINUS_ONE;
    }

    /**
     * Returns a fixed list of {@link NoticeOfChangeAnswers} extracted from the given {@link CaseData}.
     * <p>
     * The list contains entries for notice of change answers indexed from 0 to 9. If any individual
     * answer in {@code CaseData} is {@code null} or empty, it is replaced with a default
     * {@link NoticeOfChangeAnswers} instance.
     * </p>
     *
     * @param caseData the case data containing notice of change answer fields
     * @return an immutable list of {@link NoticeOfChangeAnswers} with guaranteed non-null entries
     */
    public static List<NoticeOfChangeAnswers> getNoticeOfChangeAnswers(CaseData caseData) {
        return List.of(
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers0()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers1()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers2()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers3()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers4()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers5()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers6()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers7()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers8()),
                defaultAnswerIfEmpty(caseData.getNoticeOfChangeAnswers9())
        );
    }

    private static NoticeOfChangeAnswers defaultAnswerIfEmpty(NoticeOfChangeAnswers answer) {
        return ObjectUtils.isNotEmpty(answer)
                ? answer
                : NoticeOfChangeAnswers.builder().build();
    }

    /**
     * Returns the solicitor case role label corresponding to the given
     * Notice of Change (NoC) answer index.
     * <p>
     * The index is expected to be within the valid NoC range
     * ({@code 0} to {@code MAX_NOC_ANSWERS - 1}). If the index is out of range,
     * an empty string is returned.
     * </p>
     *
     * @param index the Notice of Change answer index
     * @return the solicitor case role label for the given index,
     *         or an empty string if the index is invalid
     */
    public static String solicitorRoleLabelForIndex(int index) {
        if (index < 0 || index >= MAX_NOC_ANSWERS) {
            return StringUtils.EMPTY;
        }
        return SolicitorRole.values()[index].getCaseRoleLabel();
    }

    /**
     * Determines the solicitor case role for a represented party.
     * <p>
     * If an explicit role is already provided on the representative, that role
     * is returned. Otherwise, the role is resolved by matching the representative’s
     * respondent name against the Notice of Change answers in the supplied
     * {@link CaseData}.
     * </p>
     * <p>
     * If the representative, its value, the respondent name, or the case data
     * is missing or invalid, an empty string is returned.
     * </p>
     *
     * @param representative the represented party item containing role and respondent details
     * @param caseData the case data used to resolve the role via Notice of Change answers
     * @return the resolved solicitor case role label, or an empty string if the role
     *         cannot be determined
     */
    public static String findRespondentRepresentativeRole(RepresentedTypeRItem representative, CaseData caseData) {
        if (ObjectUtils.isEmpty(representative) || ObjectUtils.isEmpty(representative.getValue())) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isNotBlank(representative.getValue().getRole())) {
            return representative.getValue().getRole();
        }
        String respondentName = representative.getValue().getRespRepName();
        if (StringUtils.isBlank(respondentName) || ObjectUtils.isEmpty(caseData)) {
            return StringUtils.EMPTY;
        }
        int noticeOfChangeAnswerIndex =
                RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, respondentName);
        return RoleUtils.solicitorRoleLabelForIndex(noticeOfChangeAnswerIndex);
    }

    /**
     * Returns the respondent index associated with the given solicitor case role label.
     * <p>
     * The role label is expected to match one of the CCD solicitor role labels
     * (e.g. {@code "[SOLICITORA]"}, {@code "[SOLICITORB]"}). If the role label does not
     * correspond to any {@link SolicitorRole}, this method returns {@code -1}.
     * </p>
     *
     * @param role the CCD solicitor case role label
     * @return the zero-based respondent index for the solicitor role, or {@code -1}
     *         if the role label is not recognised
     */
    public static int findRoleIndexByRoleLabel(String role) {
        return SolicitorRole.from(role).map(SolicitorRole::getIndex).orElse(NumberUtils.INTEGER_MINUS_ONE);
    }

    /**
     * Retrieves the respondent name for the given respondent index from the Notice of Change answers.
     * <p>
     * The index is expected to be zero-based and within the valid range of Notice of Change answers.
     * If the index is out of range, or if the respondent name is {@code null}, empty, or blank,
     * this method returns an empty string.
     * </p>
     *
     * @param caseData the case data containing the Notice of Change answers
     * @param index the zero-based index of the respondent
     * @return the respondent name for the given index, or an empty string if the index is invalid
     *         or the respondent name is blank
     */
    public static String findRespondentNameByIndex(CaseData caseData, int index) {
        if (index < 0 || index >= MAX_NOC_ANSWERS) {
            return StringUtils.EMPTY;
        }
        String respondentName = getNoticeOfChangeAnswers(caseData).get(index).getRespondentName();
        return StringUtils.isNotBlank(respondentName) ? respondentName : StringUtils.EMPTY;
    }

    /**
     * Finds the respondent name associated with the given role.
     * <p>
     * If the {@code caseData} is {@code null} or empty, or if the supplied {@code role}
     * is {@code null}, empty, or blank, this method returns an empty string.
     *
     * @param caseData the case data containing respondent information
     * @param role the role label used to identify the respondent
     * @return the respondent name for the given role, or an empty string if the input
     *         is invalid or no respondent can be resolved
     */
    public static String findRespondentNameByRole(CaseData caseData, String role) {
        if (ObjectUtils.isEmpty(caseData) || StringUtils.isBlank(role)) {
            return StringUtils.EMPTY;
        }
        int roleIndex = findRoleIndexByRoleLabel(role);
        return findRespondentNameByIndex(caseData, roleIndex);
    }

    /**
     * Removes the respondent organisation policy and Notice of Change answers
     * associated with the given representative.
     * <p>
     * This method determines the respondent role represented by the supplied
     * {@link RepresentedTypeRItem}, resolves the corresponding role index, and
     * clears both the {@link OrganisationPolicy} and {@link NoticeOfChangeAnswers}
     * entries at that index on the provided {@link CaseData}.
     * <p>
     * If the {@code caseData} or {@code representative} is {@code null} or empty,
     * the method performs no action.
     *
     * @param caseData the case data containing respondent organisation policies and
     *                 Notice of Change answers
     * @param representative the representative whose associated organisation policy
     *                       and Notice of Change answers should be removed
     */
    public static void removeOrganisationPolicyAndNocAnswersByRepresentative(CaseData caseData,
                                                                             RepresentedTypeRItem representative) {
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(representative)) {
            return;
        }
        int roleIndex = findRoleIndexByRoleLabel(findRespondentRepresentativeRole(representative, caseData));
        removeOrganisationPolicyByIndex(caseData, roleIndex);
        removeNocAnswersByIndex(caseData, roleIndex);
    }

    /**
     * Removes (clears) the respondent organisation policy at the given index.
     * <p>
     * This method resets the corresponding {@link OrganisationPolicy} field on the
     * supplied {@link CaseData} to an empty instance. If the {@code caseData} is
     * {@code null} or empty, or if the {@code index} is out of range
     * ({@code index < 0} or {@code index >= MAX_NOC_ANSWERS}), the method performs
     * no action.
     *
     * @param caseData the case data containing respondent organisation policies
     * @param index the zero-based index of the respondent organisation policy to remove
     */
    public static void removeOrganisationPolicyByIndex(CaseData caseData, int index) {
        if (ObjectUtils.isEmpty(caseData) || index < 0 || index >= MAX_NOC_ANSWERS) {
            return;
        }
        OrganisationPolicy emptyPolicy = OrganisationPolicy.builder().build();

        switch (index) {
            case 0 -> caseData.setRespondentOrganisationPolicy0(emptyPolicy);
            case 1 -> caseData.setRespondentOrganisationPolicy1(emptyPolicy);
            case 2 -> caseData.setRespondentOrganisationPolicy2(emptyPolicy);
            case 3 -> caseData.setRespondentOrganisationPolicy3(emptyPolicy);
            case 4 -> caseData.setRespondentOrganisationPolicy4(emptyPolicy);
            case 5 -> caseData.setRespondentOrganisationPolicy5(emptyPolicy);
            case 6 -> caseData.setRespondentOrganisationPolicy6(emptyPolicy);
            case 7 -> caseData.setRespondentOrganisationPolicy7(emptyPolicy);
            case 8 -> caseData.setRespondentOrganisationPolicy8(emptyPolicy);
            case 9 -> caseData.setRespondentOrganisationPolicy9(emptyPolicy);
            default -> { /* no-op */ }
        }
    }

    /**
     * Removes (clears) the Notice of Change answers at the given index.
     * <p>
     * This method resets the corresponding {@link NoticeOfChangeAnswers} field
     * on the supplied {@link CaseData} to an empty instance. If the {@code caseData}
     * is {@code null} or empty, or if the {@code index} is out of range
     * ({@code index < 0} or {@code index >= MAX_NOC_ANSWERS}), the method performs
     * no action.
     *
     * @param caseData the case data containing Notice of Change answers
     * @param index the zero-based index of the Notice of Change answers to remove
     */
    public static void removeNocAnswersByIndex(CaseData caseData, int index) {
        if (ObjectUtils.isEmpty(caseData) || index < 0 || index >= MAX_NOC_ANSWERS) {
            return;
        }
        NoticeOfChangeAnswers emptyNoticeOfChangeAnswers = NoticeOfChangeAnswers.builder().build();

        switch (index) {
            case 0 -> caseData.setNoticeOfChangeAnswers0(emptyNoticeOfChangeAnswers);
            case 1 -> caseData.setNoticeOfChangeAnswers1(emptyNoticeOfChangeAnswers);
            case 2 -> caseData.setNoticeOfChangeAnswers2(emptyNoticeOfChangeAnswers);
            case 3 -> caseData.setNoticeOfChangeAnswers3(emptyNoticeOfChangeAnswers);
            case 4 -> caseData.setNoticeOfChangeAnswers4(emptyNoticeOfChangeAnswers);
            case 5 -> caseData.setNoticeOfChangeAnswers5(emptyNoticeOfChangeAnswers);
            case 6 -> caseData.setNoticeOfChangeAnswers6(emptyNoticeOfChangeAnswers);
            case 7 -> caseData.setNoticeOfChangeAnswers7(emptyNoticeOfChangeAnswers);
            case 8 -> caseData.setNoticeOfChangeAnswers8(emptyNoticeOfChangeAnswers);
            case 9 -> caseData.setNoticeOfChangeAnswers9(emptyNoticeOfChangeAnswers);
            default -> { /* no-op */ }
        }
    }

    /**
     * Determines the next available respondent solicitor role label for the given case data.
     *
     * <p>The method evaluates respondent organisation policies in order and returns the
     * case role label of the first respondent solicitor role whose corresponding
     * organisation policy is missing or incomplete.</p>
     *
     * <p>If the case data is {@code null} or empty, or if no available role can be
     * determined, an empty string is returned.</p>
     *
     * <p>The order of evaluation is significant and reflects the predefined allocation
     * sequence of respondent solicitor roles.</p>
     *
     * @param caseData the case data containing respondent organisation policies
     * @return the next available respondent solicitor case role label, or an empty
     *         string if none is available
     */
    public static String getNextAvailableRespondentSolicitorRoleLabel(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)) {
            return StringUtils.EMPTY;
        }
        List<OrganisationPolicy> organisationPolicies = getAllOrganisationPoliciesByCaseData(caseData);
        List<SolicitorRole> respondentRepresentativeRoles = getRespondentSolicitorRoles();
        return IntStream.range(0, organisationPolicies.size())
                .filter(i -> isOrganisationMissing(organisationPolicies.get(i)))
                .mapToObj(i -> respondentRepresentativeRoles.get(i).getCaseRoleLabel())
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    /**
     * Determines whether an organisation is missing from the given organisation policy.
     *
     * <p>An organisation is considered missing if the policy itself is {@code null} or empty,
     * if the organisation is {@code null} or empty, or if the organisation identifier is
     * {@code null}, empty, or blank.</p>
     *
     * @param organisationPolicy the organisation policy to evaluate
     * @return {@code true} if the organisation is missing; {@code false} otherwise
     */
    public static boolean isOrganisationMissing(OrganisationPolicy organisationPolicy) {
        if (ObjectUtils.isEmpty(organisationPolicy) || ObjectUtils.isEmpty(organisationPolicy.getOrganisation())) {
            return true;
        }
        return StringUtils.isBlank(organisationPolicy.getOrganisation().getOrganisationID());
    }

    /**
     * Retrieves all respondent organisation policies from the given case data.
     *
     * <p>This method returns a list containing all respondent organisation policies
     * (indices 0 to 9). Each policy is guaranteed to be non-null; if a policy is
     * missing or empty in the case data, an empty {@link OrganisationPolicy}
     * instance is returned in its place.</p>
     *
     * <p>The order of policies in the returned list corresponds to their index
     * within the case data.</p>
     *
     * @param caseData the case data containing respondent organisation policies
     * @return a list of ten {@link OrganisationPolicy} instances, never {@code null}
     */
    public static List<OrganisationPolicy> getAllOrganisationPoliciesByCaseData(CaseData caseData) {
        return List.of(
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy0()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy1()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy2()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy3()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy4()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy5()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy6()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy7()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy8()),
                ensureOrganisationPolicy(caseData.getRespondentOrganisationPolicy9())
        );
    }

    /**
     * Ensures that an {@link OrganisationPolicy} instance is always returned.
     *
     * <p>If the provided organisation policy is {@code null} or empty, a new
     * empty {@link OrganisationPolicy} instance is created and returned.
     * Otherwise, the provided instance is returned unchanged.</p>
     *
     * @param organisationPolicy the organisation policy to validate
     * @return a non-null {@link OrganisationPolicy} instance
     */
    public static OrganisationPolicy ensureOrganisationPolicy(OrganisationPolicy organisationPolicy) {
        return ObjectUtils.isEmpty(organisationPolicy) ? OrganisationPolicy.builder().build() : organisationPolicy;
    }

    /**
     * Returns all solicitor roles that can represent respondents.
     *
     * <p>The returned list contains all respondent representative solicitor roles
     * in a fixed order (A to J). The list is immutable and will always contain
     * the same set of roles.</p>
     *
     * <p>This method provides a convenient, centralised view of all respondent
     * representative roles.</p>
     *
     * @return an immutable list of respondent representative {@link SolicitorRole}s
     */
    public static List<SolicitorRole> getRespondentSolicitorRoles() {
        return List.of(
                SolicitorRole.SOLICITORA,
                SolicitorRole.SOLICITORB,
                SolicitorRole.SOLICITORC,
                SolicitorRole.SOLICITORD,
                SolicitorRole.SOLICITORE,
                SolicitorRole.SOLICITORF,
                SolicitorRole.SOLICITORG,
                SolicitorRole.SOLICITORH,
                SolicitorRole.SOLICITORI,
                SolicitorRole.SOLICITORJ
        );
    }
}
