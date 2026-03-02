package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

final class OrganisationUtilsTest {

    private static final String EXCEPTION_CASE_DATA_NOT_FOUND = "Case data not found";
    private static final String EXCEPTION_RESPONDENT_REPRESENTATIVE_REQUEST_EMPTY =
            "Update respondent representative request is empty";
    private static final String EXCEPTION_RESPONDENT_NAME_EMPTY = "Respondent name is empty";
    private static final String TEST_RESPONDENT_NAME_1 = "Test Respondent Name 1";
    private static final String TEST_REPRESENTATIVE_ID = "tes_representative_id";
    private static final String TEST_RESPONDENT_NAME_2 = "Test Respondent Name 2";
    private static final String TEST_ORGANISATION_ID_1 = "Test Organisation ID 1";
    private static final String TEST_ORGANISATION_ID_2 = "Test Organisation ID 2";

    private MockedStatic<OrganisationUtils> organisationUtils;

    @BeforeEach
    public void setUp() {
        organisationUtils = mockStatic(OrganisationUtils.class);
    }

    @Test
    void theFindRespondentOrganisationPolicyIndicesByOrganisationId() {
        organisationUtils.close();
        // when caseData is null
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                null, TEST_ORGANISATION_ID_1)).hasSize(NumberUtils.INTEGER_ZERO);
        // when caseData is not null but organisationId is empty
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                new CaseData(), StringUtils.EMPTY)).hasSize(NumberUtils.INTEGER_ZERO);
        // when caseData is not null but doesn't contain any respondent organisation policy
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                new CaseData(), TEST_ORGANISATION_ID_1)).hasSize(NumberUtils.INTEGER_ZERO);
        // when caseData is not null and contains a respondent organisation policy but not matching with
        // the organisationId
        CaseData caseData = new CaseData();
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(TEST_ORGANISATION_ID_2).build()).build());
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                caseData, TEST_ORGANISATION_ID_1)).hasSize(NumberUtils.INTEGER_ZERO);
        // when caseData is not null and contains a respondent organisation policy matching with
        // the organisationId
        caseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(TEST_ORGANISATION_ID_1).build()).build());
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                caseData, TEST_ORGANISATION_ID_1)).hasSize(NumberUtils.INTEGER_ONE);
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                caseData, TEST_ORGANISATION_ID_1).getFirst()).isEqualTo(NumberUtils.INTEGER_ONE);
        // when caseData is not null and contains multiple respondent organisation policies matching with
        // the organisationId
        caseData.setRespondentOrganisationPolicy2(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(TEST_ORGANISATION_ID_1).build()).build());
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                caseData, TEST_ORGANISATION_ID_1)).hasSize(2);
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                caseData, TEST_ORGANISATION_ID_1).getFirst()).isEqualTo(NumberUtils.INTEGER_ONE);
        assertThat(OrganisationUtils.findRespondentOrganisationPolicyIndicesByOrganisationId(
                caseData, TEST_ORGANISATION_ID_1).get(1)).isEqualTo(NumberUtils.INTEGER_TWO);
    }

    @Test
    void theGetRespondentOrganisationPolicies() {
        organisationUtils.close();
        // when caseData is null
        assertThat(OrganisationUtils.getRespondentOrganisationPolicies(null)).isNotNull();
        assertThat(OrganisationUtils
                .getRespondentOrganisationPolicies(null)).hasSize(NumberUtils.INTEGER_ZERO);
        // when caseData is not null but doesn't contain any respondent organisation policy
        assertThat(OrganisationUtils.getRespondentOrganisationPolicies(new CaseData())).isNotNull();
        assertThat(OrganisationUtils
                .getRespondentOrganisationPolicies(new CaseData())).hasSize(NumberUtils.INTEGER_ZERO);
        // when caseData is not null and contains a respondent organisation policy
        CaseData caseData = new CaseData();
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        assertThat(OrganisationUtils.getRespondentOrganisationPolicies(caseData)).isNotNull();
        assertThat(OrganisationUtils.getRespondentOrganisationPolicies(caseData)).hasSize(NumberUtils.INTEGER_ONE);
        // when caseData is not null and contains multiple respondent organisation policies
        caseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder().build());
        assertThat(OrganisationUtils.getRespondentOrganisationPolicies(caseData)).isNotNull();
        assertThat(OrganisationUtils.getRespondentOrganisationPolicies(caseData)).hasSize(NumberUtils.INTEGER_TWO);
    }

    @Test
    @SneakyThrows
    void theRemoveOrganisationPolicyByIndex() {
        // when caseData is empty there should be no change in the case data
        OrganisationUtils.removeOrganisationPolicyByIndex(null, 0);
        organisationUtils.verify(() -> OrganisationUtils.removeOrganisationPolicyByIndex(null, 0),
                times(NumberUtils.INTEGER_ONE));
        // when caseData is not empty but index is negative
        CaseData caseData = new CaseData();
        OrganisationUtils.removeOrganisationPolicyByIndex(caseData, -1);
        organisationUtils.verify(() -> OrganisationUtils.removeOrganisationPolicyByIndex(caseData, -1),
                times(NumberUtils.INTEGER_ONE));
        // when caseData is not empty but index is greater than 9
        OrganisationUtils.removeOrganisationPolicyByIndex(caseData, 10);
        organisationUtils.verify(() -> OrganisationUtils.removeOrganisationPolicyByIndex(caseData, 10),
                times(NumberUtils.INTEGER_ONE));
        // when caseData is not empty and index is valid
        organisationUtils.close();
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        OrganisationUtils.removeOrganisationPolicyByIndex(caseData, 0);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy.builder().build());
    }

    @Test
    void theGetNoticeOfChangeAnswersList() {
        // when caseData is null
        OrganisationUtils.getNoticeOfChangeAnswersList(null);
        organisationUtils.verify(() -> OrganisationUtils.getNoticeOfChangeAnswersList(null),
                times(NumberUtils.INTEGER_ONE));

        // when caseData is not null but doesn't contain any valid notice of change answer
        organisationUtils.close();
        assertThat(OrganisationUtils.getNoticeOfChangeAnswersList(new CaseData())).isNotNull();
        assertThat(OrganisationUtils.getNoticeOfChangeAnswersList(new CaseData())).hasSize(NumberUtils.INTEGER_ZERO);

        // when caseData is not null and contains a valid notice of change answer
        CaseData caseData = new CaseData();
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().build());
        assertThat(OrganisationUtils.getNoticeOfChangeAnswersList(caseData)).isNotNull();
        assertThat(OrganisationUtils.getNoticeOfChangeAnswersList(caseData)).hasSize(NumberUtils.INTEGER_ONE);

    }

    @Test
    void theFindNoticeOfChangeAnswerIndexByRespondentName() {
        organisationUtils.close();
        // when caseData is null
        OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(null, TEST_RESPONDENT_NAME_1);
        assertThat(OrganisationUtils
                .findNoticeOfChangeAnswerIndexByRespondentName(null, TEST_RESPONDENT_NAME_1))
                .isEqualTo(-1);
        // when caseData is not null but respondent name is blank
        OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(new CaseData(), StringUtils.EMPTY);
        assertThat(OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(new CaseData(), StringUtils.EMPTY))
                .isEqualTo(-1);
        // when caseData is not null but doesn't contain a valid notice of change answer
        OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(new CaseData(), TEST_RESPONDENT_NAME_1);
        assertThat(OrganisationUtils
                .findNoticeOfChangeAnswerIndexByRespondentName(new CaseData(), TEST_RESPONDENT_NAME_1))
                .isEqualTo(-1);
        // when caseData is not null and contains a valid notice of change answer but not matching
        // with the respondent name
        CaseData caseData = new CaseData();
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(TEST_RESPONDENT_NAME_2).build());
        OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(caseData, TEST_RESPONDENT_NAME_1);
        assertThat(OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(caseData, TEST_RESPONDENT_NAME_1))
                .isEqualTo(-1);
        // when caseData is not null and contains a valid notice of change answer matching
        // with the respondent name
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(TEST_RESPONDENT_NAME_1).build());
        OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(caseData, TEST_RESPONDENT_NAME_1);
        assertThat(OrganisationUtils.findNoticeOfChangeAnswerIndexByRespondentName(caseData, TEST_RESPONDENT_NAME_1))
                .isEqualTo(NumberUtils.INTEGER_ZERO);
    }

    @Test
    @SneakyThrows
    void theRemoveRespondentOrganisationPolicyByRespondentName() {
        organisationUtils.close();
        // when both caseData and update respondent representative request are null
        GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                () -> OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(null, null));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXCEPTION_CASE_DATA_NOT_FOUND);

        // when caseData is not null but updateRespondentRepresentativeRequest is null
        CaseData caseData = new CaseData();
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData, null));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_REPRESENTATIVE_REQUEST_EMPTY);

        // when caseData is not null but updateRespondentRepresentativeRequest's respondentName is null
        UpdateRespondentRepresentativeRequest updateRespondentRepresentativeRequest =
                UpdateRespondentRepresentativeRequest.builder().build();
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(
                caseData, updateRespondentRepresentativeRequest));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_NAME_EMPTY);

        // when caseData is not null but updateRespondentRepresentativeRequest's change organisation request is null
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().build());
        updateRespondentRepresentativeRequest.setRespondentName(TEST_RESPONDENT_NAME_1);
        OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData,
                updateRespondentRepresentativeRequest);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();

        // when caseData is not null but updateRespondentRepresentativeRequest's change organisation request's
        // organisation to remove is null
        updateRespondentRepresentativeRequest.setChangeOrganisationRequest(ChangeOrganisationRequest.builder().build());
        OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData,
                updateRespondentRepresentativeRequest);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();

        // when caseData is not null but updateRespondentRepresentativeRequest's change organisation request's
        // organisation to remove's organisation id is empty
        updateRespondentRepresentativeRequest.getChangeOrganisationRequest()
                .setOrganisationToRemove(Organisation.builder().build());
        OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData,
                updateRespondentRepresentativeRequest);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();

        // when caseData is not null and contains a valid notice of change answer not matching with the respondent name
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(TEST_RESPONDENT_NAME_2).build());
        updateRespondentRepresentativeRequest.getChangeOrganisationRequest()
                .getOrganisationToRemove().setOrganisationID(TEST_ORGANISATION_ID_1);
        OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData,
                updateRespondentRepresentativeRequest);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();

        // when caseData is not null and contains a valid notice of change answer matching with the respondent name
        // and respondent organisation policy id not match with the organisation id to remove
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(TEST_RESPONDENT_NAME_1).build());
        updateRespondentRepresentativeRequest.setRespondentName(TEST_RESPONDENT_NAME_1);
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(TEST_ORGANISATION_ID_2).build()).build());
        OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData,
                updateRespondentRepresentativeRequest);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();

        // when caseData is not null and contains a valid notice of change answer matching with the respondent name
        // and respondent organisation policy id match with the organisation id to remove but not match with
        // notice of change answer index
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(TEST_RESPONDENT_NAME_1).build());
        updateRespondentRepresentativeRequest.setRespondentName(TEST_RESPONDENT_NAME_1);
        caseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(TEST_ORGANISATION_ID_1).build()).build());
        OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData,
                updateRespondentRepresentativeRequest);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();

        // when caseData is not null and contains a valid notice of change answer matching with the respondent name
        // and respondent organisation policy id match with the organisation id to remove and match with
        // notice of change answer index
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(TEST_RESPONDENT_NAME_1).build());
        updateRespondentRepresentativeRequest.setRespondentName(TEST_RESPONDENT_NAME_1);
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(TEST_ORGANISATION_ID_1).build()).build());
        OrganisationUtils.removeRespondentOrganisationPolicyByRespondentName(caseData,
                updateRespondentRepresentativeRequest);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy.builder().build());
    }

    @Test
    void theHasUserIdentifier() {
        organisationUtils.close();
        // when user response is empty should return false
        assertThat(OrganisationUtils.hasUserIdentifier(null)).isFalse();
        // when user response not has body should return false
        ResponseEntity<AccountIdByEmailResponse> userResponse = new ResponseEntity<>(null, HttpStatus.OK);
        assertThat(OrganisationUtils.hasUserIdentifier(userResponse)).isFalse();
        // when user response not has user identifier should return false
        userResponse = new ResponseEntity<>(new AccountIdByEmailResponse(), HttpStatus.OK);
        assertThat(OrganisationUtils.hasUserIdentifier(userResponse)).isFalse();
        // when user response has user identifier should return true
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(TEST_REPRESENTATIVE_ID);
        userResponse = new ResponseEntity<>(accountIdByEmailResponse, HttpStatus.OK);
        assertThat(OrganisationUtils.hasUserIdentifier(userResponse)).isTrue();
    }
}
