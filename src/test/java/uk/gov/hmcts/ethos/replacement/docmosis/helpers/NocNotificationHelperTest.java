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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class NocNotificationHelperTest {
    private CaseData caseData;
    private CaseDetails caseDetails;

    private RespondentSumType respondentSumType;

    @BeforeEach
    void setUp() {

        respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent");
        respondentSumType.setRespondentEmail("res@rep.com");

        Organisation organisationToAdd = Organisation.builder()
                .organisationID("1")
                .organisationName("New Organisation").build();
        Organisation organisationToRemove = Organisation.builder()
                .organisationID("2")
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
            .withRespondentRepresentative("Respondent Represented", "Rep LastName", "newres@rep.com")
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
        for (String value : claimantPersonalisation.values()) {
            assertThat(value, notNullValue());
        }
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
    void testBuildNewRespondentSolicitorPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildPersonalisationWithPartyName(caseDetails, "test_party");
        assertThat(claimantPersonalisation.size(), is(5));
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
}
