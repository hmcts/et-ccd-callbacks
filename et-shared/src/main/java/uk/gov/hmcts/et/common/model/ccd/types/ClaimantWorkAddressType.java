package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "WorkAddressDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimantWorkAddressType {

    @CCD(label = "Claimant Work Address", searchable = false, typeOverride = FieldType.AddressUK)
    @JsonProperty("claimant_work_address")
    private Address claimantWorkAddress;
    @CCD(label = "Work phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    @JsonProperty("claimant_work_phone_number")
    private String claimantWorkPhoneNumber;
}
