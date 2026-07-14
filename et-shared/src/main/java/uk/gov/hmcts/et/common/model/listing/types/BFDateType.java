package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@ComplexType(name = "bfReport", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BFDateType {

    @CCD(label = "Case Number")
    @JsonProperty("caseReference")
    private String caseReference;
    @CCD(label = "Action")
    @JsonProperty("broughtForwardAction")
    private String broughtForwardAction;
    @CCD(label = "B/F Date", typeOverride = FieldType.Date)
    @JsonProperty("broughtForwardDate")
    private String broughtForwardDate;
    @CCD(label = "Cleared?", typeOverride = FieldType.Date)
    @JsonProperty("broughtForwardDateCleared")
    private String broughtForwardDateCleared;
    @CCD(label = "B/F Entered Date")
    @JsonProperty("broughtForwardEnteredDate")
    private String broughtForwardEnteredDate;
    @CCD(label = "Comments")
    @JsonProperty("broughtForwardDateReason")
    private String broughtForwardDateReason;
}
