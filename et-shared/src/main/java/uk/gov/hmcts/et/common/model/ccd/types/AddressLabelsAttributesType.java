package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "addressLabelsAttributes", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AddressLabelsAttributesType {

    @CCD(label = "Number of selected labels to print in this run")
    @JsonProperty("numberOfSelectedLabels")
    private String numberOfSelectedLabels;
    @CCD(label = "Number of copies of each label", typeOverride = FieldType.Number)
    @JsonProperty("numberOfCopies")
    private String numberOfCopies;
    @CCD(
            label = "Select the label to start printing from",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_AddressLabelNumber"
    )
    @JsonProperty("startingLabel")
    private String startingLabel;
    @CCD(label = "Show Tel / Fax Numbers?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("showTelFax")
    private String showTelFax;
}
