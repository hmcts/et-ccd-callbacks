package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;

public final class PdfMapperTestUtil {

    private PdfMapperTestUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static boolean isNotApplicable(String checkValue) {
        return isBlank(checkValue) || !List.of(NO_CAPITALISED, YES_CAPITALISED).contains(checkValue);
    }

    public static boolean isValueEnteredEqualsExpectedValue(String valueEntered, String expectedValue) {
        return isNotBlank(valueEntered) && isNotBlank(expectedValue) && expectedValue.equalsIgnoreCase(valueEntered);
    }
}
