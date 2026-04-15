package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Overrides the production {@link SecurityConfig} for all Spring test slices.
 *
 * <p>Placing a permit-all {@link SecurityFilterChain} at order 1 ensures it is selected
 * before the production chain for every request dispatched via {@code MockMvc}.  This
 * lets controller tests exercise their own internal {@code verifyTokenService} checks
 * (which are already mocked) without the Spring Security filter chain interfering with
 * the {@code SecurityContextHolder} state in the test thread.
 *
 * <p>Security-filter behaviour is covered separately by
 * {@code JwtAuthenticationFilterTest} and {@code MultiIssuerValidatorTest}.
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnit4TestShouldUseTestAnnotation"})
public class TestSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .build();
    }
}
