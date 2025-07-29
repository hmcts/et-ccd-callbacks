package uk.gov.hmcts.ethos.replacement.docmosis.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Intercepts any call and validates the token.
 */
@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    private final VerifyTokenService verifyTokenService;

    @Autowired
    public RequestInterceptor(VerifyTokenService verifyTokenService) {
        this.verifyTokenService = verifyTokenService;
    }

    /**
     * Intercepts any incoming calls and throws exception if token is invalid.
     * @param request current HTTP request
     * @param response current HTTP response
     * @param hnd chosen handler to execute, for type and/or instance evaluation
     * @return true if the token is verified
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object hnd) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (!verifyTokenService.verifyTokenSignature(authorizationHeader)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Failed to verify bearer token.");
            return false;
        }

        return true;
    }
}
