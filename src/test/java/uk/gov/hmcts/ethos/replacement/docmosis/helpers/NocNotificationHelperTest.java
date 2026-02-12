package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class   NocNotificationHelperTest {
    private static final String RESPONDENT_NAME_1 = "Respondent Name 1";
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
        respondentSumType.setRespondentName(RESPONDENT_NAME_1);
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
                organisationToRemove, null,
                null)
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                        "length num", "type", "Yes")
            .withHearingSession(
                        0, "2029-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_LISTED,
                        true)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");
        caseData = caseDetails.getCaseData();
        caseData.setClaimant("Claimant LastName");
        caseData.setTribunalCorrespondenceEmail("respondent@unrepresented.com");
        caseData.setTribunalAndOfficeLocation("Leeds");
    }

    @Test
    void testBuildClaimantPersonalisation() {
        String citLink = "http://domain/citizen-hub/1234";
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildPersonalisationWithPartyName(caseDetails, "test_party", citLink);
        assertThat(claimantPersonalisation.size()).isEqualTo(6);
        assertThat(claimantPersonalisation.get("party_name")).isEqualTo("test_party");
        assertThat(claimantPersonalisation.get("ccdId")).isEqualTo("1234");
        assertThat(claimantPersonalisation.get("claimant")).isEqualTo("Claimant LastName");
        assertThat(claimantPersonalisation.get("list_of_respondents")).isEqualTo(
                "Respondent Name 1 Respondent Unrepresented Respondent Represented Respondent");
        assertThat(claimantPersonalisation.get("case_number")).isEqualTo("12345/6789");
        assertThat(claimantPersonalisation.get("linkToCitUI")).isEqualTo(citLink);
    }

    @Test
    void testBuildPreviousRespondentSolicitorPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseData);
        assertThat(claimantPersonalisation.size()).isEqualTo(3);
        for (String value : claimantPersonalisation.values()) {
            assertThat(value).isNotNull();
        }
    }

    @Test
    void testBuildRespondentPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildNoCPersonalisation(caseDetails, respondentSumType.getRespondentName());
        assertThat(claimantPersonalisation.size()).isEqualTo(6);
        for (String value : claimantPersonalisation.values()) {
            assertThat(value).isNotNull();
        }
    }

    @Test
    void testBuildTribunalPersonalisation() {
        Map<String, String> claimantPersonalisation = NocNotificationHelper.buildTribunalPersonalisation(caseData);
        assertThat(claimantPersonalisation.size()).isEqualTo(5);
        assertThat(claimantPersonalisation.get("date")).isEqualTo("25 Nov 2029");
        for (String value : claimantPersonalisation.values()) {
            assertThat(value).isNotNull();
        }
    }

    @Test
    void testBuildTribunalPersonalisationWithHearingDate() {
        caseData.setHearingCollection(new ArrayList<>());
        Map<String, String> claimantPersonalisation = NocNotificationHelper.buildTribunalPersonalisation(caseData);
        assertThat(claimantPersonalisation.size()).isEqualTo(5);
        assertThat(claimantPersonalisation.get("date")).isEqualTo("Not set");
        for (String value : claimantPersonalisation.values()) {
            assertThat(value).isNotNull();
        }
    }

    @Test
    void testGetRespondentNameForNewSolicitor() {
        String respondentName = NocNotificationHelper
                .getRespondentNameForNewSolicitor(caseData.getChangeOrganisationRequestField(), caseData);
        assertThat(respondentName).isEqualTo(RESPONDENT_NAME_1);
    }

    @Test
    void testGetRespondent_ReturnsCorrectRespondent() {
        ChangeOrganisationRequest changeRequest = mock(ChangeOrganisationRequest.class);
        DynamicFixedListType caseRoleId = mock(DynamicFixedListType.class);
        CaseData caseDataLocal = mock(CaseData.class);
        RespondentSumType respondentSumType2 = mock(RespondentSumType.class);
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        item.setValue(respondentSumType2);
        caseDataLocal.setRespondentCollection(List.of(item));
        when(changeRequest.getCaseRoleId()).thenReturn(caseRoleId);
        when(caseRoleId.getSelectedCode()).thenReturn("SOLICITORA");
        when(respondentSumType2.getRespondentName()).thenReturn("Respondent Name");

        NocRespondentHelper nocRespondentHelper = mock(NocRespondentHelper.class);
        when(nocRespondentHelper.getRespondent("Respondent Name", caseDataLocal))
                .thenReturn(respondentSumType2);
        when(caseDataLocal.getRespondentCollection()).thenReturn(List.of(item));
        try (var mocked = Mockito.mockStatic(SolicitorRole.class)) {
            mocked.when(() -> SolicitorRole.from("SOLICITORA"))
                    .thenReturn(Optional.of(SolicitorRole.SOLICITORA));

            RespondentSumType result = NocNotificationHelper.getRespondent(changeRequest, caseDataLocal,
                    nocRespondentHelper);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(respondentSumType2, result);
        }
    }

    @Test
    void testGetRespondent_ReturnsNullOnNullInputs() {
        assertNull(NocNotificationHelper.getRespondent(null, null, null));
    }

    @Test
    void testGetRespondent_ReturnsNullOnMissingCaseRoleId() {
        ChangeOrganisationRequest changeRequest = mock(ChangeOrganisationRequest.class);
        when(changeRequest.getCaseRoleId()).thenReturn(null);
        assertNull(NocNotificationHelper.getRespondent(changeRequest, mock(CaseData.class),
                mock(NocRespondentHelper.class)));
    }

    @Test
    void testGetRespondent_ReturnsNullOnNullSelectedRole() {
        ChangeOrganisationRequest changeRequest = mock(ChangeOrganisationRequest.class);
        DynamicFixedListType caseRoleId = mock(DynamicFixedListType.class);
        when(changeRequest.getCaseRoleId()).thenReturn(caseRoleId);
        when(caseRoleId.getSelectedCode()).thenReturn(null);
        assertNull(NocNotificationHelper.getRespondent(changeRequest, mock(CaseData.class),
                mock(NocRespondentHelper.class)));
    }

}
