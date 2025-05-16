package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.CHARACTER_DOT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.CURRENCY_DECIMAL_ZERO_WITH_DOT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.STRING_ZERO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.TWO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.PdfBoxServiceConstants.ZERO;

public final class PdfBoxServiceUtil {

    private PdfBoxServiceUtil() {
        // Utility classes should not have a public or default constructor.
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
            return StringUtils.EMPTY;
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
