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
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

@Slf4j
public class TseClaimantControllerFunctionalTest extends BaseFunctionalTest  {
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_LABEL = "1 - Amend response";
    private static final String ABOUT_TO_SUBMIT_URL = "/tseClaimant/aboutToSubmit";
    private static final String APPLICATION_CODE = "1";
    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("testCaseReference");

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();
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

}
