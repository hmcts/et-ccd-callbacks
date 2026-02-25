package uk.gov.hmcts.ecm.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class HearingListingType {
    @JsonProperty("hearingDateType")
    private String hearingDateType;
    @JsonProperty("hearingDateFrom")
    private String hearingDateFrom;
    @JsonProperty("hearingDateTo")
    private String hearingDateTo;
    @JsonProperty("hearingDate")
    private String hearingDate;
}
