package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;

import static org.assertj.core.api.Assertions.assertThat;

final class AddressUtilsTest {

    private static final String ORGANISATION_ADDRESS_LINE_1 = "Organisation Address Line 1";
    private static final String ORGANISATION_ADDRESS_LINE_2 = "Organisation Address Line 2";
    private static final String ORGANISATION_ADDRESS_LINE_3 = "Organisation Address Line 3";
    private static final String ORGANISATION_COUNTRY = "Organisation Country";
    private static final String ORGANISATION_COUNTY = "Organisation County";
    private static final String ORGANISATION_POST_CODE = "Organisation Post Code";
    private static final String ORGANISATION_TOWN_CITY = "Organisation Town/City";

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

    @Test
    void testCreateIfNull() {
        // When address is null, it should return a new Address instance
        assertThat(AddressUtils.createIfNull(null)).isNotNull().isInstanceOf(Address.class);

        // When address is not null, it should return the same instance
        Address address = new Address();
        address.setAddressLine1("123 Street");
        assertThat(AddressUtils.createIfNull(address)).isNotNull().isEqualTo(address);
    }

    @Test
    void testGetAddress() {

        OrganisationAddress organisationAddress = OrganisationAddress.builder()
                .addressLine1(ORGANISATION_ADDRESS_LINE_1)
                .addressLine2(ORGANISATION_ADDRESS_LINE_2)
                .addressLine3(ORGANISATION_ADDRESS_LINE_3)
                .country(ORGANISATION_COUNTRY)
                .county(ORGANISATION_COUNTY)
                .postCode(ORGANISATION_POST_CODE)
                .townCity(ORGANISATION_TOWN_CITY)
                .build();
        Address address = AddressUtils.getAddress(organisationAddress);
        assertThat(address.getAddressLine1()).isEqualTo(ORGANISATION_ADDRESS_LINE_1);
        assertThat(address.getAddressLine2()).isEqualTo(ORGANISATION_ADDRESS_LINE_2);
        assertThat(address.getAddressLine3()).isEqualTo(ORGANISATION_ADDRESS_LINE_3);
        assertThat(address.getCountry()).isEqualTo(ORGANISATION_COUNTRY);
        assertThat(address.getCounty()).isEqualTo(ORGANISATION_COUNTY);
        assertThat(address.getPostCode()).isEqualTo(ORGANISATION_POST_CODE);
        assertThat(address.getPostTown()).isEqualTo(ORGANISATION_TOWN_CITY);
    }
}
