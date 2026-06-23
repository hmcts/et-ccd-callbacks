package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfiguration {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
