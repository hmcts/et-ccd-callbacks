package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.apitest.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

class RespondNotificationControllerFunctionalTest extends BaseFunctionalTest {

    private static final String AUTHORIZATION = "Authorization";
    private static final String ABOUT_TO_START_URL = "/respondNotification/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/respondNotification/aboutToSubmit";
    private static final String MID_GET_NOTIFICATION_URL = "/respondNotification/midGetNotification";
    private CCDRequest ccdRequest;

    private CaseData caseData;
    @Mock
    private EmailService emailService;
    @Mock
    private HearingSelectionService hearingSelectionService;
    private SendNotificationService sendNotificationService;
    private RespondNotificationService respondNotificationService;

    @BeforeEach
    public void setUp() {
        sendNotificationService = new SendNotificationService(hearingSelectionService, emailService);
        respondNotificationService = new RespondNotificationService(emailService, sendNotificationService);
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();
    }

    @Test
    void aboutToStartSuccessResponse() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post(ABOUT_TO_START_URL)
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
    void midGetNotificationSuccessResponse() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post(MID_GET_NOTIFICATION_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }
}
