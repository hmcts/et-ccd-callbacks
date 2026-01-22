package uk.gov.hmcts.ethos.replacement.apitest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.apitest.config.TestCacheConfig;
import uk.gov.hmcts.ethos.replacement.apitest.model.CreateUser;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DocmosisApplication.class, TestCacheConfig.class})
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseFunctionalTest {
    public static final String AUTHORIZATION = "Authorization";

    protected String userToken;
    protected String userId;
    protected CloseableHttpClient client;
    protected IdamTestApiRequests idamTestApiRequests;

    @Autowired
    protected ResourceLoader resourceLoader;
    @Value("${ft.base.url}")
    protected String baseUrl;
    @Value("${ft.idam.url}")
    private String idamApiUrl;
    @Value("${et-sya-api.url}")
    protected String syaApiUrl;
    @Value("${ccd.data-store-api-url}")
    private String ccdDataStoreUrl;
    protected RequestSpecification spec;
    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private UserIdamService userIdamService;

    @BeforeAll
    public void setup() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException {
        log.info("BaseFunctionalTest setup started.");
        client = buildClient();
        idamTestApiRequests = new IdamTestApiRequests(client, idamApiUrl);
        CreateUser user = idamTestApiRequests.createUser(createRandomEmail());
        userToken = baseUrl.contains("localhost") ? idamTestApiRequests.getLocalAccessToken()
                : idamTestApiRequests.getAccessToken(user.getEmail());
        userId = user.getId();
        useRelaxedHTTPSValidation();
        spec = new RequestSpecBuilder().setBaseUri(baseUrl).build();
        log.info("BaseFunctionalTest setup completed.");
        userToken = adminUserService.getAdminUserToken();
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        userId = userDetails.getUid();
    }

    protected JSONObject createSinglesCaseDataStore() throws IOException {
        return createCaseDataStore("initiateCase", readJsonResource("eventInitiateCase"), "ET_Scotland");
    }

    protected JSONObject createMultiplesCaseDataStore(String singleEthosRef) throws IOException {
        String eventCreateMultiple = readJsonResource("eventCreateMultiple");
        String transformed = eventCreateMultiple.replace("%LEAD_ETHOS_REF%", singleEthosRef);
        return createCaseDataStore("createMultiple", transformed, "ET_Scotland_Multiple");
    }

    private String createRandomEmail() {
        int randomNumber = (int) (Math.random() * 10_000_000);
        return "test" + randomNumber + "@hmcts.net";
    }

    private CloseableHttpClient buildClient()
        throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf =
            new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
            .setSSLSocketFactory(sslsf);
        log.info("BaseFunctionalTest client built.");
        return httpClientBuilder.build();
    }

    protected JSONObject createCaseDataStore(String name, String payload, String caseTypeId) throws IOException {
        String serviceToken = serviceAuthTokenGenerator.generate();
        String triggerFormat = "%s/caseworkers/%s/jurisdictions/EMPLOYMENT/case-types/%s/event-triggers/%s/token";
        String triggerUrl = String.format(triggerFormat, ccdDataStoreUrl, userId, caseTypeId, name);
        RequestBuilder requestBuilder = get(triggerUrl)
                .setHeader(AUTHORIZATION, userToken)
                .setHeader("ServiceAuthorization", serviceToken)
                .setHeader("Content-Type", "application/json");

        CloseableHttpResponse execute = client.execute(requestBuilder.build());
        JSONObject jsonObject = new JSONObject(EntityUtils.toString(execute.getEntity()));
        String eventToken = jsonObject.getString("token");

        StringEntity caseEntity = new StringEntity(payload.replace("%EVENT_TOKEN%", eventToken));

        String postFormat = "%s/caseworkers/%s/jurisdictions/EMPLOYMENT/case-types/%s/cases";
        RequestBuilder submitRequest = post(String.format(postFormat, ccdDataStoreUrl, userId, caseTypeId))
                .setHeader(AUTHORIZATION, userToken)
                .setHeader("ServiceAuthorization", serviceToken)
                .setHeader("Content-Type", "application/json")
                .setEntity(caseEntity);

        CloseableHttpResponse postResponse = client.execute(submitRequest.build());
        String result = EntityUtils.toString(postResponse.getEntity());
        return new JSONObject(result);
    }

    public String readJsonResource(String name) throws IOException {
        Resource resource = resourceLoader.getResource(String.format("classpath:/%s.json", name));
        InputStreamReader in = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader(in)) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public CaseDetails generateCaseDetails(String jsonFileName) throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }
}
