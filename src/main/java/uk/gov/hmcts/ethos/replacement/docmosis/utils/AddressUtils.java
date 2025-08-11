package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;

public final class AddressUtils {

    private AddressUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Checks whether the given {@link Address} object is considered null or empty.
     * <p>
     * An {@code Address} is deemed empty if it is:
     * <ul>
     *     <li>{@code null}, or</li>
     *     <li>an empty object as determined by {@link ObjectUtils#isEmpty(Object)}, or</li>
     *     <li>all of its key string fields are blank (i.e., null, empty, or whitespace-only):</li>
     *     <ul>
     *         <li>{@code addressLine1}</li>
     *         <li>{@code addressLine2}</li>
     *         <li>{@code addressLine3}</li>
     *         <li>{@code country}</li>
     *         <li>{@code postCode}</li>
     *         <li>{@code postTown}</li>
     *         <li>{@code county}</li>
     *     </ul>
     * </ul>
     *
     * @param address the {@code Address} object to check
     * @return {@code true} if the address is null or all its string fields are blank; {@code false} otherwise
     */
    public static boolean isNullOrEmpty(Address address) {
        return ObjectUtils.isEmpty(address)
                || (StringUtils.isBlank(address.getAddressLine1())
                && StringUtils.isBlank(address.getAddressLine2())
                && StringUtils.isBlank(address.getAddressLine3())
                && StringUtils.isBlank(address.getCountry())
                && StringUtils.isBlank(address.getPostCode())
                && StringUtils.isBlank(address.getPostTown())
                && StringUtils.isBlank(address.getCounty()));
    }

    /**
     * Returns the given {@link Address} instance, or creates a new one if the input is {@code null}.
     * <p>
     * This method is useful for ensuring that a non-null {@code Address} object is always returned,
     * avoiding potential {@code NullPointerException} issues when the caller expects a usable address.
     *
     * @param address the {@code Address} object to check for nullity
     * @return the same {@code Address} instance if it is not {@code null};
     *         otherwise, a new {@code Address} instance
     */
    public static Address createIfNull(Address address) {
        return address == null ? new Address() : address;
    }

    @NotNull
    public static Address getAddress(OrganisationAddress organisationAddress) {
        Address et3ResponseAddress = new Address();
        et3ResponseAddress.setAddressLine1(organisationAddress.getAddressLine1());
        et3ResponseAddress.setAddressLine2(organisationAddress.getAddressLine2());
        et3ResponseAddress.setAddressLine3(organisationAddress.getAddressLine3());
        et3ResponseAddress.setCountry(organisationAddress.getCountry());
        et3ResponseAddress.setCounty(organisationAddress.getCounty());
        et3ResponseAddress.setPostCode(organisationAddress.getPostCode());
        et3ResponseAddress.setPostTown(organisationAddress.getTownCity());
        return et3ResponseAddress;
    }
}
