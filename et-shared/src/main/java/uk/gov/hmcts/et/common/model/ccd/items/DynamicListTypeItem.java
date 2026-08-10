package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DynamicListCollection", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DynamicListTypeItem {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private DynamicListType value;
}
