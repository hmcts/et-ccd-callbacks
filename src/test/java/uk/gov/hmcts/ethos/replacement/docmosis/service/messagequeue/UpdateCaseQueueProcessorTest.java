package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.UpdateCaseQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.UpdateManagementService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCaseQueueProcessorTest {

    @Mock
    private UpdateCaseQueueRepository updateCaseQueueRepository;

    @Mock
    private UpdateManagementService updateManagementService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationContext applicationContext;

    private UpdateCaseQueueProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new UpdateCaseQueueProcessor(
                updateCaseQueueRepository,
                objectMapper,
                updateManagementService,
                applicationContext
        );
        ReflectionTestUtils.setField(processor, "threadCount", 5);
        ReflectionTestUtils.setField(processor, "batchSize", 10);
        processor.init(); // Initialize processor ID and executor
    }

    @Test
    void processMessage_success() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);
        
        when(objectMapper.readValue(anyString(), eq(UpdateCaseMsg.class))).thenReturn(msg);
        when(updateCaseQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(updateCaseQueueRepository).lockMessage(anyString(), anyString(), any(), any());
        verify(updateManagementService).updateLogic(msg);
        verify(updateCaseQueueRepository).markAsCompleted(eq(queueMessage.getMessageId()), any());
    }

    @Test
    void processMessage_alreadyLocked() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);
        
        when(updateCaseQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(0);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(updateCaseQueueRepository).lockMessage(anyString(), anyString(), any(), any());
        verify(updateManagementService, never()).updateLogic(any());
        verify(updateCaseQueueRepository, never()).markAsCompleted(anyString(), any());
    }

    @Test
    void processMessage_ioException_unrecoverable() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();

        when(objectMapper.readValue(anyString(), eq(UpdateCaseMsg.class))).thenReturn(msg);
        when(updateCaseQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);
        doThrow(new IOException("Connection failed")).when(updateManagementService)
                .updateLogic(any());
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(updateCaseQueueRepository).markAsFailed(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(1),
                eq(QueueMessageStatus.FAILED),
                any()
        );
        verify(updateManagementService).addUnrecoverableErrorToDatabase(msg);
    }

    @Test
    void processMessage_runtimeException_recoverable() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();

        when(objectMapper.readValue(anyString(), eq(UpdateCaseMsg.class))).thenReturn(msg);
        when(updateCaseQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);
        doThrow(new RuntimeException("Temporary failure")).when(updateManagementService)
                .updateLogic(any());
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(updateCaseQueueRepository).markAsFailed(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(1),
                eq(QueueMessageStatus.PENDING),
                any()
        );
        verify(updateManagementService, never()).addUnrecoverableErrorToDatabase(any());
    }

    @Test
    void processMessage_runtimeException_maxRetriesReached() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);
        queueMessage.setRetryCount(9); // MAX_RETRIES = 10, so this is the last retry
        
        when(objectMapper.readValue(anyString(), eq(UpdateCaseMsg.class))).thenReturn(msg);
        when(updateCaseQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);
        doThrow(new RuntimeException("Temporary failure")).when(updateManagementService)
                .updateLogic(any());

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(updateCaseQueueRepository).markAsFailed(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(10),
                eq(QueueMessageStatus.FAILED),
                any()
        );
        verify(updateManagementService).addUnrecoverableErrorToDatabase(msg);
        verify(updateManagementService).checkIfFinish(msg);
    }

    @Test
    void processMessage_jsonParseException() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);
        
        when(objectMapper.readValue(anyString(), eq(UpdateCaseMsg.class)))
                .thenThrow(new com.fasterxml.jackson.databind.JsonMappingException(null, "Failed to parse"));
        when(updateCaseQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(updateCaseQueueRepository).markAsFailed(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(1),
                eq(QueueMessageStatus.PENDING),
                any()
        );
    }

    private UpdateCaseMsg generateUpdateCaseMsg() {
        UpdateCaseMsg msg = new UpdateCaseMsg();
        msg.setMsgId(UUID.randomUUID().toString());
        msg.setJurisdiction("EMPLOYMENT");
        msg.setCaseTypeId("ET_EnglandWales");
        msg.setMultipleRef("6000001");
        msg.setEthosCaseReference("240001/2024");
        msg.setTotalCases("10");
        msg.setUsername("test@test.com");
        msg.setDataModelParent(new CloseDataModel());
        return msg;
    }

    @Test
    void processMessage_interruptedException() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();

        when(objectMapper.readValue(anyString(), eq(UpdateCaseMsg.class))).thenReturn(msg);
        when(updateCaseQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);
        doThrow(new InterruptedException("Thread interrupted")).when(updateManagementService)
                .updateLogic(any());
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);

        // When
        processor.processMessage(queueMessage);

        // Then
        // InterruptedException should be caught and handled, completing without error
        verify(updateManagementService).updateLogic(msg);
        // Thread interruption should not trigger any failure marking
        verify(updateCaseQueueRepository, never()).markAsCompleted(anyString(), any());
        verify(updateCaseQueueRepository, never()).markAsFailed(anyString(), anyString(), any(Integer.class),
            any(), any());
    }

    @Test
    void processPendingMessages_emptyQueue() {
        // Given
        when(updateCaseQueueRepository.findPendingMessages(any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // When
        processor.processPendingMessages();

        // Then
        verify(updateCaseQueueRepository).findPendingMessages(any(LocalDateTime.class), any(PageRequest.class));
        verify(applicationContext, never()).getBean(UpdateCaseQueueProcessor.class);
    }

    @Test
    void processPendingMessages_withMessages() {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        UpdateCaseQueueMessage queueMessage = createQueueMessage(msg);
        List<UpdateCaseQueueMessage> messages = Arrays.asList(queueMessage);
        
        when(updateCaseQueueRepository.findPendingMessages(any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(messages);
        when(applicationContext.getBean(UpdateCaseQueueProcessor.class))
                .thenReturn(processor);

        // When
        processor.processPendingMessages();

        // Then
        verify(updateCaseQueueRepository).findPendingMessages(any(LocalDateTime.class), any(PageRequest.class));
        verify(applicationContext).getBean(UpdateCaseQueueProcessor.class);
        // Note: actual message processing happens in executor thread, so we can't verify it directly
    }

    private UpdateCaseQueueMessage createQueueMessage(UpdateCaseMsg msg) {
        return UpdateCaseQueueMessage.builder()
                .id(1L)
                .messageId(msg.getMsgId())
                .messageBody("{\"msgId\":\"" + msg.getMsgId() + "\"}")
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }
}
