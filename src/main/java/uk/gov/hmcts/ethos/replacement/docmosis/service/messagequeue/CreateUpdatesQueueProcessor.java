package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.TransferToEcmDataModel;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.CreateUpdatesQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.TransferToEcmService;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Processes messages from the create_updates_queue table.
 * Replaces CreateUpdatesBusReceiverTask from et-message-handler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUpdatesQueueProcessor {

    private static final int MAX_RETRIES = 10;
    private static final int LOCK_DURATION_MINUTES = 5;

    private final CreateUpdatesQueueRepository createUpdatesQueueRepository;
    private final UpdateCaseQueueSender updateCaseQueueSender;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<CreateUpdatesQueueProcessor> selfProvider;
    private final TransferToEcmService transferToEcmService;

    @Value("${queue.create-updates.batch-size:10}")
    private int batchSize;

    @Value("${queue.create-updates.threads:15}")
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

    @Scheduled(fixedDelayString = "${queue.create-updates.poll-interval:1000}")
    public void processPendingMessages() {
        log.info("Polling for create-updates messages...");
        init();
        
        List<CreateUpdatesQueueMessage> messages = createUpdatesQueueRepository.findPendingMessages(
                LocalDateTime.now(),
                PageRequest.of(0, batchSize)
        );

        log.info("Found {} messages in create-updates queue", messages.size());
        
        if (messages.isEmpty()) {
            return;
        }

        messages.forEach(message -> {
            log.info("Submitting message {} to executor", message.getMessageId());
            // Use self-proxy to ensure @Transactional works
            CreateUpdatesQueueProcessor self = selfProvider.getObject();
            executor.submit(() -> self.processMessage(message));
        });
    }

    @Transactional
    public void processMessage(CreateUpdatesQueueMessage queueMessage) {
        log.info("processMessage called for message: {}", queueMessage.getMessageId());
        
        // Try to lock the message
        int locked = createUpdatesQueueRepository.lockMessage(
                queueMessage.getMessageId(),
                processorId,
                LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES),
                LocalDateTime.now()
        );

        log.info("Lock attempt result for message {}: locked={}", queueMessage.getMessageId(), locked);
        
        if (locked == 0) {
            log.info("Message {} already locked by another processor", queueMessage.getMessageId());
            return;
        }

        try {
            CreateUpdatesMsg createUpdatesMsg = objectMapper.readValue(
                    queueMessage.getMessageBody(),
                    CreateUpdatesMsg.class
            );

            log.info("Processing create-updates message: {}", createUpdatesMsg.getMsgId());

            if (createUpdatesMsg.getDataModelParent() instanceof TransferToEcmDataModel) {
                transferToEcmService.transferToEcm(createUpdatesMsg);
            } else {
                sendUpdateCaseMessages(createUpdatesMsg);
            }

            // Mark as completed
            log.info("Marking message {} as completed", queueMessage.getMessageId());
            createUpdatesQueueRepository.markAsCompleted(
                    queueMessage.getMessageId(),
                    LocalDateTime.now()
            );

            log.info("Successfully processed create-updates message: {}", queueMessage.getMessageId());

        } catch (Exception e) {
            selfProvider.getObject().handleError(queueMessage, e);
        }
    }

    private void sendUpdateCaseMessages(CreateUpdatesMsg createUpdatesMsg) {
        if (createUpdatesMsg.getEthosCaseRefCollection() != null) {
            for (String ethosCaseReference : createUpdatesMsg.getEthosCaseRefCollection()) {
                UpdateCaseMsg updateCaseMsg = mapToUpdateCaseMsg(createUpdatesMsg, ethosCaseReference);
                updateCaseQueueSender.sendMessage(updateCaseMsg);
            }
        }
    }

    private UpdateCaseMsg mapToUpdateCaseMsg(CreateUpdatesMsg createUpdatesMsg, String ethosCaseReference) {
        return UpdateCaseMsg.builder()
                .msgId(UUID.randomUUID().toString())
                .multipleRef(createUpdatesMsg.getMultipleRef())
                .ethosCaseReference(ethosCaseReference)
                .totalCases(createUpdatesMsg.getTotalCases())
                .multipleReferenceLinkMarkUp(createUpdatesMsg.getMultipleReferenceLinkMarkUp())
                .jurisdiction(createUpdatesMsg.getJurisdiction())
                .caseTypeId(createUpdatesMsg.getCaseTypeId())
                .username(createUpdatesMsg.getUsername())
                .confirmation(createUpdatesMsg.getConfirmation())
                .dataModelParent(createUpdatesMsg.getDataModelParent())
                .build();
    }

    @Transactional
    protected void handleError(CreateUpdatesQueueMessage queueMessage, Exception ex) {
        log.error("Error processing create-updates message {}: {}",
                queueMessage.getMessageId(), ex.getMessage(), ex);

        int newRetryCount = queueMessage.getRetryCount() + 1;
        QueueMessageStatus newStatus = newRetryCount >= MAX_RETRIES
                ? QueueMessageStatus.FAILED
                : QueueMessageStatus.PENDING;

        createUpdatesQueueRepository.markAsFailed(
                queueMessage.getMessageId(),
                ex.getMessage(),
                newRetryCount,
                newStatus,
                LocalDateTime.now()
        );
    }
}
