package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.client.taskmonitor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskMonitorJobRequest {

    @JsonProperty("job_details")
    private final JobDetails jobDetails;

    public static TaskMonitorJobRequest initiation() {
        return build(JobName.INITIATION);
    }

    public static TaskMonitorJobRequest reconfiguration() {
        return build(JobName.RECONFIGURATION);
    }

    public static TaskMonitorJobRequest termination() {
        return build(JobName.TERMINATION);
    }

    private static TaskMonitorJobRequest build(JobName name) {
        return TaskMonitorJobRequest.builder()
                .jobDetails(JobDetails.builder().name(name).build())
                .build();
    }

    @Data
    @Builder
    public static class JobDetails {
        private final JobName name;
    }
}
