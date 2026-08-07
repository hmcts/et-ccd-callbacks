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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
public class CaseActionsForCaseWorkerControllerFunctionalTest extends BaseFunctionalTest {
    private static final String RESTRICTED_CASES_URL = "/restrictedCases";
    private static final String AMEND_CLAIMANT_DETAILS_URL = "/amendClaimantDetails";
    private static final String AMEND_RESPONDENT_DETAILS_URL = "/amendRespondentDetails";
    private static final String AMEND_RESPONDENT_REPRESENTATIVE_URL =
            "/respondentRepresentative/amendRespondentRepresentativeAboutToSubmit";
    private static final String RESPONDENT_ID = "respondent-id";
    private static final String REPRESENTATIVE_ID = "representative-id";
    private static final String RESPONDENT_NAME = "Respondent Ltd";
    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("6000001/2023")
                .withClaimant("claimant")
                .withRespondent(RESPONDENT_NAME, NO, null, false)
                .build();

        caseData.getRespondentCollection().getFirst().setId(RESPONDENT_ID);
        caseData.setIcListingPreliminaryHearing(YES);
        caseData.setRepCollection(List.of(createRespondentRepresentative()));

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("1694206942069420")
                .withCaseTypeId(SCOTLAND_CASE_TYPE_ID)
                .build();
    }

    @Test
    void restrictedCases() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(RESTRICTED_CASES_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void amendClaimantDetails() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(AMEND_CLAIMANT_DETAILS_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void amendRespondentDetails() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(AMEND_RESPONDENT_DETAILS_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void amendRespondentRepresentative() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(AMEND_RESPONDENT_REPRESENTATIVE_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    private RepresentedTypeRItem createRespondentRepresentative() {
        RepresentedTypeR representative = RepresentedTypeR.builder()
                .respRepName(RESPONDENT_NAME)
                .nameOfRepresentative("Respondent Rep")
                .representativeEmailAddress("rep@example.com")
                .myHmctsYesNo(NO)
                .dynamicRespRepName(DynamicFixedListType.of(
                        DynamicValueType.create("R: " + RESPONDENT_NAME, RESPONDENT_NAME)))
                .respondentId(RESPONDENT_ID)
                .build();

        RepresentedTypeRItem item = new RepresentedTypeRItem();
        item.setId(REPRESENTATIVE_ID);
        item.setValue(representative);

        return item;
    }
}
