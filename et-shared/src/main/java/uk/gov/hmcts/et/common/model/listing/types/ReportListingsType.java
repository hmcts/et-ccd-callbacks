package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@ComplexType(name = "reportListings", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReportListingsType {

    @CCD(label = "Date")
    @JsonProperty("listedDate")
    private String listedDate;
    @CCD(label = "Number")
    @JsonProperty("hearingNumber")
    private String hearingNumber;
    @CCD(label = "Type")
    @JsonProperty("hearingType")
    private String hearingType;
    @CCD(label = "Status")
    @JsonProperty("hearingStatus")
    private String hearingStatus;
    @CCD(label = "Clerk")
    @JsonProperty("hearingClerk")
    private String hearingClerk;
}
