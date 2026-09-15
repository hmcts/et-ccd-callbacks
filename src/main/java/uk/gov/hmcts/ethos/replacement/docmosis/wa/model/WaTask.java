package uk.gov.hmcts.ethos.replacement.docmosis.wa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * Subset of the WA task representation that this service needs. The API returns many more
 * fields; they are ignored deliberately so that additions upstream do not break deserialisation.
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaTask {
    @JsonProperty("id")
    String id;
    @JsonProperty("type")
    String type;
    @JsonProperty("task_title")
    String taskTitle;
    @JsonProperty("assignee")
    String assignee;
    @JsonProperty("additional_properties")
    Map<String, String> additionalProperties;
}
