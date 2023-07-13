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
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@WithTag("FunctionalTest")
public class PseRespondToTribunalControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";

    private static final String ABOUT_TO_START_URL = "/pseRespondToTribunal/aboutToStart";
    private static final String MID_TABLE_DETAILS = "/pseRespondToTribunal/midDetailsTable";
    private static final String MID_VALIDATE_INPUT = "/pseRespondToTribunal/midValidateInput";
    private static final String ABOUT_TO_SUBMIT_URL = "/pseRespondToTribunal/aboutToSubmit";
    private static final String SUBMITTED_URL = "/pseRespondToTribunal/submitted";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("testCaseReference")
            .build();

        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                    .respondCollection(List.of(PseResponseTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(PseResponseType.builder()
                            .from(RESPONDENT_TITLE)
                            .copyToOtherParty(YES)
                            .build())
                        .build()))
                    .build())
                .build()
        ));

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
            .post(MID_TABLE_DETAILS)
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
            .post(MID_VALIDATE_INPUT)
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
