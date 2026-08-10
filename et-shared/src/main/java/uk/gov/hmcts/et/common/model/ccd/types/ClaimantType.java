package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ClaimantCorrespondence", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimantType {

    @CCD(label = "Address", typeOverride = FieldType.AddressUK)
    @JsonProperty("claimant_addressUK")
    private Address claimantAddressUK;
    @CCD(label = "Phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    @JsonProperty("claimant_phone_number")
    private String claimantPhoneNumber;
    @CCD(label = "Alternative number", searchable = false, typeOverride = FieldType.PhoneUK)
    @JsonProperty("claimant_mobile_number")
    private String claimantMobileNumber;
    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("claimant_email_address")
    private String claimantEmailAddress;
    @CCD(
            label = "Contact preference",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ContactPreference"
    )
    @JsonProperty("claimant_contact_preference")
    private String claimantContactPreference;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Contact language",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "fl_languages"
  )
  private String claimant_contact_language;
  @CCD(
          label = "Hearing language",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "fl_languages"
  )
  private String claimant_hearing_language;
  // ==== end synthesised definition-only fields ====
}
