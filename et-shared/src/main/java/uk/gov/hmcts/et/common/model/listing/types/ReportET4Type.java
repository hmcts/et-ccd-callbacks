package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@ComplexType(name = "reportET4", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReportET4Type {

    @CCD(label = "Actioned")
    @JsonProperty("actioned")
    private String actioned;
    @CCD(label = "BF Date")
    @JsonProperty("bfDate")
    private String bfDate;
    @CCD(label = "Cleared?")
    @JsonProperty("bfDateCleared")
    private String bfDateCleared;
    @CCD(label = "User")
    @JsonProperty("user")
    private String user;
}
