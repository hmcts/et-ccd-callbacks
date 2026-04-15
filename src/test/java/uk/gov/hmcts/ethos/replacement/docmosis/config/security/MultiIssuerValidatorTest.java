package uk.gov.hmcts.ethos.replacement.docmosis.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiIssuerValidatorTest {

    private static final String ISSUER_A = "https://idam-api.platform.hmcts.net/o";
    private static final String ISSUER_B = "http://sidam-api:5000/o";

    private Jwt buildJwtWithIssuer(String issuer) {
        return Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claim("iss", URI.create(issuer))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }

    @Test
    void validate_emptyIssuerList_alwaysPasses() {
        MultiIssuerValidator validator = new MultiIssuerValidator(List.of());
        OAuth2TokenValidatorResult result = validator.validate(buildJwtWithIssuer(ISSUER_A));
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_issuerInPermittedList_passes() {
        MultiIssuerValidator validator = new MultiIssuerValidator(List.of(ISSUER_A, ISSUER_B));
        OAuth2TokenValidatorResult result = validator.validate(buildJwtWithIssuer(ISSUER_A));
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_secondIssuerInList_passes() {
        MultiIssuerValidator validator = new MultiIssuerValidator(List.of(ISSUER_A, ISSUER_B));
        OAuth2TokenValidatorResult result = validator.validate(buildJwtWithIssuer(ISSUER_B));
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void validate_issuerNotInList_fails() {
        MultiIssuerValidator validator = new MultiIssuerValidator(List.of(ISSUER_A));
        OAuth2TokenValidatorResult result = validator.validate(buildJwtWithIssuer(ISSUER_B));
        assertFalse(result.getErrors().isEmpty());
    }

    @Test
    void validate_nullIssuerClaim_fails() {
        MultiIssuerValidator validator = new MultiIssuerValidator(List.of(ISSUER_A));
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .claims(c -> c.put("sub", "user"))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
        OAuth2TokenValidatorResult result = validator.validate(jwt);
        assertFalse(result.getErrors().isEmpty());
    }
}
