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
    public static final String INVALID_REPRESENTATIVE_PHONE_NUMBER_FORMAT_MESSAGE =
            INVALID_PHONE_NUMBER_FORMAT_MESSAGE + " for %s";
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
     * Validates the telephone numbers of all respondent representatives in the given collection. Each invalid
     * telephone number gets its own error naming the representative, followed by a single digits-only message.
     *
     * @param representatives the respondent representatives to validate
     * @return List of validation errors or an empty list if all telephone numbers are valid
     */
    public static List<String> validateRepresentativePhoneNumbers(List<RepresentedTypeRItem> representatives) {
        List<String> validationErrors = new ArrayList<>();
        if (CollectionUtils.isEmpty(representatives)) {
            return validationErrors;
        }
        for (int i = 0; i < representatives.size(); i++) {
            RepresentedTypeRItem item = representatives.get(i);
            if (item != null && item.getValue() != null
                    && !isValidPhoneNumber(item.getValue().getRepresentativePhoneNumber())) {
                validationErrors.add(String.format(INVALID_REPRESENTATIVE_PHONE_NUMBER_FORMAT_MESSAGE,
                        getRepresentativeName(item, i)));
            }
        }
        if (!validationErrors.isEmpty()) {
            validationErrors.add(INVALID_PHONE_NUMBER_DIGITS_ONLY_MESSAGE);
        }
        return validationErrors;
    }

    private static String getRepresentativeName(RepresentedTypeRItem representative, int index) {
        String name = representative.getValue().getNameOfRepresentative();
        return StringUtils.isNotBlank(name) ? name : "representative " + (index + 1);
    }

    private static List<String> invalidPhoneNumberErrors() {
        return new ArrayList<>(List.of(INVALID_PHONE_NUMBER_FORMAT_MESSAGE, INVALID_PHONE_NUMBER_DIGITS_ONLY_MESSAGE));
    }
}
