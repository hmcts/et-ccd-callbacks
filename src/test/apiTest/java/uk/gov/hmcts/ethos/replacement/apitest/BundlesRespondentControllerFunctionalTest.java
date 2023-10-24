package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
class BundlesRespondentControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";
    private static final String ABOUT_TO_START_URL = "/bundlesRespondent/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/bundlesRespondent/aboutToSubmit";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(CaseDataBuilder.builder().build())
            .build();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setBundlesRespondentAgreedDocWith(YES);
        caseData.setBundlesRespondentSelectHearing(DynamicFixedListType.from("hearing 1", "1", true));
        caseData.setBundlesRespondentWhatDocuments(YES);
        caseData.setBundlesRespondentWhoseDocuments(YES);
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

}
