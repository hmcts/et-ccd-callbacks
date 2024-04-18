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
import org.springframework.retry.annotation.Retryable;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ethos.replacement.apitest.model.CreateUser;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpRetryException;
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
    protected ResourceLoader resourceLoader;
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

    private static HttpResponse retryRequest(HttpClient instance, HttpUriRequest request) throws IOException, InterruptedException {
        int maxAttempts = 5;
        int attempt = 0;
        while (attempt++ < maxAttempts) {
            HttpResponse response = instance.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 500) {
                log.error("something unexpected happened. status code was " + statusCode);
                Thread.sleep(1000);
                continue;
            }

            return response;
        }

        HttpResponse response = instance.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 500) {
            throw new HttpRetryException("Request to " + request.getURI() + " failed", 500);
        }

        return response;
    }

    private List<String> retryLogin() throws InterruptedException {
        int maxAttempts = 5;
        int attempt = 0;
        while (attempt++ < maxAttempts) {
            try {
                return idamTestApiRequests.idamAuth();
            } catch (Exception e) {
                log.warn("Login to idam failed - retrying in 5 seconds...");
                Thread.sleep(5000);
            }
        }
        throw new InterruptedException("Failed to login to Idam");
    }

    protected JSONObject runEventOnCase(String name, String jsonPayload, String caseId) throws IOException, InterruptedException {
        List<String> cookies = retryLogin();

        HttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();
        String url = String.format("%s/data/internal/cases/%s/event-triggers/%s", exuiUrl, caseId, name);
        HttpResponse triggerCreateResponse = retryRequest(instance, buildAuthorisedRequest(get(url), cookies, null));

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(triggerCreateResponse.getEntity()));
        String eventToken = jsonObject.get("event_token").toString();

        StringEntity caseEntity = new StringEntity(jsonPayload.replace("%EVENT_TOKEN%", eventToken));

        String postUrl = String.format("%s/data/cases/%s/events", exuiUrl, caseId);
        HttpUriRequest request = buildAuthorisedRequest(
                post(postUrl).setHeader("Content-Type", "application/json"), cookies, caseEntity
        );

        HttpResponse createResponse = retryRequest(instance, request);

        return new JSONObject(EntityUtils.toString(createResponse.getEntity()));
    }

    protected JSONObject createSinglesCase() throws IOException, InterruptedException {
        return createCase("initiateCase", readJsonResource("eventInitiateCase"), "ET_Scotland");
    }

    protected JSONObject createMultiplesCase(String leadCaseId) throws IOException, InterruptedException {
        String eventCreateMultiple = readJsonResource("eventCreateMultiple");
        String transformed = eventCreateMultiple.replace("%LEAD_ETHOS_REF%", leadCaseId);
        return createCase("createMultiple", transformed, "ET_Scotland_Multiple");
    }

    protected JSONObject createCase(String name, String payload, String caseTypeId) throws IOException, InterruptedException {
        List<String> cookies = retryLogin();

        HttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();
        String url = String.format("%s/data/internal/case-types/%s/event-triggers/%s", exuiUrl, caseTypeId, name);
        HttpResponse triggerCreateResponse = retryRequest(instance, buildAuthorisedRequest(get(url), cookies,null));

        JSONObject jsonObject = new JSONObject(EntityUtils.toString(triggerCreateResponse.getEntity()));
        String eventToken = jsonObject.get("event_token").toString();

        StringEntity caseEntity = new StringEntity(payload.replace("%EVENT_TOKEN%", eventToken));

        RequestBuilder postUrl = post(String.format("%s/data/case-types/%s/cases", exuiUrl, caseTypeId))
                .setHeader("Content-Type", "application/json");

        HttpResponse createResponse = retryRequest(instance, buildAuthorisedRequest(postUrl, cookies, caseEntity));

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

    public String readJsonResource(String name) throws IOException {
        Resource resource = resourceLoader.getResource(String.format("classpath:/%s.json", name));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
