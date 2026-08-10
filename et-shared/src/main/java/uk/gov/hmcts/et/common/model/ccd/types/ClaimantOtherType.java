package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "EmploymentDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimantOtherType {

    @CCD(label = "Occupation", searchable = false)
    @JsonProperty("claimant_occupation")
    private String claimantOccupation;
    @CCD(label = "Employed from", searchable = false, typeOverride = FieldType.Date)
    @JsonProperty("claimant_employed_from")
    private String claimantEmployedFrom;
    @CCD(label = "Is the employment continuing?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("claimant_employed_currently")
    private String claimantEmployedCurrently;
    @CCD(
            label = "Employed to",
            showCondition = "claimant_employed_currently=\"No\"",
            searchable = false,
            typeOverride = FieldType.Date
    )
    @JsonProperty("claimant_employed_to")
    private String claimantEmployedTo;
    @CCD(
            label = "Notice Period End Date",
            showCondition = "claimant_employed_currently=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.Date
    )
    @JsonProperty("claimant_employed_notice_period")
    private String claimantEmployedNoticePeriod;
    @CCD(
            label = "Are there any disabilities or special requirements?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("claimant_disabled")
    private String claimantDisabled;
    @CCD(
            label = "Please provide details",
            showCondition = "claimant_disabled=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("claimant_disabled_details")
    private String claimantDisabledDetails;
    @CCD(label = "Notice Period", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("claimant_notice_period")
    private String claimantNoticePeriod;
    @CCD(
            label = "Notice Weeks or Months",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_notice_period_unit"
    )
    @JsonProperty("claimant_notice_period_unit")
    private String claimantNoticePeriodUnit;
    @CCD(label = "Notice Period Duration", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("claimant_notice_period_duration")
    private String claimantNoticePeriodDuration;
    @CCD(label = "Average weekly hours", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("claimant_average_weekly_hours")
    private String claimantAverageWeeklyHours;
    @CCD(label = "Pay before tax", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("claimant_pay_before_tax")
    private String claimantPayBeforeTax;
    @CCD(label = "Pay after tax", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("claimant_pay_after_tax")
    private String claimantPayAfterTax;
    @CCD(
            label = "Weekly, monthly or annual pay",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_pay_cycle"
    )
    @JsonProperty("claimant_pay_cycle")
    private String claimantPayCycle;
    @CCD(
            label = "Pension Scheme",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_pension_contribution"
    )
    @JsonProperty("claimant_pension_contribution")
    private String claimantPensionContribution;
    @CCD(label = "Pension Contribution", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("claimant_pension_weekly_contribution")
    private String claimantPensionWeeklyContribution;
    @CCD(label = "Employee Benefits", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("claimant_benefits")
    private String claimantBenefits;
    @CCD(label = "Employee Benefits Details", searchable = false)
    @JsonProperty("claimant_benefits_detail")
    private String claimantBenefitsDetail;
    @CCD(label = "Did you work for the organisation or person", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("pastEmployer")
    private String pastEmployer;
    @CCD(
            label = "Are you still working for the organisation or person",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_StillWorking"
    )
    @JsonProperty("stillWorking")
    private String stillWorking;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Employment Details", searchable = false, typeOverride = FieldType.Label)
  private String claimantEmploymentDetails;
  // ==== end synthesised definition-only fields ====
}
