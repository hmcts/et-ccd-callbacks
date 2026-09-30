package uk.gov.hmcts.ethos.replacement.docmosis.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ethos.replacement.docmosis.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.UnAuthorisedServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVICE_AUTHORIZATION;

class RequestInterceptorTest {

    @Mock
    private VerifyTokenService verifyTokenService;

    @Mock
    private AuthTokenValidator tokenValidator;

    private RequestInterceptor requestInterceptor;
    private MockMvc mockMvc;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        requestInterceptor = new RequestInterceptor(verifyTokenService, tokenValidator);
        mockMvc = MockMvcBuilders.standaloneSetup(new PersistenceController())
            .addInterceptors(requestInterceptor)
            .build();
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

        mockMvc.perform(MockMvcRequestBuilders.post(URI.create("/ccd-persistence/cases"))
                .header(AUTHORIZATION, "Bearer user-token")
                .header(SERVICE_AUTHORIZATION, "test-service-token"))
            .andExpect(status().isOk());
    }

    @Test
    void preHandle_EncodedCcdPersistencePathWithAuthorizedService_ReturnsTrue() throws Exception {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);
        when(tokenValidator.getServiceName("Bearer test-service-token")).thenReturn("ccd_data");

        mockMvc.perform(MockMvcRequestBuilders.post(URI.create("/%63cd-persistence/cases"))
                .header(AUTHORIZATION, "Bearer user-token")
                .header(SERVICE_AUTHORIZATION, "test-service-token"))
            .andExpect(status().isOk());
    }

    @Test
    void preHandle_CcdPersistencePathWithoutAuthorizedService_ThrowsException() {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        assertPersistenceRequestRejected("/ccd-persistence/cases", "");
    }

    @Test
    void preHandle_EncodedCcdPersistencePathWithoutAuthorizedService_ThrowsException() {
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);
        when(tokenValidator.getServiceName("Bearer test-service-token")).thenReturn("test_service");

        assertPersistenceRequestRejected("/%63cd-persistence/cases", "test-service-token");
    }

    private void assertPersistenceRequestRejected(String path, String serviceToken) {
        ServletException exception = assertThrows(ServletException.class,
            () -> mockMvc.perform(MockMvcRequestBuilders.post(URI.create(path))
                .header(AUTHORIZATION, "Bearer user-token")
                .header(SERVICE_AUTHORIZATION, serviceToken)));

        UnAuthorisedServiceException cause = assertInstanceOf(
            UnAuthorisedServiceException.class, exception.getCause());
        assertEquals("Service not authorised to access ccd-persistence endpoints",
            cause.getMessage());
    }

    @RestController
    static class PersistenceController {
        @PostMapping("/ccd-persistence/cases")
        void createCase() {
            // The interceptor must reject the request before this handler runs.
        }
    }
}
