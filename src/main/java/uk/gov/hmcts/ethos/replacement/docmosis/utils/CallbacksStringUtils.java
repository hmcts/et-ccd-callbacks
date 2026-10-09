package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CallbacksStringUtils {

    private CallbacksStringUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static String buildFullName(String title, String firstName, String lastName) {
        return Stream.of(title, firstName, lastName)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

}
