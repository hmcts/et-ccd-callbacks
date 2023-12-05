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
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;

import java.util.UUID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;

@Slf4j
public class TseAdmReplyControllerFunctionalTest extends BaseFunctionalTest  {

    private static final String AUTHORIZATION = "Authorization";
    private static final String MID_DETAILS_TABLE = "/tseAdmReply/midDetailsTable";
    private static final String MID_VALIDATE_INPUT = "/tseAdmReply/midValidateInput";
    private static final String SUBMITTED_URL = "/tseAdmReply/submitted";

    private static final String APPLICATION_CODE = "1";
    private static final String APPLICATION_LABEL = "1 - Amend response";

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
    void midDetailsTableSuccessResponse() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(MID_DETAILS_TABLE)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void midValidateInputSuccessResponse() {
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
    void submittedSuccessResponse() {
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

    private ListTypeItem<GenericTseApplicationType> createApplicationCollection() {
        GenericTseApplicationType respondentTseType = new GenericTseApplicationType();
        respondentTseType.setNumber(APPLICATION_CODE);

        TypeItem<GenericTseApplicationType> tseApplicationTypeItem = new TypeItem<>();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(respondentTseType);

        return ListTypeItem.from(respondentTseType);
    }
}
