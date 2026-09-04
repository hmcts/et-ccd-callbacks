package uk.gov.hmcts.ethos.replacement.docmosis.wa.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Reason supplied when terminating a task. "completed" is the value WA treats as a
 * successful completion rather than a cancellation or deletion.
 */
@Data
@Builder
@Jacksonized
public class TerminateInfo {
    @JsonProperty("terminate_reason")
    String terminateReason;
}
