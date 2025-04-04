package uk.gov.hmcts.ethos.replacement.docmosis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.client.CaseDataBuilder;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.client.CcdClientConfig;
import uk.gov.hmcts.ecm.common.client.EcmCaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Configuration
public class CcdClientConfiguration {

    @Value("${ccd.data-store-api-url}")
    private String ccdDataStoreApiBaseUrl;

    @Bean
    public CcdClient ccdClient(RestTemplate restTemplate, UserIdamService userIdamService,
                               CaseDataBuilder caseDataBuilder, AuthTokenGenerator authTokenGenerator,
                               EcmCaseDataBuilder ecmCaseDataBuilder) {
        return new CcdClient(restTemplate, userIdamService, caseDataBuilder,
                new CcdClientConfig(ccdDataStoreApiBaseUrl), authTokenGenerator, ecmCaseDataBuilder);
    }

    @Bean
    public CaseDataBuilder caseDataBuilder(ObjectMapper objectMapper) {
        return new CaseDataBuilder(objectMapper);
    }

    @Bean
    public EcmCaseDataBuilder ecmCaseDataBuilder(ObjectMapper objectMapper) {
        return new EcmCaseDataBuilder(objectMapper);
    }
}
