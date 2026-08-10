package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Deposit", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DepositType {

    @CCD(
            label = "Deposit amount (£)",
            hint = "Enter a value less than £10,000",
            min = 0,
            max = 999900,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("Deposit_amount")
    private String depositAmount;
    @CCD(label = "Deposit ordered against", typeOverride = FieldType.DynamicList)
    @JsonProperty("dynamicDepositOrderAgainst")
    private DynamicFixedListType dynamicDepositOrderAgainst;
    @CCD(
            label = "Deposit ordered against",
            showCondition = "deposit_covers=\"dummy\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("depositOrderAgainst")
    private String depositOrderAgainst;
    @CCD(label = "Deposit requested by", typeOverride = FieldType.DynamicList)
    @JsonProperty("dynamicDepositRequestedBy")
    private DynamicFixedListType dynamicDepositRequestedBy;
    @CCD(
            label = "Deposit requested by",
            showCondition = "deposit_covers=\"dummy\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_DepositRequestedBy"
    )
    @JsonProperty("deposit_requested_by")
    private String depositRequestedBy;
    @CCD(label = "Deposit covers", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_DepositCovers")
    @JsonProperty("deposit_covers")
    private String depositCovers;
    @CCD(label = "Deposit order sent", typeOverride = FieldType.Date)
    @JsonProperty("deposit_order_sent")
    private String depositOrderSent;
    @CCD(label = "Deposit due", typeOverride = FieldType.Date)
    @JsonProperty("deposit_due")
    private String depositDue;
    @CCD(label = "Deposit received", typeOverride = FieldType.YesOrNo)
    @JsonProperty("depositReceived")
    private String depositReceived;
    @CCD(
            label = "Date deposit received",
            showCondition = "depositReceived=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("depositReceivedDate")
    private String depositReceivedDate;
    @CCD(label = "Deposit time extension", typeOverride = FieldType.YesOrNo)
    @JsonProperty("deposit_time_ext")
    private String depositTimeExt;
    @CCD(
            label = "Deposit time extension due",
            showCondition = "deposit_time_ext=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("deposit_time_ext_due")
    private String depositTimeExtDue;
    @CCD(label = "Deposit refund", typeOverride = FieldType.YesOrNo)
    @JsonProperty("deposit_refund")
    private String depositRefund;
    @CCD(
            label = "Deposit refund date",
            showCondition = "deposit_refund=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("deposit_refund_date")
    private String depositRefundDate;
    @CCD(label = "Deposit refunded to", showCondition = "deposit_refund=\"Yes\"", typeOverride = FieldType.DynamicList)
    @JsonProperty("dynamicDepositRefundedTo")
    private DynamicFixedListType dynamicDepositRefundedTo;
    @CCD(
            label = "Deposit refunded to",
            showCondition = "deposit_covers=\"dummy\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantOrRespondent"
    )
    @JsonProperty("depositRefundedTo")
    private String depositRefundedTo;
    @CCD(label = "Notes", typeOverride = FieldType.TextArea)
    @JsonProperty("depositNotes")
    private String depositNotes;
    @CCD(label = "Document Upload", categoryID = "C27")
    @JsonProperty("depositDoc")
    private Document depositDoc;
    @CCD(
            label = "Amount Refunded",
            showCondition = "deposit_refund=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonProperty("depositAmountRefunded")
    private String depositAmountRefunded;
}
