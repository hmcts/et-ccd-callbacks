package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.repository.MessageQueueCandidateRepository;

@Component
@ConditionalOnProperty(name = "et.work-allocation.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class MessageQueueCandidateTask {

    private final MessageQueueCandidateRepository messageQueueCandidateRepository;
    private final MessageQueueCandidateHandler messageQueueCandidateHandler;

    @Scheduled(initialDelay = 10_000, fixedRateString = "${wa.message-queue-candidate-task.interval:5000}")
    public void runTask() {
        log.debug("Checking for new message queue candidates");
        messageQueueCandidateRepository.findByPublishedIsNullOrderByTimeStampAsc()
                .forEach(messageQueueCandidateHandler::handle);
    }
}
