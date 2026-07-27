package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.client.wacaseeventhandler;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "wa-case-event-handler-client", url = "${wa.case-event-handler.url:http://localhost:8088}")
public interface WaCaseEventHandlerClient {
    @PostMapping(value = "/messages/jobs/{jobName}")
    void messagesJob(@RequestHeader("ServiceAuthorization") String serviceAuthorization,
                     @PathVariable("jobName") JobName jobName);
}
