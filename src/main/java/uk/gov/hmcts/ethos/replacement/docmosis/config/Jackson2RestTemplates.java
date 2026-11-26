package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Keeps hand-built HTTP clients on Jackson 2.
 *
 * <p>Spring Framework 7 picks Jackson 3 ({@code tools.jackson}) for {@code new RestTemplate()} and for
 * WebClient's default codecs whenever it is on the classpath. elasticsearch-java brings it in
 * transitively, so without this the clients would silently change from the Jackson 2 wire contract
 * ET had on Spring Boot 3. The converters and codecs used here are the same Jackson 2 defaults Spring 6 used.
 */
@SuppressWarnings("removal")
public final class Jackson2RestTemplates {

    private Jackson2RestTemplates() {
    }

    public static RestTemplate restTemplate() {
        return new RestTemplate(HttpMessageConverters.forClient()
            .registerDefaults()
            .withJsonConverter(new MappingJackson2HttpMessageConverter())
            .build());
    }

    public static void useJackson2(ClientCodecConfigurer configurer) {
        configurer.defaultCodecs().jacksonJsonDecoder(new Jackson2JsonDecoder());
        configurer.defaultCodecs().jacksonJsonEncoder(new Jackson2JsonEncoder());
    }
}
