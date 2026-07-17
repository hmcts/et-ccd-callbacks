package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtInitialConsiderationRule28 {
    @CCD(
            label = "Response to be",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_rule2728ClaimToBe"
    )
    @JsonProperty("etICRule28ClaimToBe")
    private String etICRule28ClaimToBe;
    @CCD(label = "Which part?", showCondition = "etICRule28ClaimToBe=\"Dismissed in part\"", searchable = false)
    @JsonProperty("etICRule28WhichPart")
    private String etICRule28WhichPart;
    @CCD(
            label = "Employment Judge’s reasons",
            hint = "No reasonable prospect of success because",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICRule28DirectionReason")
    private String etICRule28DirectionReason;
    @CCD(label = "Number of days for respondent to provide written representations", searchable = false)
    @JsonProperty("etICRule28NumberOfDays")
    private String etICRule28NumberOfDays;
}
