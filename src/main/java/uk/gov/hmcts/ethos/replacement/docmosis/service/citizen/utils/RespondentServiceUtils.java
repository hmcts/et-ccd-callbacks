package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MapperUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NoticeOfChangeUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_EMPTY_RESPONDENT_COLLECTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_INVALID_RESPONDENT_INDEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_RESPONDENT_NOT_EXISTS;

public final class RespondentServiceUtils {

    private RespondentServiceUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Retrieves the {@link RespondentSolicitorType} for a specified respondent within a given case.
     *
     * <p>This method validates the provided {@link CaseDetails} and respondent index before attempting
     * to extract the respondent’s solicitor information. It performs several checks to ensure data
     * integrity, such as verifying the existence of case data, respondent collection, and the validity
     * of the respondent index. If any validation fails, a {@link CallbacksRuntimeException} is thrown
     * with an appropriate message.</p>
     *
     * <p><b>Validation steps include:</b></p>
     * <ul>
     *   <li>Ensures {@code caseDetails} is not null or empty.</li>
     *   <li>Checks that {@code caseDetails.getData()} contains valid case data.</li>
     *   <li>Validates that {@code respondentIndex} is not blank, numeric, and non-negative.</li>
     *   <li>Ensures the respondent collection in {@code caseData} is not empty.</li>
     *   <li>Checks that {@code respondentIndex} is within the bounds of the respondent collection.</li>
     *   <li>Validates that the respondent exists and has a non-empty name.</li>
     *   <li>Finds the corresponding Notice of Change (NoC) answer index for the respondent.</li>
     *   <li>Retrieves and validates the {@link RespondentSolicitorType} using the NoC answer index.</li>
     * </ul>
     *
     * <p>If all validations pass, the method returns the {@link RespondentSolicitorType} associated
     * with the respondent at the specified index.</p>
     *
     * @param caseDetails     the {@link CaseDetails} object containing the case data and respondent information
     * @param respondentIndex the index of the respondent whose solicitor type is to be retrieved;
     *                        must be a non-negative integer within the bounds of the respondent collection
     * @return the {@link RespondentSolicitorType} for the respondent at the specified index
     *
     * @throws CallbacksRuntimeException if:
     *      <ul>
         *      <li>{@code caseDetails} or its data is null or empty;</li>
         *      <li>{@code respondentIndex} is blank, non-numeric, or out of range;</li>
         *      <li>the respondent collection is empty or the respondent at the given index does not exist;</li>
         *      <li>the respondent has no associated Notice of Change answer;</li>
         *      <li>the solicitor type could not be found for the given respondent.</li>
     *      </ul>
     *
     * @see MapperUtils#convertCaseDataMapToCaseDataObject(Map)
     * @see NoticeOfChangeUtils#findNoticeOfChangeAnswerIndex(CaseData, String)
     * @see NoticeOfChangeUtils#findRespondentSolicitorTypeByIndex(int)
     */
    public static RespondentSolicitorType getRespondentSolicitorType(CaseDetails caseDetails, String respondentIndex) {
        // Check if caseDetails is null or empty
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_FOUND, StringUtils.EMPTY)));
        }
        String caseId = ObjectUtils.isNotEmpty(
                caseDetails.getId()) ? caseDetails.getId().toString() : StringUtils.EMPTY;

        // Check if caseDetails has no case data
        if (MapUtils.isEmpty(caseDetails.getData())) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, caseId)));
        }

        // Check if respondentIndex is blank or not a valid number
        if (StringUtils.isBlank(respondentIndex)
                || !NumberUtils.isCreatable(respondentIndex)
                || NumberUtils.createInteger(respondentIndex) < 0) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX, respondentIndex, caseId)));
        }
        CaseData caseData = MapperUtils.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        // Check if caseData is null or empty
        if (ObjectUtils.isEmpty(caseData)) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, caseId)));
        }

        // Check if respondentCollection is null or empty
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_EMPTY_RESPONDENT_COLLECTION, caseId)));
        }

        // Check if respondentIndex is within bounds of respondentCollection
        if (NumberUtils.createInteger(respondentIndex) >= caseData.getRespondentCollection().size()) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX, respondentIndex, caseId)));
        }

        // Check if caseData has respondent at the given index
        RespondentSumTypeItem respondentSumTypeItem = caseData.getRespondentCollection()
                .get(NumberUtils.createInteger(respondentIndex));
        if (ObjectUtils.isEmpty(respondentSumTypeItem)
                || ObjectUtils.isEmpty(respondentSumTypeItem.getValue())
                || StringUtils.isBlank(respondentSumTypeItem.getValue().getRespondentName())) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_RESPONDENT_NOT_EXISTS, caseId)));
        }

        int noticeOfChangeAnswerIndex = NoticeOfChangeUtils
                .findNoticeOfChangeAnswerIndex(caseData, respondentSumTypeItem.getValue().getRespondentName());
        if (noticeOfChangeAnswerIndex == -1) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND,
                            respondentSumTypeItem.getValue().getRespondentName(),
                            caseId)));
        }
        RespondentSolicitorType respondentSolicitorType = NoticeOfChangeUtils
                .findRespondentSolicitorTypeByIndex(noticeOfChangeAnswerIndex);
        if (ObjectUtils.isEmpty(respondentSolicitorType)) {
            throw new CallbacksRuntimeException(new Exception(String.format(
                    EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND, caseId, noticeOfChangeAnswerIndex)));
        }
        return respondentSolicitorType;
    }
}
