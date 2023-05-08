package controllers;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

@WebMvcTest(DocmosisApplication.class)
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
    public void setup() throws FileNotFoundException {
        String reportingDirectory = "functional-output";

        PrintStream fileOutputStream = new PrintStream(new FileOutputStream(reportingDirectory
                + "/javaFunctionalreport.log", true));

        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig().defaultStream(fileOutputStream));

        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    public void testInitialiseHearingDynamicList() throws IOException {
        try {
            String environment = System.getProperty("VAULTNAME").replace("ethos-", "");
            userToken = getAuthToken(environment);
        } catch (NullPointerException e) {
            userToken = getAuthTokenFromLocal();
        }
        CCDRequest ccdRequest = generateCCDRequest();

        RestAssured.given().log().all()
                .baseUri("http://localhost:8081")
                .basePath("/allocatehearing/initialiseHearings")
                .header("Content-type", ContentType.JSON)
                .header("Authorization", userToken)
                .body(ccdRequest)
                .when()
                .post()
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    //todo Will add similar tests for other endpoints: handleListingSelected, handleManagingOfficeSelected,
    // populateRooms,
    // aboutToSubmit tomorrow

    private CCDRequest generateCCDRequest() {
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(generateCaseData())
                .withCaseId("123")
                .build();
        return ccdRequest;
    }

    public String getAuthTokenFromLocal() {
        Response response = RestAssured.given()
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
            try (InputStream inputStream = new FileInputStream("src/test/javaFunctional/resources/config.properties")) {
                properties = new Properties();
                properties.load(inputStream);
            }
        }

        return properties.getProperty(name);
    }

    public static String getAuthToken(String environment) throws IOException {

        //Generate Auth token using code
        RestAssured.useRelaxedHTTPSValidation();
        //RestAssured.config = RestAssuredConfig.config().sslConfig(SSLConfig.sslConfig().allowAllHostnames());
        RequestSpecification httpRequest = SerenityRest.given().relaxedHTTPSValidation().config(RestAssured.config);
        httpRequest.header("Accept", "application/json");
        httpRequest.header("Content-Type", "application/x-www-form-urlencoded");
        httpRequest.formParam("username", getProperty(environment.toLowerCase() + ".ccd.username"));
        httpRequest.formParam("password", getProperty(environment.toLowerCase() + ".ccd.password"));
        Response response = httpRequest.post(getProperty(environment.toLowerCase() + ".idam.auth.url"));

        Assert.assertEquals(200, response.getStatusCode());

        return response.body().jsonPath().getString("access_token");
    }

}
