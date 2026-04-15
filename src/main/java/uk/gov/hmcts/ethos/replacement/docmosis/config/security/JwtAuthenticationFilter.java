package uk.gov.hmcts.ethos.replacement.docmosis.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Single JWT authentication filter shared across both the docmosis-callbacks and
 * et-sya-api request paths.  Replaces the two hand-rolled {@code HandlerInterceptor}
 * implementations with a single, properly placed Spring Security filter.
 *
 * <p>Delegates full token validation (signature, expiry, issuer, audience) to
 * {@link VerifyTokenService}.  On success the authenticated principal is placed in
 * the {@link SecurityContextHolder} so that Spring Security's authorization rules
 * can enforce path-level access control.  The context is always cleared in a
 * {@code finally} block to prevent leakage across pooled threads.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final VerifyTokenService verifyTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader(AUTHORIZATION);
            if (token != null && verifyTokenService.verifyTokenSignature(token)) {
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        token, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
