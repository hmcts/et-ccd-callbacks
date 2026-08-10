package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JudgmentDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JudgementDetailsType {

    @CCD(label = "Folio Number")
    @JsonProperty("folio_number")
    private String folioNumber;
    @CCD(label = "Reasons given", typeOverride = FieldType.YesOrNo)
    @JsonProperty("reasons_given")
    private String reasonsGiven;
    @CCD(
            label = "Date reasons issued",
            showCondition = "reasons_given=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("date_reasons_issued")
    private String dateReasonsIssued;
    @CCD(label = "Award made?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("awardMade")
    private String awardMade;
    @CCD(
            label = "Financial award made?",
            showCondition = "awardMade=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("financialAwardMade")
    private String financialAwardMade;
    @CCD(
            label = "Remedy left to parties",
            showCondition = "non-financial_award=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("remedy_left_to_parties")
    private String remedyLeftToParties;
    @CCD(
            label = "Reinstate / reengage order",
            showCondition = "non-financial_award=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Reinstate"
    )
    @JsonProperty("reinstate_reengage_order")
    private String reinstateReengageOrder;
    @CCD(
            label = "Reinstated / reengaged",
            showCondition = "non-financial_award=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Reinstated"
    )
    @JsonProperty("reinstated_reengaged")
    private String reinstatedReengaged;
    @CCD(
            label = "Cert. of correction date",
            showCondition = "certificateOfCorrection=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("cert_of_correction_date")
    private String certOfCorrectionDate;
    @CCD(
            label = "Cert. of correction sent",
            showCondition = "certificateOfCorrection=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("cert_of_correction_sent")
    private String certOfCorrectionSent;
    @CCD(label = "Non-financial award", typeOverride = FieldType.YesOrNo)
    @JsonProperty("non-financial_award")
    private String nonFinancialAward;
    @CCD(
            label = "Total award £",
            showCondition = "financialAwardMade=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("total_award")
    private String totalAward;
    @CCD(
            label = "Adjustment (Old Regs) / ACAS Code Adj (New Regs) ",
            showCondition = "financialAwardMade=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Adjustment"
    )
    @JsonProperty("adjustment")
    private String adjustment;
    @CCD(
            label = "Adjustment % (New Regs)",
            hint = "0 - 25%",
            showCondition = "adjustment=\"Increase (New regs)\" OR adjustment=\"Decrease (New regs)\"",
            retainHiddenValue = true,
            min = 0,
            max = 25,
            typeOverride = FieldType.Number
    )
    @JsonProperty("adjustmentPercentage")
    private String adjustmentPercentage;
    @CCD(label = "Certificate of Correction Issued?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("certificateOfCorrection")
    private String certificateOfCorrection;
}
