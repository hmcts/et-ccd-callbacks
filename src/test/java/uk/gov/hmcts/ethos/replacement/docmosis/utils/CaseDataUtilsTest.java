package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

    private static final String DUMMY_SUBMISSION_REFERENCE = "1234567890123456";

    @Test
    void theValidateCaseDetails() {
        // When case details is empty should throw EXCEPTION_CASE_DETAILS_NOT_FOUND
        GenericRuntimeException genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetails(null));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_DETAILS_NOT_FOUND);

        // When case details not have case id should throw EXPECTED_EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND
        final CaseDetails newCaseDetails = new CaseDetails();
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetails(newCaseDetails));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_SUBMISSION_REFERENCE_NOT_FOUND);

        // When case details not have case data should throw EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND
        newCaseDetails.setCaseId(DUMMY_SUBMISSION_REFERENCE);
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> CaseDataUtils.validateCaseDetails(newCaseDetails));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CASE_DATA_NOT_FOUND);

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
}
