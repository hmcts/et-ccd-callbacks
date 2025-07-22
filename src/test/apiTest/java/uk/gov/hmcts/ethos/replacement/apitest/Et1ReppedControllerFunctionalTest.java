package uk.gov.hmcts.ethos.replacement.apitest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import java.io.IOException;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
public class Et1ReppedControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";
    private static final String VALIDATE_POSTCODE = "/et1Repped/createCase/validatePostcode";
    private static final String VALIDATE_CLAIMANT_SEX = "/et1Repped/sectionOne/validateClaimantSex";
    private static final String VALIDATE_CLAIMANT_SUPPORT = "/et1Repped/sectionOne/validateClaimantSupport";
    private static final String VALIDATE_REPRESENTATIVE_INFORMATION =
            "/et1Repped/sectionOne/validateRepresentativeInformation";
    private static final String ABOUT_TO_SUBMIT_SECTION = "/et1Repped/aboutToSubmitSection";
    private static final String VALIDATE_CLAIMANT_WORKED = "/et1Repped/sectionTwo/validateClaimantWorked";
    private static final String VALIDATE_CLAIMANT_WORKING = "/et1Repped/sectionTwo/validateClaimantWorking";
    private static final String VALIDATE_CLAIMANT_PAY = "/et1Repped/sectionTwo/validateClaimantPay";
    private static final String VALIDATE_CLAIMANT_PENSION_BENEFITS =
            "/et1Repped/sectionTwo/validateClaimantPensionBenefits";
    private static final String GENERATE_RESPONDENT_PREAMBLE = "/et1Repped/sectionTwo/generateRespondentPreamble";
    private static final String GENERATE_WORK_ADDRESS_LABEL = "/et1Repped/sectionTwo/generateWorkAddressLabel";
    private static final String SECTION_COMPLETED = "/et1Repped/sectionCompleted";
    private static final String VALIDATE_WHISTLEBLOWING = "/et1Repped/sectionThree/validateWhistleblowing";
    private static final String VALIDATE_LINKED_CASES = "/et1Repped/sectionThree/validateLinkedCases";
    private static final String SUBMIT_CLAIM = "/et1Repped/submitClaim";
    private static final String SUBMIT_CLAIM_ABOUT_TO_START = "/et1Repped/submitClaim/aboutToStart";
    private static final String CREATE_DRAFT_ET1 = "/et1Repped/createDraftEt1";
    private static final String CREATE_DRAFT_ET1_SUBMITTED = "/et1Repped/createDraftEt1Submitted";
    private static final String VALIDATE_GROUNDS = "/et1Repped/validateGrounds";
    private static final String VALIDATE_HEARING_PREFERENCES = "/et1Repped/sectionOne/validateHearingPreferences";

    private CCDRequest ccdRequest;

    @BeforeAll
    void setUpEt1ReppedData() throws IOException {
        // Create a real case in CCD
        JSONObject caseJson = createSinglesCaseDataStore();

        // Map the created case JSON to CaseDetails and CaseData
        ObjectMapper mapper = new ObjectMapper();
        CaseDetails caseDetails = mapper.readValue(caseJson.toString(),
                CaseDetails.class);
        CaseData caseData = caseDetails.getCaseData();

        // Build the CCDRequest using the real case data
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseTypeId(caseDetails.getCaseTypeId())
                .withCaseId(caseDetails.getCaseId())
                .build();
    }

    @Test
    void shouldValidatePostcode() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_POSTCODE)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateClaimantSex() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_CLAIMANT_SEX)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateHearingPreference() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_HEARING_PREFERENCES)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateClaimantSupport() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_CLAIMANT_SUPPORT)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateRepInfo() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_REPRESENTATIVE_INFORMATION)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateGrounds() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_GROUNDS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateLinkedCases() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_LINKED_CASES)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateDidClaimantWork() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_CLAIMANT_WORKED)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateClaimantWorking() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_CLAIMANT_WORKING)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateClaimantPay() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_CLAIMANT_PAY)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateClaimantPension() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_CLAIMANT_PENSION_BENEFITS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldValidateWhistleblowing() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(VALIDATE_WHISTLEBLOWING)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldSetStatus() {
        ccdRequest.setEventId("et1SectionOne");
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(ABOUT_TO_SUBMIT_SECTION)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldCreateDraftEt1() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(CREATE_DRAFT_ET1)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldShowDraftEt1Submitted() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(CREATE_DRAFT_ET1_SUBMITTED)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldGenerateRespondentPreamble() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(GENERATE_RESPONDENT_PREAMBLE)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldGenerateWorkAddressLabel() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(GENERATE_WORK_ADDRESS_LABEL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldShowSectionComplete() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(SECTION_COMPLETED)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldStartSubmitClaim() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(SUBMIT_CLAIM_ABOUT_TO_START)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().all(true);
    }

    @Test
    void shouldSubmitClaim() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(SUBMIT_CLAIM)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true)
                .body("data.ethosCaseReference", notNullValue());
    }

}
