import org.junit.jupiter.api.extension.ExtendWith;import com.microsoft.azure.servicebus.IQueueClient;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class MockServiceBus {
    @Bean("create-updates-send-client")
    IQueueClient client() {
        return Mockito.mock(IQueueClient.class);
    }
}
