package uk.gov.hmcts.ethos.replacement.apitest;

import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

/**
 * Functional test for retrieving an ACAS certificate which verifies the ACAS API is working correctly.
 */
@Slf4j
public class AcasCertificateFunctionalTest extends BaseFunctionalTest {
    private static final String RETRIEVE_ACAS_CERTIFICATE_URL = "/acasCertificate/retrieveCertificate";

    @Test
    public void retrieveAcasCertificateSuccessResponse() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(CaseDataBuilder.builder().build())
            .build();
        ccdRequest.getCaseDetails().getCaseData().setAcasCertificate("R874121/23/84");
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post(RETRIEVE_ACAS_CERTIFICATE_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }

}
