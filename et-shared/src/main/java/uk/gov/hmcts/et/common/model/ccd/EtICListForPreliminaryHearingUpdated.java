package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "etICHearingNotListedListForPrelimHearingUpdated", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICListForPreliminaryHearingUpdated {
    @CCD(
            label = "Type of preliminary hearing",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICTypeOfHearing_v2"
    )
    @JsonProperty("etICTypeOfPreliminaryHearingV2")
    private List<String> etICTypeOfPreliminaryHearing;
    @CCD(
            label = "Purpose of preliminary hearing",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICPurposeOfPrelimHearing_v2"
    )
    @JsonProperty("etICPurposeOfPreliminaryHearingV2")
    private List<String> etICPurposeOfPreliminaryHearing;
    @CCD(
            label = "Give details of hearing notice",
            showCondition = "etICPurposeOfPreliminaryHearingV2 != \"\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICGiveDetailsOfHearingNoticeV2")
    private String etICGiveDetailsOfHearingNotice;
    @CCD(label = "Length of hearing", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("etICLengthOfPrelimHearingV2")
    private String etICLengthOfPrelimHearing;
    @CCD(
            label = "Days, Hours or Minutes",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingLength"
    )
    @JsonProperty("prelimHearingLengthNumTypeV2")
    private String prelimHearingLengthNumType;
    @CCD(
            label = "Do you consider this preliminary hearing should be listed with members?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("etICIsPreliminaryHearingWithMembersV2")
    private String etICIsPreliminaryHearingWithMembers;
    @CCD(
            label = "Give reasons for requiring members",
            showCondition = "etICIsPreliminaryHearingWithMembersV2=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICIsPreliminaryHearingWithMembersReasonV2")
    private String etICIsPreliminaryHearingWithMembersReason;
}
