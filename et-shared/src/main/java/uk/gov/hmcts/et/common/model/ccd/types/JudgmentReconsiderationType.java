package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "judgmentReconsideration", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JudgmentReconsiderationType {

    @CCD(label = "Has a reconsideration application been made?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("reconsideration")
    private String reconsideration;
    @CCD(
            label = "Date of application for reconsideration",
            showCondition = "reconsideration=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("reconsiderationDate")
    private String reconsiderationDate;
    @CCD(
            label = "Did the Tribunal reconsider the Judgment on its own initiative",
            showCondition = "reconsideration=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("reconsiderationOwnInitiative")
    private String reconsiderationOwnInitiative;
    @CCD(
            label = "Who applied for reconsideration?",
            showCondition = "reconsiderationOwnInitiative=\"No\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("reconsiderationPartyInitiative")
    private String reconsiderationPartyInitiative;
    @CCD(ignore = true)
    @JsonProperty("dynamicReconsiderationPartyInitiative")
    private DynamicFixedListType dynamicReconsiderationPartyInitiative;
    @CCD(
            label = "Employment Judge's direction",
            showCondition = "reconsideration=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_JudgeDirection"
    )
    @JsonProperty("reconsiderationDirection")
    private String reconsiderationDirection;
    @CCD(
            label = "Employment Judge's decision",
            showCondition = "reconsideration=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_JudgeDecision"
    )
    @JsonProperty("reconsiderationDecision")
    private String reconsiderationDecision;
}
