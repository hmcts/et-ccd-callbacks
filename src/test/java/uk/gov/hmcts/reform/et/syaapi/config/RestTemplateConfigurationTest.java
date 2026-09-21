package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ethos.replacement.docmosis.config.JacksonConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigurationTest {

    @Test
    void createRestTemplateGeneratesNewRestTemplate() {
        RestTemplate restTemplate = new RestTemplateConfiguration()
            .getRestTemplate(new JacksonConfiguration().objectMapper());

        MappingJackson2HttpMessageConverter jackson2Converter = restTemplate.getMessageConverters().stream()
            .filter(MappingJackson2HttpMessageConverter.class::isInstance)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .findFirst()
            .orElseThrow();

        assertThat(jackson2Converter.getObjectMapper().version().getMajorVersion()).isEqualTo(2);
        assertThat(restTemplate.getMessageConverters())
            .noneMatch(converter -> converter.getClass().getName()
                .equals("org.springframework.http.converter.json.JacksonJsonHttpMessageConverter"));
    }
}
