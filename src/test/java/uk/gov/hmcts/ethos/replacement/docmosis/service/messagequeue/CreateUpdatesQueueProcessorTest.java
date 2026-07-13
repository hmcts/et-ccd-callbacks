package uk.gov.hmcts.ethos.replacement.docmosis.service.messagequeue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreateMultiplesDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.TransferToEcmDataModel;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.CreateUpdatesQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.CreateMultiplesService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
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
    private ObjectProvider<CreateUpdatesQueueProcessor> selfProvider;

    @Mock
    private uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler.TransferToEcmService transferToEcmService;

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private CreateMultiplesService createMultiplesService;

    private CreateUpdatesQueueProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CreateUpdatesQueueProcessor(
                createUpdatesQueueRepository,
                updateCaseQueueSender,
                objectMapper,
                selfProvider,
                transferToEcmService,
                adminUserService,
                createMultiplesService
        );
        lenient().when(selfProvider.getObject()).thenReturn(processor);
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
        verify(updateCaseQueueSender, times(2)).sendMessage(any(UpdateCaseMsg.class)); // 2 cases in the collection
        verify(createUpdatesQueueRepository).markAsCompleted(eq(queueMessage.getMessageId()), any());
    }

    @Test
    void processMessage_alreadyLocked() {
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
        verify(createUpdatesQueueRepository).incrementRetryAndMarkFailureIfMax(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(10),
                isNull()
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
        verify(createUpdatesQueueRepository).incrementRetryAndMarkFailureIfMax(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(10),
                isNull()
        );
    }

    @Test
    void processMessage_runtimeExceptionAtMaxRetry_setsProcessedAtTimestamp() throws Exception {
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        queueMessage.setRetryCount(9);

        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class)))
            .thenThrow(new RuntimeException("Failed"));
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        processor.processMessage(queueMessage);

        verify(createUpdatesQueueRepository).incrementRetryAndMarkFailureIfMax(
            eq(queueMessage.getMessageId()),
            anyString(),
            eq(10),
            any(LocalDateTime.class)
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

    @Test
    void processMessage_transferToEcmDataModel() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        msg.setDataModelParent(new TransferToEcmDataModel());
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class))).thenReturn(msg);
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).lockMessage(anyString(), anyString(), any(), any());
        verify(transferToEcmService).transferToEcm(msg);
        verify(updateCaseQueueSender, never()).sendMessage(any(UpdateCaseMsg.class));
        verify(createUpdatesQueueRepository).markAsCompleted(eq(queueMessage.getMessageId()), any());
    }

    @Test
    void processMessage_createMultiplesDataModel_noAdditionalClaimants() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        msg.setDataModelParent(CreateMultiplesDataModel.builder().additionalClaimants(List.of()).build());
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class))).thenReturn(msg);
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createMultiplesService, never()).retrieveLeadCase(anyString(), any(CreateUpdatesMsg.class));
        verify(createMultiplesService, never()).createCase(any(), anyString(), any(), any());
        verify(createMultiplesService, never()).createMultipleShell(anyString(), any(), any(), anyList(), anyMap());
        verify(createUpdatesQueueRepository).markAsCompleted(eq(queueMessage.getMessageId()), any());
    }

    @Test
    void processMessage_createMultiplesDataModel_leadCaseMissing_retriesMessage() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        msg.setDataModelParent(CreateMultiplesDataModel.builder()
                .additionalClaimants(List.of(new AdditionalClaimant()))
                .build());
        msg.setEthosCaseRefCollection(List.of("240001/2024"));

        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class))).thenReturn(msg);
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);
        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(createMultiplesService.retrieveLeadCase("token", msg)).thenReturn(null);

        // When
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).incrementRetryAndMarkFailureIfMax(
                eq(queueMessage.getMessageId()),
                anyString(),
                eq(10),
                isNull()
        );
        verify(createMultiplesService, never()).createMultipleShell(anyString(), any(), any(), anyList(), anyMap());
    }

    @Test
    void processMessage_createMultiplesDataModel() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateMultiplesDataModel dataModel = CreateMultiplesDataModel.builder()
                .additionalClaimants(new ArrayList<>(List.of(
                        new AdditionalClaimant(),
                        new AdditionalClaimant(),
                        new AdditionalClaimant(),
                        new AdditionalClaimant())))
                .build();
        msg.setDataModelParent(dataModel);
        msg.setEthosCaseRefCollection(List.of("240001/2024"));

        SubmitEvent leadCase = new SubmitEvent();
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class))).thenReturn(msg);
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);
        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(createMultiplesService.retrieveLeadCase("token", msg)).thenReturn(leadCase);
        when(createMultiplesService.createCase(eq(leadCase), eq("token"), eq(msg), any()))
                .thenReturn("240002/2024", "240003/2024", null, null, null, null, null, null);

        // When
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        processor.processMessage(queueMessage);

        // Then
        verify(createMultiplesService).retrieveLeadCase("token", msg);
        verify(createMultiplesService, times(8))
                .createCase(eq(leadCase), eq("token"), eq(msg), any());
        ArgumentCaptor<List> createdRefsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map> failedCasesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(createMultiplesService).createMultipleShell(eq("token"), eq(msg),
                eq(leadCase), createdRefsCaptor.capture(), failedCasesCaptor.capture());
        assertEquals(2, createdRefsCaptor.getValue().size());
        assertEquals(2, failedCasesCaptor.getValue().size());
        verify(updateCaseQueueSender, never()).sendMessage(any(UpdateCaseMsg.class));
        verify(createUpdatesQueueRepository).markAsCompleted(eq(queueMessage.getMessageId()), any());
    }

    @Test
    void processMessage_unprocessableEntity_noRetry() throws Exception {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable Entity",
                HttpHeaders.EMPTY,
                "{\"message\":\"Case data validation failed\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        
        when(objectMapper.readValue(anyString(), eq(CreateUpdatesMsg.class)))
                .thenThrow(exception);
        when(createUpdatesQueueRepository.lockMessage(anyString(), anyString(), any(), any())).thenReturn(1);

        // When
        processor.processMessage(queueMessage);

        // Then
        verify(createUpdatesQueueRepository).markAsFailedNoRetry(
                eq(queueMessage.getMessageId()),
                anyString(),
                any()
        );
        verify(createUpdatesQueueRepository, never()).incrementRetryAndMarkFailureIfMax(anyString(), anyString(),
                any(Integer.class), any());
    }

    @Test
    void processPendingMessages_emptyQueue() {
        // Given
        when(createUpdatesQueueRepository.findPendingMessages(any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // When
        processor.processPendingMessages();

        // Then
        verify(createUpdatesQueueRepository).findPendingMessages(any(LocalDateTime.class), any(PageRequest.class));
        verify(selfProvider, never()).getObject();
    }

    @Test
    void processPendingMessages_withMessages() {
        // Given
        CreateUpdatesMsg msg = generateCreateUpdatesMsg();
        CreateUpdatesQueueMessage queueMessage = createQueueMessage(msg);
        List<CreateUpdatesQueueMessage> messages = Collections.singletonList(queueMessage);
        
        when(createUpdatesQueueRepository.findPendingMessages(any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(messages);

        // When
        processor.processPendingMessages();

        // Then
        verify(createUpdatesQueueRepository).findPendingMessages(any(LocalDateTime.class), any(PageRequest.class));
        verify(selfProvider).getObject();
        // Note: actual message processing happens in the executor thread, so we can't verify it directly
    }

    private CreateUpdatesQueueMessage createQueueMessage(CreateUpdatesMsg msg) {
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
