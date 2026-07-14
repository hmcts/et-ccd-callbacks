package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCDCollectionValue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@CCDCollectionValue
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimServedTypeItem {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private ClaimServedType value;
}
