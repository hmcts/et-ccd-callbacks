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
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole.CLAIMANTSOLICITOR;

@Slf4j
public class ClaimantRepresentativeControllerFunctionalTest extends BaseFunctionalTest {

    private static final String REMOVE_OWN_REPRESENTATIVE_URL = "/claimantRepresentative/removeOwnRepresentative";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String REPRESENTATIVE_NAME = "Claimant Representative";

    private CCDRequest claimantNotRepresentedRequest;
    private CCDRequest claimantRepresentedRequest;

    @BeforeAll
    public void setUpCaseData() {
        claimantNotRepresentedRequest = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithClaimantRepresentativeFlags(NO))
                .withCaseId("1694206942069420")
                .build();
        claimantRepresentedRequest = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithClaimantRepresentativeFlags(YES))
                .withCaseId("1694206942069421")
                .build();
    }

    @Test
    void removeOwnRepresentativeClearsClaimantRepresentativeFlags() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(claimantNotRepresentedRequest)
                .post(REMOVE_OWN_REPRESENTATIVE_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.representativeClaimantType", nullValue())
                .body("data.claimantRepresentativeOrganisationPolicy.OrgPolicyCaseAssignedRole",
                        equalTo(CLAIMANTSOLICITOR.getCaseRoleLabel()))
                .body("data.claimantFlags.partyName", equalTo(CLAIMANT_NAME))
                .body("data.claimantRepresentativeFlags", nullValue())
                .body("data.claimantRepresentativeExternalFlags", nullValue())
                .log()
                .all(true);
    }

    @Test
    void removeOwnRepresentativeKeepsClaimantRepresentativeFlagsWhenStillRepresented() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(claimantRepresentedRequest)
                .post(REMOVE_OWN_REPRESENTATIVE_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.representativeClaimantType.name_of_representative", equalTo(REPRESENTATIVE_NAME))
                .body("data.claimantRepresentativeFlags.partyName", equalTo(REPRESENTATIVE_NAME))
                .body("data.claimantRepresentativeExternalFlags.partyName", equalTo(REPRESENTATIVE_NAME))
                .log()
                .all(true);
    }

    private static CaseData caseDataWithClaimantRepresentativeFlags(String claimantRepresentedQuestion) {
        CaseData caseData = CaseDataBuilder.builder()
                .withClaimantRepresentedQuestion(claimantRepresentedQuestion)
                .build();
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder()
                .nameOfRepresentative(REPRESENTATIVE_NAME)
                .build());
        caseData.setAllPartyFlags(AllPartyFlags.builder()
                .claimantFlags(CaseFlagsType.builder().partyName(CLAIMANT_NAME).build())
                .claimantRepresentativeFlags(CaseFlagsType.builder().partyName(REPRESENTATIVE_NAME).build())
                .claimantRepresentativeExternalFlags(CaseFlagsType.builder().partyName(REPRESENTATIVE_NAME).build())
                .build());
        return caseData;
    }
}
