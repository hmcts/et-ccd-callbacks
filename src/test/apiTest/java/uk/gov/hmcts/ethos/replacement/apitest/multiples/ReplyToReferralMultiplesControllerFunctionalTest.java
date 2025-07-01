package uk.gov.hmcts.ethos.replacement.apitest.multiples;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.apitest.BaseFunctionalTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ReferralsUtil;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@Slf4j
public class ReplyToReferralMultiplesControllerFunctionalTest extends BaseFunctionalTest {
    private static final String START_REPLY_REFERRAL_URL = "/multiples/replyReferral/aboutToStart";
    private static final String MID_HEARING_DETAILS_URL = "/multiples/replyReferral/initHearingAndReferralDetails";
    private static final String ABOUT_TO_SUBMIT_URL = "/multiples/replyReferral/aboutToSubmit";
    private static final String SUBMITTED_REFERRAL_URL = "/multiples/replyReferral/completeReplyToReferral";
    private static final String VALIDATE_EMAIL_URL = "/multiples/replyReferral/validateReplyToEmail";
    private MultipleRequest request;

    @BeforeAll
    public void setUpCaseData() throws IOException, InterruptedException, ParseException {
        MultipleData multipleData = MultipleData.builder().build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(SCOTLAND_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);

        multipleData.setReferralCollection(List.of(ReferralsUtil.createReferralTypeItem()));

        DynamicFixedListType selectReferralList =
                ReferralHelper.populateSelectReferralDropdown(multipleData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        selectReferralList.getValue().setLabel("idklol");
        multipleData.setSelectReferral(selectReferralList);

        multipleData.setReplyDetails("response details");

        JSONObject singleCase = createSinglesCaseDataStore();
        multipleData.setLeadCase(String.valueOf(singleCase.getLong("id")));
    }

    @Test
    void aboutToStartUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(START_REPLY_REFERRAL_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void midHearingUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(MID_HEARING_DETAILS_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void aboutToSubmitUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(ABOUT_TO_SUBMIT_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void submittedUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(SUBMITTED_REFERRAL_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void midValidateUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(VALIDATE_EMAIL_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }
}