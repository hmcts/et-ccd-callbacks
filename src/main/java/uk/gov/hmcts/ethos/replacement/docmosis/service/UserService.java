package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.OAuth2Configuration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;

@Component
public class UserService implements uk.gov.hmcts.ecm.common.service.UserService {
    private final IdamApi idamApi;
    private final OAuth2Configuration oauth2Configuration;

    public static final String OPENID_GRANT_TYPE = "password";

    @Autowired
    public UserService(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        this.idamApi = idamApi;
        this.oauth2Configuration = oauth2Configuration;
    }

    @Override
    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    @Override
    public UserDetails getUserDetailsById(String authorisation, String id) {
        return idamApi.retrieveUserDetails(authorisation);
    }

}
