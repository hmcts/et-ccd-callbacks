package controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//@WebMvcTest(DocmosisApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {DocmosisApplication.class})
public class AllocateHearingControllerTest {

    private static Properties properties;
    @Autowired
    private MockMvc mockMvc;
    private uk.gov.hmcts.et.common.model.ccd.CCDRequest ccdRequest;
    private String userToken;
    @Value("${cftUser}")
    private String cftUser;
    @Value("${cftPassword}")
    private String cftPassword;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void testInitialiseHearingDynamicList() throws IOException {
        String test_url = System.getenv("TEST_URL") != null ? System.getenv("TEST_URL")
                : "http://localhost:8080";
        try {
            userToken = getAuthToken();
        } catch (NullPointerException e) {
            userToken = getAuthTokenFromLocal();
        }
        CCDRequest ccdRequest = generateCCDRequest();

        RestAssured.given().log().all()
                .relaxedHTTPSValidation()
                .baseUri(test_url)
                .basePath("/allocatehearing/initialiseHearings")
                .header("Content-type", ContentType.JSON)
                .header("Authorization", userToken)
                .body(ccdRequest)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    private CCDRequest generateCCDRequest() {
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(generateCaseData())
                .withCaseId("123")
                .build();
        return ccdRequest;
    }

    public String getAuthTokenFromLocal() {
        Response response = RestAssured.given()
                .log().all()
                .contentType(ContentType.URLENC)
                .formParam("client_id", "fake")
                .formParam("client_secret", "fake")
                .formParam("grant_type", "password")
                .formParam("redirect_uri", "example.com")
                .formParam("username", cftUser)
                .formParam("password", cftPassword)
                .formParam("scope", "openid profile roles")
                .post("http://localhost:5000/o/token");
        JSONObject jsonResponse = new JSONObject(response.getBody().asString());
        return "Bearer " + jsonResponse.getString("access_token");
    }

    private CaseData generateCaseData() {
        uk.gov.hmcts.et.common.model.ccd.CaseData caseData = CaseDataBuilder.builder()
                .withHearingScotland("hearingNumber", Constants.HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                        TribunalOffice.ABERDEEN, "venue")
                .withHearingSession(
                        0,
                        "hearingNumber",
                        "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();
        return caseData;
    }

    public static String getProperty(String name) throws IOException {

        if (properties == null) {
            try (InputStream inputStream =
                         new FileInputStream("src/test/apiTest/resources/application.properties")) {
                properties = new Properties();
                properties.load(inputStream);
            }
        }

        return properties.getProperty(name);
    }

    public static String getAuthToken() throws IOException {
        String idamBaseUrl = "https://idam-api.aat.platform.hmcts.net";
        String redirectUri = "https://et-cos-pr-927.preview.platform.hmcts.net/oauth2/callback";
        String clientId = "xuiwebapp";
        String clientSecret = System.getenv("IDAM_CLIENT_SECRET");
        String username = System.getenv("ET_CCD_CASEWORKER_USER_NAME");
        String password = System.getenv("ET_CCD_CASEWORKER_PASSWORD");
        String scope = "openid profile roles";
        String idamCodePath =
                "/oauth2/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri;

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        Response codeResponse = RestAssured.given()
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(idamBaseUrl + idamCodePath);

        String code = codeResponse.jsonPath().getString("code");

        String idamAuthPath =
                "/oauth2/token?grant_type=authorization_code&client_id=" + clientId + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + redirectUri + "&code=" + code + "&scope=" + scope;

        Response authTokenResponse = RestAssured.given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(idamBaseUrl + idamAuthPath);

        String accessToken = authTokenResponse.jsonPath().getString("access_token");
        System.out.println("Access Token: " + accessToken);

        return accessToken;
    }

}
