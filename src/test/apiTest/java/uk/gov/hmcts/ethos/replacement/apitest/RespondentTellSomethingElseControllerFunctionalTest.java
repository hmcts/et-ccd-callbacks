package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.apitest.utils.CCDRequestBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
class RespondentTellSomethingElseControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";
    private static final String NO = "I do not want to copy";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("testCaseReference");
        caseData.setResTseSelectApplication("Amend response");
        caseData.setResTseCopyToOtherPartyYesOrNo(NO);
        caseData.setClaimant("claimant");
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress("person@email.com");
        caseData.setClaimantType(claimantType);
        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));
        caseData.setGenericTseApplicationCollection(createApplicationCollection());

        ccdRequest = CCDRequestBuilder.builder()
            .withCaseData(caseData)
            .withCaseId("123")
            .build();
    }

    @Test
    void shouldReceiveSuccessResponseWhenValidateGiveDetailsInvoked() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post("/respondentTSE/validateGiveDetails")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }

    @Test
    void shouldReceiveSuccessResponseWhenAboutToSubmitRespondentTseInvoked() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post("/respondentTSE/aboutToSubmit")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }

    @Test
    void shouldReceiveSuccessResponseWhenDisplayRespondentApplicationsTableInvoked() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post("/respondentTSE/displayTable")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }

    @Test
    void shouldReceiveSuccessResponseWhenCompleteApplicationInvoked() {
        RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(ccdRequest)
            .post("/respondentTSE/completeApplication")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Boris Johnson");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }

    private List<GenericTseApplicationTypeItem> createApplicationCollection() {
        GenericTseApplicationType respondentTseType = new GenericTseApplicationType();
        respondentTseType.setCopyToOtherPartyYesOrNo(NO);

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(respondentTseType);

        return new ArrayList<>(Collections.singletonList(tseApplicationTypeItem));
    }
}
