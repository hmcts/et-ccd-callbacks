package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
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
@RequiredArgsConstructor
public class UpdateCaseQueueSender {

    private final UpdateCaseQueueRepository updateCaseQueueRepository;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<UpdateCaseQueueSender> selfProvider;

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
        } catch (JsonProcessingException | DataAccessException e) {
            log.error("Failed to send UpdateCaseMsg to queue", e);
            throw new IllegalStateException("Failed to queue update case message", e);
        }
    }

    public void sendMessageAsync(UpdateCaseMsg updateCaseMsg) {
        // For compatibility with existing code that expects async behavior
        selfProvider.getObject().sendMessage(updateCaseMsg);
    }

    /**
     * Builds and enqueues an {@link UpdateCaseMsg} for a multiple-creation payload.
     * The message targets the lead case (identified by {@code ethosCaseReference}) so that
     * {@code SingleReadingService} can retrieve it as the template for the new case.
     *
     * @param createUpdatesMsg the originating create-updates message carrying the
     *                         {@code CreateMultiplesDataModel} and shared metadata
     * @param ethosCaseReference ethos case reference of the lead case
     */
    @Transactional
    public void sendCreateMultiplesMessage(CreateUpdatesMsg createUpdatesMsg, String ethosCaseReference) {
        UpdateCaseMsg updateCaseMsg = UpdateCaseMsg.builder()
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
        sendMessage(updateCaseMsg);
    }
}
