package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "addressLabel", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AddressLabelType {

    @CCD(label = "Print label?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("printLabel")
    private String printLabel;
    @CCD(label = "Full name", searchable = false)
    @JsonProperty("fullName")
    private String fullName;
    @CCD(label = "Full address", searchable = false)
    @JsonProperty("fullAddress")
    private String fullAddress;
    @CCD(label = "Entity name 1", searchable = false)
    @JsonProperty("labelEntityName01")
    private String labelEntityName01;
    @CCD(label = "Entity name 2", searchable = false)
    @JsonProperty("labelEntityName02")
    private String labelEntityName02;
    @CCD(label = "Address", searchable = false, typeOverride = FieldType.AddressUK)
    @JsonProperty("labelEntityAddress")
    private Address labelEntityAddress;
    @CCD(label = "Telephone", searchable = false)
    @JsonProperty("labelEntityTelephone")
    private String labelEntityTelephone;
    @CCD(label = "Fax", searchable = false)
    @JsonProperty("labelEntityFax")
    private String labelEntityFax;
    @CCD(label = "Entity Reference", searchable = false)
    @JsonProperty("labelEntityReference")
    private String labelEntityReference;
    @CCD(label = "Case Reference", searchable = false)
    @JsonProperty("labelCaseReference")
    private String labelCaseReference;
}
