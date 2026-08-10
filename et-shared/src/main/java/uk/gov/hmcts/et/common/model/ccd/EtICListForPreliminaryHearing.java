package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "etICHearingNotListedListForPrelimHearing", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICListForPreliminaryHearing {
    @CCD(
            label = "Type of preliminary hearing",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICTypeOfHearing"
    )
    @JsonProperty("etICTypeOfPreliminaryHearing")
    private List<String> etICTypeOfPreliminaryHearing;
    @CCD(
            label = "Purpose of preliminary hearing",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICPurposeOfPrelimHearing"
    )
    @JsonProperty("etICPurposeOfPreliminaryHearing")
    private List<String> etICPurposeOfPreliminaryHearing;
    @CCD(
            label = "Give details of hearing notice",
            showCondition = "etICPurposeOfPreliminaryHearing != \"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICGiveDetailsOfHearingNotice")
    private String etICGiveDetailsOfHearingNotice;
    @CCD(
            label = "Length of hearing",
            showCondition = "etICPurposeOfPreliminaryHearing != \"\"",
            searchable = false,
            typeOverride = FieldType.Number
    )
    @JsonProperty("etICLengthOfPrelimHearing")
    private String etICLengthOfPrelimHearing;
    @CCD(
            label = "Days, Hours or Minutes",
            showCondition = "etICPurposeOfPreliminaryHearing != \"\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingLength"
    )
    @JsonProperty("prelimHearingLengthNumType")
    private String prelimHearingLengthNumType;
}
