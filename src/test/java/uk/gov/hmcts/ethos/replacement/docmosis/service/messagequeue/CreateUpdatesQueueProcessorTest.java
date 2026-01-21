package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.CreateUpdatesQueueRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUpdatesQueueProcessorTest {

    @Mock
    private CreateUpdatesQueueRepository createUpdatesQueueRepository;

    @Mock
    private UpdateCaseQueueSender updateCaseQueueSender;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.TransferToEcmService transferToEcmService;

    private CreateUpdatesQueueProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CreateUpdatesQueueProcessor(
                createUpdatesQueueRepository,
                updateCaseQueueSender,
                objectMapper,
                applicationContext,
                transferToEcmService
        );
        ReflectionTestUtils.setField(processor, "threadCount", 5);
        ReflectionTestUtils.setField(processor, "batchSize", 10);
        processor.init(); // Initialize processor ID and executor
    }

    @Test
    void processMessage_success() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class))).thenReturn(msg);
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).lockMessage(anyString(), anyString(), any(), any());
        verify(updateCaseQueueSender, times(2)).sendMessage(any(UpdateCaseMsg.class)); // 2 cases in collection
        verify(createUpdatesQueueRepository).markAsCompleted(eq(queueMessage.getMessageId()), any());
    }

    @Test
    void processMessage_alreadyLocked() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(0);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).lockMessage(anyString(), anyString(), any(), any());
        verify(updateCaseQueueSender, never()).sendMessage(any());
        verify(createUpdatesQueueRepository, never()).markAsCompleted(anyString(), any());
    }

    @Test
    void processMessage_emptyEthosCaseRefCollection() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        msg.setEthosCaseRefCollection(null);
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class))).thenReturn(msg);
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).lockMessage(anyString(), anyString(), any(), any());
        verify(updateCaseQueueSender, never()).sendMessage(any());
        verify(createUpdatesQueueRepository).markAsCompleted(eq(queueMessage.getMessageId()), any());
    }

    @Test
    void processMessage_jsonParseException() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class)))
                .thenThrow(new com.fasterxml.jackson.databind.JsonMappingException(null, "Failed to parse"));
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).markAsFailed(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(1),
                eq(QueueMessageStatus.PENDING),
                any()
        );
    }

    @Test
    void processMessage_runtimeException() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class)))
                .thenThrow(new RuntimeException("Failed"));
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).markAsFailed(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(1),
                eq(QueueMessageStatus.PENDING),
                any()
        );
    }

    private CreateUpdatesMsg generateCreateUpdatesMsg() {
        CreateUpdatesMsg msg = new CreateUpdatesMsg();
        msg.setMsgId(UUID.randomUUID().toString());
        msg.setJurisdiction("EMPLOYMENT");
        msg.setCaseTypeId("ET_EnglandWales");
        msg.setMultipleRef("6000001");
        msg.setEthosCaseRefCollection(Arrays.asList("240001/2024", "240002/2024"));
        msg.setTotalCases("2");
        msg.setUsername("test@test.com");
        msg.setDataModelParent(new CloseDataModel());
        return msg;
    }

    private CreateUpdatesQueueMessage createQueueMessage(CreateUpdatesMsg msg) throws Exception {
        return CreateUpdatesQueueMessage.builder()
                .id(1L)
                .messageId(msg.getMsgId())
                .messageBody("{\"msgId\":\"" + msg.getMsgId() + "\"}")
                .status(QueueMessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .build();
    }
}
