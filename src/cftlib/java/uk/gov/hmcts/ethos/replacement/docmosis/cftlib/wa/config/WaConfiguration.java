package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

/**
 * Enables all Work Allocation components within CFTLib.
 *
 * <p>Only activated when {@code et.work-allocation.enabled=true}, which maps to the
 * {@code ET_WORK_ALLOCATION=true} environment variable. When disabled (the default),
 * no WA scheduled tasks, Feign clients, or S2S beans are created.
 *
 * <p>To activate, set in {@code build.gradle}:
 * <pre>
 *   environment 'ET_WORK_ALLOCATION', 'true'
 * </pre>
 */
@Configuration
@ConditionalOnProperty(name = "et.work-allocation.enabled", havingValue = "true")
@EnableScheduling
@EnableFeignClients(basePackageClasses = WaConfiguration.class)
public class WaConfiguration {

    /**
     * A dedicated S2S token generator for WA services (microservice = wa_task_monitor).
     * Uses a separate bean name to avoid conflicting with the main {@code et_cos} S2S bean.
     */
    @Bean("waAuthTokenGenerator")
    public AuthTokenGenerator waAuthTokenGenerator(
            @Value("${wa.s2s-auth.totp-secret:AAAAAAAAAAAAAAAA}") String secret,
            @Value("${wa.s2s-auth.microservice:wa_task_monitor}") String microservice,
            ServiceAuthorisationApi serviceAuthorisationApi) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microservice, serviceAuthorisationApi);
    }
}
