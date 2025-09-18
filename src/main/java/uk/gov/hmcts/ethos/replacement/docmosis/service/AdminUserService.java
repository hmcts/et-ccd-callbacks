package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;

@CacheConfig(cacheNames = {"adminUserToken"})
@RequiredArgsConstructor
@Service
@Slf4j
public class AdminUserService {
    public static final String BEARER = "Bearer";
    private final UserIdamService userIdamService;

    @Value("${etcos.system.username}")
    private String systemUserName;

    @Value("${etcos.system.password}")
    private String systemUserPassword;

    @Cacheable("adminUserToken")
    public String getAdminUserToken() {
        return String.join(" ", BEARER, userIdamService.getAccessToken(systemUserName, systemUserPassword));
    }

    @CacheEvict(value = "adminUserToken", allEntries = true)
    @Scheduled(fixedRateString = "${caching.adminUserService}")
    public void emptyAdminUserToken() {
        log.info("emptying adminUserToken cache");
    }

    public UserDetails getUserDetails(String accessToken, String userId) {
        return userIdamService.getUserDetailsById(accessToken, userId);
    }
}
