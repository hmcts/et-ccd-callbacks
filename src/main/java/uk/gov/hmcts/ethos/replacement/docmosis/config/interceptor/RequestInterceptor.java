package uk.gov.hmcts.ethos.replacement.docmosis.config.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    private static final String FAILED_TO_VERIFY_TOKEN = "Failed to verify the following token: {}";

    private final VerifyTokenService verifyTokenService;

    @Autowired
    public RequestInterceptor(VerifyTokenService verifyTokenService) {
        this.verifyTokenService = verifyTokenService;
    }

    /**
     * Intercepts any incoming calls and throws exception if token is invalid.
     * @param requestServlet current HTTP request
     * @param responseServlet current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return true if the token is verified
     */
    @Override
    public boolean preHandle(HttpServletRequest requestServlet, HttpServletResponse responseServlet, Object handler) {
        String authorizationHeader = requestServlet.getHeader(AUTHORIZATION);
        boolean jwtVerified = verifyTokenService.verifyTokenSignature(authorizationHeader);
        if (!jwtVerified) {
            log.error(FAILED_TO_VERIFY_TOKEN, authorizationHeader);
            throw new UnauthorisedServiceException("Failed to verify bearer token.");
        }
        return true;
    }
}
