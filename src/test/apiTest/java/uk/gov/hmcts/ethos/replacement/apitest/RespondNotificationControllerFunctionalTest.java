package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponse;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotification;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
public class RespondNotificationControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";

    private static final String ABOUT_TO_START_URL = "/respondNotification/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/respondNotification/aboutToSubmit";
    private static final String MID_GET_NOTIFICATION_URL = "/respondNotification/midGetNotification";
    private static final String MID_GET_INPUT_URL = "/respondNotification/midValidateInput";
    private static final String SUBMITTED_URL = "/respondNotification/submitted";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("testCaseReference")
            .withManagingOffice("Manchester")
            .build();

        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotification.builder()
                    .respondCollection(ListTypeItem.from(TypeItem.<PseResponse>builder()
                        .id(UUID.randomUUID().toString())
                        .value(PseResponse.builder()
                            .from(RESPONDENT_TITLE)
                            .copyToOtherParty(YES)
                            .build())
                        .build()))
                    .build())
                .build()
        ));

        caseData.setSelectNotificationDropdown(DynamicFixedListType.of(DynamicValueType.create("uuid", "")));

        SendNotification notificationType = new SendNotification();
        notificationType.setSendNotificationSubject(List.of("Judgment"));

        SendNotificationTypeItem notificationTypeItem = new SendNotificationTypeItem();
        String uuid = UUID.randomUUID().toString();
        notificationTypeItem.setId(uuid);
        notificationTypeItem.setValue(notificationType);

        List<SendNotificationTypeItem> notificationTypeItems = new ArrayList<>();
        notificationTypeItems.add(notificationTypeItem);
        caseData.setSendNotificationCollection(notificationTypeItems);

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withCaseId("123")
            .build();
    }

    @Test
    void shouldReceiveSuccessResponseWhenAboutToStartInvoked() {
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
            .post(MID_GET_NOTIFICATION_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }

    @Test
    void shouldReceiveSuccessResponseWhenMidValidateInputInvoked() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post(MID_GET_INPUT_URL)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
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
