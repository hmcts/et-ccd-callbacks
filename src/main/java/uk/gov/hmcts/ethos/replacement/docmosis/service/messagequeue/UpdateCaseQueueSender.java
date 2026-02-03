package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.UpdateCaseQueueRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service to send UpdateCaseMsg to the database queue.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "queue", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class UpdateCaseQueueSender {

    private final UpdateCaseQueueRepository updateCaseQueueRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void sendMessage(UpdateCaseMsg updateCaseMsg) {
        try {
            String messageBody = objectMapper.writeValueAsString(updateCaseMsg);
            UpdateCaseQueueMessage queueMessage = UpdateCaseQueueMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageBody(messageBody)
                    .status(QueueMessageStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            updateCaseQueueRepository.save(queueMessage);
            log.info("Sent UpdateCaseMsg to database queue: ethosCaseRef={}, multipleRef={}",
                    updateCaseMsg.getEthosCaseReference(), updateCaseMsg.getMultipleRef());
        } catch (Exception e) {
            log.error("Failed to send UpdateCaseMsg to queue", e);
            throw new RuntimeException("Failed to queue update case message", e);
        }
    }

    @Transactional
    public void sendMessageAsync(UpdateCaseMsg updateCaseMsg) {
        // For compatibility with existing code that expects async behavior
        sendMessage(updateCaseMsg);
    }
}
