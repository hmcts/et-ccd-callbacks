package uk.gov.hmcts.ethos.replacement.docmosis.config;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class QueueClientConfiguration {
    @ConditionalOnProperty(name = "servicebus.fake", havingValue = "false", matchIfMissing = true)
    @Bean("createUpdatesSendClient")
    public IQueueClient createUpdatesSendClient(
        @Value("${queue.create-updates.send.connection-string}") String connectionString,
        @Value("${queue.create-updates.queue-name}") String queueName
    ) throws InterruptedException, ServiceBusException {
        return createQueueClient(connectionString, queueName);
    }

    private QueueClient createQueueClient(
        String connectionString,
        String queueName
    ) throws ServiceBusException, InterruptedException {
        return new QueueClient(
            new ConnectionStringBuilder(connectionString, queueName),
            ReceiveMode.PEEKLOCK
        );
    }
}
