package uk.gov.hmcts.et.common.model.listing.items;

import uk.gov.hmcts.ccd.sdk.api.CCDCollectionValue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.listing.types.BFDateType;

@CCDCollectionValue
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BFDateTypeItem {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private BFDateType value;
}
