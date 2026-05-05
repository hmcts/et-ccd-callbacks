package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util.PdfBoxServiceUtil.correctCurrency;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils.logException;

class PdfBoxServiceUtilTest {

    @ParameterizedTest
    @CsvSource(value = {"Test First Word:Test Reference Number:Test Message:Test Class Name:Test Method Name",
                        "null:null:null:null:null"}, delimiter = ':', nullValues = {"null"})
    void testLogException(
            String firstWord, String referenceNumber, String message, String className, String methodName) {
        assertDoesNotThrow(() -> logException(firstWord, referenceNumber, message, className, methodName));
    }

    @ParameterizedTest
    @CsvSource(value = {"null:''", "'':''", "0.256:0.256", "25.36:25.36", "5000000:50000.00", ".125:0.125"},
            delimiter = ':', nullValues = {"null"})
    void testCorrectCurrency(String valueToCorrect, String expectedValue) {
        assertThat(correctCurrency(valueToCorrect)).contains(expectedValue);
    }

}
