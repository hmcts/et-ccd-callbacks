package uk.gov.hmcts.ethos.replacement.docmosis.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.helpers.CreateUpdatesHelper;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sends create updates messages to create-updates queue.
 */
@Slf4j
@Component
public class CreateUpdatesBusSender {
    private static final String ERROR_MESSAGE = "Failed to send the message to the queue";
    private final ServiceBusSender serviceBusSender;
    private final FeatureToggleService featureToggleService;
    private final DataSource dataSource;

    public CreateUpdatesBusSender(
            @Qualifier("create-updates-send-helper") ServiceBusSender serviceBusSender,
            FeatureToggleService featureToggleService, DataSource dataSource) {
        this.serviceBusSender = serviceBusSender;
        this.featureToggleService = featureToggleService;
        this.dataSource = dataSource;
    }

    public void sendMessageToServiceBus(CreateUpdatesDto createUpdatesDto, DataModelParent dataModelParent,
            List<String> errors, String updateSize) {
        log.info("Started sending messages to create-updates queue");

        AtomicInteger successCount = new AtomicInteger(0);

        List<CreateUpdatesMsg> createUpdatesMsgList = CreateUpdatesHelper.getCreateUpdatesMessagesCollection(
                createUpdatesDto,
                dataModelParent,
                500,
                updateSize);

        createUpdatesMsgList
                .forEach(msg -> {
                    try {
                        serviceBusSender.sendMessage(msg);
                        log.info("SENT -----> " + msg.toString());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("Error sending messages to create-updates queue", e);
                        errors.add(ERROR_MESSAGE);
                    }
                });

        log.info(
                "Finished sending messages to create-updates queue. Successful: {}. Failures {}.",
                successCount.get(),
                createUpdatesMsgList.size() - successCount.get());
    }

    private void sendMessageToDbQueue(CreateUpdatesDto createUpdatesDto, DataModelParent dataModelParent,
            String updateSize) {
        log.info("Started sending messages to work table");

        List<CreateUpdatesMsg> createUpdatesMsgList = CreateUpdatesHelper.getCreateUpdatesMessagesCollection(
                createUpdatesDto,
                dataModelParent,
                500,
                updateSize);

        try (Connection conn = dataSource.getConnection()) {
            createUpdatesMsgList.forEach(msg -> sendMessage(conn, msg));
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }

    private void sendMessage(Connection conn, CreateUpdatesMsg msg) {
        try (CallableStatement addWork = conn.prepareCall("{ call add_work(?, ?) }")) {
            addWork.setString(1, msg.getMultipleRef());

            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(new ObjectMapper().writeValueAsString(msg));
            addWork.setObject(2, jsonObject);

            addWork.execute();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public void sendUpdatesToQueue(CreateUpdatesDto createUpdatesDto, DataModelParent dataModelParent,
            List<String> errors, String updateSize) {
        if (featureToggleService.isMultiplesDBEnabled()) {
            // Use new way
            sendMessageToDbQueue(createUpdatesDto, dataModelParent, updateSize);
            return;
        }

        sendMessageToServiceBus(createUpdatesDto, dataModelParent, errors, updateSize);
    }

}
