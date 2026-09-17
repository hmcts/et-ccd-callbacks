package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
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
    private ClosureRetry closureRetry = new ClosureRetry();

    @Data
    public static class Review {
        private Set<String> adminFlagCodes = Set.of();
        private Set<String> legalOfficerFlagCodes = Set.of();
        private Set<String> judgeFlagCodes = Set.of();
        private List<PathFlag> adminPathFlags = List.of();
        private List<PathFlag> legalOfficerPathFlags = List.of();
        private List<PathFlag> judgePathFlags = List.of();
    }

    @Data
    public static class Arrange {
        private Map<String, String> flagTitles = Map.of();
        private List<ArrangePathFlag> pathFlags = List.of();
    }

    @Data
    public static class ClosureRetry {
        private int maxAttempts = 3;
        private long initialBackoffMs = 200;
        private double multiplier = 2;
        private long maxBackoffMs = 1000;
    }

    @Data
    public static class PathFlag {
        private String flagCode;
        private List<String> path = List.of();
    }

    @Data
    public static class ArrangePathFlag {
        private String flagCode;
        private List<String> path = List.of();
        private String taskTitle;
    }
}
