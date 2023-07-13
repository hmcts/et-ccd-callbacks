package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@Slf4j
@WithTag("ApiTest")
public class PseRespondentViewControllerFunctionalTest extends BaseFunctionalTest {

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

        caseData.setSendNotificationCollection(List.of(
                SendNotificationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(SendNotificationType.builder()
                                .number("1")
                                .date("5 Aug 2022")
                                .sendNotificationTitle("View notice of hearing")
                                .sendNotificationLetter(NO)
                                .sendNotificationSubject(List.of("Case management orders / requests"))
                                .sendNotificationCaseManagement("Request")
                                .sendNotificationResponseTribunal("No")
                                .sendNotificationRequestMadeBy("Judge")
                                .sendNotificationFullName("Mr Lee Gal Officer")
                                .sendNotificationNotify(BOTH_PARTIES)
                                .build())
                        .build()
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
