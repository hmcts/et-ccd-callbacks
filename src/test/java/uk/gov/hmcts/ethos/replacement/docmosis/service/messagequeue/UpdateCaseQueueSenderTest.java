package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.UpdateCaseQueueRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCaseQueueSenderTest {

    @Mock
    private UpdateCaseQueueRepository updateCaseQueueRepository;

    @Mock
    private ObjectMapper objectMapper;

    private UpdateCaseQueueSender sender;

    @BeforeEach
    void setUp() {
        sender = new UpdateCaseQueueSender(updateCaseQueueRepository, objectMapper);
    }

    @Test
    void sendMessage_success() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        String messageBody = "{\"msgId\":\"" + msg.getMsgId() + "\"}";
        
        when(objectMapper.writeValueAsString(msg)).thenReturn(messageBody);
        when(updateCaseQueueRepository.save(any(UpdateCaseQueueMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sender.sendMessage(msg);

        // Then
        ArgumentCaptor<UpdateCaseQueueMessage> captor = ArgumentCaptor.forClass(UpdateCaseQueueMessage.class);
        verify(updateCaseQueueRepository).save(captor.capture());
        
        UpdateCaseQueueMessage savedMessage = captor.getValue();
        assertNotNull(savedMessage.getMessageId());
        assertEquals(messageBody, savedMessage.getMessageBody());
        assertEquals(QueueMessageStatus.PENDING, savedMessage.getStatus());
        assertEquals(0, savedMessage.getRetryCount());
        assertNotNull(savedMessage.getCreatedAt());
    }

    @Test
    void sendMessage_jsonSerializationException() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        
        when(objectMapper.writeValueAsString(msg))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Serialization failed") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> sender.sendMessage(msg));
        assertEquals("Failed to queue update case message", exception.getMessage());
    }

    @Test
    void sendMessage_repositoryException() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        String messageBody = "{\"msgId\":\"" + msg.getMsgId() + "\"}";
        
        when(objectMapper.writeValueAsString(msg)).thenReturn(messageBody);
        when(updateCaseQueueRepository.save(any(UpdateCaseQueueMessage.class)))
                .thenThrow(new DataAccessResourceFailureException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> sender.sendMessage(msg));
        assertEquals("Failed to queue update case message", exception.getMessage());
    }

    @Test
    void sendMessageAsync_delegatesToSendMessage() throws Exception {
        // Given
        UpdateCaseMsg msg = generateUpdateCaseMsg();
        String messageBody = "{\"msgId\":\"" + msg.getMsgId() + "\"}";
        
        when(objectMapper.writeValueAsString(msg)).thenReturn(messageBody);
        when(updateCaseQueueRepository.save(any(UpdateCaseQueueMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        sender.sendMessageAsync(msg);

        // Then
        verify(updateCaseQueueRepository).save(any(UpdateCaseQueueMessage.class));
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
}
