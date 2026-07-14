package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@ComplexType(name = "reportRespondent", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReportRespondentType {
    @CCD(label = "Date")
    @JsonProperty("et3ReceivedDate")
    private String et3ReceivedDate;
    @CCD(label = "Respondent")
    @JsonProperty("respondentName")
    private String respondentName;
}
