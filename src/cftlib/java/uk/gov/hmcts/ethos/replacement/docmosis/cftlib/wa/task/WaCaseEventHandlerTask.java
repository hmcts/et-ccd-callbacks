package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.client.wacaseeventhandler.JobName;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.client.wacaseeventhandler.WaCaseEventHandlerClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
@ConditionalOnProperty(name = "et.work-allocation.enabled", havingValue = "true")
@Slf4j
public class WaCaseEventHandlerTask {

    private final WaCaseEventHandlerClient waCaseEventHandlerClient;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public WaCaseEventHandlerTask(WaCaseEventHandlerClient waCaseEventHandlerClient,
                                  @Qualifier("waAuthTokenGenerator") AuthTokenGenerator authTokenGenerator) {
        this.waCaseEventHandlerClient = waCaseEventHandlerClient;
        this.authTokenGenerator = authTokenGenerator;
    }

    @Scheduled(initialDelay = 10_000,
               fixedRateString = "${wa.case-event-handler.find-problem-messages-task.interval:60000}")
    public void runFindProblemMessagesTask() {
        log.debug("Executing WA Case Event Handler Find Problem Messages request");
        waCaseEventHandlerClient.messagesJob(authTokenGenerator.generate(), JobName.FIND_PROBLEM_MESSAGES);
    }
}
