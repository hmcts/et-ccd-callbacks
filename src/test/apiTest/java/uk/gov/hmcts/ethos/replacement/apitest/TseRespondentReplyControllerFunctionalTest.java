package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.apitest.utils.CCDRequestBuilder;

import java.util.ArrayList;
import java.util.Collections;

@Slf4j
public class TseRespondentReplyControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";
    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("testCaseReference");
        caseData.setClaimant("claimant");


        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withCaseId("123")
            .build();

    }

    @Test
    void aboutToStart_successResponse() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post("/tseRespond/aboutToStart")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }
}
