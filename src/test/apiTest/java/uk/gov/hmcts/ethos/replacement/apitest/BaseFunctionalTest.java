package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ethos.replacement.apitest.model.CreateUser;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.VenueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static io.restassured.RestAssured.useRelaxedHTTPSValidation;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DocmosisApplication.class})
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@WithTag("ApiTest")
public abstract class BaseFunctionalTest {
    protected String userToken;
    protected CloseableHttpClient client;
    protected IdamTestApiRequests idamTestApiRequests;

    @MockBean
    private JudgeRepository judgeRepository;
    @MockBean
    private VenueRepository venueRepository;
    @MockBean
    private uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.RoomRepository roomRepository;
    @MockBean
    private CourtWorkerRepository courtWorkerRepository;
    @MockBean
    private MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;
    @MockBean
    private MultipleRefScotlandRepository multipleRefScotlandRepository;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private SubMultipleRefEnglandWalesRepository subMultipleRefEnglandWalesRepository;
    @MockBean
    private SubMultipleRefScotlandRepository subMultipleRefScotlandRepository;
    @MockBean
    private SingleRefScotlandRepository singleRefScotlandRepository;
    @MockBean
    private SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;
    @MockBean
    private FileLocationRepository fileLocationRepository;

    @Value("${docmosis.test.url}")
    protected String baseUrl;
    @Value("${idam.url}")
    private String idamApiUrl;
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
        return httpClientBuilder.build();
    }

}

