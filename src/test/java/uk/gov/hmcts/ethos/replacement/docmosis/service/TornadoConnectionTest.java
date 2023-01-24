package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TornadoConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MockHttpURLConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TornadoConnectionTest {

    @Test
    public void shouldCreateConnection() throws IOException {
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
    public void shouldReturnAccessKey() {
        String accessKey = "test-access-key";
        TornadoConfiguration tornadoConfiguration = new TornadoConfiguration();
        tornadoConfiguration.setAccessKey(accessKey);

        TornadoConnection tornadoConnection = new TornadoConnection(tornadoConfiguration);
        String actualAccessKey = tornadoConnection.getAccessKey();

        assertEquals(actualAccessKey, accessKey);
    }
}
