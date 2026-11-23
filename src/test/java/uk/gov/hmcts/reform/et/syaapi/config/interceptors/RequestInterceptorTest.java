package uk.gov.hmcts.reform.et.syaapi.config.interceptors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class RequestInterceptorTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private VerifyTokenService verifyTokenService;

    private RequestInterceptor requestInterceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        requestInterceptor = new RequestInterceptor(verifyTokenService);
        request = new MockHttpServletRequest("GET", "/cases/user-cases");
        request.addHeader(AUTHORIZATION, AUTH_TOKEN);
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandleAllowsValidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        assertThat(requestInterceptor.preHandle(request, response, new Object())).isTrue();
    }

    @Test
    void preHandleRejectsInvalidToken() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> requestInterceptor.preHandle(request, response, new Object()))
            .isInstanceOf(UnAuthorisedServiceException.class)
            .hasMessage("Failed to verify bearer token.");
    }

    @Test
    void preHandleSkipsTokenCheckForUnauthorisedApis() throws Exception {
        request.setRequestURI("/et3/findCaseByEthosCaseReference");

        assertThat(requestInterceptor.preHandle(request, response, new Object())).isTrue();
        verifyNoInteractions(verifyTokenService);
    }
}
