package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@SuppressWarnings({"PMD.LinguisticNaming"})
public class NotificationHelperTest {
    private CaseData caseData;
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("12345/6789")
            .withClaimantType("claimant@unrepresented.com")
            .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
            .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
            .withRespondentWithAddress("Respondent Unrepresented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null, "respondent@unrepresented.com")
            .withRespondentWithAddress("Respondent Represented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withRespondentRepresentative("Respondent Represented", "Rep LastName", "res@rep.com")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");
        caseData = caseDetails.getCaseData();
        caseData.setClaimant("Claimant LastName");
    }

    @Test
    public void getParties_withRepresentedClaimant_shouldReturnClaimantRep() {
        String actual = NotificationHelper.getParties(caseData);

        assertThat(actual).isEqualTo("Claimant Rep, Respondent Unrepresented, Rep LastName");
    }

    @Test
    public void getParties_withUnrepresentedClaimant_shouldReturnClaimant() {
        caseData.setRepresentativeClaimantType(null);
        String actual = NotificationHelper.getParties(caseData);

        assertThat(actual).isEqualTo("Claimant LastName, Respondent Unrepresented, Rep LastName");
    }

    @Test
    public void buildMapForClaimant_withRepresentedClaimant_shouldReturnClaimantRepDetails() {
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual.get("emailAddress")).isEqualTo("claimant@represented.com");
        assertThat(actual.get("name")).isEqualTo("C Rep");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForClaimant_withRepresentedClaimantWithNoEmail_shouldReturnClaimantRepDetails() {
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual.get("emailAddress")).isEqualTo("");
        assertThat(actual.get("name")).isEqualTo("C Rep");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForClaimant_withUnrepresentedClaimant_shouldReturnClaimantDetailsWithTitle() {
        caseData.setRepresentativeClaimantType(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual.get("emailAddress")).isEqualTo("claimant@unrepresented.com");
        assertThat(actual.get("name")).isEqualTo("Mr LastName");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForClaimant_withUnrepresentedClaimant_shouldReturnClaimantDetailsWithPreferredTitle() {
        caseData.setRepresentativeClaimantType(null);
        caseData.getClaimantIndType().setClaimantTitle(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual.get("emailAddress")).isEqualTo("claimant@unrepresented.com");
        assertThat(actual.get("name")).isEqualTo("Mr LastName");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForClaimant_withUnrepresentedClaimant_shouldReturnClaimantDetailsWithInitial() {
        caseData.setRepresentativeClaimantType(null);
        caseData.getClaimantIndType().setClaimantTitle(null);
        caseData.getClaimantIndType().setClaimantPreferredTitle(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual.get("emailAddress")).isEqualTo("claimant@unrepresented.com");
        assertThat(actual.get("name")).isEqualTo("C LastName");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForRespondent_withUnrepresentedRespondent_shouldReturnRespondentDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType unrepresentedRespondent = respondents.get(0).getValue();
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, unrepresentedRespondent);

        assertThat(actual.get("emailAddress")).isEqualTo("respondent@unrepresented.com");
        assertThat(actual.get("name")).isEqualTo("Respondent Unrepresented");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForRespondent_withRespondentRepresentative_shouldReturnRepresentativeDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType representedRespondent = respondents.get(1).getValue();
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, representedRespondent);

        assertThat(actual.get("emailAddress")).isEqualTo("res@rep.com");
        assertThat(actual.get("name")).isEqualTo("R LastName");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForRespondent_withRespondentRepresentativeWithNoEmail_shouldReturnRepresentativeDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType representedRespondent = respondents.get(1).getValue();
        caseData.getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, representedRespondent);

        assertThat(actual.get("emailAddress")).isEqualTo("");
        assertThat(actual.get("name")).isEqualTo("R LastName");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }

    @Test
    public void buildMapForRespondent_withUnrepresentedRespondentWithNoEmail_shouldReturnRepresentativeDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType unrepresentedRespondent = respondents.get(0).getValue();
        unrepresentedRespondent.setRespondentEmail(null);
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, unrepresentedRespondent);

        assertThat(actual.get("emailAddress")).isEqualTo("");
        assertThat(actual.get("name")).isEqualTo("Respondent Unrepresented");
        assertThat(actual.get("caseNumber")).isEqualTo("12345/6789");
    }
}
