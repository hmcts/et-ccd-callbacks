package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICHearingListedAnswers {
    @JsonProperty("etICHearingListed")
    private List<String> etICHearingListed;
    @JsonProperty("etICExtendDurationGiveDetails")
    private String etICExtendDurationGiveDetails;
    @JsonProperty("etICOtherGiveDetails")
    private String etICOtherGiveDetails;
    @JsonProperty("etICPostponeGiveDetails")
    private String etICPostponeGiveDetails;
    @JsonProperty("etICConvertPreliminaryGiveDetails")
    private String etICConvertPreliminaryGiveDetails;
    @JsonProperty("etICConvertF2fGiveDetails")
    private String etICConvertF2fGiveDetails;

    @JsonProperty("etICIsHearingWithJudgeOrMembers")
    private String etICIsHearingWithJudgeOrMembers;

    @JsonProperty("etICIsHearingWithJsa")
    private String etICIsHearingWithJsa;
    @JsonProperty("etICIsHearingWithJsaReasonOther")
    private String etICIsHearingWithJsaReasonOther;
    @JsonProperty("etICIsHearingWithMembers")
    private String etICIsHearingWithMembers;

    @JsonProperty("etICIsHearingWithJudgeOrMembersReason")
    private List<String> etICIsHearingWithJudgeOrMembersReason;
    @JsonProperty("etICIsHearingWithJudgeOrMembersReasonOther")
    private String etICIsHearingWithJudgeOrMembersReasonOther;

    @JsonProperty("etICIsHearingWithJudgeOrMembersFurtherDetails")
    private String etICIsHearingWithJudgeOrMembersFurtherDetails;

    @JsonProperty("etICIsFinalHearingWithJudgeOrMembersJsaReason")
    private List<String> etICIsFinalHearingWithJudgeOrMembersJsaReason;
    @JsonProperty("etICJsaFinalHearingReasonOther")
    private String etICJsaFinalHearingReasonOther;

    @JsonProperty("etICJsaCmPreliminaryHearingReasonOther")
    private String etICJsaCmPreliminaryHearingReasonOther;

    @JsonProperty("etICIsFinalHearingWithJudgeOrMembersReason")
    private List<String> etICIsFinalHearingWithJudgeOrMembersReason;
    @JsonProperty("etICMembersFinalHearingReasonOther")
    private String etICMembersFinalHearingReasonOther;

    @JsonProperty("otherDirectionsLabel")
    private String otherDirectionsLabel;
    @JsonProperty("etICAnyOtherDirectionsDividerHrLabel3")
    private String etICAnyOtherDirectionsDividerHrLabel3;
    @JsonProperty("etICHearingAnyOtherDirections")
    private String etICHearingAnyOtherDirections;

    @JsonProperty("etInitialConsiderationListedHearingType")
    private String etInitialConsiderationListedHearingType;
}
