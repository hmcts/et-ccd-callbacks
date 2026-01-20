package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.UpdateCaseQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.UpdateManagementService;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Processes messages from the update_case_queue table.
 * Replaces UpdateCaseBusReceiverTask from et-message-handler.
 * 
 * NOTE: This is a basic implementation. Full business logic from UpdateManagementService,
 * SingleReadingService, and related services needs to be migrated.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCaseQueueProcessor {

    private static final int MAX_RETRIES = 10;
    private static final int LOCK_DURATION_MINUTES = 5;

    private final UpdateCaseQueueRepository updateCaseQueueRepository;
    private final ObjectMapper objectMapper;
    private final UpdateManagementService updateManagementService;

    @Value("${queue.update-case.batch-size:10}")
    private int batchSize;

    @Value("${queue.update-case.threads:15}")
    private int threadCount;

    private ExecutorService executor;
    private String processorId;

    public void init() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(threadCount);
            try {
                processorId = InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID();
            } catch (Exception e) {
                processorId = "processor-" + UUID.randomUUID();
            }
        }
    }

    @Scheduled(fixedDelayString = "${queue.update-case.poll-interval:1000}")
    public void processPendingMessages() {
        init();
        
        List<UpdateCaseQueueMessage> messages = updateCaseQueueRepository.findPendingMessages(
                LocalDateTime.now(),
                PageRequest.of(0, batchSize)
        );

        if (messages.isEmpty()) {
            return;
        }

        log.info("Found {} pending update-case messages to process", messages.size());

        messages.forEach(message -> executor.submit(() -> processMessage(message)));
    }

    @Transactional
    public void processMessage(UpdateCaseQueueMessage queueMessage) {
        // Try to lock the message
        int locked = updateCaseQueueRepository.lockMessage(
                queueMessage.getMessageId(),
                processorId,
                LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES),
                LocalDateTime.now()
        );

        if (locked == 0) {
            log.debug("Message {} already locked by another processor", queueMessage.getMessageId());
            return;
        }

        try {
            UpdateCaseMsg updateCaseMsg = objectMapper.readValue(
                    queueMessage.getMessageBody(),
                    UpdateCaseMsg.class
            );

            log.info("Processing update-case message: ethosCaseRef={}, multipleRef={}",
                    updateCaseMsg.getEthosCaseReference(), updateCaseMsg.getMultipleRef());

            // Call business logic (migrated from et-message-handler)
            // NOTE: This requires all dependent services to be migrated
            // If dependencies are missing, this will fail during Spring startup
            try {
                updateManagementService.updateLogic(updateCaseMsg);
                
                // Mark as completed
                updateCaseQueueRepository.markAsCompleted(
                        queueMessage.getMessageId(),
                        LocalDateTime.now()
                );
            } catch (javax.naming.NameNotFoundException e) {
                log.error("Name not found error processing message: {}", queueMessage.getMessageId(), e);
                throw new RuntimeException("Name not found", e);
            } catch (InterruptedException e) {
                log.error("Interrupted while processing message: {}", queueMessage.getMessageId(), e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted", e);
            }

            log.info("Processed update-case message: {}", queueMessage.getMessageId());

        } catch (Exception e) {
            handleError(queueMessage, e);
        }
    }

    @Transactional
    protected void handleError(UpdateCaseQueueMessage queueMessage, Exception e) {
        log.error("Error processing update-case message {}: {}",
                queueMessage.getMessageId(), e.getMessage(), e);

        int newRetryCount = queueMessage.getRetryCount() + 1;
        QueueMessageStatus newStatus = newRetryCount >= MAX_RETRIES
                ? QueueMessageStatus.FAILED
                : QueueMessageStatus.PENDING;

        updateCaseQueueRepository.markAsFailed(
                queueMessage.getMessageId(),
                e.getMessage(),
                newRetryCount,
                newStatus,
                LocalDateTime.now()
        );
    }
}
