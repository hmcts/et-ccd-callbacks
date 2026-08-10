package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "etICHearingNotListedListForFinalHearingUpdated", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICListForFinalHearingUpdated {
    @CCD(
            label = "Type of final hearing",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICTypeOfHearing_v2"
    )
    @JsonProperty("etICTypeOfFinalHearingV2")
    private List<String> etICTypeOfFinalHearing;
    @CCD(ignore = true)
    @JsonProperty("etICTypeOfVideoHearingOrder")
    private List<String> etICTypeOfVideoHearingOrder;
    @CCD(ignore = true)
    @JsonProperty("etICTypeOfF2fHearingOrder")
    private List<String> etICTypeOfF2fHearingOrder;
    @CCD(ignore = true)
    @JsonProperty("etICHearingOrderBUCompliance")
    private String etICHearingOrderBUCompliance;
    @CCD(label = "Length of hearing", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("etICLengthOfFinalHearingV2")
    private String etICLengthOfFinalHearing;
    @CCD(
            label = "Days, Hours or Minutes",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingLength"
    )
    @JsonProperty("finalHearingLengthNumTypeV2")
    private String finalHearingLengthNumType;
    @CCD(
            label = "Listed judge or members",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_finalHearingListedJudgeOrMembers"
    )
    @JsonProperty("etICFinalHearingIsEJSitAlone")
    private String etICFinalHearingIsEJSitAlone;

    @CCD(
            label = "Listed judge or members reason",
            hint = "Select all that apply",
            showCondition = "etICFinalHearingIsEJSitAlone = \"JSA\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ms_FinalHearingIsEJSitAloneReasonYes"
    )
    @JsonProperty("etICFinalHearingIsEJSitAloneReasonYes")
    private List<String> etICFinalHearingIsEJSitAloneReasonYes;
    @CCD(ignore = true)
    @JsonProperty("etICFinalHearingIsEJSitAloneReasonYesOther")
    private String etICFinalHearingIsEJSitAloneReasonYesOther;
    @CCD(
            label = "Listed judge or members reason",
            hint = "Select all that apply",
            showCondition = "etICFinalHearingIsEJSitAlone = \"With members\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ms_FinalHearingIsEJSitAloneReasonNo"
    )
    @JsonProperty("etICFinalHearingIsEJSitAloneReasonNo")
    private List<String> etICFinalHearingIsEJSitAloneReasonNo;
    @CCD(ignore = true)
    @JsonProperty("etICFinalHearingIsEJSitAloneReasonNoOther")
    private String etICFinalHearingIsEJSitAloneReasonNoOther;

    @CCD(
            label = "Give reason for requesting EJ sit alone:",
            hint = "Select all that apply",
            showCondition = "etICFinalHearingIsEJSitAlone = \"dummy\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_FinalHearingIsEJSitAloneReason"
    )
    @JsonProperty("etICFinalHearingIsEJSitAloneReason")
    private String etICFinalHearingIsEJSitAloneReason;
    @CCD(ignore = true)
    @JsonProperty("etICNoLFinalHearingIsEJSitAloneReasonsJsa")
    private List<String> etICNoLFinalHearingIsEJSitAloneReasonsJsa;
    @CCD(
            label = "EJ Sit Alone Reason - JSA Other",
            showCondition = "etICFinalHearingIsEJSitAlone = \"JSA\" AND etICFinalHearingIsEJSitAloneReasonYes CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICNoLFinalHearingIsEJSitAloneReasonsJsaOther")
    private String  etICNoLFinalHearingIsEJSitAloneReasonsJsaOther;

    @CCD(ignore = true)
    @JsonProperty("etICNoLFinalHearingIsEJSitAloneReasonsMembers")
    private List<String> etICNoLFinalHearingIsEJSitAloneReasonsMembers;
    @CCD(
            label = "EJ Sit Alone Reason - JSA Other",
            showCondition = "etICFinalHearingIsEJSitAlone = \"With members\" AND etICFinalHearingIsEJSitAloneReasonNo CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICNoLFinalHearingIsEJSitAloneReasonsMembersOther")
    private String etICNoLFinalHearingIsEJSitAloneReasonsMembersOther;

    @CCD(
            label = "Further details",
            showCondition = "etICFinalHearingIsEJSitAlone != \"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICFinalHearingIsEJSitAloneFurtherDetails")
    private String etICFinalHearingIsEJSitAloneFurtherDetails;
}
