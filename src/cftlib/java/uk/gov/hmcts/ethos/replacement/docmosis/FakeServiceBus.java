package uk.gov.hmcts.ethos.replacement.docmosis;

import com.microsoft.azure.servicebus.IQueueClient;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Alternative to requiring a connection to a Azure Service Bus queue in a development environment.
 */
@Configuration
public class FakeServiceBus {
    @ConditionalOnProperty(name = "servicebus.fake", havingValue = "true", matchIfMissing = false)
    @Bean("create-updates-send-client")
    IQueueClient client() {
        return Mockito.mock(IQueueClient.class);
    }
}
