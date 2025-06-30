package uk.gov.hmcts.ethos.replacement.docmosis.config;

import com.microsoft.azure.servicebus.IQueueClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class MockServiceBus {
    @Bean("createUpdatesSendClient")
    IQueueClient client() {
        return Mockito.mock(IQueueClient.class);
    }
}
