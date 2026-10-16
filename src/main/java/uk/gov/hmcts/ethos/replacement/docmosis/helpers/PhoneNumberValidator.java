package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Provides methods to validate representative telephone numbers.
 */
public final class PhoneNumberValidator {

    public static final String INVALID_PHONE_NUMBER_FORMAT_MESSAGE =
            "Enter a telephone number in the correct format";
    public static final String INVALID_PHONE_NUMBER_DIGITS_ONLY_MESSAGE =
            "Do not include the + symbol. Enter the number using digits only.";

    /**
     * Maximum number of digits allowed in a telephone number, as defined by ITU-T E.164.
     */
    static final int MAX_PHONE_NUMBER_DIGITS = 15;
    private static final Pattern VALID_PHONE_NUMBER =
            Pattern.compile("\\d{1," + MAX_PHONE_NUMBER_DIGITS + "}");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private PhoneNumberValidator() {
        // All access through static methods
    }

    /**
     * Checks whether the given telephone number contains digits only (spaces are ignored) and is no longer than
     * {@value #MAX_PHONE_NUMBER_DIGITS} digits. An empty value is considered valid as the field is optional.
     *
     * @param phoneNumber the telephone number to validate
     * @return true if the telephone number is empty or valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return true;
        }
        return VALID_PHONE_NUMBER.matcher(WHITESPACE.matcher(phoneNumber).replaceAll(StringUtils.EMPTY)).matches();
    }

    /**
     * Validates the given telephone number.
     *
     * @param phoneNumber the telephone number to validate
     * @return List of validation errors or an empty list if the telephone number is valid
     */
    public static List<String> validatePhoneNumber(String phoneNumber) {
        return isValidPhoneNumber(phoneNumber) ? new ArrayList<>() : invalidPhoneNumberErrors();
    }

    /**
     * Validates the telephone numbers of all respondent representatives in the given collection.
     *
     * @param representatives the respondent representatives to validate
     * @return List of validation errors or an empty list if all telephone numbers are valid
     */
    public static List<String> validateRepresentativePhoneNumbers(List<RepresentedTypeRItem> representatives) {
        if (CollectionUtils.isEmpty(representatives)) {
            return new ArrayList<>();
        }
        boolean allValid = representatives.stream()
                .filter(item -> item != null && item.getValue() != null)
                .allMatch(item -> isValidPhoneNumber(item.getValue().getRepresentativePhoneNumber()));
        return allValid ? new ArrayList<>() : invalidPhoneNumberErrors();
    }

    private static List<String> invalidPhoneNumberErrors() {
        return new ArrayList<>(List.of(INVALID_PHONE_NUMBER_FORMAT_MESSAGE, INVALID_PHONE_NUMBER_DIGITS_ONLY_MESSAGE));
    }
}
