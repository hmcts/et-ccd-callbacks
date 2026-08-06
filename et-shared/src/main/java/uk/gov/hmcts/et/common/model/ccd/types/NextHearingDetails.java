package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "nextHearingDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NextHearingDetails {
    @CCD(label = " ")
    @JsonProperty("hearingID")
    private String hearingID;
    @CCD(label = " ", typeOverride = FieldType.DateTime)
    @JsonProperty("hearingDateTime")
    private String hearingDateTime;
}
