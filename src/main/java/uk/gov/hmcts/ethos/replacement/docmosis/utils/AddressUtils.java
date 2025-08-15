package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static Address mapOrganisationAddressToAddress(OrganisationAddress organisationAddress) {
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
     * Builds a single multi-line string representation of the given {@link OrganisationAddress}.
     *
     * <p>
     * Each non-null and non-blank field from the address is appended in the following order:
     * <ul>
     *     <li>Address Line 1</li>
     *     <li>Address Line 2</li>
     *     <li>Address Line 3</li>
     *     <li>Town/City</li>
     *     <li>Postcode</li>
     *     <li>County</li>
     *     <li>Country</li>
     * </ul>
     * Fields are separated by a newline character (<code>\n</code>).
     * Missing or blank fields are skipped without adding extra blank lines.
     * </p>
     * If the {@code organisationAddress} argument is {@code null}, an empty string is returned.
     *
     * @param organisationAddress the address object to convert to text; may be {@code null}
     * @return a multi-line string containing the address fields, or an empty string if the address is {@code null}
     *         or has no non-blank fields
     */
    @NotNull
    public static String getOrganisationAddressAsText(OrganisationAddress organisationAddress) {
        if (ObjectUtils.isEmpty(organisationAddress)) {
            return StringUtils.EMPTY;
        }
        return Stream.of(
                        organisationAddress.getAddressLine1(),
                        organisationAddress.getAddressLine2(),
                        organisationAddress.getAddressLine3(),
                        organisationAddress.getTownCity(),
                        organisationAddress.getPostCode(),
                        organisationAddress.getCounty(),
                        organisationAddress.getCountry()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(StringUtils.LF));
    }
}
