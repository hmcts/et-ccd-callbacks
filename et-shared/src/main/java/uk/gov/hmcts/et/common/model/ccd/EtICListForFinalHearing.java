package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "etICHearingNotListedListForFinalHearing", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICListForFinalHearing {
    @CCD(
            label = "Type of final hearing",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICTypeOfHearing"
    )
    @JsonProperty("etICTypeOfFinalHearing")
    private List<String> etICTypeOfFinalHearing;
    @CCD(label = "Length of hearing", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("etICLengthOfFinalHearing")
    private String etICLengthOfFinalHearing;
    @CCD(
            label = "Days, Hours or Minutes",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingLength"
    )
    @JsonProperty("finalHearingLengthNumType")
    private String finalHearingLengthNumType;
}
