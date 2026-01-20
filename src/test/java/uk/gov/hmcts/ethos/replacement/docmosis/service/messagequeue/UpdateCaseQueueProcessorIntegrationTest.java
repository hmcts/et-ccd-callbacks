package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.UpdateDataModel;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.UpdateCaseQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.UpdateManagementService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

/**
 * Integration test for UpdateCaseQueueProcessor.
 * Tests the end-to-end flow of processing case update messages from the database queue.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UpdateCaseQueueProcessorIntegrationTest {

    @Autowired
    private UpdateCaseQueueRepository queueRepository;

    @Autowired
    private UpdateCaseQueueProcessor queueProcessor;

    @MockBean
    private UpdateManagementService updateManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private UpdateCaseMsg testMessage;

    @BeforeEach
    void setUp() {
        queueRepository.deleteAll();
        
        // Create test update message
        testMessage = new UpdateCaseMsg();
        testMessage.setMsgId(UUID.randomUUID().toString());
        testMessage.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        testMessage.setJurisdiction("EMPLOYMENT");
        testMessage.setMultipleRef(SINGLE_CASE_TYPE);
        testMessage.setEthosCaseReference("1234567/2024");
        testMessage.setTotalCases("1");
        
        UpdateDataModel dataModel = UpdateDataModel.builder()
                .caseRefForUpdate("1234567/2024")
                .build();
        testMessage.setDataModelParent(dataModel);
    }

    @Test
    void shouldProcessPendingUpdateMessageSuccessfully() throws Exception {
        // Given: A pending update message in the queue
        String messageBody = objectMapper.writeValueAsString(testMessage);
        UpdateCaseQueueMessage queueMessage = UpdateCaseQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(queueMessage);

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Message is processed and marked as completed
        List<UpdateCaseQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals(QueueMessageStatus.COMPLETED, messages.get(0).getStatus());
        assertNotNull(messages.get(0).getProcessedAt());
        
        verify(updateManagementService, times(1)).updateLogic(any(UpdateCaseMsg.class));
    }

    @Test
    void shouldHandleUpdateFailureAndRetry() throws Exception {
        // Given: A message that will fail processing
        String messageBody = objectMapper.writeValueAsString(testMessage);
        UpdateCaseQueueMessage queueMessage = UpdateCaseQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(queueMessage);

        doThrow(new RuntimeException("Update failed"))
                .when(updateManagementService).updateLogic(any());

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Message is marked for retry
        List<UpdateCaseQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals(QueueMessageStatus.RETRY, messages.get(0).getStatus());
        assertEquals(1, messages.get(0).getRetryCount());
        assertNotNull(messages.get(0).getLastError());
    }

    @Test
    void shouldMarkUpdateAsFailedAfterMaxRetries() throws Exception {
        // Given: A message that has exceeded max retries
        String messageBody = objectMapper.writeValueAsString(testMessage);
        UpdateCaseQueueMessage queueMessage = UpdateCaseQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.RETRY)
                .createdAt(LocalDateTime.now())
                .retryCount(3)
                .build();
        queueRepository.save(queueMessage);

        doThrow(new RuntimeException("Persistent failure"))
                .when(updateManagementService).updateLogic(any());

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Message is marked as failed
        List<UpdateCaseQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals(QueueMessageStatus.FAILED, messages.get(0).getStatus());
        assertEquals(4, messages.get(0).getRetryCount());
    }

    @Test
    void shouldProcessMultipleCaseUpdatesInOrder() throws Exception {
        // Given: Multiple update messages for different cases
        for (int i = 1; i <= 3; i++) {
            UpdateCaseMsg msg = new UpdateCaseMsg();
            msg.setMsgId(UUID.randomUUID().toString());
            msg.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
            msg.setJurisdiction("EMPLOYMENT");
            msg.setMultipleRef(SINGLE_CASE_TYPE);
            msg.setEthosCaseReference("123456" + i + "/2024");
            msg.setTotalCases("1");
            
            String messageBody = objectMapper.writeValueAsString(msg);
            UpdateCaseQueueMessage queueMessage = UpdateCaseQueueMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageBody(messageBody)
                    .status(QueueMessageStatus.PENDING)
                    .createdAt(LocalDateTime.now().minusMinutes(4 - i)) // Different timestamps
                    .retryCount(0)
                    .build();
            queueRepository.save(queueMessage);
        }

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: All messages are processed
        List<UpdateCaseQueueMessage> messages = queueRepository.findAll();
        assertEquals(3, messages.size());
        assertEquals(3, messages.stream()
                .filter(m -> m.getStatus() == QueueMessageStatus.COMPLETED)
                .count());
        
        verify(updateManagementService, times(3)).updateLogic(any(UpdateCaseMsg.class));
    }

    @Test
    void shouldHandleMultipleUpdatesWithLockedMessages() throws Exception {
        // Given: One message being processed, one pending
        String messageBody1 = objectMapper.writeValueAsString(testMessage);
        UpdateCaseQueueMessage processingMessage = UpdateCaseQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody1)
                .status(QueueMessageStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(processingMessage);

        UpdateCaseMsg msg2 = new UpdateCaseMsg();
        msg2.setMsgId(UUID.randomUUID().toString());
        msg2.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        msg2.setJurisdiction("EMPLOYMENT");
        msg2.setMultipleRef(SINGLE_CASE_TYPE);
        msg2.setEthosCaseReference("9999999/2024");
        msg2.setTotalCases("1");
        
        String messageBody2 = objectMapper.writeValueAsString(msg2);
        UpdateCaseQueueMessage pendingMessage = UpdateCaseQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody2)
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(pendingMessage);

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Only pending message is processed, processing message untouched
        List<UpdateCaseQueueMessage> messages = queueRepository.findAll();
        assertEquals(2, messages.size());
        
        assertEquals(1, messages.stream()
                .filter(m -> m.getStatus() == QueueMessageStatus.PROCESSING)
                .count());
        assertEquals(1, messages.stream()
                .filter(m -> m.getStatus() == QueueMessageStatus.COMPLETED)
                .count());
        
        verify(updateManagementService, times(1)).updateLogic(any(UpdateCaseMsg.class));
    }

    @Test
    void shouldUpdateLastErrorFieldOnFailure() throws Exception {
        // Given: A message that will fail with specific error
        String messageBody = objectMapper.writeValueAsString(testMessage);
        UpdateCaseQueueMessage queueMessage = UpdateCaseQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(queueMessage);

        String expectedError = "Database connection failed";
        doThrow(new RuntimeException(expectedError))
                .when(updateManagementService).updateLogic(any());

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Error message is captured
        List<UpdateCaseQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertNotNull(messages.get(0).getLastError());
        assertEquals(expectedError, messages.get(0).getLastError());
    }
}
