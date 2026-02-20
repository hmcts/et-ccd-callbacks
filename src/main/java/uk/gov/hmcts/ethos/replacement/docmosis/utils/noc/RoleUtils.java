package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;

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
        for (int i = 0; i < MAX_NOC_ANSWERS; i++) {
            NoticeOfChangeAnswers answer = getNoticeOfChangeAnswersAtIndex(caseData, i);
            if (ObjectUtils.isNotEmpty(answer) && Strings.CI.equals(answer.getRespondentName(), respondentName)) {
                return i;
            }
        }
        // Checks in respondents
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return NumberUtils.INTEGER_MINUS_ONE;
        }
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        for (int i = 0; i < respondents.size(); i++) {
            RespondentSumTypeItem respondent = respondents.get(i);
            if (!RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }
            if (Strings.CI.equals(respondent.getValue().getRespondentName(), respondentName)) {
                return i;
            }
        }
        return NumberUtils.INTEGER_MINUS_ONE;
    }

    /**
     * Returns the Notice of Change answers associated with the given index
     * from the provided case data.
     *
     * <p>The index corresponds to the numbered Notice of Change answer fields
     * on the case data (e.g. {@code noticeOfChangeAnswers0} through
     * {@code noticeOfChangeAnswers9}).</p>
     *
     * <p>If the case data is null, or if the index is outside the supported
     * range (0 to {@code MAX_NOC_ANSWERS - 1}), the method returns {@code null}.</p>
     *
     * @param caseData the case data containing Notice of Change answer fields
     * @param index the zero-based index identifying which Notice of Change
     *              answers to retrieve
     * @return the Notice of Change answers for the given index, or {@code null}
     *         if the input is invalid or no answers exist at that index
     */
    public static NoticeOfChangeAnswers getNoticeOfChangeAnswersAtIndex(CaseData caseData, int index) {
        if (ObjectUtils.isEmpty(caseData) || index < 0 || index >= MAX_NOC_ANSWERS) {
            return null;
        }
        return switch (index) {
            case 0 -> caseData.getNoticeOfChangeAnswers0();
            case 1 -> caseData.getNoticeOfChangeAnswers1();
            case 2 -> caseData.getNoticeOfChangeAnswers2();
            case 3 -> caseData.getNoticeOfChangeAnswers3();
            case 4 -> caseData.getNoticeOfChangeAnswers4();
            case 5 -> caseData.getNoticeOfChangeAnswers5();
            case 6 -> caseData.getNoticeOfChangeAnswers6();
            case 7 -> caseData.getNoticeOfChangeAnswers7();
            case 8 -> caseData.getNoticeOfChangeAnswers8();
            case 9 -> caseData.getNoticeOfChangeAnswers9();
            default -> null;
        };
    }

    /**
     * Determines whether the given Notice of Change answers are valid.
     *
     * <p>A Notice of Change answer is considered valid if it is not {@code null}
     * and has a non-blank respondent name.</p>
     *
     * @param answer the Notice of Change answers to validate
     * @return {@code true} if the answers are valid; {@code false} otherwise
     */
    public static boolean isValidNoticeOfChangeAnswers(NoticeOfChangeAnswers answer) {
        return answer != null && StringUtils.isNotBlank(answer.getRespondentName());
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
        int noticeOfChangeAnswerIndex = findSolicitorRoleIndexByRespondentName(caseData, respondentName);
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
        if (ObjectUtils.isEmpty(caseData) || index < 0 || index >= MAX_NOC_ANSWERS) {
            return StringUtils.EMPTY;
        }
        NoticeOfChangeAnswers answers = RoleUtils.getNoticeOfChangeAnswersAtIndex(caseData, index);
        if (ObjectUtils.isNotEmpty(answers) && StringUtils.isNotBlank(answers.getRespondentName())) {
            return answers.getRespondentName();
        }
        RespondentSumTypeItem respondent = RespondentUtils.getRespondentAtIndex(caseData, index);
        return ObjectUtils.isNotEmpty(respondent)
                && ObjectUtils.isNotEmpty(respondent.getValue())
                && StringUtils.isNotBlank(respondent.getValue().getRespondentName())
                ? respondent.getValue().getRespondentName()
                : StringUtils.EMPTY;
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
     * Resets the respondent organisation policy associated with the given representative.
     * <p>
     * The method determines the solicitor role for the provided representative within the
     * supplied {@link CaseData}, resolves the corresponding organisation policy index, and
     * delegates to {@link #resetOrganisationPolicyByIndex(CaseData, int)} to perform the reset.
     * <p>
     * If {@code caseData} or {@code representative} is {@code null} or empty, the method
     * performs no action.
     *
     * @param caseData       the case data containing respondent organisation policies to reset
     * @param representative the representative whose associated organisation policy should be reset
     */
    public static void resetOrganisationPolicyByRepresentative(CaseData caseData,
                                                               RepresentedTypeRItem representative) {
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(representative)) {
            return;
        }
        int roleIndex = findRoleIndexByRoleLabel(findRespondentRepresentativeRole(representative, caseData));
        resetOrganisationPolicyByIndex(caseData, roleIndex);
    }

    /**
     * Resets the respondent organisation policy on the given {@link CaseData} for the specified index.
     * <p>
     * The method initialises the corresponding respondent organisation policy field (0–9) with:
     * <ul>
     *   <li>a predefined solicitor case role based on the index</li>
     *   <li>an empty {@link Organisation} instance</li>
     * </ul>
     * <p>
     * If {@code caseData} is {@code null} or empty, or if the index is out of bounds
     * (less than 0 or greater than or equal to {@code MAX_NOC_ANSWERS}), the method
     * performs no action.
     *
     * @param caseData the case data containing respondent organisation policies to reset
     * @param index    the zero-based index identifying which respondent organisation policy to reset
     */
    public static void resetOrganisationPolicyByIndex(CaseData caseData, int index) {
        if (ObjectUtils.isEmpty(caseData) || index < 0 || index >= MAX_NOC_ANSWERS) {
            return;
        }

        switch (index) {
            case 0 -> caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORA.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 1 -> caseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORB.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 2 -> caseData.setRespondentOrganisationPolicy2(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORC.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 3 -> caseData.setRespondentOrganisationPolicy3(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORD.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 4 -> caseData.setRespondentOrganisationPolicy4(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORE.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 5 -> caseData.setRespondentOrganisationPolicy5(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORF.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 6 -> caseData.setRespondentOrganisationPolicy6(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORG.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 7 -> caseData.setRespondentOrganisationPolicy7(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORH.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 8 -> caseData.setRespondentOrganisationPolicy8(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORI.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            case 9 -> caseData.setRespondentOrganisationPolicy9(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                    SolicitorRole.SOLICITORJ.getCaseRoleLabel()).organisation(Organisation.builder().build()).build());
            default -> { /* no-op */ }
        }
    }

    /**
     * Derives the solicitor role label to be assigned to a representative
     * based on the respondent they are associated with.
     *
     * <p>The method determines the index of the respondent identified by the
     * representative’s respondent ID within the case data and uses that index
     * to generate the corresponding solicitor role label.</p>
     *
     * <p>If the case data is null, the respondent collection is empty, the
     * representative value is missing, or the respondent ID is blank, an
     * empty string is returned.</p>
     *
     * @param caseData the case data containing the respondent collection
     * @param representative the representative for whom the solicitor role
     *                       is being derived
     * @return the solicitor role label to assign, or an empty string if the
     *         role cannot be derived due to invalid or missing input
     */
    public static String deriveSolicitorRoleToAssign(CaseData caseData, RepresentedTypeRItem representative) {
        if (ObjectUtils.isEmpty(caseData)
                || ObjectUtils.isEmpty(caseData.getRespondentCollection())
                || ObjectUtils.isEmpty(representative)
                || ObjectUtils.isEmpty(representative.getValue())
                || StringUtils.isBlank(representative.getValue().getRespondentId())) {
            return StringUtils.EMPTY;
        }
        int roleIndex = RespondentUtils.getRespondentIndexById(caseData, representative.getValue().getRespondentId());
        return RoleUtils.solicitorRoleLabelForIndex(roleIndex);
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
}
