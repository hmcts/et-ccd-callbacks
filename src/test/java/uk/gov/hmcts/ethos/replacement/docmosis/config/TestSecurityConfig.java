package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import uk.gov.hmcts.ethos.replacement.docmosis.config.SecurityConfig;
import uk.gov.hmcts.ethos.replacement.docmosis.config.security.JwtAuthenticationFilter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

/**
 * Replaces the production {@link SecurityConfig} for all Spring test slices.
 *
 * <p>Uses the real {@link JwtAuthenticationFilter} (which delegates to the mocked
 * {@link VerifyTokenService}) so that controller tests exercise genuine token
 * validation without relying on Spring Security's deferred-context infrastructure.
 * When the mock returns {@code true} the filter creates a fresh
 * {@code SecurityContext} via {@code SecurityContextHolder.setContext()}, bypassing
 * {@code spring-security-test}'s {@code SecurityMockMvcConfigurer} interference.
 * When the mock returns {@code false} no context is set and the
 * {@code .anyRequest().authenticated()} rule returns 403.
 *
 * <p>The permit-all paths mirror {@link SecurityConfig#PERMIT_ALL_PATHS} so that
 * management and public endpoints remain accessible in tests.
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnit4TestShouldUseTestAnnotation"})
public class TestSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http,
                                                       VerifyTokenService verifyTokenService) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(
                new JwtAuthenticationFilter(verifyTokenService),
                UsernamePasswordAuthenticationFilter.class
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(SecurityConfig.PERMIT_ALL_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
