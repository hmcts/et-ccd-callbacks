package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.LinkReason;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@Slf4j
class CaseLinksControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_SUBMIT_CREATE_URL = "/caseLinks/create/aboutToSubmit";
    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() {
        LinkReason linkReason = new LinkReason();
        linkReason.setReason("CLRC017");
        ListTypeItem<LinkReason> linkReasons = ListTypeItem.from(linkReason, "1");
        CaseLink caseLink = CaseLink.builder().caseReference("1").caseType(ENGLANDWALES_CASE_TYPE_ID)
                .reasonForLink(linkReasons).build();
        ListTypeItem<CaseLink> caseLinks = ListTypeItem.from(caseLink);

        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("RESPONDENT_NAME");
        respondentSumType.setRespondentEmail("res@rep.com");

        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference("12345/6789")
                .withClaimantType("claimant@unrepresented.com")
                .withClaimantRepresentedQuestion("No")
                .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
                .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
                .withRespondent(respondentSumType)
                .withRespondentWithAddress("Respondent Unrepresented",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null, "respondent@unrepresented.com")
                .withRespondentWithAddress("Respondent Represented",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null)
                .withRespondentRepresentative("Respondent Represented", "Legal rep", "mail@mail.com")
                .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                        "length num", "type", "Yes")
                .withHearingSession(
                        0,
                        "1",
                        "2029-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_LISTED,
                        true)
                .withCaseLinks(caseLinks)
                .withClaimant("claimant")
                .withCaseLinks(caseLinks)
                .withRespondent(respondentSumType)
                .build();

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .build();
    }

    @Test
    void aboutToSubmitCaseLinks() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(ccdRequest)
                .post(ABOUT_TO_SUBMIT_CREATE_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }
}
