package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.CLAIMANT_REPRESENTATIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.EXTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
public class AddAmendClaimantRepresentativeControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_SUBMIT_URL = "/addAmendClaimantRepresentative/aboutToSubmit";
    private static final String SUBMITTED_URL = "/addAmendClaimantRepresentative/amendClaimantRepSubmitted";
    private static final String OLD_REPRESENTATIVE_NAME = "Old Claimant Representative";
    private static final String NEW_REPRESENTATIVE_NAME = "New Claimant Representative";

    private CallbackRequest callbackRequest;

    @BeforeAll
    public void setUpCaseData() throws IOException {
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
        caseDataBefore.setRepresentativeClaimantType(createClaimantRepresentative(OLD_REPRESENTATIVE_NAME));
        caseDataAfter.setClaimantRepresentedQuestion(YES);
        caseDataAfter.setRepresentativeClaimantType(createClaimantRepresentative(NEW_REPRESENTATIVE_NAME));
        caseDataAfter.setAllPartyFlags(createClaimantRepresentativeFlags(OLD_REPRESENTATIVE_NAME));

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
                .body(callbackRequest)
                .post(ABOUT_TO_SUBMIT_URL)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(HttpStatus.SC_OK)
                .body("data.claimantRepresentativeFlags.partyName", equalTo(NEW_REPRESENTATIVE_NAME))
                .body("data.claimantRepresentativeFlags.details", nullValue())
                .body("data.claimantRepresentativeExternalFlags.partyName",
                        equalTo(NEW_REPRESENTATIVE_NAME))
                .body("data.claimantRepresentativeExternalFlags.details", nullValue());
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

    private RepresentedTypeC createClaimantRepresentative(String representativeName) {
        RepresentedTypeC claimantRepresentative = new RepresentedTypeC();
        claimantRepresentative.setNameOfRepresentative(representativeName);
        claimantRepresentative.setMyHmctsOrganisation(Organisation.builder()
                .organisationID("ORG1")
                .organisationName("ET Org 1")
                .build());

        return claimantRepresentative;
    }

    private AllPartyFlags createClaimantRepresentativeFlags(String representativeName) {
        return AllPartyFlags.builder()
                .claimantRepresentativeFlags(createCaseFlags(representativeName, INTERNAL, "Internal flag"))
                .claimantRepresentativeExternalFlags(createCaseFlags(representativeName, EXTERNAL, "External flag"))
                .build();
    }

    private CaseFlagsType createCaseFlags(String representativeName, String visibility, String flagName) {
        return CaseFlagsType.builder()
                .partyName(representativeName)
                .roleOnCase(CLAIMANT_REPRESENTATIVE)
                .groupId(CLAIMANT_REPRESENTATIVE)
                .visibility(visibility)
                .details(ListTypeItem.from(FlagDetailType.builder()
                        .name(flagName)
                        .status(ACTIVE)
                        .build()))
                .build();
    }
}
