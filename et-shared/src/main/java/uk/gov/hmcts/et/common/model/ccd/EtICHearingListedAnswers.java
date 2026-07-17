package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICHearingListedAnswers {
    @CCD(
            label = "Hearing already listed",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingAlreadyListed"
    )
    @JsonProperty("etICHearingListed")
    private List<String> etICHearingListed;
    @CCD(
            label = "Extend duration of hearing",
            hint = "Give details",
            showCondition = "etICHearingListed CONTAINS \"Extend duration of hearing\"",
            searchable = false
    )
    @JsonProperty("etICExtendDurationGiveDetails")
    private String etICExtendDurationGiveDetails;
    @CCD(
            label = "Other",
            hint = "Give details",
            showCondition = "etICHearingListed CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICOtherGiveDetails")
    private String etICOtherGiveDetails;

    @CCD(
            label = "Is this hearing judge alone or with members?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_hearingJudgeAloneOrWithMembers"
    )
    @JsonProperty("etICIsHearingWithJudgeOrMembers")
    private String etICIsHearingWithJudgeOrMembers;

    @CCD(
            label = "Hearing With Jsa",
            showCondition = "etICIsHearingWithJudgeOrMembers=\"JSA\" AND etInitialConsiderationListedHearingType!=\"\" AND etInitialConsiderationListedHearingType=\"Preliminary Hearing(CM)\"",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_listedCmPreliminaryHearing_Jsa"
    )
    @JsonProperty("etICIsHearingWithJsa")
    private String etICIsHearingWithJsa;
    @CCD(
            label = "Jsa Other - Details:",
            showCondition = "etICIsHearingWithJudgeOrMembers != \"\" AND etICIsHearingWithJudgeOrMembers = \"JSA\" AND etInitialConsiderationListedHearingType != \"\" AND etInitialConsiderationListedHearingType != \"Final Hearing\" AND etICIsFinalHearingWithJudgeOrMembersJsaReason CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICIsHearingWithJsaReasonOther")
    private String etICIsHearingWithJsaReasonOther;
    @CCD(
            label = "Even though this is a case management hearing in private, members’ experience is likely to add significant value to the process. \nGive reasons for this choice",
            showCondition = "etICIsHearingWithJudgeOrMembers = \"With members\" AND etInitialConsiderationListedHearingType != \"\" AND etInitialConsiderationListedHearingType = \"Preliminary Hearing(CM)\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICIsHearingWithMembers")
    private String etICIsHearingWithMembers;

    @CCD(
            label = "Reasons",
            showCondition = "etICIsHearingWithJudgeOrMembers != \"\" AND etInitialConsiderationListedHearingType != \"\" AND etInitialConsiderationListedHearingType = \"dummy\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_hearingWithJudgeOrMembersReasons"
    )
    @JsonProperty("etICIsHearingWithJudgeOrMembersReason")
    private List<String> etICIsHearingWithJudgeOrMembersReason;
    @CCD(
            label = "Members Other - Details:",
            showCondition = "etICIsHearingWithJudgeOrMembers != \"\" AND etICIsHearingWithJudgeOrMembers = \"With members\" AND etInitialConsiderationListedHearingType != \"Final Hearing\" AND etICIsFinalHearingWithJudgeOrMembersReason CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICIsHearingWithJudgeOrMembersReasonOther")
    private String etICIsHearingWithJudgeOrMembersReasonOther;

    @CCD(label = "Further details", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("etICIsHearingWithJudgeOrMembersFurtherDetails")
    private String etICIsHearingWithJudgeOrMembersFurtherDetails;

    @CCD(
            label = "Hearing JSA reasons",
            showCondition = "etICIsHearingWithJudgeOrMembers != \"\" AND etICIsHearingWithJudgeOrMembers = \"JSA\" AND etInitialConsiderationListedHearingType != \"\" AND etInitialConsiderationListedHearingType != \"Preliminary Hearing(CM)\" OR etICHearingListed != \"\" AND etICIsHearingWithJudgeOrMembers = \"JSA\" AND etInitialConsiderationListedHearingType != \"Preliminary Hearing(CM)\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_finalHearingWithJudgeOrMembersReasonsJsa"
    )
    @JsonProperty("etICIsFinalHearingWithJudgeOrMembersJsaReason")
    private List<String> etICIsFinalHearingWithJudgeOrMembersJsaReason;
    @CCD(
            label = "Jsa Final Other - Details:",
            showCondition = "etICIsHearingWithJudgeOrMembers = \"JSA\" AND etInitialConsiderationListedHearingType = \"Final Hearing\" AND etICIsFinalHearingWithJudgeOrMembersJsaReason CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICJsaFinalHearingReasonOther")
    private String etICJsaFinalHearingReasonOther;

    @CCD(
            label = "Other - Details:",
            showCondition = "etICIsHearingWithJudgeOrMembers = \"JSA\" AND etInitialConsiderationListedHearingType = \"Preliminary Hearing(CM)\" AND etICIsHearingWithJsa = \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICJsaCmPreliminaryHearingReasonOther")
    private String etICJsaCmPreliminaryHearingReasonOther;

    @CCD(
            label = "Hearing With Members reasons",
            showCondition = "etICIsHearingWithJudgeOrMembers != \"\" AND etICIsHearingWithJudgeOrMembers = \"With members\" AND etInitialConsiderationListedHearingType != \"\" AND etInitialConsiderationListedHearingType != \"Preliminary Hearing(CM)\" OR etICHearingListed != \"\" AND etICIsHearingWithJudgeOrMembers = \"With members\" AND etInitialConsiderationListedHearingType != \"Preliminary Hearing(CM)\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_finalHearingWithJudgeOrMembersReasonsMembers"
    )
    @JsonProperty("etICIsFinalHearingWithJudgeOrMembersReason")
    private List<String> etICIsFinalHearingWithJudgeOrMembersReason;
    @CCD(
            label = "Members Final Other - Details:",
            showCondition = "etICIsHearingWithJudgeOrMembers = \"With members\" AND etInitialConsiderationListedHearingType != \"\" AND etInitialConsiderationListedHearingType = \"Final Hearing\" AND etICIsFinalHearingWithJudgeOrMembersReason CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICMembersFinalHearingReasonOther")
    private String etICMembersFinalHearingReasonOther;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("etICHearingAnyOtherDirections")
    private String etICHearingAnyOtherDirections;

    @CCD(
            label = "Initial Consideration Listed Hearing Type",
            showCondition = "etICIsHearingWithJudgeOrMembersReason = \"dummy\"",
            searchable = false
    )
    @JsonProperty("etInitialConsiderationListedHearingType")
    private String etInitialConsiderationListedHearingType;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<h3>Any other directions (Optional)</h3><p>Are there any other issues or instructions to consider, or further orders to give?</p><p>This could include:</p><ul><li>Rule 49</li><li>Interpreters</li><li>Adjustments required for hearings</li><li>Further information required</li><li>Employer’s Contract Claim</li><li>Respondent’s identity</li><li>Time limits: claim or response</li></ul>",
          typeOverride = FieldType.Label
  )
  private String otherDirectionsLabel;
  // ==== end synthesised definition-only fields ====
}
