package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class CaseConverterTest {
    private CaseConverter caseConverter;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        caseConverter = new CaseConverter(objectMapper);
    }

    @Test
    void shouldConvertToObject() {

        RespondentSumType respondent = RespondentSumType.builder()
            .respondentName("Test Company")
            .respondentEmail("test@acme.org")
            .responseReference("1789000")
            .build();

        Map<String, Object> respondentAsMap = Map.of(
            "respondent_name", "Test Company",
            "respondent_email", "test@acme.org",
            "responseReference", "1789000");

        assertThat(caseConverter.convert(respondentAsMap, RespondentSumType.class)).isEqualTo(respondent);
    }

    @Test
    void shouldConvertToMap() {
        RespondentSumType respondent = RespondentSumType.builder()
            .respondentName("Test Company")
            .respondentEmail("test@acme.org")
            .responseReference("1789000")
            .build();

        Map<String, Object> expectedRespondentMap = Map.of(
            "respondent_name", "Test Company",
            "respondent_email", "test@acme.org",
            "responseReference", "1789000");

        assertThat(caseConverter.toMap(respondent)).isEqualTo(expectedRespondentMap);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        }

    }
}