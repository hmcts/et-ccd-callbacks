package uk.gov.hmcts.ethos.replacement.apitest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

public class SendNotificationControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";

    private static final String ABOUT_TO_START_URL = "/sendNotification/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/sendNotification/aboutToSubmit";
    private static final String SUBMITTED_URL = "/sendNotification/submitted";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() throws Exception {
        // Create a real case in CCD
        JSONObject caseJson = createSinglesCaseDataStore();

        // Map the created case JSON to CaseDetails and CaseData
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        CaseDetails caseDetails = mapper.readValue(caseJson.toString(), CaseDetails.class);
        CaseData caseData = caseDetails.getCaseData();

        // Build the CCDRequest using the real case data
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId(caseDetails.getCaseId())
                .build();
    }

    @Test
    void shouldReceiveSuccessResponseWhenAboutToStartInvoked() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(ABOUT_TO_START_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void shouldReceiveSuccessResponseWhenAboutToSubmitInvoked() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(ABOUT_TO_SUBMIT_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void shouldReceiveSuccessResponseWhenSubmittedInvoked() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(SUBMITTED_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }
}
