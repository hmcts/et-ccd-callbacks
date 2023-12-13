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
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;

@Slf4j
public class TseViewApplicationsControllerFunctionalTest extends BaseFunctionalTest  {
    private static final String AUTHORIZATION = "Authorization";
    private static final String APPLICATION_LABEL = "1 - Amend response";
    private static final String ABOUT_TO_START_URL = "/viewRespondentTSEApplications/aboutToStart";
    private static final String POPULATE_APP_URL = "/viewRespondentTSEApplications/midPopulateChooseApplication";
    private static final String POPULATE_APP_DATA_URL =
            "/viewRespondentTSEApplications/midPopulateSelectedApplicationData";
    private static final String APPLICATION_CODE = "1";
    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("testCaseReference");
        caseData.setResTseSelectApplication(TSE_APP_AMEND_RESPONSE);
        caseData.setGenericTseApplicationCollection(createApplicationCollection());
        caseData.setTseAdminSelectApplication(
                DynamicFixedListType.of(
                        DynamicValueType.create(APPLICATION_CODE, APPLICATION_LABEL)));

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();
    }

    @Test
    void aboutToSubmitSuccessResponse() {
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
    void populateChooseApplicationSuccessResponse() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(POPULATE_APP_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void populateSelectedApplicationDataSuccessResponse() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(POPULATE_APP_DATA_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    private ListTypeItem<GenericTseApplicationType> createApplicationCollection() {
        GenericTseApplicationType respondentTseType = new GenericTseApplicationType();
        respondentTseType.setNumber(APPLICATION_CODE);

        return ListTypeItem.from(respondentTseType);
    }
}
