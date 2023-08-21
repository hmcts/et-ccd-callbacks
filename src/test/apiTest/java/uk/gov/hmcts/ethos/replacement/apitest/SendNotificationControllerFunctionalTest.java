package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

public class SendNotificationControllerFunctionalTest extends BaseFunctionalTest {

    private static final String AUTHORIZATION = "Authorization";

    private static final String ABOUT_TO_SUBMIT_URL = "/sendNotification/aboutToSubmit";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("1234")
                .withRespondent("Name", YES, "2020-01-02", "respondent@email.com", false)
                .withRespondentRepresentative("Name", "Sally", "respondentRep@email.com")
                .withClaimant("Claimant")
                .withClaimantType("claimant@email.com")
                .build();

        caseData.setSendNotificationTitle("Test title");
        caseData.setSendNotificationSubject(List.of("Judgment", "Hearing"));

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();
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
}
