package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import feign.form.FormProperty;
import lombok.Getter;

@Getter
public class TokenRequest {
    @FormProperty("client_id")
    private final String clientId;
    @FormProperty("client_secret")
    private final String clientSecret;
    @FormProperty("grant_type")
    private final String grantType;
    @FormProperty("redirect_uri")
    private final String redirectUri;
    @FormProperty("username")
    private final String username;
    @FormProperty("password")
    private final String password;
    @FormProperty("scope")
    private final String scope;
    @FormProperty("refresh_token")
    private final String refreshToken;
    @FormProperty("code")
    private final String code;

    public TokenRequest(
        String clientId,
        String clientSecret,
        String grantType,
        String redirectUri,
        String username,
        String password,
        String scope,
        String refreshToken,
        String code
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
        this.username = username;
        this.password = password;
        this.scope = scope;
        this.refreshToken = refreshToken;
        this.code = code;
    }
}

