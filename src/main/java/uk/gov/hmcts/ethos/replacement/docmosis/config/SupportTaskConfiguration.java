package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.CONFIGURATION_PREFIX;

@Configuration
@ConfigurationProperties(CONFIGURATION_PREFIX)
@PropertySource(value = "classpath:support-tasks.yml", factory = YamlPropertySourceFactory.class)
@Data
public class SupportTaskConfiguration {
    private Review review = new Review();
    private Arrange arrange = new Arrange();

    @Data
    public static class Review {
        private Set<String> adminFlagCodes = Set.of();
        private Set<String> legalOfficerFlagCodes = Set.of();
        private Set<String> judgeFlagCodes = Set.of();
    }

    @Data
    public static class Arrange {
        private Map<String, String> flagTitles = Map.of();
    }
}
