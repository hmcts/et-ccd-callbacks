package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CCD_REQUEST_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND;

public final class CaseDataUtils {

    private static final String CLASS_NAME = RespondentUtils.class.getSimpleName();

    private CaseDataUtils() {
        // Utility classes should not have a public or default constructor.
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
        if (ObjectUtils.isEmpty(ccdRequest.getCaseDetails())) {
            throw new GenericRuntimeException(new GenericServiceException(EXCEPTION_CASE_DETAILS_NOT_FOUND,
                    new Exception(EXCEPTION_CASE_DETAILS_NOT_FOUND), EXCEPTION_CASE_DETAILS_NOT_FOUND,
                    EXCEPTION_CASE_DETAILS_NOT_FOUND, CLASS_NAME, methodName));

        }
        if (StringUtils.isBlank(ccdRequest.getCaseDetails().getCaseId())) {
            throw new GenericRuntimeException(new GenericServiceException(EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND,
                    new Exception(EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND), EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND,
                    EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND, CLASS_NAME, methodName));
        }
        if (ObjectUtils.isEmpty(ccdRequest.getCaseDetails().getCaseData())) {
            String exceptionMessage = String.format(EXCEPTION_CASE_DATA_NOT_FOUND,
                    ccdRequest.getCaseDetails().getCaseId());
            throw new GenericRuntimeException(new GenericServiceException(exceptionMessage,
                    new Exception(exceptionMessage), exceptionMessage, exceptionMessage, CLASS_NAME, methodName));
        }
    }

}
