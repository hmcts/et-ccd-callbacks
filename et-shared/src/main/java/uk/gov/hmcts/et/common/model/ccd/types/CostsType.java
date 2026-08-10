package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JudgmentCosts", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CostsType {

    @CCD(label = "Have costs been awarded?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("costs_question")
    private String costsQuestion;
    @CCD(
            label = "Costs/Expenses awarded to",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("costs_expenses_awarded_to")
    private String costsExpensesAwardedTo;
    @CCD(
            label = "Costs/Expenses awarded against",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("costs_expenses_awarded_against")
    private String costExpensesAwardedAgainst;
    @CCD(
            label = "Costs/Expenses amount awarded",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("costs_expenses_awarded_amount")
    private String costsExpensesAwardedAmount;
    @CCD(
            label = "Preparation of time awarded to",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("preparation_of_time_awarded_to")
    private String preparationOfTimeAwardedTo;
    @CCD(
            label = "Preparation of time awarded against",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("preparation_of_time_awarded_against")
    private String preparationOfTimeAwardedAgainst;
    @CCD(
            label = "Preparation of time amount awarded",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("preparation_of_time_amount_awarded")
    private String preparationOfTimeAmountAwarded;
    @CCD(
            label = "Wasted cost awarded to",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("wasted_cost_awarded_to")
    private String wastedCostAwardedTo;
    @CCD(
            label = "Wasted cost awarded against",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("wasted_cost_awarded_against")
    private String wastedCostAwardedAgainst;
    @CCD(
            label = "Wasted cost amount awarded",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("wasted_cost_amount_awarded")
    private String wastedCostAmountAwarded;
    @CCD(
            label = "Pro Bono costs awarded to",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_costs_pro_bono_awarded_to"
    )
    @JsonProperty("pro_bono_costs_awarded_to")
    private String proBonoCostsAwardedTo;
    @CCD(
            label = "Pro Bono costs awarded against",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_pro_bono_awarded_against"
    )
    @JsonProperty("pro_bono_costs_awarded_against")
    private String proBonoCostsAwardedAgainst;
    @CCD(
            label = "Pro Bono costs amount awarded",
            showCondition = "costs_question=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("pro_bono_costs_amount_awarded")
    private String proBonoCostsAmountAwarded;
}
