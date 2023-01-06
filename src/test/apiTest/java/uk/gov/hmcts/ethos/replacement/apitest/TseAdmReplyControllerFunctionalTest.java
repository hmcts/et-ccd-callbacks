package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.apitest.utils.CCDRequestBuilder;

@Slf4j
class TseAdmReplyControllerFunctionalTest extends BaseFunctionalTest  {

    private static final String AUTHORIZATION = "Authorization";
    private static final String MID_DETAILS_TABLE = "/tseAdmReply/midDetailsTable";
    private static final String ABOUT_TO_SUBMIT_URL = "/tseAdmReply/aboutToSubmit";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        ccdRequest = CCDRequestBuilder.builder().build();
    }

    @Test
    void midDetailsTableSuccessResponse() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(MID_DETAILS_TABLE)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void aboutToSubmitSuccessResponse() {
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
    void submittedSuccessResponse() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post("/tseAdmReply/submitted")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }
}
