package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PreHearingDepositData {
    @JsonProperty("caseReferenceNumber")
    @CCD(label = "Case Reference Number", access = PreHearingDepositAccess.class)
    private String caseReferenceNumber;
    @JsonProperty("preHearingDepositImportFile")
    @CCD(label = "Pre-Hearing Deposit Import File", access = PreHearingDepositAccess.class)
    private ImportFile preHearingDepositImportFile;
    @JsonProperty("caseNumber")
    @CCD(label = "Case Number", access = PreHearingDepositAccess.class)
    private String caseNumber;
    @JsonProperty("payeeName")
    @CCD(label = "Payee Name (if different from applicant)", access = PreHearingDepositAccess.class)
    private String payeeName;
    @JsonProperty("regionOffice")
    @CCD(label = "Region Office", access = PreHearingDepositAccess.class)
    private String regionOffice;
    @JsonProperty("refundReference")
    @CCD(label = "Refund Reference", access = PreHearingDepositAccess.class)
    private String refundReference;
    @JsonProperty("dateSentForRefund")
    @CCD(label = "Date sent for Refund", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String dateSentForRefund;
    @JsonProperty("status")
    @CCD(label = "Status", access = PreHearingDepositAccess.class)
    private String status;
    @JsonProperty("journalConfirmedPaid")
    @CCD(label = "Journal Confirmed Paid", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String journalConfirmedPaid;
    @JsonProperty("comments")
    @CCD(label = "Other Comments", access = PreHearingDepositAccess.class)
    private String comments;
    @JsonProperty("journalConfirmedReceipt")
    @CCD(label = "Journal Confirmed Receipt", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String journalConfirmedReceipt;
    @JsonProperty("bankingDate")
    @CCD(label = "Banking Date", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String bankingDate;
    @JsonProperty("phrNumber")
    @CCD(label = "PHR No.", access = PreHearingDepositAccess.class)
    private String phrNumber;
    @JsonProperty("mr1Reference")
    @CCD(label = "MR1 Reference", access = PreHearingDepositAccess.class)
    private String mr1Reference;
    @JsonProperty("depositComments")
    @CCD(label = "Deposit Comments", access = PreHearingDepositAccess.class)
    private String depositComments;
    @JsonProperty("depositReceivedFrom")
    @CCD(label = "Deposit received From", access = PreHearingDepositAccess.class)
    private String depositReceivedFrom;
    @JsonProperty("documentUpload")
    @CCD(label = "Document Upload", access = PreHearingDepositAccess.class)
    private String documentUpload;
    @JsonProperty("depositRefundedTo")
    @CCD(label = "Deposit refunded to", access = PreHearingDepositAccess.class)
    private String depositRefundedTo;
    @JsonProperty("notes")
    @CCD(label = "Notes", access = PreHearingDepositAccess.class)
    private String notes;
    @JsonProperty("claimantOrRespondentName")
    @CCD(label = "Claimant or Respondent Name", access = PreHearingDepositAccess.class)
    private String claimantOrRespondentName;
    @JsonProperty("depositAmount")
    @CCD(label = "Deposit amount", typeOverride = FieldType.MoneyGBP,
        access = PreHearingDepositAccess.class)
    private String depositAmount;
    @JsonProperty("depositOrderedAgainst")
    @CCD(label = "Deposit ordered against", access = PreHearingDepositAccess.class)
    private String depositOrderedAgainst;
    @JsonProperty("depositRequestedBy")
    @CCD(label = "Deposit requested by", access = PreHearingDepositAccess.class)
    private String depositRequestedBy;
    @JsonProperty("depositCovers")
    @CCD(label = "Deposit covers", access = PreHearingDepositAccess.class)
    private String depositCovers;
    @JsonProperty("depositOrderSent")
    @CCD(label = "Deposit order sent", access = PreHearingDepositAccess.class)
    private String depositOrderSent;
    @JsonProperty("depositDue")
    @CCD(label = "Deposit due", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String depositDue;
    @JsonProperty("depositTimeExtension")
    @CCD(label = "Deposit time extension", access = PreHearingDepositAccess.class)
    private String depositTimeExtension;
    @JsonProperty("depositTimeExtensionDue")
    @CCD(label = "Deposit time extension due", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String depositTimeExtensionDue;
    @JsonProperty("depositReceived")
    @CCD(label = "Deposit received", access = PreHearingDepositAccess.class)
    private String depositReceived;
    @JsonProperty("dateDepositReceived")
    @CCD(label = "Date deposit received", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String dateDepositReceived;
    @JsonProperty("depositRefund")
    @CCD(label = "Deposit refund", typeOverride = FieldType.YesOrNo,
        access = PreHearingDepositAccess.class)
    private String depositRefund;
    @JsonProperty("amountRefunded")
    @CCD(label = "Amount refunded", typeOverride = FieldType.MoneyGBP,
        access = PreHearingDepositAccess.class)
    private String amountRefunded;
    @JsonProperty("depositRefundDate")
    @CCD(label = "Deposit refund date", typeOverride = FieldType.Date,
        access = PreHearingDepositAccess.class)
    private String depositRefundDate;
    @JsonProperty("chequeOrPONumber")
    @CCD(label = "Cheque or PO number", access = PreHearingDepositAccess.class)
    private String chequeOrPONumber;
    @JsonProperty("receivedBy")
    @CCD(label = "Deposit received by", access = PreHearingDepositAccess.class)
    private String receivedBy;
}
