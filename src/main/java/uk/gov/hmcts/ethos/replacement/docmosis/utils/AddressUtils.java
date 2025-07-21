package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;

public final class AddressUtils {

    private AddressUtils() {
        // Utility classes should not have a public or default constructor.
    }

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
}
