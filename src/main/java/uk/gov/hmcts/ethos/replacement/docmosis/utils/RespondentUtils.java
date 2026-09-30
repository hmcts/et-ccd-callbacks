package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;
import uk.gov.hmcts.reform.et.syaapi.service.utils.NoticeOfChangeUtils;

import java.util.Collections;
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
     * Returns the index of a respondent in the case data respondent collection
     * that matches the given respondent ID.
     *
     * <p>The method iterates through the respondent collection and returns the
     * index of the first valid respondent whose ID matches the provided
     * {@code respondentId}.</p>
     *
     * <p>If the case data is null, the respondent collection is empty, the
     * respondent ID is blank, or no matching respondent is found, the method
     * returns {@code -1}.</p>
     *
     * @param caseData the case data containing the respondent collection
     * @param respondentId the unique identifier of the respondent to locate
     * @return the zero-based index of the matching respondent, or {@code -1}
     *         if no matching respondent is found or the input is invalid
     */
    public static int getRespondentIndexById(CaseData caseData, String respondentId) {
        if (ObjectUtils.isEmpty(caseData)
                || CollectionUtils.isEmpty(caseData.getRespondentCollection())
                || StringUtils.isBlank(respondentId)) {
            return NumberUtils.INTEGER_MINUS_ONE;
        }
        for (int i = 0; i < caseData.getRespondentCollection().size(); i++) {
            RespondentSumTypeItem respondent = caseData.getRespondentCollection().get(i);
            if (isValidRespondent(respondent) && respondentId.equals(respondent.getId())) {
                return i;
            }
        }
        return NumberUtils.INTEGER_MINUS_ONE;
    }

    /**
     * Determines the expected solicitor role for the respondent identified by the given respondent ID.
     * <p>
     * The respondent's index is resolved from the case data and then used to obtain
     * the corresponding solicitor role label.
     *
     * @param caseData the case data containing the respondent information
     * @param respondentId the ID of the respondent whose expected role is required
     * @return the solicitor role label associated with the respondent
     */
    public static String determineExpectedRoleByRespondentId(CaseData caseData, String respondentId) {
        return RoleUtils.solicitorRoleLabelForIndex(getRespondentIndexById(caseData, respondentId));
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

    /**
     * Returns the respondent at the specified index from the case data respondent collection.
     *
     * <p>The method performs bounds and null checks before accessing the collection.
     * If the case data is null, the respondent collection is empty, or the provided
     * index is out of range, the method returns {@code null}.</p>
     *
     * @param caseData the case data containing the respondent collection
     * @param index the zero-based index of the respondent to retrieve
     * @return the respondent at the given index, or {@code null} if the input is
     *         invalid or the index is out of bounds
     */
    public static RespondentSumTypeItem getRespondentAtIndex(CaseData caseData, int index) {
        if (ObjectUtils.isEmpty(caseData)
                || CollectionUtils.isEmpty(caseData.getRespondentCollection())
                || index < 0
                || index >= caseData.getRespondentCollection().size()) {
            return null;
        }
        return caseData.getRespondentCollection().get(index);
    }

    /**
     * Returns the valid respondents associated with the supplied case data.
     * <p>
     * If the case data is {@code null} or contains no respondents, an empty list
     * is returned. Respondents that do not satisfy the validation criteria defined
     * by {@code isValidRespondent(...)} are excluded from the result.
     * </p>
     *
     * @param caseData the case data containing the respondent collection
     * @return a list of valid respondents, or an empty list if none are available
     */
    public static List<RespondentSumTypeItem> getValidRespondents(CaseData caseData) {
        if (caseData == null || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return Collections.emptyList();
        }
        return caseData.getRespondentCollection().stream()
                .filter(RespondentUtils::isValidRespondent)
                .toList();
    }

    /**
     * Retrieves the respondent name associated with the specified role from the case data.
     *
     * <p>The method determines the index of the given role using
     * {@link RoleUtils#findRoleIndexByRoleLabel(String)} and then retrieves the corresponding
     * {@link NoticeOfChangeAnswers} from the {@link CaseData}. If a matching entry exists and
     * contains a respondent name, that name is returned.</p>
     *
     * <p>If the role cannot be resolved to a valid index, or if the corresponding
     * {@link NoticeOfChangeAnswers} object or respondent name is empty, the method returns {@code null}.</p>
     *
     * @param caseData the case data containing notice of change answers
     * @param role the role label used to locate the respondent
     * @return the respondent name associated with the given role, or {@code null} if the role is invalid
     *         or no respondent name is available
     */
    public static String findRespondentNameByRole(CaseData caseData, String role) {
        int roleIndex = RoleUtils.findRoleIndexByRoleLabel(role);
        if (roleIndex == -1) {
            return null;
        }
        NoticeOfChangeAnswers noticeOfChangeAnswers = NoticeOfChangeUtils
                .getNoticeOfChangeAnswersAtIndex(caseData, roleIndex);
        if (ObjectUtils.isEmpty(noticeOfChangeAnswers)
                || ObjectUtils.isEmpty(noticeOfChangeAnswers.getRespondentName())) {
            return null;
        }
        return noticeOfChangeAnswers.getRespondentName();
    }

    /**
     * Determines whether the given respondent contains sufficient information
     * to be used when looking up a representative.
     * <p>
     * The respondent is considered to have representative lookup criteria when
     * it has a non-null value and at least one of the following is present:
     * the respondent ID, representative ID, or respondent name.
     *
     * @param respondent the respondent to inspect
     * @return {@code true} if representative lookup criteria are available;
     *         {@code false} otherwise
     */
    public static boolean hasRepresentativeLookupData(RespondentSumTypeItem respondent) {
        return respondent != null
                && (StringUtils.isNotBlank(respondent.getId())
                || (respondent.getValue() != null
                && (StringUtils.isNotBlank(respondent.getValue().getRepresentativeId())
                || StringUtils.isNotBlank(respondent.getValue().getRespondentName()))));
    }
}
