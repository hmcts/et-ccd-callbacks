package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.config.security.JwtAuthenticationFilter;
import uk.gov.hmcts.ethos.replacement.docmosis.config.security.MultiIssuerValidator;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified Spring Security configuration for the application.
 *
 * <p>Replaces the two separate {@code HandlerInterceptor}-based authentication
 * chains (one per package) with a single {@link SecurityFilterChain} that covers
 * all endpoints.  A custom {@link JwtAuthenticationFilter} delegates to
 * {@link VerifyTokenService} so that existing controller-level token checks and
 * their mocks in unit tests continue to work without modification.
 *
 * <p>The {@link JwtDecoder} bean performs full token validation:
 * <ul>
 *   <li>Signature – verified against IDAM's JWKS endpoint</li>
 *   <li>Expiry / not-before – via {@link JwtTimestampValidator}</li>
 *   <li>Issuer – via {@link MultiIssuerValidator} (supports multiple permitted issuers)</li>
 *   <li>Audience – verified against the configured client ID</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class SecurityConfig {

    /** Paths that do not require a valid JWT. */
    static final String[] PERMIT_ALL_PATHS = {
        "/",
        "/favicon.ico",
        "/health",
        "/info",
        "/mappings",
        "/metrics",
        "/metrics/**",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/v2/**",
        "/v3/**",
        "/webjars/**",
        // SYA endpoint explicitly opted out of authentication (UNAUTHORIZED_APIS)
        "/et3/findCaseByEthosCaseReference"
    };

    /**
     * Single security filter chain used by both the docmosis-callbacks and
     * et-sya-api request paths.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    VerifyTokenService verifyTokenService) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(
                new JwtAuthenticationFilter(verifyTokenService),
                UsernamePasswordAuthenticationFilter.class
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }

    /**
     * {@link JwtDecoder} that performs full token validation: signature, expiry,
     * issuer (multi-issuer aware), and audience.
     *
     * @param jwksUri        IDAM JWKS endpoint URL
     * @param issuerUris     list of permitted issuer URIs – validation is skipped when empty
     * @param clientId       expected {@code aud} claim value
     */
    @Bean
    public JwtDecoder jwtDecoder(
        @Value("${idam.api.jwksUrl}") String jwksUri,
        @Value("${idam.api.issuer-uris:}") List<String> issuerUris,
        @Value("${idam.client.id:et-cos}") String clientId
    ) {

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        validators.add(new MultiIssuerValidator(issuerUris));

        if (StringUtils.hasText(clientId)) {
            validators.add(new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                aud -> aud != null && aud.contains(clientId)
            ));
        }

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
        return decoder;
    }
}
