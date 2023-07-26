package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;

public class Et3ResponseControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";

    private static final String ABOUT_TO_START_URL = "/et3Response/aboutToStart";
    private static final String MID_EVENT_URL = "/et3Response/midEmploymentDates";
    private static final String ABOUT_TO_SUBMIT_URL = "/et3Response/aboutToSubmit";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("testCaseReference")
                .withClaimant("claimant")
                .build();

        caseData.setResTseSelectApplication(TSE_APP_AMEND_RESPONSE);
        caseData.setResTseCopyToOtherPartyYesOrNo(NO);
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));
        caseData.setSubmitEt3Respondent(DynamicFixedListType.of(DynamicValueType.create("test", "Boris Johnson")));
        caseData.setTribunalCorrespondenceEmail("respondent@unrepresented.com");

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();
    }

    @Test
    void initEt3ResponseSuccessResponse() {
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
    void midEmploymentDatesSuccessResponse() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(MID_EVENT_URL)
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

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Boris Johnson");

        Address respondentET3Address = new Address();
        respondentET3Address.setPostCode("DH1 1AJ");
        respondentSumType.setResponseRespondentAddress(respondentET3Address);
        respondentSumType.setEt3ResponseHearingRespondent(List.of("Phone hearings"));
        respondentSumType.setEt3ResponseHearingRepresentative(List.of("rep"));
        respondentSumType.setResponseReceived(NO);

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }

}
