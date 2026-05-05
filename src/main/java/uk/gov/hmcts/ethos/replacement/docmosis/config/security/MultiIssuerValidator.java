package uk.gov.hmcts.ethos.replacement.docmosis.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

/**
 * Validates the {@code iss} claim of an incoming JWT against a configurable list of
 * permitted issuer URIs.  The token is accepted if its issuer matches <em>any</em> entry
 * in the list.  When the list is empty the validator is a no-op so that environments
 * where no issuer is configured are not broken.
 */
@RequiredArgsConstructor
public class MultiIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_ISSUER_ERROR =
        new OAuth2Error("invalid_token", "Token issuer is not in the list of permitted issuers", null);

    private final List<String> permittedIssuers;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (permittedIssuers.isEmpty()) {
            return OAuth2TokenValidatorResult.success();
        }

        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
        if (issuer != null && permittedIssuers.contains(issuer)) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(INVALID_ISSUER_ERROR);
    }
}
