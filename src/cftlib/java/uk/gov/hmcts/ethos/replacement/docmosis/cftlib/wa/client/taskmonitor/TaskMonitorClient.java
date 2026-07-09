package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.client.taskmonitor;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "wa-task-monitor-client", url = "${wa.task-monitor.url:http://localhost:9194}")
public interface TaskMonitorClient {
    @PostMapping(value = "/monitor/tasks/jobs")
    void taskMonitorJob(@RequestHeader("ServiceAuthorization") String serviceAuthorization,
                        @RequestBody TaskMonitorJobRequest request);
}
