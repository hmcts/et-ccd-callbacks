package uk.gov.hmcts.ethos.replacement.docmosis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IQueueClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;

@AutoConfigureAfter(QueueClientConfiguration.class)
@Configuration
public class ServiceBusSenderConfiguration {

    private final ObjectMapper objectMapper;

    public ServiceBusSenderConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean(name = "createUpdatesSendHelper")
    public ServiceBusSender createUpdatesSendHelper(
        @Qualifier("createUpdatesSendClient") IQueueClient queueClient) {
        return new ServiceBusSender(queueClient, objectMapper);
    }

}
