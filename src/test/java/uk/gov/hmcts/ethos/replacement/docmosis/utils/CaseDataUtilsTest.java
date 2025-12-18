package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NEW;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.OLD;

final class CaseDataUtilsTest {

    private static final String EXPECTED_EXCEPTION_CCD_REQUEST_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: CCD request not found.";
    private static final String EXPECTED_EXCEPTION_CASE_DETAILS_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Case details not found.";
    private static final String EXPECTED_EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: "
                    + "Submission reference not found.";
    private static final String EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Case data not found for "
                    + "submission reference, 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_CALLBACK_REQUEST_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Callback request not found.";
    private static final String EXPECTED_EXCEPTION_CASE_DETAILS_BEFORE_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Case details, "
                    + "BEFORE not found.";
    private static final String EXPECTED_EXCEPTION_CASE_DETAILS_BEFORE_SUBMISSION_REFERENCE_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Case details, "
                    + "BEFORE, submission reference not found.";
    private static final String EXPECTED_EXCEPTION_CASE_DETAILS_BEFORE_CASE_DATA_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Case details, "
                    + "BEFORE, case data not found for submission reference, 1234567890123456.";

    private static final String DUMMY_SUBMISSION_REFERENCE = "1234567890123456";

    @Test
    void theValidateCaseDetails() {
        // When new case details is empty should throw EXCEPTION_CASE_DETAILS_NOT_FOUND
        GenericRuntimeException genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetailsBeforeAndCurrent(null, NEW));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_DETAILS_NOT_FOUND);

        // When new case details not have case id should throw EXPECTED_EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND
        final CaseDetails newCaseDetails = new CaseDetails();
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetailsBeforeAndCurrent(newCaseDetails, null));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND);

        // When new case details not have case data should throw EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND
        newCaseDetails.setCaseId(DUMMY_SUBMISSION_REFERENCE);
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetailsBeforeAndCurrent(newCaseDetails, NEW));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND);

        // When old case details is empty should throw EXCEPTION_CASE_DETAILS_BEFORE_NOT_FOUND
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetailsBeforeAndCurrent(null, OLD));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_DETAILS_BEFORE_NOT_FOUND);

        // When old case details not has case id should throw
        // EXCEPTION_CASE_DETAILS_BEFORE_SUBMISSION_REFERENCE_NOT_FOUND
        final CaseDetails oldCaseDetails = new CaseDetails();
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetailsBeforeAndCurrent(oldCaseDetails, OLD));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_CASE_DETAILS_BEFORE_SUBMISSION_REFERENCE_NOT_FOUND);

        // When old case details not has case data should throw EXCEPTION_CASE_DETAILS_BEFORE_CASE_DATA_NOT_FOUND
        oldCaseDetails.setCaseId(DUMMY_SUBMISSION_REFERENCE);
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetailsBeforeAndCurrent(oldCaseDetails, OLD));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_CASE_DETAILS_BEFORE_CASE_DATA_NOT_FOUND);
    }

    @Test
    void theValidateCCDRequest() {
        // When CCD Request is empty
        GenericRuntimeException genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCCDRequest(null));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CCD_REQUEST_NOT_FOUND);

        // When CCD Request is not empty and has valid case details, should not throw exception.
        final CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(DUMMY_SUBMISSION_REFERENCE);
        caseDetails.setCaseData(new CaseData());
        final CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        assertDoesNotThrow(() -> CaseDataUtils.validateCCDRequest(ccdRequest));
    }

    @Test
    void theValidateCallbackRequest() {
        // When Callback Request is empty should throw EXCEPTION_CALLBACK_REQUEST_NOT_FOUND
        GenericRuntimeException genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCallbackRequest(null));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CALLBACK_REQUEST_NOT_FOUND);

        // When Callback Request is not empty and has valid old and new case details, should not throw exception.
        final CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(DUMMY_SUBMISSION_REFERENCE);
        caseDetails.setCaseData(new CaseData());
        final CallbackRequest callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetails(caseDetails);
        callbackRequest.setCaseDetailsBefore(caseDetails);
        assertDoesNotThrow(() -> CaseDataUtils.validateCallbackRequest(callbackRequest));
    }
}
