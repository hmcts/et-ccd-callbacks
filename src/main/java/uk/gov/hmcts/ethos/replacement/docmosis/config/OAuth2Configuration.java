package uk.gov.hmcts.ethos.replacement.docmosis.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@Slf4j
public class OAuth2Configuration {

    private String clientId;
    private String redirectUri;
    private String clientSecret;
    private String clientScope;

    @Autowired
    public OAuth2Configuration(
        @Value("${idam.client.redirect_uri:}") String redirectUri,
        @Value("${idam.client.id:}") String clientId,
        @Value("${idam.client.secret:}") String clientSecret,
        @Value("${idam.client.scope:openid profile roles}") String clientScope
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.clientSecret = clientSecret;
        this.clientScope = clientScope;

        log.error(this.clientId);
        log.error(this.redirectUri);
        log.error(this.clientScope);
        log.error(this.clientSecret);
    }
}