package uk.gov.hmcts.ethos.replacement.docmosis.wa.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Request body for POST /task on the WA task management API.
 */
@Data
@Builder
@Jacksonized
public class TaskSearchRequest {
    @JsonProperty("search_parameters")
    List<TaskSearchParameter> searchParameters;
}
