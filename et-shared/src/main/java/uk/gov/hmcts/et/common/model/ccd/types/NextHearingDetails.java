package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NextHearingDetails {
    @CCD(label = " ")
    @JsonProperty("hearingID")
    private String hearingID;
    @CCD(label = " ")
    @JsonProperty("hearingDateTime")
    private String hearingDateTime;
}
