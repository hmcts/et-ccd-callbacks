package uk.gov.hmcts.ethos.replacement.functional;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.baseURI;
import static org.hamcrest.Matchers.equalTo;

class HealthCheckTest {

    @Test
    @Tag("smokeTest")
    void healthcheckReturns200() {
        baseURI = System.getProperty("etcos.url",
                         System.getenv("ET_COS_URL") != null ? System.getenv("ET_COS_URL") : "http://localhost:8080");
        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.given()
                .get("/health")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true)
                .assertThat().body("status", equalTo("UP"));
    }
}
