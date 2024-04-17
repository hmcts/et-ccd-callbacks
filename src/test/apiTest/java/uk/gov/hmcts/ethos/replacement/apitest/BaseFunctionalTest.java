package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
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
import uk.gov.hmcts.ethos.replacement.apitest.model.CreateUser;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DocmosisApplication.class})
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public abstract class BaseFunctionalTest {
    public static final String AUTHORIZATION = "Authorization";

    protected String userToken;
    protected CloseableHttpClient client;
    protected IdamTestApiRequests idamTestApiRequests;

    @Autowired
    private ResourceLoader resourceLoader;
    @Value("${ft.base.url}")
    protected String baseUrl;
    @Value("${ft.idam.url}")
    private String idamApiUrl;
    @Value("${et-sya-api.url}")
    protected String syaApiUrl;
    @Value("${ft.exui.url}")
    protected String exuiUrl;
    protected RequestSpecification spec;

    @BeforeAll
    public void setup() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException {
        log.info("BaseFunctionalTest setup started.");
        client = buildClient();
        idamTestApiRequests = new IdamTestApiRequests(client, idamApiUrl);
        CreateUser user = idamTestApiRequests.createUser(createRandomEmail());
        userToken = baseUrl.contains("localhost") ? idamTestApiRequests.getLocalAccessToken()
                : idamTestApiRequests.getAccessToken(user.getEmail());
        useRelaxedHTTPSValidation();
        spec = new RequestSpecBuilder().setBaseUri(baseUrl).build();
        log.info("BaseFunctionalTest setup completed.");
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

    protected JSONObject createCase() throws IOException {
        List<String> cookies = idamTestApiRequests.idamAuth();

        HttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse triggerCreateResponse = instance.execute(
                buildAuthorisedRequest(
                        get(exuiUrl + "/data/internal/case-types/ET_Scotland/event-triggers/initiateCase"),
                        cookies,
                        null
                ));

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(triggerCreateResponse.getEntity()));
        String eventToken = jsonObject.get("event_token").toString();

        String newCase = readJsonResource(resourceLoader.getResource("classpath:/caseDetails2024Flex.json"));
        StringEntity caseEntity = new StringEntity(newCase.replace("%EVENT_TOKEN%", eventToken));

        HttpResponse createResponse = instance.execute(
                buildAuthorisedRequest(
                        post(exuiUrl + "/data/case-types/ET_Scotland/cases")
                                .setHeader("Content-Type", "application/json"), cookies, caseEntity
                )
        );

        return new JSONObject(EntityUtils.toString(createResponse.getEntity()));
    }

    private HttpUriRequest buildAuthorisedRequest(RequestBuilder request, List<String> cookies, HttpEntity entity) {
        String xsrfToken = cookies.stream().filter(o -> o.startsWith("XSRF-TOKEN")).findFirst().get().replace("XSRF-TOKEN=", "");

        RequestBuilder requestBuilder = request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:102.0) Gecko/20100101 Firefox/102.0")
                .setHeader("Accept", "*/*")
                .setHeader("Accept-Language", "en-GB,en;q=0.5")
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .setHeader("Referer", exuiUrl)
                .setHeader("Cookie", String.join("; ", cookies))
                .setHeader("Connection", "keep-alive")
                .setHeader("Upgrade-Insecure-Requests", "1")
                .setHeader("Sec-Fetch-Dest", "empty")
                .setHeader("Sec-Fetch-Mode", "cors")
                .setHeader("Sec-Fetch-Site", "same-origin")
                .setHeader("Pragma", "no-cache")
                .setHeader("Cache-Control", "no-cache")
                .setHeader("X-XSRF-TOKEN", xsrfToken)
                .setHeader("experimental", "true");

        requestBuilder.setEntity(entity);
        return requestBuilder.build();
    }

    public String readJsonResource(Resource jsonResource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}

