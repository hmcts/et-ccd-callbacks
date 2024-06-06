package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfBoxServiceUtil.logException;

class PdfBoxServiceUtilTest {

    @ParameterizedTest
    @CsvSource(value = {"Test First Word:Test Reference Number:Test Message:Test Class Name:Test Method Name",
                        "null:null:null:null:null"}, delimiter = ':', nullValues = {"null"})
    void testLogException(
            String firstWord, String referenceNumber, String message, String className, String methodName) {
        assertDoesNotThrow(() -> logException(firstWord, referenceNumber, message, className, methodName));
    }

}
