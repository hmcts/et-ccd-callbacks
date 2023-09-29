package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PreHearingDepositTypeItem {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private PreHearingDepositType value;
}
