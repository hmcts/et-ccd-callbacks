package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DateListed", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DateListedTypeItem {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private DateListedType value;
}
