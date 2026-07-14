package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;

@JsonIgnoreProperties(ignoreUnknown = true)
@CCD(access = PreHearingDepositAccess.class)
@Data
public class PreHearingDepositData {
    @JsonProperty("caseReferenceNumber")
    @CCD(label = "Case Reference Number")
    private String caseReferenceNumber;
    @JsonProperty("preHearingDepositImportFile")
    @CCD(label = "Pre-Hearing Deposit Import File")
    private ImportFile preHearingDepositImportFile;
    @JsonProperty("caseNumber")
    @CCD(label = "Case Number")
    private String caseNumber;
    @JsonProperty("payeeName")
    @CCD(label = "Payee Name (if different from applicant)")
    private String payeeName;
    @JsonProperty("regionOffice")
    @CCD(label = "Region Office")
    private String regionOffice;
    @JsonProperty("refundReference")
    @CCD(label = "Refund Reference")
    private String refundReference;
    @JsonProperty("dateSentForRefund")
    @CCD(label = "Date sent for Refund", typeOverride = FieldType.Date)
    private String dateSentForRefund;
    @JsonProperty("status")
    @CCD(label = "Status")
    private String status;
    @JsonProperty("journalConfirmedPaid")
    @CCD(label = "Journal Confirmed Paid", typeOverride = FieldType.Date)
    private String journalConfirmedPaid;
    @JsonProperty("comments")
    @CCD(label = "Other Comments")
    private String comments;
    @JsonProperty("journalConfirmedReceipt")
    @CCD(label = "Journal Confirmed Receipt", typeOverride = FieldType.Date)
    private String journalConfirmedReceipt;
    @JsonProperty("bankingDate")
    @CCD(label = "Banking Date", typeOverride = FieldType.Date)
    private String bankingDate;
    @JsonProperty("phrNumber")
    @CCD(label = "PHR No.")
    private String phrNumber;
    @JsonProperty("mr1Reference")
    @CCD(label = "MR1 Reference")
    private String mr1Reference;
    @JsonProperty("depositComments")
    @CCD(label = "Deposit Comments")
    private String depositComments;
    @JsonProperty("depositReceivedFrom")
    @CCD(label = "Deposit received From")
    private String depositReceivedFrom;
    @JsonProperty("documentUpload")
    @CCD(label = "Document Upload")
    private String documentUpload;
    @JsonProperty("depositRefundedTo")
    @CCD(label = "Deposit refunded to")
    private String depositRefundedTo;
    @JsonProperty("notes")
    @CCD(label = "Notes")
    private String notes;
    @JsonProperty("claimantOrRespondentName")
    @CCD(label = "Claimant or Respondent Name")
    private String claimantOrRespondentName;
    @JsonProperty("depositAmount")
    @CCD(label = "Deposit amount", typeOverride = FieldType.MoneyGBP)
    private String depositAmount;
    @JsonProperty("depositOrderedAgainst")
    @CCD(label = "Deposit ordered against")
    private String depositOrderedAgainst;
    @JsonProperty("depositRequestedBy")
    @CCD(label = "Deposit requested by")
    private String depositRequestedBy;
    @JsonProperty("depositCovers")
    @CCD(label = "Deposit covers")
    private String depositCovers;
    @JsonProperty("depositOrderSent")
    @CCD(label = "Deposit order sent")
    private String depositOrderSent;
    @JsonProperty("depositDue")
    @CCD(label = "Deposit due", typeOverride = FieldType.Date)
    private String depositDue;
    @JsonProperty("depositTimeExtension")
    @CCD(label = "Deposit time extension")
    private String depositTimeExtension;
    @JsonProperty("depositTimeExtensionDue")
    @CCD(label = "Deposit time extension due", typeOverride = FieldType.Date)
    private String depositTimeExtensionDue;
    @JsonProperty("depositReceived")
    @CCD(label = "Deposit received")
    private String depositReceived;
    @JsonProperty("dateDepositReceived")
    @CCD(label = "Date deposit received", typeOverride = FieldType.Date)
    private String dateDepositReceived;
    @JsonProperty("depositRefund")
    @CCD(label = "Deposit refund", typeOverride = FieldType.YesOrNo)
    private String depositRefund;
    @JsonProperty("amountRefunded")
    @CCD(label = "Amount refunded", typeOverride = FieldType.MoneyGBP)
    private String amountRefunded;
    @JsonProperty("depositRefundDate")
    @CCD(label = "Deposit refund date", typeOverride = FieldType.Date)
    private String depositRefundDate;
    @JsonProperty("chequeOrPONumber")
    @CCD(label = "Cheque or PO number")
    private String chequeOrPONumber;
    @JsonProperty("receivedBy")
    @CCD(label = "Deposit received by")
    private String receivedBy;
}
