package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class MockHttpURLConnectionFactory {

    private static final HttpUrlStreamHandler HTTP_URL_STREAM_HANDLER;

    static {
        var urlStreamHandlerFactory = mock(URLStreamHandlerFactory.class);
        URL.setURLStreamHandlerFactory(urlStreamHandlerFactory);

        HTTP_URL_STREAM_HANDLER = new HttpUrlStreamHandler();
        given(urlStreamHandlerFactory.createURLStreamHandler("http")).willReturn(HTTP_URL_STREAM_HANDLER);
    }

    private MockHttpURLConnectionFactory() {
        // All access through static methods
    }

    public static HttpURLConnection create(String url) throws MalformedURLException {
        var urlConnection = mock(HttpURLConnection.class);
        HTTP_URL_STREAM_HANDLER.reset();
        HTTP_URL_STREAM_HANDLER.addConnection(new URL(url), urlConnection);

        return urlConnection;
    }
}
