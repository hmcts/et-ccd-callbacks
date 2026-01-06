package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.util.List;

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

    public static String findRespondentNameByRole(CaseData caseData, String role) {
        if (ObjectUtils.isEmpty(caseData) || StringUtils.isBlank(role)) {
            return StringUtils.EMPTY;
        }
        int roleIndex = findRoleIndexByRoleLabel(role);
        return RoleUtils.findRespondentNameByIndex(caseData, roleIndex);
    }
}
