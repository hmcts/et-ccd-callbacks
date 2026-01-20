package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;

/**
 * User service for getting access tokens and user details.
 * Migrated from et-message-handler.
 */
@Slf4j
@Component
public class UserService implements uk.gov.hmcts.ecm.common.service.UserService {

    private final IdamApi idamApi;
    private final AccessTokenService accessTokenService;

    @Value("${caseWorkerUserName:caseworker@example.com}")
    private String caseWorkerUserName;
    
    @Value("${caseWorkerPassword:password}")
    private String caseWorkerPassword;

    @Autowired
    public UserService(IdamApi idamApi, AccessTokenService accessTokenService) {
        this.idamApi = idamApi;
        this.accessTokenService = accessTokenService;
    }

    @Override
    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    @Override
    public UserDetails getUserDetailsById(String authToken, String userId) {
        return idamApi.getUserByUserId(authToken, userId);
    }

    public String getAccessToken() {
        return accessTokenService.getAccessToken(caseWorkerUserName, caseWorkerPassword);
    }
}
