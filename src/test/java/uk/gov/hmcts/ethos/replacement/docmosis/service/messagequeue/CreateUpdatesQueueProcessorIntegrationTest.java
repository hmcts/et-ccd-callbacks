package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.CreateUpdatesQueueRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

/**
 * Integration test for CreateUpdatesQueueProcessor.
 * Tests the end-to-end flow of processing create updates messages from the database queue.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreateUpdatesQueueProcessorIntegrationTest {

    @Autowired
    private CreateUpdatesQueueRepository queueRepository;

    @Autowired
    private CreateUpdatesQueueProcessor queueProcessor;

    @MockBean
    private CcdClient ccdClient;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUpdatesMsg testMessage;
    private SubmitEvent testCase;

    @BeforeEach
    void setUp() {
        queueRepository.deleteAll();
        
        // Create test message
        testMessage = new CreateUpdatesMsg();
        testMessage.setMsgId(UUID.randomUUID().toString());
        testMessage.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        testMessage.setJurisdiction("EMPLOYMENT");
        testMessage.setMultipleRef("TEST123");
        testMessage.setEthosCaseRefCollection(Collections.singletonList("1234567/2024"));
        
        CreationDataModel dataModel = CreationDataModel.builder()
                .lead("1234567/2024")
                .multipleRef("TEST123")
                .build();
        testMessage.setDataModelParent(dataModel);
        
        // Create test case
        testCase = new SubmitEvent();
        testCase.setCaseId(12345L);
    }

    @Test
    void shouldProcessPendingMessageSuccessfully() throws Exception {
        // Given: A pending message in the queue
        String messageBody = objectMapper.writeValueAsString(testMessage);
        CreateUpdatesQueueMessage queueMessage = CreateUpdatesQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(queueMessage);

        when(ccdClient.retrieveCasesElasticSearch(any(), any(), any()))
                .thenReturn(Collections.singletonList(testCase));

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Message is marked as completed
        List<CreateUpdatesQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals(QueueMessageStatus.COMPLETED, messages.get(0).getStatus());
        
        verify(ccdClient, times(1)).retrieveCasesElasticSearch(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any());
    }

    @Test
    void shouldHandleProcessingErrorAndRetry() throws Exception {
        // Given: A message that will fail processing
        String messageBody = objectMapper.writeValueAsString(testMessage);
        CreateUpdatesQueueMessage queueMessage = CreateUpdatesQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(queueMessage);

        when(ccdClient.retrieveCasesElasticSearch(any(), any(), any()))
                .thenThrow(new RuntimeException("Test exception"));

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Message is marked for retry
        List<CreateUpdatesQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals(QueueMessageStatus.RETRY, messages.get(0).getStatus());
        assertEquals(1, messages.get(0).getRetryCount());
    }

    @Test
    void shouldMarkMessageAsFailedAfterMaxRetries() throws Exception {
        // Given: A message that has reached max retries
        String messageBody = objectMapper.writeValueAsString(testMessage);
        CreateUpdatesQueueMessage queueMessage = CreateUpdatesQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.RETRY)
                .createdAt(LocalDateTime.now())
                .retryCount(3)
                .build();
        queueRepository.save(queueMessage);

        when(ccdClient.retrieveCasesElasticSearch(any(), any(), any()))
                .thenThrow(new RuntimeException("Test exception"));

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Message is marked as failed
        List<CreateUpdatesQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals(QueueMessageStatus.FAILED, messages.get(0).getStatus());
    }

    @Test
    void shouldProcessMultipleMessagesInBatch() throws Exception {
        // Given: Multiple pending messages
        for (int i = 0; i < 5; i++) {
            String messageBody = objectMapper.writeValueAsString(testMessage);
            CreateUpdatesQueueMessage queueMessage = CreateUpdatesQueueMessage.builder()
                    .messageId(UUID.randomUUID().toString())
                    .messageBody(messageBody)
                    .status(QueueMessageStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();
            queueRepository.save(queueMessage);
        }

        when(ccdClient.retrieveCasesElasticSearch(any(), any(), any()))
                .thenReturn(Collections.singletonList(testCase));

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: All messages are processed
        List<CreateUpdatesQueueMessage> messages = queueRepository.findAll();
        assertEquals(5, messages.size());
        assertEquals(5, messages.stream()
                .filter(m -> m.getStatus() == QueueMessageStatus.COMPLETED)
                .count());
    }

    @Test
    void shouldSkipMessagesInProcessingStatus() throws Exception {
        // Given: A message in PROCESSING status
        String messageBody = objectMapper.writeValueAsString(testMessage);
        CreateUpdatesQueueMessage queueMessage = CreateUpdatesQueueMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .messageBody(messageBody)
                .status(QueueMessageStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
        queueRepository.save(queueMessage);

        // When: Processor runs
        queueProcessor.processQueue();

        // Then: Message status unchanged
        List<CreateUpdatesQueueMessage> messages = queueRepository.findAll();
        assertEquals(1, messages.size());
        assertEquals(QueueMessageStatus.PROCESSING, messages.get(0).getStatus());
        
        verify(ccdClient, times(0)).retrieveCasesElasticSearch(any(), any(), any());
    }
}
