package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator;
import com.nimbusds.jose.jwk.source.JWKSetSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that IDAM's signing keys are cached rather than fetched for every token.
 */
class VerifyTokenServiceCachingTest {

    private RSAKey currentKey;
    private RSAKey rotatedKey;
    private StubJwkSetSource jwkSetSource;
    private VerifyTokenService verifyTokenService;

    @BeforeEach
    void setUp() throws JOSEException {
        currentKey = new RSAKeyGenerator(2048).keyID("current").generate();
        rotatedKey = new RSAKeyGenerator(2048).keyID("rotated").generate();
        jwkSetSource = new StubJwkSetSource();
        verifyTokenService = new VerifyTokenService(jwkSetSource);
    }

    @Test
    void fetchesKeySetOnceForRepeatedTokens() throws JOSEException {
        jwkSetSource.respondWith(new JWKSet(currentKey.toPublicJWK()));
        String token = bearerToken(currentKey);

        assertThat(verifyTokenService.verifyTokenSignature(token)).isTrue();
        assertThat(verifyTokenService.verifyTokenSignature(token)).isTrue();
        assertThat(verifyTokenService.verifyTokenSignature(token)).isTrue();

        assertThat(jwkSetSource.calls).isEqualTo(1);
    }

    @Test
    void refetchesKeySetWhenTokenUsesUnknownKeyId() throws JOSEException {
        jwkSetSource.respondWith(new JWKSet(currentKey.toPublicJWK()));
        jwkSetSource.respondWith(new JWKSet(List.of(currentKey.toPublicJWK(), rotatedKey.toPublicJWK())));

        assertThat(verifyTokenService.verifyTokenSignature(bearerToken(currentKey))).isTrue();
        assertThat(verifyTokenService.verifyTokenSignature(bearerToken(rotatedKey))).isTrue();

        assertThat(jwkSetSource.calls).isEqualTo(2);
    }

    @Test
    void rejectsTokenSignedWithKeyNotInKeySet() throws JOSEException {
        jwkSetSource.respondWith(new JWKSet(currentKey.toPublicJWK()));
        RSAKey unknownKey = new RSAKeyGenerator(2048).keyID("current").generate();

        assertThat(verifyTokenService.verifyTokenSignature(bearerToken(unknownKey))).isFalse();
    }

    @Test
    void rejectsTokenWhenKeySetCannotBeFetched() throws JOSEException {
        jwkSetSource.failWith(new KeySourceException("IDAM unavailable"));

        assertThat(verifyTokenService.verifyTokenSignature(bearerToken(currentKey))).isFalse();
    }

    private static String bearerToken(RSAKey signingKey) throws JOSEException {
        SignedJWT jwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID()).build(),
            new JWTClaimsSet.Builder().subject("user").build());
        jwt.sign(new RSASSASigner(signingKey));
        return "Bearer " + jwt.serialize();
    }

    /**
     * Stands in for IDAM's JWKS endpoint, returning queued key sets in order and repeating the last one.
     */
    private static final class StubJwkSetSource implements JWKSetSource<SecurityContext> {
        private final Deque<JWKSet> responses = new ArrayDeque<>();
        private KeySourceException failure;
        private int calls;

        void respondWith(JWKSet jwkSet) {
            responses.add(jwkSet);
        }

        void failWith(KeySourceException exception) {
            failure = exception;
        }

        @Override
        public JWKSet getJWKSet(JWKSetCacheRefreshEvaluator refreshEvaluator, long currentTime,
                                SecurityContext context) throws KeySourceException {
            calls++;
            if (failure != null) {
                throw failure;
            }
            return responses.size() > 1 ? responses.poll() : responses.peek();
        }

        @Override
        public void close() {
            // nothing to release
        }
    }
}
