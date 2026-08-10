package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CounterClaim", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EccCounterClaimType {

    @CCD(label = " ")
    @JsonProperty("counterClaim")
    private String counterClaim;
}
