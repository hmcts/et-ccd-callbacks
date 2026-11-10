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
    public static final String INVALID_PHONE_NUMBER_UK_FORMAT_MESSAGE =
            "Enter a UK phone number starting with 0 or +44, using digits and spaces only.";

    /**
     * The UK national number formats accepted by the CCD PhoneUK field type, which representative_phone_number
     * uses. The brackets and extensions that PhoneUK also allows are excluded, so a number accepted here is
     * always accepted by PhoneUK.
     */
    private static final Pattern GROUP_4_3_3 = Pattern.compile("(\\+44\\s?\\d{4}|0\\d{4})\\s?\\d{3}\\s?\\d{3}");
    private static final Pattern GROUP_3_3_4 = Pattern.compile("(\\+44\\s?\\d{3}|0\\d{3})\\s?\\d{3}\\s?\\d{4}");
    private static final Pattern GROUP_2_4_4 = Pattern.compile("(\\+44\\s?\\d{2}|0\\d{2})\\s?\\d{4}\\s?\\d{4}");
    private static final List<Pattern> VALID_PHONE_NUMBERS = List.of(GROUP_4_3_3, GROUP_3_3_4, GROUP_2_4_4);

    private PhoneNumberValidator() {
        // All access through static methods
    }

    /**
     * Checks whether the given telephone number is a UK number starting with 0 (11 digits) or +44 (followed by
     * 10 digits), optionally split by single spaces in the positions PhoneUK allows. An empty value is considered
     * valid as the field is optional.
     *
     * @param phoneNumber the telephone number to validate
     * @return true if the telephone number is empty or valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return true;
        }
        return VALID_PHONE_NUMBERS.stream().anyMatch(pattern -> pattern.matcher(phoneNumber).matches());
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
     * telephone number gets its own error naming the representative, followed by a single UK format message.
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
            validationErrors.add(INVALID_PHONE_NUMBER_UK_FORMAT_MESSAGE);
        }
        return validationErrors;
    }

    private static String getRepresentativeName(RepresentedTypeRItem representative, int index) {
        String name = representative.getValue().getNameOfRepresentative();
        return StringUtils.isNotBlank(name) ? name : "representative " + (index + 1);
    }

    private static List<String> invalidPhoneNumberErrors() {
        return new ArrayList<>(List.of(INVALID_PHONE_NUMBER_FORMAT_MESSAGE, INVALID_PHONE_NUMBER_UK_FORMAT_MESSAGE));
    }
}
