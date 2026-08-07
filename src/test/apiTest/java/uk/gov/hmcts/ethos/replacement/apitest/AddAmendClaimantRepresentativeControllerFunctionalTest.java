package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Slf4j
public class AddAmendClaimantRepresentativeControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_SUBMIT_URL = "/addAmendClaimantRepresentative/aboutToSubmit";
    private static final String SUBMITTED_URL = "/addAmendClaimantRepresentative/amendClaimantRepSubmitted";
    private CCDRequest ccdRequest;
    private CallbackRequest callbackRequest;

    @BeforeAll
    public void setUpCaseData() throws IOException {
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(CaseDataBuilder.builder().build())
                .withCaseId("1234567890123456")
                .build();
        final JSONObject createdCase = createSinglesCaseDataStore();

        CaseData caseDataBefore = CaseDataBuilder.builder()
                .withEthosCaseReference("1234")
                .withClaimant("claimant")
                .build();
        CaseData caseDataAfter = CaseDataBuilder.builder()
                .withEthosCaseReference("1234")
                .withClaimant("claimant")
                .build();
        caseDataBefore.setRespondentCollection(new ArrayList<>());
        caseDataAfter.setRespondentCollection(new ArrayList<>());
        caseDataAfter.setRepresentativeClaimantType(createClaimantRepresentative());

        CaseDetails caseDetailsBefore = createCaseDetails(String.valueOf(createdCase.getLong("id")), caseDataBefore);
        CaseDetails caseDetails = createCaseDetails(String.valueOf(createdCase.getLong("id")), caseDataAfter);

        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .caseDetailsBefore(caseDetailsBefore)
                .build();
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
    void submittedSuccessResponse() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(callbackRequest)
                .post(SUBMITTED_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data", notNullValue())
                .body("errors", nullValue())
                .body("warnings", nullValue())
                .log()
                .all(true);
    }

    private CaseDetails createCaseDetails(String caseId, CaseData caseData) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(caseId);
        caseDetails.setCaseTypeId(SCOTLAND_CASE_TYPE_ID);
        caseDetails.setJurisdiction(EMPLOYMENT);
        caseDetails.setCaseData(caseData);

        return caseDetails;
    }

    private RepresentedTypeC createClaimantRepresentative() {
        RepresentedTypeC claimantRepresentative = new RepresentedTypeC();
        claimantRepresentative.setMyHmctsOrganisation(Organisation.builder()
                .organisationID("ORG1")
                .organisationName("ET Org 1")
                .build());

        return claimantRepresentative;
    }
}
