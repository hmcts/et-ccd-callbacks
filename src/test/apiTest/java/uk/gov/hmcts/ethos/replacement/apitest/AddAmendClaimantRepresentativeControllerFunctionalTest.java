package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

@Slf4j
public class AddAmendClaimantRepresentativeControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_SUBMIT_URL = "/addAmendClaimantRepresentative/aboutToSubmit";
    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(CaseDataBuilder.builder().build())
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
