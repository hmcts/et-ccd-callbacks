package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CALLBACK_REQUEST_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CCD_REQUEST_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_CASE_DETAILS_BEFORE_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_CASE_DETAILS_BEFORE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_CASE_DETAILS_BEFORE_SUBMISSION_REFERENCE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NEW;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.OLD;

public final class CaseDataUtils {

    private static final String CLASS_NAME = RespondentUtils.class.getSimpleName();

    private CaseDataUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Validates that the provided {@link CaseDetails} object and its mandatory fields
     * are present either for a "before" submission state or the current state.
     * <p>
     * This method performs the following checks in order:
     * <ul>
     *   <li>Ensures {@code caseDetails} is not {@code null} or empty</li>
     *   <li>Ensures the case submission reference (case ID) is present and not blank</li>
     *   <li>Ensures the case data is present</li>
     * </ul>
     * <p>
     * The validation behaviour and exception messages differ depending on whether
     * the validation is being performed for the {@code BEFORE} state or the current state,
     * as indicated by the {@code beforeOrCurrent} parameter.
     *
     * @param caseDetails     the {@link CaseDetails} object to validate
     * @param beforeOrCurrent a flag indicating whether the validation is for the
     *                        "before" submission state or the current state
     *
     * @throws GenericRuntimeException if:
     *      <ul>
     *          <li>{@code caseDetails} is {@code null} or empty</li>
     *          <li>the case submission reference (case ID) is {@code null} or blank</li>
     *          <li>the case data is {@code null} or empty</li>
     *      </ul>
     */
    public static void validateCaseDetailsBeforeAndCurrent(CaseDetails caseDetails, String beforeOrCurrent) {
        String methodName = "checkCaseDetails";
        if (ObjectUtils.isEmpty(caseDetails)) {
            String exception = OLD.equals(beforeOrCurrent)
                    ? EXCEPTION_CASE_DETAILS_BEFORE_NOT_FOUND
                    : EXCEPTION_CASE_DETAILS_NOT_FOUND;
            throw new GenericRuntimeException(new GenericServiceException(exception, new Exception(exception),
                    exception, exception, CLASS_NAME, methodName));
        }
        if (StringUtils.isBlank(caseDetails.getCaseId())) {
            String exception = OLD.equals(beforeOrCurrent)
                    ? EXCEPTION_CASE_DETAILS_BEFORE_SUBMISSION_REFERENCE_NOT_FOUND
                    : EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND;
            throw new GenericRuntimeException(new GenericServiceException(exception, new Exception(exception),
                    exception, exception, CLASS_NAME, methodName));
        }
        if (ObjectUtils.isEmpty(caseDetails.getCaseData())) {
            String exception = OLD.equals(beforeOrCurrent)
                    ? EXCEPTION_CASE_DETAILS_BEFORE_CASE_DATA_NOT_FOUND
                    : EXCEPTION_CASE_DATA_NOT_FOUND;
            String exceptionMessage = String.format(exception, caseDetails.getCaseId());
            throw new GenericRuntimeException(new GenericServiceException(exceptionMessage,
                    new Exception(exceptionMessage), exceptionMessage, exceptionMessage, CLASS_NAME, methodName));
        }
    }

    /**
     * Performs structural validation on the provided {@link CCDRequest} to ensure it contains
     * all mandatory components required for further processing. This method validates the
     * presence and integrity of the {@code CCDRequest}, its case details, case ID, and case data.
     *
     * <p>The validation is executed in a fail-fast manner. For each missing or invalid element,
     * the method throws a {@link GenericRuntimeException} wrapping a {@link GenericServiceException}
     * that identifies the specific validation failure. The following conditions are verified:</p>
     *
     * <ul>
     *     <li><b>CCDRequest object is present:</b>
     *         If {@code ccdRequest} is {@code null} or empty, an exception is thrown with
     *         {@code EXCEPTION_CCD_REQUEST_NOT_FOUND}.</li>
     *
     *     <li><b>Case details are present:</b>
     *         If {@code caseDetails} is missing or empty, an exception is thrown with
     *         {@code EXCEPTION_CASE_DETAILS_NOT_FOUND}.</li>
     *
     *     <li><b>Case ID is present and non-blank:</b>
     *         If the case ID is {@code null}, empty, or blank, an exception is thrown with
     *         {@code EXCEPTION_CASE_ID_NOT_FOUND}.</li>
     *
     *     <li><b>Case data is present:</b>
     *         If {@code caseData} is missing or empty, an exception is thrown with
     *         {@code EXCEPTION_CASE_DATA_NOT_FOUND}.</li>
     * </ul>
     *
     * <p>
     * This method does not perform any domain-level or business-rule validation; it strictly
     * checks for the required structural fields necessary before additional case processing
     * or Notice of Change (NoC) validations can occur.
     * </p>
     *
     * @param ccdRequest the CCD request to validate; must not be null and must contain valid case details,
     *                   case ID, and case data
     * @throws GenericRuntimeException if any required part of the {@code CCDRequest} is missing or invalid
     */
    public static void validateCCDRequest(CCDRequest ccdRequest) {
        final String methodName = "validateCCDRequest";
        if (ObjectUtils.isEmpty(ccdRequest)) {
            throw new GenericRuntimeException(new GenericServiceException(EXCEPTION_CCD_REQUEST_NOT_FOUND,
                    new Exception(EXCEPTION_CCD_REQUEST_NOT_FOUND), EXCEPTION_CCD_REQUEST_NOT_FOUND,
                    EXCEPTION_CCD_REQUEST_NOT_FOUND, CLASS_NAME, methodName));
        }
        validateCaseDetailsBeforeAndCurrent(ccdRequest.getCaseDetails(), NEW);
    }

    public static void validateCallbackRequest(CallbackRequest callbackRequest) {
        final String methodName = "validateCallbackRequest";
        if (ObjectUtils.isEmpty(callbackRequest)) {
            throw new GenericRuntimeException(new GenericServiceException(EXCEPTION_CALLBACK_REQUEST_NOT_FOUND,
                    new Exception(EXCEPTION_CALLBACK_REQUEST_NOT_FOUND), EXCEPTION_CALLBACK_REQUEST_NOT_FOUND,
                    EXCEPTION_CALLBACK_REQUEST_NOT_FOUND, CLASS_NAME, methodName));
        }
        validateCaseDetailsBeforeAndCurrent(callbackRequest.getCaseDetailsBefore(), OLD);
        validateCaseDetailsBeforeAndCurrent(callbackRequest.getCaseDetails(), NEW);
    }
}
