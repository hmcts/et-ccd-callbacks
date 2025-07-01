package uk.gov.hmcts.ethos.replacement.apitest.multiples;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.apitest.BaseFunctionalTest;

import java.io.IOException;
import org.apache.hc.core5.http.ParseException;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@Slf4j
public class CreateReferralMultiplesControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_START_URL = "multiples/createReferral/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "multiples/createReferral/aboutToSubmit";
    private static final String SUBMITTED_URL = "multiples/createReferral/completeCreateReferral";
    private static final String MID_VALIDATE_URL = "multiples/createReferral/validateReferentEmail";
    private MultipleRequest request;

    @BeforeAll
    public void setUpCaseData() throws IOException, InterruptedException, ParseException {
        MultipleData multipleData = MultipleData.builder().build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(SCOTLAND_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);

        JSONObject singleCase = createSinglesCaseDataStore();
        multipleData.setLeadCase(String.valueOf(singleCase.getLong("id")));

        multipleData.setIsUrgent("Yes");
        multipleData.setReferentEmail("test@gmail.com");
        multipleData.setReferralSubject("ET1");
        multipleData.setMultipleReference("123");
    }

    @Test
    void aboutToStartUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(ABOUT_TO_START_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void midValidateUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(MID_VALIDATE_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void aboutToSubmitUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(ABOUT_TO_SUBMIT_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void submittedUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(SUBMITTED_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }
}