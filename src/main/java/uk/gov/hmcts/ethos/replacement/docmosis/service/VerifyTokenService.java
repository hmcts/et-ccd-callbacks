package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/**
 * Verifies incoming JWT bearer tokens using Spring Security's {@link JwtDecoder}.
 *
 * <p>Validation covers:
 * <ul>
 *   <li>Cryptographic signature – verified against IDAM's JWKS endpoint</li>
 *   <li>Expiry / not-before – via {@code JwtTimestampValidator}</li>
 *   <li>Issuer – via {@code MultiIssuerValidator} (configurable list)</li>
 *   <li>Audience – verified against the configured client ID</li>
 * </ul>
 */
@Slf4j
@Service("verifyTokenService")
@RequiredArgsConstructor
public class VerifyTokenService {

    public static final String INVALID_TOKEN = "Invalid Token {}";

    private final JwtDecoder jwtDecoder;

    /**
     * Validates the supplied bearer token.
     *
     * @param token the raw {@code Authorization} header value, with or without the
     *              {@code Bearer } prefix
     * @return {@code true} if the token passes all validation checks; {@code false} otherwise
     */
    public boolean verifyTokenSignature(String token) {
        if (token == null) {
            return false;
        }
        try {
            String tokenValue = token.startsWith("Bearer ") ? token.substring(7) : token;
            jwtDecoder.decode(tokenValue);
            return true;
        } catch (JwtException e) {
            log.error(INVALID_TOKEN, e.getMessage());
            return false;
        }
    }
}
