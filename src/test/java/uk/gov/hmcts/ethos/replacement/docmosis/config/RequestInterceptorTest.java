package uk.gov.hmcts.ethos.replacement.docmosis.config;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.UnAuthorisedServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVICE_AUTHORIZATION;

class RequestInterceptorTest {

    @Mock
    private VerifyTokenService verifyTokenService;

    @Mock
    private AuthTokenValidator tokenValidator;

    private RequestInterceptor requestInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestInterceptor = new RequestInterceptor(verifyTokenService, tokenValidator);
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

    @Test
    void preHandle_CcdPersistencePathWithNonBearerServiceToken_ReturnsTrue() throws Exception {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);
        when(tokenValidator.getServiceName("Bearer test-service-token")).thenReturn("ccd_data");

        request.setRequestURI("/ccd-persistence/cases");
        request.addHeader(AUTHORIZATION, "Bearer user-token");
        request.addHeader(SERVICE_AUTHORIZATION, "test-service-token");

        boolean result = requestInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_CcdPersistencePathWithoutAuthorizedService_ThrowsException() {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);
        request.setRequestURI("/ccd-persistence/cases");
        request.addHeader(AUTHORIZATION, "Bearer user-token");
        request.addHeader(SERVICE_AUTHORIZATION, "");

        UnAuthorisedServiceException exception = assertThrows(UnAuthorisedServiceException.class,
            () -> requestInterceptor.preHandle(request, response, new Object()));

        assertEquals("Service not authorised to access ccd-persistence endpoints", exception.getMessage());
    }
}
