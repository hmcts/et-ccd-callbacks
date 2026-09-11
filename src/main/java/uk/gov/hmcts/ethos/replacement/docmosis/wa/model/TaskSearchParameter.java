package uk.gov.hmcts.ethos.replacement.docmosis.wa.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * A single search parameter for the WA task management search endpoint.
 */
@Data
@Builder
@Jacksonized
public class TaskSearchParameter {
    @JsonProperty("key")
    String key;
    @JsonProperty("operator")
    String operator;
    @JsonProperty("values")
    List<String> values;
}
