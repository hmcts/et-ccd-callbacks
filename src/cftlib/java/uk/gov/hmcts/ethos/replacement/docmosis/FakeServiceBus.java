package uk.gov.hmcts.ethos.replacement.docmosis;

import com.microsoft.azure.servicebus.IQueueClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FakeServiceBus {
  @Bean("create-updates-send-client")
  IQueueClient client() {
    return Mockito.mock(IQueueClient.class);
  }
}
