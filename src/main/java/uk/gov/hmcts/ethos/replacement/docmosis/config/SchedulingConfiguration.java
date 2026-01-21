package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable scheduling.
 * Can be disabled by setting spring.task.scheduling.enabled=false in application.yaml
 * This allows tests to disable scheduling without using profiles.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
    value = "spring.task.scheduling.enabled",
    havingValue = "true",
    matchIfMissing = true  // Enabled by default if property is not set
)
public class SchedulingConfiguration {
}
