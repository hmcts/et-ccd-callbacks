package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class NocNotificationHelperTest {
    private static final String RESPONDENT_NAME = "Respondent";
    private static final String NEW_REP_EMAIL = "rep1@test.com";
    private static final String OLD_REP_EMAIL = "rep2@test.com";
    private static final String NEW_ORG_ID = "1";
    private static final String OLD_ORG_ID = "2";

    private CaseData caseData;
    private CaseDetails caseDetails;

    private RespondentSumType respondentSumType;

    @BeforeEach
    void setUp() {

        respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_NAME);
        respondentSumType.setRespondentEmail("res@rep.com");

        Organisation organisationToAdd = Organisation.builder()
                .organisationID(NEW_ORG_ID)
                .organisationName("New Organisation").build();
        Organisation organisationToRemove = Organisation.builder()
                .organisationID(OLD_ORG_ID)
                .organisationName("Old Organisation").build();

        caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("12345/6789")
            .withClaimantType("claimant@unrepresented.com")
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
            .withTwoRespondentRepresentative(NEW_ORG_ID, OLD_ORG_ID, NEW_REP_EMAIL, OLD_REP_EMAIL)
            .withRespondent("Respondent", YES, "2022-03-01", "res@rep.com", false)
            .withChangeOrganisationRequestField(
                organisationToAdd,
                organisationToRemove,
                null,
                null,
                null)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");
        caseData = caseDetails.getCaseData();
        caseData.setClaimant("Claimant LastName");
        caseData.setTribunalCorrespondenceEmail("respondent@unrepresented.com");
        caseData.setTribunalAndOfficeLocation("Leeds");
    }

    @Test
    void testbuildClaimantPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildPersonalisationWithPartyName(caseDetails, "test_party");
        assertThat(claimantPersonalisation.size(), is(5));
        assertThat(claimantPersonalisation.get("party_name"), is("test_party"));
        assertThat(claimantPersonalisation.get("ccdId"), is("1234"));
        assertThat(claimantPersonalisation.get("claimant"), is("Claimant LastName"));
        assertThat(claimantPersonalisation.get("list_of_respondents"),
                is("Respondent Respondent Unrepresented Respondent Represented Respondent")
        );
        assertThat(claimantPersonalisation.get("case_number"), is("12345/6789"));
    }

    @Test
    void testBuildPreviousRespondentSolicitorPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseData);
        assertThat(claimantPersonalisation.size(), is(3));
        for (String value : claimantPersonalisation.values()) {
            assertThat(value, notNullValue());
        }
    }

    @Test
    void testBuildRespondentPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildRespondentPersonalisation(caseData, respondentSumType);
        assertThat(claimantPersonalisation.size(), is(5));
        for (String value : claimantPersonalisation.values()) {
            assertThat(value, notNullValue());
        }
    }

    @Test
    void testBuildTribunalPersonalisation() {
        Map<String, String> claimantPersonalisation = NocNotificationHelper.buildTribunalPersonalisation(caseData);
        assertThat(claimantPersonalisation.size(), is(5));
        for (String value : claimantPersonalisation.values()) {
            assertThat(value, notNullValue());
        }
    }

    @Test
    void testGetOldSolicitorEmailForRepUpdate_haveExistingOrg() {
        String oldSolicitorEmail = NocNotificationHelper
                .getOldSolicitorEmailForRepUpdate(caseData, caseData.getChangeOrganisationRequestField());
        assertThat(oldSolicitorEmail, is(OLD_REP_EMAIL));
    }

    @Test
    void testGetOldSolicitorEmailForRepUpdate_noExistingOrg() {
        caseData.getChangeOrganisationRequestField().setOrganisationToRemove(null);
        String oldSolicitorEmail = NocNotificationHelper
                .getOldSolicitorEmailForRepUpdate(caseData, caseData.getChangeOrganisationRequestField());
        assertThat(oldSolicitorEmail, is(nullValue()));
    }

    @Test
    void testGetNewSolicitorEmailForRepUpdate() {
        String newSolicitorEmail = NocNotificationHelper
                .getNewSolicitorEmailForRepUpdate(caseData, caseData.getChangeOrganisationRequestField());
        assertThat(newSolicitorEmail, is(NEW_REP_EMAIL));
    }

    @Test
    void testGetRespondentNameForNewSolicitor() {
        String respondentName = NocNotificationHelper
                .getRespondentNameForNewSolicitor(caseData.getChangeOrganisationRequestField(), caseData);
        assertThat(respondentName, is(RESPONDENT_NAME));
    }
}
