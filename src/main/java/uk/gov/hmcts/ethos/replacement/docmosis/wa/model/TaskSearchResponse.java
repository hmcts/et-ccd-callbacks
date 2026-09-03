package uk.gov.hmcts.ethos.replacement.docmosis.wa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Response body for POST /task on the WA task management API.
 */
@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskSearchResponse {
    @JsonProperty("tasks")
    List<WaTask> tasks;
}
