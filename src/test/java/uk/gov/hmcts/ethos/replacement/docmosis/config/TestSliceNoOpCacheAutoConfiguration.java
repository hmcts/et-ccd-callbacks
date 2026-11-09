package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot 3's {@code @WebMvcTest} and {@code @DataJpaTest} carried {@code @AutoConfigureCache},
 * which gave each slice a no-op {@link CacheManager}. Boot 4 dropped it from both slices, so the
 * {@code @EnableCaching} on {@code DocmosisApplication} fails those contexts. Registered for
 * {@code @AutoConfigureWebMvc} and {@code @AutoConfigureDataJpa} via META-INF/spring to restore the
 * Boot 3 slice behaviour; tests that declare their own {@link CacheManager} keep it.
 */
@AutoConfiguration
public class TestSliceNoOpCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }
}
