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

    /**
     * Converts an {@link OrganisationAddress} into a corresponding {@link Address} object.
     *
     * <p>
     * This method maps each available field from the {@code organisationAddress} to the
     * appropriate field in a new {@code Address} instance:
     * <ul>
     *     <li>{@code addressLine1}, {@code addressLine2}, and {@code addressLine3}</li>
     *     <li>{@code country}, {@code county}, {@code postCode}, and {@code townCity} (mapped to {@code postTown})</li>
     * </ul>
     *
     * @param organisationAddress the source address object containing organisation address details
     * @return a non-null {@link Address} populated with values from the provided {@code organisationAddress}
     *
     * @throws NullPointerException if {@code organisationAddress} is {@code null}
     */
    @NotNull
    public static Address getAddress(OrganisationAddress organisationAddress) {
        Address address = new Address();
        address.setAddressLine1(organisationAddress.getAddressLine1());
        address.setAddressLine2(organisationAddress.getAddressLine2());
        address.setAddressLine3(organisationAddress.getAddressLine3());
        address.setCountry(organisationAddress.getCountry());
        address.setCounty(organisationAddress.getCounty());
        address.setPostCode(organisationAddress.getPostCode());
        address.setPostTown(organisationAddress.getTownCity());
        return address;
    }

    /**
     * Converts the given {@link OrganisationAddress} into a formatted multi-line string representation.
     *
     * <p>
     * Each non-null component of the address is appended in a readable format:
     * <ul>
     *     <li>Address lines 1 to 3 are concatenated with spaces (if present).</li>
     *     <li>Town/City, Postcode, County, and Country are appended on new lines (if present).</li>
     * </ul>
     * Null fields are skipped to avoid blank or malformed lines.
     *
     * @param organisationAddress the organisation address object to convert
     * @return a non-null string containing the formatted address
     *
     * @throws NullPointerException if {@code organisationAddress} is {@code null}
     */
    @NotNull
    public static String getAddressAsText(OrganisationAddress organisationAddress) {
        StringBuilder sb = (organisationAddress.getAddressLine1() == null ? new StringBuilder() : new StringBuilder(
                organisationAddress.getAddressLine1()))
                .append(organisationAddress.getAddressLine2() == null ? "" :
                        "\n" + organisationAddress.getAddressLine2()).append(
                                organisationAddress.getAddressLine3() == null ? "" :
                                        "\n" + organisationAddress.getAddressLine3()).append(
                                                organisationAddress.getTownCity() == null ? "" :
                                                        "\n" + organisationAddress.getTownCity()).append(
                                                                organisationAddress.getPostCode() == null ? "" :
                                                                        "\n" + organisationAddress.getPostCode())
                .append(organisationAddress.getCounty() == null ? "" :
                        "\n" + organisationAddress.getCounty()).append(organisationAddress.getCountry() == null ? "" :
                        "\n" + organisationAddress.getCountry());
        return sb.toString();
    }
}
