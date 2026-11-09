package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.AsymmetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.SecretJWK;
import com.nimbusds.jose.jwk.source.JWKSetSource;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.jwk.source.URLBasedJWKSetSource;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.VerifyTokenServiceException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Key;
import java.util.List;

@Slf4j
@Service("verifyTokenService")
public class VerifyTokenService {

    // Timeouts for fetching IDAM's JWKS. The key set is cached, so these apply only to the occasional refresh.
    private static final int JWKS_CONNECT_TIMEOUT_MILLIS = 2000;
    private static final int JWKS_READ_TIMEOUT_MILLIS = 2000;

    private final JWSVerifierFactory jwsVerifierFactory;
    private final JWKSource<SecurityContext> jwkSource;
    public static final String INVALID_TOKEN = "Invalid Token {}";

    /**
     * Fetches IDAM's signing keys at most once per cache period (five minutes), and again, rate limited, when a
     * token carries a key id the cached set doesn't contain, so key rotation is picked up without a restart.
     */
    @Autowired
    public VerifyTokenService(@Value("${idam.api.jwkUrl}") String idamJwkUrl) {
        this(new URLBasedJWKSetSource<>(
            toUrl(idamJwkUrl),
            new DefaultResourceRetriever(JWKS_CONNECT_TIMEOUT_MILLIS, JWKS_READ_TIMEOUT_MILLIS,
                JWKSourceBuilder.DEFAULT_HTTP_SIZE_LIMIT)));
    }

    VerifyTokenService(JWKSetSource<SecurityContext> jwkSetSource) {
        this.jwsVerifierFactory = new DefaultJWSVerifierFactory();
        this.jwkSource = JWKSourceBuilder.create(jwkSetSource).build();
    }

    public boolean verifyTokenSignature(String token) {
        try {
            String tokenTocheck = token.replace("Bearer ", "");
            SignedJWT signedJwt = SignedJWT.parse(tokenTocheck);

            JWSHeader jwsHeader = signedJwt.getHeader();
            Key key = findKeyById(jwsHeader.getKeyID());

            JWSVerifier jwsVerifier = jwsVerifierFactory.createJWSVerifier(jwsHeader, key);

            return signedJwt.verify(jwsVerifier);
        } catch (Exception e) {
            log.error("Token validation error:", e);
            return false;
        }
    }

    private static URL toUrl(String jwksUrl) {
        try {
            return new URI(jwksUrl).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException("Invalid IDAM JWKS URL " + jwksUrl, e);
        }
    }

    private List<JWK> loadKeys(String keyId) {
        try {
            return jwkSource.get(new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build()), null);
        } catch (KeySourceException e) {
            log.error("JWKS key loading error", e);
            throw new VerifyTokenServiceException("JWKS error", e);
        }
    }

    private Key findKeyById(String keyId) {
        try {
            List<JWK> matchingKeys = keyId == null ? List.of() : loadKeys(keyId);
            if (matchingKeys.isEmpty()) {
                throw new VerifyTokenServiceException("JWK does not exist in the key set");
            }
            JWK jsonWebKey = matchingKeys.get(0);
            if (jsonWebKey instanceof SecretJWK secretJWK) {
                return secretJWK.toSecretKey();
            }
            if (jsonWebKey instanceof AsymmetricJWK asymmetricJWK) {
                return asymmetricJWK.toPublicKey();
            }
            throw new VerifyTokenServiceException("Unsupported JWK " + jsonWebKey.getClass().getName());
        } catch (JOSEException e) {
            log.error("Invalid JWK key", e);
            throw new VerifyTokenServiceException("Invalid JWK", e);
        }
    }

    public boolean isTokenSignatureValid(String token) {
        return verifyTokenSignature(token);
    }
}
