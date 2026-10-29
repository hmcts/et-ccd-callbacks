package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PhoneNumberValidator.INVALID_PHONE_NUMBER_FORMAT_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PhoneNumberValidator.INVALID_PHONE_NUMBER_UK_FORMAT_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PhoneNumberValidator.INVALID_REPRESENTATIVE_PHONE_NUMBER_FORMAT_MESSAGE;

class PhoneNumberValidatorTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "07700900123", "07700 900123", "07700 900 123", "01632 960001", "0161 496 0000",
        "0161 4960000", "020 7946 0000", "02079460000", "+447700900123", "+44 7700 900123", "+44 7700 900 123",
        "+44 161 496 0000", "+44 20 7946 0000"})
    void isValidPhoneNumber_validValues(String phoneNumber) {
        assertThat(PhoneNumberValidator.isValidPhoneNumber(phoneNumber)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Via representative", "+4407700900123", "+44 (0)7700 900123", "+1 202 555 0100",
        "+33 1 23 45 67 89", "+44770090012", "+44-7700-900123", "07700-900-123",
        "(020) 79460000", "07700 900123 #1234", "07700900123 ext 2", "0770090012a", "0044 7700 900123",
        "00447700900123", "17700900123", "0770090012", "077009001234", "0 7700900123", "07700  900123",
        " 07700900123", "07700900123 07700900124"})
    void isValidPhoneNumber_invalidValues(String phoneNumber) {
        assertThat(PhoneNumberValidator.isValidPhoneNumber(phoneNumber)).isFalse();
    }

    @Test
    void validatePhoneNumber_valid_returnsNoErrors() {
        assertThat(PhoneNumberValidator.validatePhoneNumber("07700900123")).isEmpty();
    }

    @Test
    void validatePhoneNumber_invalid_returnsErrors() {
        assertThat(PhoneNumberValidator.validatePhoneNumber("Via representative"))
                .containsExactly(INVALID_PHONE_NUMBER_FORMAT_MESSAGE, INVALID_PHONE_NUMBER_UK_FORMAT_MESSAGE);
    }

    @Test
    void validateRepresentativePhoneNumbers_emptyCollection_returnsNoErrors() {
        assertThat(PhoneNumberValidator.validateRepresentativePhoneNumbers(null)).isEmpty();
        assertThat(PhoneNumberValidator.validateRepresentativePhoneNumbers(new ArrayList<>())).isEmpty();
    }

    @Test
    void validateRepresentativePhoneNumbers_allValid_returnsNoErrors() {
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        representatives.add(representative("07700900123"));
        representatives.add(representative(null));
        representatives.add(RepresentedTypeRItem.builder().build());
        representatives.add(null);
        assertThat(PhoneNumberValidator.validateRepresentativePhoneNumbers(representatives)).isEmpty();
    }

    @Test
    void validateRepresentativePhoneNumbers_anyInvalid_namesEachInvalidRepresentative() {
        List<RepresentedTypeRItem> representatives = List.of(
                representative("Jane Smith", "07700900123"),
                representative("John Doe", "Via representative"),
                representative(null, "+44770090"));
        assertThat(PhoneNumberValidator.validateRepresentativePhoneNumbers(representatives)).containsExactly(
                String.format(INVALID_REPRESENTATIVE_PHONE_NUMBER_FORMAT_MESSAGE, "John Doe"),
                String.format(INVALID_REPRESENTATIVE_PHONE_NUMBER_FORMAT_MESSAGE, "representative 3"),
                INVALID_PHONE_NUMBER_UK_FORMAT_MESSAGE);
    }

    private static RepresentedTypeRItem representative(String phoneNumber) {
        return representative(null, phoneNumber);
    }

    private static RepresentedTypeRItem representative(String name, String phoneNumber) {
        return RepresentedTypeRItem.builder()
                .value(RepresentedTypeR.builder().nameOfRepresentative(name).representativePhoneNumber(phoneNumber)
                        .build())
                .build();
    }
}
