package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.Address;

import static org.assertj.core.api.Assertions.assertThat;

final class AddressUtilsTest {

    @Test
    void testIsNullOrEmpty() {
        // Test cases for AddressUtil.isNullOrEmpty method
        assertThat(AddressUtils.isNullOrEmpty(null)).isTrue();
        // When all address fields are empty
        Address emptyAddress = new Address();
        assertThat(AddressUtils.isNullOrEmpty(emptyAddress)).isTrue();

        // When addressLine1 is not empty
        Address nonEmptyAddressLine1 = new Address();
        nonEmptyAddressLine1.setAddressLine1("123 Street");
        assertThat(AddressUtils.isNullOrEmpty(nonEmptyAddressLine1)).isFalse();

        // When addressLine2 is not empty
        Address nonEmptyAddressLine2 = new Address();
        nonEmptyAddressLine2.setAddressLine2("123 Street");
        assertThat(AddressUtils.isNullOrEmpty(nonEmptyAddressLine2)).isFalse();

        // When addressLine3 is not empty
        Address nonEmptyAddressLine3 = new Address();
        nonEmptyAddressLine3.setAddressLine3("123 Street");
        assertThat(AddressUtils.isNullOrEmpty(nonEmptyAddressLine3)).isFalse();

        // When postTown is not empty
        Address nonEmptyPostTown = new Address();
        nonEmptyPostTown.setPostTown("Test Post/Town");
        assertThat(AddressUtils.isNullOrEmpty(nonEmptyPostTown)).isFalse();

        // When postCode is not empty
        Address nonEmptyPostCode = new Address();
        nonEmptyPostCode.setPostCode("Test Post/Town");
        assertThat(AddressUtils.isNullOrEmpty(nonEmptyPostCode)).isFalse();

        // When county is not empty
        Address nonEmptyCounty = new Address();
        nonEmptyCounty.setCounty("Test County");
        assertThat(AddressUtils.isNullOrEmpty(nonEmptyCounty)).isFalse();

        // When country is not empty
        Address nonEmptyCountry = new Address();
        nonEmptyCountry.setCountry("Test Country");
        assertThat(AddressUtils.isNullOrEmpty(nonEmptyCountry)).isFalse();
    }
}
