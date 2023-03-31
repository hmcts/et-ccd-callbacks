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
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

@Slf4j
class PseRespondentViewControllerFunctionalTest extends BaseFunctionalTest {

    private static final String AUTHORIZATION = "Authorization";

    private static final String ABOUT_TO_START_URL = "/pseRespondentView/aboutToStart";
    private static final String MID_TABLE_DETAILS = "/pseRespondentView/midDetailsTable";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("testCaseReference")
            .build();

        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder().build()
        ));

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withCaseId("123")
            .build();
    }

    @Test
    void shouldReceiveSuccessResponseWhenAboutToStartViewInvoked() {
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
    void shouldReceiveSuccessResponseWhenMidDetailsTableInvoked() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post(MID_TABLE_DETAILS)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }

}
