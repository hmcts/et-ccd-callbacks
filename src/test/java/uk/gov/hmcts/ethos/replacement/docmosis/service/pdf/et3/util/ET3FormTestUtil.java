package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;

public final class ET3FormTestUtil {

    private ET3FormTestUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static String getCheckBoxNotApplicableValue(String actualValue,
                                                       List<String> expectedValues,
                                                       String valueToEnableCheckbox) {
        return isBlank(actualValue) || !expectedValues.contains(actualValue)
                ? valueToEnableCheckbox : STRING_EMPTY;
    }

    public static String getCheckboxValue(String actualValue, String expectedValue, String valueToEnableCheckbox) {
        return isNotBlank(actualValue) && isNotBlank(expectedValue) && expectedValue.equalsIgnoreCase(actualValue)
                ? valueToEnableCheckbox : STRING_EMPTY;
    }

    public static String getCorrectedDetailValue(String checkboxActualValue,
                                                 String expectedCheckBoxValue,
                                                 String correctedDetailValue,
                                                 String expectedCorrectedDetailValue) {
        return isNotBlank(checkboxActualValue)
                && expectedCheckBoxValue.equalsIgnoreCase(checkboxActualValue)
                && isNotBlank(correctedDetailValue) ? expectedCorrectedDetailValue : STRING_EMPTY;
    }

    public static String getCorrectedCheckboxValue(String checkboxActualValue,
                                                   String expectedCheckBoxValue,
                                                   String correctedDetailCheckboxValue,
                                                   String expectedDetailCheckboxValue,
                                                   String expectedCorrectedDetailValue) {
        return isNotBlank(checkboxActualValue)
                && expectedCheckBoxValue.equalsIgnoreCase(checkboxActualValue)
                && isNotBlank(correctedDetailCheckboxValue)
                && expectedDetailCheckboxValue.equalsIgnoreCase(correctedDetailCheckboxValue)
                ? expectedCorrectedDetailValue : STRING_EMPTY;
    }
}
