package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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
        RestTemplate restTemplate = new RestTemplate();
        useJackson2(restTemplate.getMessageConverters());
        return restTemplate;
    }

    static void useJackson2(List<HttpMessageConverter<?>> converters) {
        int index = -1;
        for (int i = 0; i < converters.size(); i++) {
            if (isJackson3Json(converters.get(i))) {
                index = i;
                break;
            }
        }
        converters.removeIf(Jackson2RestTemplates::isJackson3Json);
        if (converters.stream().noneMatch(MappingJackson2HttpMessageConverter.class::isInstance)) {
            converters.add(index < 0 ? converters.size() : index, new MappingJackson2HttpMessageConverter());
        }
    }

    public static void useJackson2(ClientCodecConfigurer configurer) {
        configurer.defaultCodecs().jacksonJsonDecoder(new Jackson2JsonDecoder());
        configurer.defaultCodecs().jacksonJsonEncoder(new Jackson2JsonEncoder());
    }

    private static boolean isJackson3Json(HttpMessageConverter<?> converter) {
        return "org.springframework.http.converter.json.JacksonJsonHttpMessageConverter"
            .equals(converter.getClass().getName());
    }
}
