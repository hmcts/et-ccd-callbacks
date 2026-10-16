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
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PhoneNumberValidator.INVALID_PHONE_NUMBER_DIGITS_ONLY_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.PhoneNumberValidator.INVALID_PHONE_NUMBER_FORMAT_MESSAGE;

class PhoneNumberValidatorTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "07700900123", "07700 900 123", "02079460000", "00447700900123", "123456789012345"})
    void isValidPhoneNumber_validValues(String phoneNumber) {
        assertThat(PhoneNumberValidator.isValidPhoneNumber(phoneNumber)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Via representative", "+447700900123", "07700-900-123", "(020) 79460000",
        "07700900123 ext 2", "0770090012a", "1234567890123456", "07700900123 07700900124"})
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
                .containsExactly(INVALID_PHONE_NUMBER_FORMAT_MESSAGE, INVALID_PHONE_NUMBER_DIGITS_ONLY_MESSAGE);
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
    void validateRepresentativePhoneNumbers_anyInvalid_returnsErrorsOnce() {
        List<RepresentedTypeRItem> representatives = List.of(
                representative("07700900123"), representative("Via representative"), representative("+44770090"));
        assertThat(PhoneNumberValidator.validateRepresentativePhoneNumbers(representatives))
                .containsExactly(INVALID_PHONE_NUMBER_FORMAT_MESSAGE, INVALID_PHONE_NUMBER_DIGITS_ONLY_MESSAGE);
    }

    private static RepresentedTypeRItem representative(String phoneNumber) {
        return RepresentedTypeRItem.builder()
                .value(RepresentedTypeR.builder().representativePhoneNumber(phoneNumber).build())
                .build();
    }
}
