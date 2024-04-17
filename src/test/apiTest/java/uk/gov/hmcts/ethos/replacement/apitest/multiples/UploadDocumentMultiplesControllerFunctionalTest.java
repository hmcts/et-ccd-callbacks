package uk.gov.hmcts.ethos.replacement.apitest.multiples;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.apitest.BaseFunctionalTest;

import java.io.IOException;

@Slf4j
public class UploadDocumentMultiplesControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_START_URL = "multiples/updateReferral/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "multiples/updateReferral/aboutToSubmit";
    private MultipleRequest request;
    
    @BeforeAll
    public void setUpCaseData() throws IOException {
        
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
}
