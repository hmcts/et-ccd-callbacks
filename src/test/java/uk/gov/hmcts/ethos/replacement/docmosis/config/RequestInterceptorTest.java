package uk.gov.hmcts.ethos.replacement.docmosis.config;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

class RequestInterceptorTest {

    @Mock
    private VerifyTokenService verifyTokenService;

    private RequestInterceptor requestInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestInterceptor = new RequestInterceptor(verifyTokenService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandle_ValidToken_ReturnsTrue() throws Exception {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        request.addHeader(AUTHORIZATION, "");
        boolean result = requestInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_InvalidToken_ReturnsFalseAndSetsErrorResponse() throws Exception {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(false);

        boolean result = requestInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("Failed to verify bearer token.", response.getErrorMessage());
    }
}