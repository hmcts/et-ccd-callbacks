package uk.gov.hmcts.ethos.replacement.docmosis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TaskSearchResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.wa.model.TerminateTaskRequest;

/**
 * Client for the Work Allocation task management API.
 */
@FeignClient(name = "wa-task-management-api", url = "${wa.task-management.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface WaTaskApiClient {

    @PostMapping(value = "/task", consumes = "application/json")
    TaskSearchResponse searchTasks(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody TaskSearchRequest searchRequest
    );

    @DeleteMapping(value = "/task/{taskId}", consumes = "application/json")
    void terminateTask(
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @PathVariable("taskId") String taskId,
            @RequestBody TerminateTaskRequest terminateTaskRequest
    );
}
