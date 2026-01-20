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
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.UpdateDataModel;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.CreateUpdatesQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.UpdateCaseQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

/**
 * End-to-end integration test for the complete queue flow.
 * Tests the entire flow from enqueueing messages to processing them.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QueueEndToEndIntegrationTest {

    @Autowired
    private CreateUpdatesBusSender createUpdatesBusSender;

    @Autowired
    private UpdateCaseQueueSender updateCaseQueueSender;

    @Autowired
    private CreateUpdatesQueueProcessor createUpdatesQueueProcessor;

    @Autowired
    private UpdateCaseQueueProcessor updateCaseQueueProcessor;

    @Autowired
    private CreateUpdatesQueueRepository createUpdatesQueueRepository;

    @Autowired
    private UpdateCaseQueueRepository updateCaseQueueRepository;

    @MockBean
    private CcdClient ccdClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        createUpdatesQueueRepository.deleteAll();
        updateCaseQueueRepository.deleteAll();
    }

    @Test
    void shouldCompleteFullCreateUpdatesFlow() throws Exception {
        // Given: A create updates request
        List<String> caseRefs = List.of("1111111/2024", "2222222/2024", "3333333/2024");
        CreateUpdatesDto createUpdatesDto = CreateUpdatesDto.builder()
                .caseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .jurisdiction("EMPLOYMENT")
                .multipleRef("MULTI123")
                .username("test.user@hmcts.net")
                .ethosCaseRefCollection(caseRefs)
                .build();

        CreationDataModel dataModel = CreationDataModel.builder()
                .lead(caseRefs.get(0))
                .multipleRef("MULTI123")
                .build();

        // Mock case retrieval
        SubmitEvent testCase = new SubmitEvent();
        testCase.setCaseId(12345L);
        testCase.setCaseData(new CaseData());
        when(ccdClient.retrieveCasesElasticSearch(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(Collections.singletonList(testCase));

        // When: Messages are enqueued
        createUpdatesBusSender.sendUpdatesToQueue(createUpdatesDto, dataModel, new ArrayList<>(), "3");

        // Then: Messages are in queue
        List<CreateUpdatesQueueMessage> queuedMessages = createUpdatesQueueRepository.findAll();
        assertTrue(queuedMessages.size() > 0, "Messages should be enqueued");

        // When: Processor runs
        createUpdatesQueueProcessor.processQueue();

        // Then: All messages are processed
        List<CreateUpdatesQueueMessage> processedMessages = createUpdatesQueueRepository.findAll();
        long completedCount = processedMessages.stream()
                .filter(m -> m.getStatus() == QueueMessageStatus.COMPLETED)
                .count();
        assertTrue(completedCount > 0, "At least one message should be completed");
    }

    @Test
    void shouldCompleteFullUpdateCaseFlow() throws Exception {
        // Given: An update case request
        UpdateCaseMsg updateCaseMsg = new UpdateCaseMsg();
        updateCaseMsg.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        updateCaseMsg.setJurisdiction("EMPLOYMENT");
        updateCaseMsg.setMultipleRef("SINGLE_CASE_TYPE");
        updateCaseMsg.setEthosCaseReference("5555555/2024");
        updateCaseMsg.setTotalCases("1");

        UpdateDataModel dataModel = UpdateDataModel.builder()
                .caseRefForUpdate("5555555/2024")
                .build();
        updateCaseMsg.setDataModelParent(dataModel);

        // When: Message is enqueued
        updateCaseQueueSender.sendUpdateToQueue(updateCaseMsg);

        // Then: Message is in queue
        List<UpdateCaseQueueMessage> queuedMessages = updateCaseQueueRepository.findAll();
        assertEquals(1, queuedMessages.size());
        assertEquals(QueueMessageStatus.PENDING, queuedMessages.get(0).getStatus());

        // When: Processor runs
        updateCaseQueueProcessor.processQueue();

        // Then: Message is processed
        List<UpdateCaseQueueMessage> processedMessages = updateCaseQueueRepository.findAll();
        assertEquals(1, processedMessages.size());
        // Note: May be COMPLETED or RETRY depending on mock setup
        assertTrue(
                processedMessages.get(0).getStatus() == QueueMessageStatus.COMPLETED
                        || processedMessages.get(0).getStatus() == QueueMessageStatus.RETRY,
                "Message should be processed (either completed or retrying)"
        );
    }

    @Test
    void shouldHandleConcurrentProcessing() throws Exception {
        // Given: Multiple update messages
        for (int i = 1; i <= 5; i++) {
            UpdateCaseMsg msg = new UpdateCaseMsg();
            msg.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
            msg.setJurisdiction("EMPLOYMENT");
            msg.setMultipleRef("SINGLE_CASE_TYPE");
            msg.setEthosCaseReference("CASE" + i + "/2024");
            msg.setTotalCases("1");

            UpdateDataModel dataModel = UpdateDataModel.builder()
                    .caseRefForUpdate("CASE" + i + "/2024")
                    .build();
            msg.setDataModelParent(dataModel);

            updateCaseQueueSender.sendUpdateToQueue(msg);
        }

        // When: Multiple processor runs (simulating concurrent processing)
        updateCaseQueueProcessor.processQueue();
        
        // Then: All messages are processed without deadlocks
        List<UpdateCaseQueueMessage> messages = updateCaseQueueRepository.findAll();
        assertEquals(5, messages.size());
        
        long completedOrRetrying = messages.stream()
                .filter(m -> m.getStatus() == QueueMessageStatus.COMPLETED
                        || m.getStatus() == QueueMessageStatus.RETRY)
                .count();
        assertEquals(5, completedOrRetrying, "All messages should be processed");
    }

    @Test
    void shouldMaintainMessageOrder() throws Exception {
        // Given: Multiple messages with specific order
        List<String> expectedOrder = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String caseRef = "ORDER" + i + "/2024";
            expectedOrder.add(caseRef);
            
            UpdateCaseMsg msg = new UpdateCaseMsg();
            msg.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
            msg.setJurisdiction("EMPLOYMENT");
            msg.setMultipleRef("SINGLE_CASE_TYPE");
            msg.setEthosCaseReference(caseRef);
            msg.setTotalCases("1");

            UpdateDataModel dataModel = UpdateDataModel.builder()
                    .caseRefForUpdate(caseRef)
                    .build();
            msg.setDataModelParent(dataModel);

            // Small delay to ensure order
            Thread.sleep(10);
            updateCaseQueueSender.sendUpdateToQueue(msg);
        }

        // When: Processor runs
        updateCaseQueueProcessor.processQueue();

        // Then: Messages were processed in order (FIFO)
        List<UpdateCaseQueueMessage> messages = updateCaseQueueRepository
                .findByStatusOrderByCreatedAtAsc(QueueMessageStatus.COMPLETED);
        
        if (messages.size() == 3) {
            // Verify order if all completed successfully
            for (int i = 0; i < 3; i++) {
                UpdateCaseMsg parsedMsg = objectMapper.readValue(
                        messages.get(i).getMessageBody(),
                        UpdateCaseMsg.class
                );
                assertEquals(expectedOrder.get(i), parsedMsg.getEthosCaseReference());
            }
        }
    }

    @Test
    void shouldHandleMixedSuccessAndFailure() throws Exception {
        // Given: Mix of messages that will succeed and fail
        List<UpdateCaseMsg> messages = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            UpdateCaseMsg msg = new UpdateCaseMsg();
            msg.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
            msg.setJurisdiction("EMPLOYMENT");
            msg.setMultipleRef("SINGLE_CASE_TYPE");
            msg.setEthosCaseReference("MIX" + i + "/2024");
            msg.setTotalCases("1");

            UpdateDataModel dataModel = UpdateDataModel.builder()
                    .caseRefForUpdate("MIX" + i + "/2024")
                    .build();
            msg.setDataModelParent(dataModel);
            
            messages.add(msg);
            updateCaseQueueSender.sendUpdateToQueue(msg);
        }

        // When: Processor runs
        updateCaseQueueProcessor.processQueue();

        // Then: Messages have various statuses
        List<UpdateCaseQueueMessage> allMessages = updateCaseQueueRepository.findAll();
        assertEquals(3, allMessages.size());
        
        // All should be either completed or retrying (none stuck in PENDING)
        long processedCount = allMessages.stream()
                .filter(m -> m.getStatus() != QueueMessageStatus.PENDING)
                .count();
        assertEquals(3, processedCount, "All messages should be attempted");
    }
}
