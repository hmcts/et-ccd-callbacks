package uk.gov.hmcts.ethos.replacement.apitest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.hc.core5.http.ParseException;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;

public class NoticeOfChangeControllerFunctionalTest extends BaseFunctionalTest {
    private static final String AUTHORIZATION = "Authorization";

    private static final String SUBMITTED_URL = "/noc-decision/submitted";

    private static final String SOLICITORA = "[SOLICITORA]";
    private static final String ORGANISATION_ID = "ORG1";
    private static final String ORGANISATION_ID_TWO = "ORG2";
    private static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";

    private CCDRequest ccdRequest;

    @BeforeAll
    public void setUpCaseData() throws IOException, ParseException {
        DynamicFixedListType caseRole = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(SOLICITORA);
        dynamicValueType.setLabel(SOLICITORA);
        caseRole.setValue(dynamicValueType);
        ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
        hearingPreference.setContactLanguage(ENGLISH_LANGUAGE);

        CaseData caseData2 = CaseDataBuilder.builder()
                .withClaimant("claimant")
                .withClaimantHearingPreference(hearingPreference.getContactLanguage())
                .build();

        caseData2.setRespondentCollection(new ArrayList<>(Collections.singletonList(createRespondentType())));

        Organisation org1 =
                Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ET_ORG_1).build();
        Organisation org2 =
                Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();

        caseData2.setChangeOrganisationRequestField(ChangeOrganisationRequest.builder()
                .organisationToAdd(org1)
                .organisationToRemove(org2)
                .caseRoleId(caseRole)
                .requestTimestamp(null)
                .approvalStatus(null)
                .build());

        var createdCase = createSinglesCaseDataStore();

        Long caseId = createdCase.getLong("id");

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData2)
                .withCaseId(String.valueOf(caseId))
                .build();

    }

    @Test
    void nocSubmittedSuccessResponse() {
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

    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Boris Johnson");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }

}
