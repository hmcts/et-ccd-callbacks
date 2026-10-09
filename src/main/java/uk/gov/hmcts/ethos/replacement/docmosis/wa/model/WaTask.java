package uk.gov.hmcts.ethos.replacement.docmosis.wa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaTask {
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
}
