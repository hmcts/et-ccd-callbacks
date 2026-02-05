package uk.gov.hmcts.ethos.replacement.docmosis.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.helpers.CreateUpdatesHelper;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.CreateUpdatesQueueRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sends create updates messages to database queue (replaces Azure Service Bus).
 */
@Slf4j
@Component
public class CreateUpdatesBusSender {

    private static final String ERROR_MESSAGE = "Failed to send the message to the queue";
    private final ServiceBusSender serviceBusSender;
    private final CreateUpdatesQueueRepository createUpdatesQueueRepository;
    private final ObjectMapper objectMapper;
    private final boolean queueEnabled;

    public CreateUpdatesBusSender(
        @Qualifier("createUpdatesSendHelper") ObjectProvider<ServiceBusSender> serviceBusSenderProvider,
        CreateUpdatesQueueRepository createUpdatesQueueRepository,
        ObjectMapper objectMapper,
        @Value("${queue.enabled:false}") boolean queueEnabled
    ) {
        this.serviceBusSender = serviceBusSenderProvider.getIfAvailable();
        this.createUpdatesQueueRepository = createUpdatesQueueRepository;
        this.objectMapper = objectMapper;
        this.queueEnabled = queueEnabled;
    }

    @Transactional
    public void sendUpdatesToQueue(CreateUpdatesDto createUpdatesDto, DataModelParent dataModelParent,
                                   List<String> errors, String updateSize) {
        log.info("Started sending messages to create-updates queue");

        AtomicInteger successCount = new AtomicInteger(0);

        List<CreateUpdatesMsg> createUpdatesMsgList =
                CreateUpdatesHelper.getCreateUpdatesMessagesCollection(
                        createUpdatesDto,
                        dataModelParent,
                        500,
                        updateSize);

        createUpdatesMsgList.forEach(msg -> {
            try {
                if (queueEnabled) {
                    String messageBody = objectMapper.writeValueAsString(msg);
                    CreateUpdatesQueueMessage queueMessage = CreateUpdatesQueueMessage.builder()
                        .messageId(UUID.randomUUID().toString())
                        .messageBody(messageBody)
                        .status(QueueMessageStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .retryCount(0)
                        .build();

                    createUpdatesQueueRepository.save(queueMessage);
                    log.info("SENT to database queue -----> " + msg.toString());
                } else {
                    if (serviceBusSender == null) {
                        throw new IllegalStateException("ServiceBusSender is not configured");
                    }
                    serviceBusSender.sendMessage(msg);
                    log.info("SENT -----> " + msg.toString());
                }
                successCount.incrementAndGet();
            } catch (Exception e) {
                log.error("Error sending messages to create-updates queue", e);
                errors.add(ERROR_MESSAGE);
            }
        });

        log.info(
                "Finished sending messages to create-updates queue. Successful: {}. Failures {}.",
                successCount.get(),
                createUpdatesMsgList.size() - successCount.get()
        );
    }

}
