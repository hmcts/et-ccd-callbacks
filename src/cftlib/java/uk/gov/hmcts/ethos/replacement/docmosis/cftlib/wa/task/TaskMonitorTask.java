package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.client.taskmonitor.TaskMonitorClient;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.client.taskmonitor.TaskMonitorJobRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Component
@ConditionalOnProperty(name = "et.work-allocation.enabled", havingValue = "true")
@Slf4j
public class TaskMonitorTask {

    private final TaskMonitorClient taskMonitorClient;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public TaskMonitorTask(TaskMonitorClient taskMonitorClient,
                           @Qualifier("waAuthTokenGenerator") AuthTokenGenerator authTokenGenerator) {
        this.taskMonitorClient = taskMonitorClient;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Scheduled(initialDelay = 10_000, fixedRateString = "${wa.task-monitor.initiation-task.interval:5000}")
    public void runInitiationTask() {
        log.debug("Executing WA Task Monitor Initiation request");
        taskMonitorClient.taskMonitorJob(authTokenGenerator.generate(), TaskMonitorJobRequest.initiation());
    }

    @Scheduled(initialDelay = 10_000, fixedRateString = "${wa.task-monitor.reconfiguration-task.interval:60000}")
    public void runReconfigurationTask() {
        log.debug("Executing WA Task Monitor Reconfiguration request");
        taskMonitorClient.taskMonitorJob(authTokenGenerator.generate(), TaskMonitorJobRequest.reconfiguration());
    }

    @Scheduled(initialDelay = 10_000, fixedRateString = "${wa.task-monitor.termination-task.interval:60000}")
    public void runTerminationTask() {
        log.debug("Executing WA Task Monitor Termination request");
        taskMonitorClient.taskMonitorJob(authTokenGenerator.generate(), TaskMonitorJobRequest.termination());
    }
}
