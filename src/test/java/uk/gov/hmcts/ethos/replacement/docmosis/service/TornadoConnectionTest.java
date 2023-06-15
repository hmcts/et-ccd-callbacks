package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TornadoConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MockHttpURLConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class TornadoConnectionTest {

    @Test
    void shouldCreateConnection() throws IOException {
        String url = "http://tornadotest";
        TornadoConfiguration tornadoConfiguration = new TornadoConfiguration();
        tornadoConfiguration.setUrl(url);
        HttpURLConnection mockConnection = MockHttpURLConnectionFactory.create(url);

        TornadoConnection tornadoConnection = new TornadoConnection(tornadoConfiguration);
        HttpURLConnection connection = tornadoConnection.createConnection();

        assertEquals(mockConnection, connection);
        verify(mockConnection, times(1)).connect();
    }

    @Test
    void shouldReturnAccessKey() {
        String accessKey = "test-access-key";
        TornadoConfiguration tornadoConfiguration = new TornadoConfiguration();
        tornadoConfiguration.setAccessKey(accessKey);

        TornadoConnection tornadoConnection = new TornadoConnection(tornadoConfiguration);
        String actualAccessKey = tornadoConnection.getAccessKey();

        assertEquals(actualAccessKey, accessKey);
    }
}
