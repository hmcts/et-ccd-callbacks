package uk.gov.hmcts.et.common.model.ccd.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "tseAdminDecision", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class TseAdminRecordDecisionTypeItem {

    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private TseAdminRecordDecisionType value;

}
