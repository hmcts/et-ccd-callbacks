package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.AsymmetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.SecretJWK;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.VerifyTokenServiceException;

import java.net.URL;
import java.security.Key;

@Slf4j
@Service("verifyTokenService")
public class VerifyTokenService {

    @Value("${idam.api.jwkUrl}")
    private String idamJwkUrl;

    private final JWSVerifierFactory jwsVerifierFactory;
    public static final String INVALID_TOKEN = "Invalid Token {}";

    public VerifyTokenService() {
        this.jwsVerifierFactory = new DefaultJWSVerifierFactory();
    }

    public boolean verifyTokenSignature(String token) {
        try {
            String tokenTocheck = token.replace("Bearer ", "");
            SignedJWT signedJwt = SignedJWT.parse(tokenTocheck);

            JWKSet jsonWebKeySet = loadJsonWebKeySet(idamJwkUrl);

            JWSHeader jwsHeader = signedJwt.getHeader();
            Key key = findKeyById(jsonWebKeySet, jwsHeader.getKeyID());

            JWSVerifier jwsVerifier = jwsVerifierFactory.createJWSVerifier(jwsHeader, key);

            return signedJwt.verify(jwsVerifier);
        } catch (Exception e) {
            log.error("Token validation error:", e);
            return false;
        }
    }

    private JWKSet loadJsonWebKeySet(String jwksUrl) {
        try {
            return JWKSet.load(new URL(jwksUrl));
        } catch (Exception e) {
            log.error("JWKS key loading error", e);
            throw new VerifyTokenServiceException("JWKS error", e);
        }
    }

    private Key findKeyById(JWKSet jsonWebKeySet, String keyId) {
        try {
            JWK jsonWebKey = jsonWebKeySet.getKeyByKeyId(keyId);
            return switch (jsonWebKey) {
                case null -> throw new VerifyTokenServiceException("JWK does not exist in the key set");
                case SecretJWK secretJWK -> secretJWK.toSecretKey();
                case AsymmetricJWK asymmetricJWK -> asymmetricJWK.toPublicKey();
                default -> throw new VerifyTokenServiceException("Unsupported JWK " + jsonWebKey.getClass().getName());
            };
        } catch (JOSEException e) {
            log.error("Invalid JWK key", e);
            throw new VerifyTokenServiceException("Invalid JWK", e);
        }
    }

}
