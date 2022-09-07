package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link URLStreamHandler} that allows us to control the {@link URLConnection URLConnections} that are returned
 * by {@link URL URLs} in the code under test.
 */
public class HttpUrlStreamHandler extends URLStreamHandler {

    private final Map<URL, URLConnection> connections = new ConcurrentHashMap<>();

    @Override
    protected URLConnection openConnection(URL url) {
        return connections.get(url);
    }

    public void addConnection(URL url, URLConnection urlConnection) {
        connections.put(url, urlConnection);
    }

    public void reset() {
        connections.clear();
    }
}