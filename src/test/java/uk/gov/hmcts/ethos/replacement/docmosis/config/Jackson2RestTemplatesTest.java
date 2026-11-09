package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import uk.gov.hmcts.reform.et.syaapi.config.RestTemplateConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("removal")
class Jackson2RestTemplatesTest {

    @Test
    void restTemplateBeansUseOnlyJackson2ForJson() {
        HttpClientConfiguration httpClientConfiguration = new HttpClientConfiguration();
        ReflectionTestUtils.setField(httpClientConfiguration, "timeout", 1000);

        assertJackson2Only(httpClientConfiguration.restTemplate());
        assertJackson2Only(new RestTemplateConfiguration().getRestTemplate());
    }

    @Test
    void webClientCodecsUseJackson2() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(Jackson2RestTemplates::useJackson2)
            .build();
        List<HttpMessageReader<?>> readers = strategies.messageReaders();

        assertThat(readers).noneMatch(r -> r.toString().contains("tools.jackson")
            || r.toString().contains("JacksonJsonDecoder") && !r.toString().contains("Jackson2JsonDecoder"));
        assertThat(readers).anyMatch(r -> r instanceof org.springframework.http.codec.DecoderHttpMessageReader<?> reader
            && reader.getDecoder() instanceof Jackson2JsonDecoder);
    }

    private static void assertJackson2Only(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        assertThat(converters).anyMatch(MappingJackson2HttpMessageConverter.class::isInstance);
        assertThat(converters).noneMatch(c -> c.getClass().getName()
            .equals("org.springframework.http.converter.json.JacksonJsonHttpMessageConverter"));
    }
}
