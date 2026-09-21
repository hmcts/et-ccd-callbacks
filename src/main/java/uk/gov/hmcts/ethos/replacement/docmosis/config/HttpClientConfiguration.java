package uk.gov.hmcts.ethos.replacement.docmosis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class HttpClientConfiguration {

    @Value("${ccd.client.timeout}")
    private int timeout;

    private static void accept(ClientCodecConfigurer clientCodecConfigurer, ObjectMapper objectMapper) {
        clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB
        clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
        clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
    }

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = createJackson2RestTemplate(objectMapper);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(getHttpClient()));
        return restTemplate;
    }

    public static RestTemplate createJackson2RestTemplate(ObjectMapper objectMapper) {
        return new RestTemplate(HttpMessageConverters.forClient()
            .registerDefaults()
            .withJsonConverter(new MappingJackson2HttpMessageConverter(objectMapper))
            .build());
    }

    private CloseableHttpClient getHttpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(timeout))
            .setConnectionRequestTimeout(Timeout.ofMilliseconds(timeout))
            .setResponseTimeout(Timeout.ofMilliseconds(timeout))
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }

    @Bean
    public WebClient webClient(ObjectMapper objectMapper) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("acas-connection-pool")
            .maxConnections(200)
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofMinutes(5))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
            .responseTimeout(Duration.ofMillis(timeout))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
            .keepAlive(true)
            .compress(true);

        return WebClient.builder()
            .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
            .codecs(codecs -> accept(codecs, objectMapper))
            .build();
    }

}
