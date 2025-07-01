package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

@Slf4j
class HearingUnavailabilityControllerTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";
    private static final String ABOUT_TO_SUBMIT_URL = "/hearingUnavailability/aboutToSubmit";
    private static final String SUBMITTED_URL = "/hearingUnavailability/submitted";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(CaseDataBuilder.builder().build())
            .build();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setPartySelection(List.of("claimant", "respondent"));
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
