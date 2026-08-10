package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "BFActions", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BFActionTypeItem {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private BFActionType value;

}
