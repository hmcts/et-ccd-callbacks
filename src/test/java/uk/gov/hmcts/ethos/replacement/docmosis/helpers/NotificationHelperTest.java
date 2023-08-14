package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class NotificationHelperTest {
    private CaseData caseData;
    private CaseDetails caseDetails;

    @BeforeEach
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
    void getParties_withRepresentedClaimant_shouldReturnClaimantRep() {
        String actual = NotificationHelper.getParties(caseData);

        assertThat(actual).isEqualTo("Claimant Rep, Respondent Unrepresented, Rep LastName");
    }

    @Test
    void getParties_withUnrepresentedClaimant_shouldReturnClaimant() {
        caseData.setRepresentativeClaimantType(null);
        String actual = NotificationHelper.getParties(caseData);

        assertThat(actual).isEqualTo("Claimant LastName, Respondent Unrepresented, Rep LastName");
    }

    @Test
    void buildMapForClaimant_withRepresentedClaimant_shouldReturnClaimantRepDetails() {
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual).containsEntry("emailAddress", "claimant@represented.com")
            .containsEntry("name", "C Rep")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForClaimant_withRepresentedClaimantWithNoEmail_shouldReturnClaimantRepDetails() {
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual).containsEntry("emailAddress", "")
            .containsEntry("name", "C Rep")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForClaimant_withUnrepresentedClaimant_shouldReturnClaimantDetailsWithTitle() {
        caseData.setRepresentativeClaimantType(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual).containsEntry("emailAddress", "claimant@unrepresented.com")
            .containsEntry("name", "Mr LastName")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForClaimant_withUnrepresentedClaimant_shouldReturnClaimantDetailsWithPreferredTitle() {
        caseData.setRepresentativeClaimantType(null);
        caseData.getClaimantIndType().setClaimantTitle(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual).containsEntry("emailAddress", "claimant@unrepresented.com")
            .containsEntry("name", "Mr LastName")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForClaimant_withUnrepresentedClaimant_shouldReturnClaimantDetailsWithInitial() {
        caseData.setRepresentativeClaimantType(null);
        caseData.getClaimantIndType().setClaimantTitle(null);
        caseData.getClaimantIndType().setClaimantPreferredTitle(null);
        Map<String, String> actual = NotificationHelper.buildMapForClaimant(caseDetails);

        assertThat(actual).containsEntry("emailAddress", "claimant@unrepresented.com")
            .containsEntry("name", "C LastName")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForRespondent_withUnrepresentedRespondent_shouldReturnRespondentDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType unrepresentedRespondent = respondents.get(0).getValue();
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, unrepresentedRespondent);

        assertThat(actual).containsEntry("emailAddress", "respondent@unrepresented.com")
            .containsEntry("name", "Respondent Unrepresented")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForRespondent_withRespondentRepresentative_shouldReturnRepresentativeDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType representedRespondent = respondents.get(1).getValue();
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, representedRespondent);

        assertThat(actual).containsEntry("emailAddress", "res@rep.com")
            .containsEntry("name", "R LastName")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForRespondent_withRespondentRepresentativeWithNoEmail_shouldReturnRepresentativeDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType representedRespondent = respondents.get(1).getValue();
        caseData.getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, representedRespondent);

        assertThat(actual).containsEntry("emailAddress", "")
            .containsEntry("name", "R LastName")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void buildMapForRespondent_withUnrepresentedRespondentWithNoEmail_shouldReturnRepresentativeDetails() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType unrepresentedRespondent = respondents.get(0).getValue();
        unrepresentedRespondent.setRespondentEmail(null);
        Map<String, String> actual = NotificationHelper.buildMapForRespondent(caseDetails, unrepresentedRespondent);

        assertThat(actual).containsEntry("emailAddress", "")
            .containsEntry("name", "Respondent Unrepresented")
            .containsEntry("caseNumber", "12345/6789");
    }

    @Test
    void shouldGetRespondentEmailAddressWhenUnrepresented() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType unrepresentedRespondent = respondents.get(0).getValue();
        String result = NotificationHelper.getEmailAddressForUnrepresentedRespondent(caseData, unrepresentedRespondent);

        assertThat(result).isEqualTo("respondent@unrepresented.com");
    }

    @Test
    void shouldReturnNullWhenRepresented() {
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RespondentSumType representedRespondent = respondents.get(0).getValue();
        representedRespondent.setRespondentName("Respondent Represented");
        String result = NotificationHelper.getEmailAddressForUnrepresentedRespondent(caseData, representedRespondent);

        assertThat(result).isNull();
    }
}
