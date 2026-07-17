package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtInitialConsiderationRule27 {
    @CCD(
            label = "Claim to be",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_rule2728ClaimToBe"
    )
    @JsonProperty("etICRule27ClaimToBe")
    private String etICRule27ClaimToBe;
    @CCD(label = "Which part?", showCondition = "etICRule27ClaimToBe=\"Dismissed in part\"", searchable = false)
    @JsonProperty("etICRule27WhichPart")
    private String etICRule27WhichPart;
    @CCD(
            label = "Employment Judge's direction to Rule 28 Notice",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_rule27direction"
    )
    @JsonProperty("etICRule27Direction")
    private List<String> etICRule27Direction;
    @CCD(
            label = "Set out reason",
            showCondition = "etICRule27Direction CONTAINS \"No reasonable prospect of success\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICRule27DirectionReason")
    private String etICRule27DirectionReason;
    @CCD(ignore = true)
    @JsonProperty("etICRule27NoJurisdictionReason")
    private String etICRule27NoJurisdictionReason;
    @CCD(label = "Number of days for claimant to provide written representations", searchable = false)
    @JsonProperty("etICRule27NumberOfDays")
    private String etICRule27NumberOfDays;
}
