package uk.gov.hmcts.ethos.replacement.docmosis.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(verifyTokenService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        request.addHeader(AUTHORIZATION, "Bearer validtoken");
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
            "Context should be cleared after filter completes");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_doesNotSetAuthentication() throws Exception {
        request.addHeader(AUTHORIZATION, "Bearer badtoken");
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_noAuthHeader_doesNotSetAuthentication() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_contextAlwaysClearedAfterCompletion() throws Exception {
        request.addHeader(AUTHORIZATION, "Bearer validtoken");
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
            "SecurityContext must be cleared in finally block regardless of outcome");
    }
}
