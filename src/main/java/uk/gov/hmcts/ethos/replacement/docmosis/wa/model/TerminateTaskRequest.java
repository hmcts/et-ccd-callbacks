package uk.gov.hmcts.ethos.replacement.docmosis.wa.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Request body for DELETE /task/{task-id} on the WA task management API.
 */
@Data
@Builder
@Jacksonized
public class TerminateTaskRequest {
    @JsonProperty("terminate_info")
    TerminateInfo terminateInfo;
}
