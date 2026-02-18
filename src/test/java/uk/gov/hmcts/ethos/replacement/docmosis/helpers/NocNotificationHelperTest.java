package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.JsonParserUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
class NocNotificationHelperTest {
    private static final String CASE_DETAILS_JSON_FILE = "caseDetailsTest1.json";
    private static final String RESPONDENT_NAME_1 = "Respondent Name 1";
    private static final String RESPONDENT_EMAIL = "respondent@hmcts.org";
    private static final String ROLE_SOLICITOR_A = "[SOLICITORA]";
    private static final String UNKNOWN = "Unknown";
    private static final String NEW_ORG_ID = "1";
    private static final String OLD_ORG_ID = "2";
    private static final String HEARING_DATE_UNFORMATTED = "2029-11-25T12:11:00.000";
    private static final String NOT_SET = "Not set";

    private static final String FIELD_PARTY_NAME = "party_name";
    private static final String PARTY_NAME = "test_party";
    private static final String FIELD_CCD_ID = "ccdId";
    private static final String CASE_ID = "1234567890123456";
    private static final String FIELD_CLAIMANT = "claimant";
    private static final String CLAIMANT = "Claimant Name";
    private static final String FIELD_LIST_OF_RESPONDENTS = "list_of_respondents";
    private static final String LIST_OF_RESPONDENTS = "Respondent Name 1";
    private static final String FIELD_CASE_NUMBER = "case_number";
    private static final String CASE_NUMBER = "600001/2026";
    private static final String FIELD_LINK_TO_CIT_UI = "linkToCitUI";
    private static final String LINK_TO_CIT_UI = "http://domain/citizen-hub/1234";
    private static final String FIELD_HEARING_DATE = "date";
    private static final String HEARING_DATE = "25 Nov 2029";
    private static final String TRIBUNAL_CORRESPONDENCE_EMAIL = "tribunal_correspondence@hmcts.org";
    private static final String TRIBUNAL_AND_OFFICE_LOCATION = "Leeds";
    private static final String NEW_ORGANISATION_NAME = "New Organisation Name";
    private static final String OLD_ORGANISATION_NAME = "Old Organisation Name";

    @Mock
    private NocRespondentHelper nocRespondentHelper;

    private CaseData caseData;
    private CaseDetails caseDetails;

    private RespondentSumType respondentSumType;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_NAME_1);
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL);
        caseDetails = JsonParserUtils.generateCaseDetails(CASE_DETAILS_JSON_FILE);
        caseDetails.setCaseId(CASE_ID);
        caseData = caseDetails.getCaseData();
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setValue(new DynamicValueType());
        dynamicFixedListType.getValue().setCode(ROLE_SOLICITOR_A);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(new DateListedType());
        dateListedTypeItem.getValue().setListedDate(HEARING_DATE_UNFORMATTED);
        dateListedTypeItem.getValue().setHearingStatus(HEARING_STATUS_LISTED);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(dateListedTypeItem));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        caseData.setEthosCaseReference(CASE_NUMBER);
        caseData.setHearingCollection(List.of(hearingTypeItem));
        Organisation organisationToAdd = Organisation.builder()
                .organisationID(NEW_ORG_ID)
                .organisationName(NEW_ORGANISATION_NAME).build();
        Organisation organisationToRemove = Organisation.builder()
                .organisationID(OLD_ORG_ID)
                .organisationName(OLD_ORGANISATION_NAME).build();
        ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
                .organisationToAdd(organisationToAdd).organisationToRemove(organisationToRemove)
                .caseRoleId(dynamicFixedListType).build();
        caseData.setChangeOrganisationRequestField(changeOrganisationRequest);
        caseData.setClaimant(CLAIMANT);
        caseData.setTribunalCorrespondenceEmail(TRIBUNAL_CORRESPONDENCE_EMAIL);
        caseData.setTribunalAndOfficeLocation(TRIBUNAL_AND_OFFICE_LOCATION);
    }

    @Test
    void testBuildClaimantPersonalisation() {

        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildPersonalisationWithPartyName(caseDetails, PARTY_NAME, LINK_TO_CIT_UI);
        assertThat(claimantPersonalisation).hasSize(LoggerTestUtils.INTEGER_SIX);
        assertThat(claimantPersonalisation).containsEntry(FIELD_PARTY_NAME, PARTY_NAME);
        assertThat(claimantPersonalisation).containsEntry(FIELD_CCD_ID, CASE_ID);
        assertThat(claimantPersonalisation).containsEntry(FIELD_CLAIMANT, CLAIMANT);
        assertThat(claimantPersonalisation).containsEntry(FIELD_LIST_OF_RESPONDENTS, LIST_OF_RESPONDENTS);
        assertThat(claimantPersonalisation).containsEntry(FIELD_CASE_NUMBER, CASE_NUMBER);
        assertThat(claimantPersonalisation).containsEntry(FIELD_LINK_TO_CIT_UI, LINK_TO_CIT_UI);
    }

    @Test
    void testBuildPreviousRespondentSolicitorPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildPreviousRespondentSolicitorPersonalisation(caseData);
        assertThat(claimantPersonalisation).hasSize(LoggerTestUtils.INTEGER_THREE);
        for (String value : claimantPersonalisation.values()) {
            assertThat(value).isNotNull();
        }
    }

    @Test
    void testBuildRespondentPersonalisation() {
        Map<String, String> claimantPersonalisation =
            NocNotificationHelper.buildNoCPersonalisation(caseDetails, respondentSumType.getRespondentName());
        assertThat(claimantPersonalisation).hasSize(LoggerTestUtils.INTEGER_SIX);
        for (String value : claimantPersonalisation.values()) {
            assertThat(value).isNotNull();
        }
    }

    @Test
    void testBuildTribunalPersonalisation() {
        Map<String, String> claimantPersonalisation = NocNotificationHelper.buildTribunalPersonalisation(caseData);
        assertThat(claimantPersonalisation).hasSize(LoggerTestUtils.INTEGER_FIVE);
        assertThat(claimantPersonalisation).containsEntry(FIELD_HEARING_DATE, HEARING_DATE);
        for (String value : claimantPersonalisation.values()) {
            assertThat(value).isNotNull();
        }
    }

    @Test
    void testBuildTribunalPersonalisationWithHearingDate() {
        caseData.setHearingCollection(new ArrayList<>());
        Map<String, String> claimantPersonalisation = NocNotificationHelper.buildTribunalPersonalisation(caseData);
        assertThat(claimantPersonalisation).hasSize(LoggerTestUtils.INTEGER_FIVE);
        assertThat(claimantPersonalisation).containsEntry(FIELD_HEARING_DATE, NOT_SET);
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
    void testGetRespondentNameForNewSolicitor_nullChangeRequest() {
        String respondentName = NocNotificationHelper.getRespondentNameForNewSolicitor(null, caseData);
        assertThat(respondentName).isEqualTo(UNKNOWN);
    }

    @Test
    void testGetRespondentNameForNewSolicitor_nullCaseRoleId() {
        caseData.getChangeOrganisationRequestField().setCaseRoleId(null);
        String respondentName = NocNotificationHelper
                .getRespondentNameForNewSolicitor(caseData.getChangeOrganisationRequestField(), caseData);
        assertThat(respondentName).isEqualTo(UNKNOWN);
    }

    @Test
    void testGetRespondent_nullChangeRequest() {
        assertThat(NocNotificationHelper.getRespondent(null, caseData, nocRespondentHelper)).isNull();
    }

    @Test
    void testGetRespondent_nullCaseRoleId() {
        caseData.getChangeOrganisationRequestField().setCaseRoleId(null);
        RespondentSumType result = NocNotificationHelper.getRespondent(
                caseData.getChangeOrganisationRequestField(), caseData, nocRespondentHelper);
        assertNull(result);
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

        NocRespondentHelper nocRespondentHelperLocal = mock(NocRespondentHelper.class);
        when(nocRespondentHelperLocal.getRespondent("Respondent Name", caseDataLocal))
                .thenReturn(respondentSumType2);
        when(caseDataLocal.getRespondentCollection()).thenReturn(List.of(item));
        try (var mocked = Mockito.mockStatic(SolicitorRole.class)) {
            mocked.when(() -> SolicitorRole.from("SOLICITORA"))
                    .thenReturn(Optional.of(SolicitorRole.SOLICITORA));

            RespondentSumType result = NocNotificationHelper.getRespondent(changeRequest, caseDataLocal,
                    nocRespondentHelperLocal);

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
