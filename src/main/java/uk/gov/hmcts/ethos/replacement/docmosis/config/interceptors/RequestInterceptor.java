package uk.gov.hmcts.ethos.replacement.docmosis.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.UnAuthorisedServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVICE_AUTHORIZATION;

/**
 * Intercepts any call and validates the token.
 */
@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    private static final String CCD_DATA_SERVICE = "ccd_data";
    private static final String CCD_PERSISTENCE_ENDPOINT_PREFIX = "/ccd-persistence";
    private static final String CCD_PERSISTENCE_UNAUTHORISED =
            "Service not authorised to access ccd-persistence endpoints";
    public static final String BEARER_PREFIX = "Bearer ";

    private final VerifyTokenService verifyTokenService;
    @Lazy
    private AuthTokenValidator tokenValidator;

    @Autowired
    public RequestInterceptor(VerifyTokenService verifyTokenService,
                              @Lazy @Nullable AuthTokenValidator tokenValidator) {
        this.verifyTokenService = verifyTokenService;
        this.tokenValidator = tokenValidator;
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
        String serviceAuthToken = request.getHeader(SERVICE_AUTHORIZATION);
        if (!verifyTokenService.verifyTokenSignature(authorizationHeader)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Failed to verify bearer token.");
            return false;
        }

        if (request.getRequestURI().startsWith(CCD_PERSISTENCE_ENDPOINT_PREFIX)) {
            String serviceName;
            if (serviceAuthToken == null || serviceAuthToken.isBlank()) {
                serviceName = null;
            } else if (!serviceAuthToken.startsWith(BEARER_PREFIX)) {
                serviceName = tokenValidator == null ? null
                    : tokenValidator.getServiceName(BEARER_PREFIX + serviceAuthToken);
            } else {
                serviceName = tokenValidator == null ? null : tokenValidator.getServiceName(serviceAuthToken);
            }

            if (!CCD_DATA_SERVICE.equals(serviceName)) {
                log.error(CCD_PERSISTENCE_UNAUTHORISED);
                throw new UnAuthorisedServiceException(CCD_PERSISTENCE_UNAUTHORISED);
            }
        }

        return true;
    }
}
