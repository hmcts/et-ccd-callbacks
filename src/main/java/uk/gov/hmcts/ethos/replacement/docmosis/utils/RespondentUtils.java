package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RespondentUtils {

    private static final String YES = "Yes";

    private RespondentUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Marks a respondent as having their representative removed within the given {@link CaseData}.
     *
     * <p>This method locates the first respondent in the respondent collection whose
     * {@code respondentName} matches the name provided in the
     * {@link UpdateRespondentRepresentativeRequest}. If such a respondent is found,
     * their {@code representativeRemoved} field is set to {@code YES}.</p>
     *
     * <p>Key points:</p>
     * <ul>
     *   <li>If {@code caseData} or {@code updateReq} is {@code null} or empty, no action is taken.</li>
     *   <li>If the {@code respondentName} in {@code updateReq} is blank, no action is taken.</li>
     *   <li>If the respondent collection in {@code caseData} is {@code null} or empty, no action is taken.</li>
     *   <li>Only respondents with a non-null value and a non-blank {@code respondentName} are considered.</li>
     *   <li>The first matching respondent found will be updated; others (if any) are ignored.</li>
     * </ul>
     *
     * @param caseData   the {@link CaseData} object containing the respondent collection
     * @param updateReq  the {@link UpdateRespondentRepresentativeRequest} containing the target respondent name
     */
    public static void markRespondentRepresentativeRemoved(
            CaseData caseData, UpdateRespondentRepresentativeRequest updateReq) {

        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(updateReq)) {
            return;
        }

        final String name = updateReq.getRespondentName();
        if (StringUtils.isBlank(name)) {
            return;
        }

        final List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        if (CollectionUtils.isEmpty(respondents)) {
            return;
        }

        respondents.stream()
                .filter(Objects::nonNull)
                .map(RespondentSumTypeItem::getValue)
                .filter(Objects::nonNull)
                .filter(r -> StringUtils.isNotBlank(r.getRespondentName()))
                .filter(r -> name.equals(r.getRespondentName()))
                .findFirst()
                .ifPresent(r -> r.setRepresentativeRemoved(YES));
    }

    /**
     * Retrieves a list of respondent names from the {@link CaseData} object based on
     * the provided list of Notice of Change (NoC) answer indexes.
     * <p>
     * For each index in {@code indexList}, this method attempts to fetch the corresponding
     * {@link NoticeOfChangeAnswers} instance from {@code caseData} using
     * {@code getNoticeOfChangeAnswersByIndex(int)}. If the instance exists and is not empty,
     * its respondent name is extracted and added to the result list.
     * </p>
     *
     * <p>This method is useful when multiple NoC answer entries are associated with
     * different respondents, and you need to retrieve their names collectively
     * based on their position indexes.</p>
     *
     * @param caseData  the {@link CaseData} object containing the Notice of Change answer entries
     * @param indexList the list of integer indexes representing the target NoC answer entries
     * @return a list of respondent names corresponding to the provided indexes;
     *         an empty list if none of the indexes map to valid answers
     *
     * @see NoticeOfChangeAnswers
     * @see #getNoticeOfChangeAnswersByIndex(CaseData, int)
     */
    public static List<String> getRespondentNamesByNoticeOfChangeIndexes(CaseData caseData,
                                                                         List<Integer> indexList) {
        List<String> respondentNames = new ArrayList<>();
        for (int i : indexList) {
            NoticeOfChangeAnswers answers = getNoticeOfChangeAnswersByIndex(caseData, i);
            if (ObjectUtils.isNotEmpty(answers)) {
                respondentNames.add(answers.getRespondentName());
            }
        }
        return respondentNames;
    }

    /**
     * Retrieves the {@link NoticeOfChangeAnswers} instance from the given {@link CaseData}
     * based on the provided index value.
     * <p>
     * This method serves as a lookup utility for accessing one of the indexed
     * Notice of Change (NoC) answer fields within {@code CaseData}, where each
     * field corresponds to a fixed respondent slot (e.g., {@code getNoticeOfChangeAnswers0()},
     * {@code getNoticeOfChangeAnswers1()}, etc.).
     * </p>
     *
     * <p>If the specified index is outside the valid range (0–9), or if the corresponding
     * field is not present, this method returns {@code null}.</p>
     *
     * @param caseData the {@link CaseData} object containing multiple
     *                 {@link NoticeOfChangeAnswers} fields
     * @param index    the numeric index of the NoC answer field to retrieve (expected range 0–9)
     * @return the {@link NoticeOfChangeAnswers} instance at the specified index,
     *         or {@code null} if the index is invalid or the field is not populated
     *
     * @see CaseData
     * @see NoticeOfChangeAnswers
     */
    public static NoticeOfChangeAnswers getNoticeOfChangeAnswersByIndex(CaseData caseData, int index) {
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
}
