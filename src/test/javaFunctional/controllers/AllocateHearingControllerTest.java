package controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.AllocateHearingController;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.AllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandAllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebMvcTest(AllocateHearingController.class)
@ContextConfiguration(classes = {DocmosisApplication.class, AllocateHearingController.class})
@ActiveProfiles("test")
public class AllocateHearingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private uk.gov.hmcts.et.common.model.ccd.CCDRequest ccdRequest;
    private static Properties properties;
    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private AllocateHearingService allocateHearingService;

    @MockBean
    private ScotlandAllocateHearingService scotlandAllocateHearingService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        Mockito.when(verifyTokenService.verifyTokenSignature("validToken")).thenReturn(true);
    }

    @Test
    public void testInitialiseHearingDynamicList() throws IOException {
        String environment = System.getProperty("VAULTNAME").replace("ethos-", "");
        CCDRequest ccdRequest = generateCCDRequest();
        String userToken = getAuthToken(environment);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", userToken)
                .body(ccdRequest)
                .post("/allocatehearing/initialiseHearings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    //todo Will add similar tests for other endpoints: handleListingSelected, handleManagingOfficeSelected,
    // populateRooms,
    // aboutToSubmit tomorrow

    private CCDRequest generateCCDRequest() {
        // Implement this method to return a valid CCDRequest object
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(generateCaseData())
                .withCaseId("123")
                .build();
        return ccdRequest;
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
        httpRequest.formParam("password",  getProperty(environment.toLowerCase() + ".ccd.password"));
        Response response = httpRequest.post(getProperty(environment.toLowerCase() + ".idam.auth.url"));

        Assert.assertEquals(200, response.getStatusCode());

        return response.body().jsonPath().getString("access_token");
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

}
