package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "addressLabelsSelection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AddressLabelsSelectionType {

    @CCD(label = "Print claimant address label?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("claimantAddressLabel")
    private String claimantAddressLabel;
    @CCD(label = "Print claimant representative address label?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("claimantRepAddressLabel")
    private String claimantRepAddressLabel;
    @CCD(label = "Print respondents address label?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("respondentsAddressLabel")
    private String respondentsAddressLabel;
    @CCD(
            label = "Print respondents representatives address label?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("respondentsRepsAddressLabel")
    private String respondentsRepsAddressLabel;
}
