package uk.gov.hmcts.ethos.replacement.docmosis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.compat.common.client.CaseDataBuilder;
import uk.gov.hmcts.ecm.compat.common.client.CcdClient;
import uk.gov.hmcts.ecm.compat.common.client.CcdClientConfig;
import uk.gov.hmcts.ecm.compat.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.compat.common.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Configuration
public class EcmCompatCcdClientConfiguration {

    @Value("${ccd.data-store-api-url}")
    private String ccdDataStoreApiBaseUrl;

    @Bean
    public CcdClient ecmCcdClient(RestTemplate restTemplate, UserIdamService userIdamService,
                                  ObjectMapper objectMapper, AuthTokenGenerator authTokenGenerator) {
        UserService compatUserService = new UserService() {
            @Override
            public UserDetails getUserDetails(String authorisation) {
                return toCompatUserDetails(userIdamService.getUserDetails(authorisation));
            }

            @Override
            public UserDetails getUserDetailsById(String authToken, String userId) {
                return toCompatUserDetails(userIdamService.getUserDetailsById(authToken, userId));
            }

            private UserDetails toCompatUserDetails(uk.gov.hmcts.ecm.common.idam.models.UserDetails ud) {
                UserDetails compatUd = new UserDetails();
                compatUd.setUid(ud.getUid());
                compatUd.setEmail(ud.getEmail());
                compatUd.setRoles(ud.getRoles());
                compatUd.setName(ud.getName());
                compatUd.setFirstName(ud.getFirstName());
                compatUd.setLastName(ud.getLastName());
                return compatUd;
            }
        };

        return new CcdClient(
                restTemplate,
                compatUserService,
                new CaseDataBuilder(objectMapper),
                new CcdClientConfig(ccdDataStoreApiBaseUrl),
                authTokenGenerator
        );
    }
}
