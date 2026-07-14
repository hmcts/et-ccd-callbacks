package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@ComplexType(name = "claimServedItemsListing", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimServedType {
    // For days less than 6, this is the same as the actual number of days.
    // But for 6 days or more of serving claim, this might differ from
    // the actual number of days. E.g. 12 days of serving claim
    // results in 6+ reported and 12 actual days in the Serving Claims report
    @CCD(label = "Reported number of Days To Serving Claim")
    @JsonProperty("reportedNumberOfDays")
    private String reportedNumberOfDays;
    @CCD(label = "Actual number of Days To Serving Claim")
    @JsonProperty("actualNumberOfDays")
    private String actualNumberOfDays;

    // Claim accepted or rejected
    @CCD(label = "Claim Served Type (accepted or rejected)")
    @JsonProperty("claimServedType")
    private String claimServedType;
    @CCD(label = "Claim Served Case Number")
    @JsonProperty("claimServedCaseNumber")
    private String claimServedCaseNumber;
    @CCD(label = "Case Receipt Date")
    @JsonProperty("caseReceiptDate")
    private String caseReceiptDate;
    @CCD(label = "Claim Served Date")
    @JsonProperty("claimServedDate")
    private String claimServedDate;
}
