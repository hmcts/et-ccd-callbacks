package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_RESPONDENT_DETAILS_NOT_EXIST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_RESPONDENT_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_RESPONDENT_NAME_NOT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_RESPONDENT_NOT_FOUND;

public final class RespondentUtils {

    private static final String YES = "Yes";
    private static final String CLASS_NAME = RespondentUtils.class.getSimpleName();
    private static final String VALIDATE_RESPONDENT_METHOD_NAME = "validateRespondent";

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

    /**
     * Validates that the provided {@link RespondentSumTypeItem} contains all required
     * respondent information necessary for Notice of Change (NoC) processing.
     *
     * <p>The following validations are performed in sequence:</p>
     * <ul>
     *     <li>The respondent object is not null or empty</li>
     *     <li>The respondent has a non-blank identifier</li>
     *     <li>The respondent contains a populated value object</li>
     *     <li>The respondent has a non-empty respondent name</li>
     * </ul>
     *
     * <p>If any validation fails, a {@link GenericServiceException} is thrown with a
     * descriptive message, including the supplied case reference number. The exception
     * also includes contextual metadata such as the helper class name and method name,
     * enabling clearer diagnostic logging and tracing.</p>
     *
     * @param respondent            the respondent to validate
     * @param caseReferenceNumber   the case reference number used to enrich error messages
     *
     * @throws GenericServiceException if:
     *     <ul>
     *      <li>the respondent object is null or empty</li>
     *      <li>the respondent ID is blank or missing</li>
     *      <li>the respondent details (value object) are missing</li>
     *      <li>the respondent name is missing</li>
     *     </ul>
     *     A detailed message describing the missing or invalid data will be included
     *     along with contextual identifiers for troubleshooting.
     */
    public static void validateRespondent(RespondentSumTypeItem respondent, String caseReferenceNumber)
            throws GenericServiceException {
        if (ObjectUtils.isEmpty(respondent)) {
            String exceptionMessage = String.format(EXCEPTION_RESPONDENT_NOT_FOUND, caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseReferenceNumber, CLASS_NAME, VALIDATE_RESPONDENT_METHOD_NAME);
        }
        if (StringUtils.isBlank(respondent.getId())) {
            String exceptionMessage = String.format(EXCEPTION_RESPONDENT_ID_NOT_FOUND, caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseReferenceNumber, CLASS_NAME, VALIDATE_RESPONDENT_METHOD_NAME);
        }
        if (ObjectUtils.isEmpty(respondent.getValue())) {
            String exceptionMessage = String.format(EXCEPTION_RESPONDENT_DETAILS_NOT_EXIST,
                    respondent.getId(), caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseReferenceNumber, CLASS_NAME, VALIDATE_RESPONDENT_METHOD_NAME);
        }
        if (ObjectUtils.isEmpty(respondent.getValue().getRespondentName())) {
            String exceptionMessage = String.format(EXCEPTION_RESPONDENT_NAME_NOT_EXISTS, respondent.getId(),
                    caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseReferenceNumber, CLASS_NAME, VALIDATE_RESPONDENT_METHOD_NAME);
        }
    }

    /**
     * Determines whether the given {@link RespondentSumTypeItem} contains valid and usable
     * respondent data.
     * <p>
     * A respondent is considered <em>valid</em> if all the following conditions are met:
     * <ul>
     *     <li>The {@code respondent} object itself is not {@code null}.</li>
     *     <li>The respondent has a non-blank identifier ({@code respondent.getId()}).</li>
     *     <li>The respondent has a non-null {@code value} object.</li>
     *     <li>The respondent's name ({@code respondent.getValue().getRespondentName()}) is non-blank.</li>
     * </ul>
     * <p>
     * This method performs no side effects and does not throw exceptions. It is intended
     * for use in pre-validation checks prior to invoking operations that require a fully
     * populated respondent.
     *
     * @param respondent the respondent to validate
     * @return {@code true} if the respondent contains all mandatory fields; {@code false} otherwise
     */
    public static boolean isValidRespondent(RespondentSumTypeItem respondent) {
        return ObjectUtils.isNotEmpty(respondent)
                && StringUtils.isNotBlank(respondent.getId())
                && ObjectUtils.isNotEmpty(respondent.getValue())
                && StringUtils.isNotBlank(respondent.getValue().getRespondentName());
    }

    /**
     * Determines whether the provided {@link CaseData} contains at least one respondent.
     *
     * <p>This method performs a simple structural check to verify that:</p>
     * <ul>
     *     <li>the {@code caseData} object is not {@code null} or empty, and</li>
     *     <li>the respondent collection within the case data is present and contains
     *         at least one respondent entry.</li>
     * </ul>
     *
     * <p>
     * This method does not validate the contents or structure of individual respondent items;
     * it only checks for their presence.
     * </p>
     *
     * @param caseData the case data to inspect
     * @return {@code true} if the case data contains at least one respondent;
     *         {@code false} otherwise
     */
    public static boolean hasRespondents(CaseData caseData) {
        return ObjectUtils.isNotEmpty(caseData)
                && CollectionUtils.isNotEmpty(caseData.getRespondentCollection());
    }

    /**
     * Finds and returns the name of a respondent with the given respondent ID.
     * <p>
     * The method iterates through the provided list of respondents and returns the
     * respondent name for the first valid respondent whose ID matches the supplied
     * {@code respondentId}. If no matching respondent is found, or if the input list
     * or respondent ID is empty or invalid, an empty string is returned.
     *
     * @param respondents the list of respondents to search
     * @param respondentId the unique identifier of the respondent
     * @return the respondent name if a matching respondent is found; otherwise,
     *         an empty string
     */
    public static RespondentSumTypeItem findRespondentById(
            List<RespondentSumTypeItem> respondents,
            String respondentId) {
        if (CollectionUtils.isEmpty(respondents) || StringUtils.isBlank(respondentId)) {
            return null;
        }
        for (RespondentSumTypeItem respondent : respondents) {
            if (isValidRespondent(respondent) && respondentId.equals(respondent.getId())) {
                return respondent;
            }
        }
        return null;
    }

    /**
     * Finds and returns a respondent with the given respondent name.
     * <p>
     * The method iterates through the provided list of respondents and returns the first
     * valid respondent whose name exactly matches the supplied {@code respondentName}.
     * If the respondents list is empty, the respondent name is blank, or no matching
     * respondent is found, {@code null} is returned.
     *
     * @param respondents the list of respondents to search
     * @param respondentName the name of the respondent to match
     * @return the matching {@link RespondentSumTypeItem}, or {@code null} if no match is found
     */
    public static RespondentSumTypeItem findRespondentByName(
            List<RespondentSumTypeItem> respondents,
            String respondentName) {
        if (CollectionUtils.isEmpty(respondents) || StringUtils.isBlank(respondentName)) {
            return null;
        }
        for (RespondentSumTypeItem respondent : respondents) {
            if (isValidRespondent(respondent) && respondentName.equals(respondent.getValue().getRespondentName())) {
                return respondent;
            }
        }
        return null;
    }

    /**
     * Finds and returns the respondent associated with the given representative ID.
     * <p>
     * The method iterates through the provided list of respondents and returns the first
     * valid respondent whose representative ID matches the supplied {@code representativeId}.
     * If the respondents list is empty, the representative ID is blank, or no matching
     * respondent is found, {@code null} is returned.
     *
     * @param respondents the list of respondents to search
     * @param representativeId the identifier of the representative associated with a respondent
     * @return the matching {@link RespondentSumTypeItem}, or {@code null} if no match is found
     */
    public static RespondentSumTypeItem findRespondentByRepresentativeId(
            List<RespondentSumTypeItem> respondents,
            String representativeId) {
        if (CollectionUtils.isEmpty(respondents)
                || StringUtils.isBlank(representativeId)) {
            return null;
        }
        for (RespondentSumTypeItem respondent : respondents) {
            if (isValidRespondent(respondent) && representativeId.equals(respondent.getValue().getRepresentativeId())) {
                return respondent;
            }
        }
        return null;
    }
}
