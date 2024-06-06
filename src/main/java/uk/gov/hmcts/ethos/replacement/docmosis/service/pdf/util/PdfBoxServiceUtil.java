package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import lombok.extern.slf4j.Slf4j;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.CHARACTER_DOT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.CURRENCY_DECIMAL_ZERO_WITH_DOT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.STRING_ZERO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.TWO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.ZERO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;

@Slf4j
public final class PdfBoxServiceUtil {

    private PdfBoxServiceUtil() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Logs exception according to given user inputs below. It enables us have a better log on azure cloud.
     * @param firstWord first wordings of the log.
     * @param caseReferenceNumber ethos case reference data
     * @param message exception message
     * @param className class name where the exception occurs
     * @param methodName method name where the exception occurs
     */
    public static void logException(String firstWord,
                                    String caseReferenceNumber,
                                    String message,
                                    String className,
                                    String methodName) {
        log.error("*************EXCEPTION OCCURED*************"
                + "\nERROR DESCRIPTION: " + (isNotBlank(firstWord) ? firstWord : STRING_EMPTY)
                + "\nCASE REFERENCE: " + (isNotBlank(caseReferenceNumber) ? caseReferenceNumber : STRING_EMPTY)
                + "\nERROR MESSAGE: " + (isNotBlank(message) ? message : STRING_EMPTY)
                + "\nCLASS NAME: " + (isNotBlank(className) ? className : STRING_EMPTY)
                + "\nMETHOD NAME: " + (isNotBlank(methodName) ? methodName : STRING_EMPTY)
                + "\n*****************END OF EXCEPTION MESSAGE***********************");
    }

    /**
     * Converts given value to currency. Value is coming from frontend with extra 2 ZEROs. That is why
     * we are adding a dot(.) in between last 2 digits to format it as currency.
     * Code checks if the entered value has any (.). If it has returns the value and checks if the value's length is
     * less than 2. If it is less than 2 returns the value adding .00, If value is empty then returns an empty string.
     * @param value entered by user but added extra 2 zeros.
     * @return string that has a dot(.) before the ending 2 zeros.
     */
    public static String correctCurrency(String value) {
        if (isBlank(value)) {
            return STRING_EMPTY;
        }
        if (value.indexOf(CHARACTER_DOT) == ZERO) {
            return STRING_ZERO + value;
        }
        if (value.indexOf(CHARACTER_DOT) >= ONE) {
            return value;
        }
        if (value.length() < TWO) {
            return value + CURRENCY_DECIMAL_ZERO_WITH_DOT;
        }
        return value.substring(0, value.length() - 2) + CHARACTER_DOT + value.substring(value.length() - TWO);
    }
}
