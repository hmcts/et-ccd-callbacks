package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

final class RespondentUtilsTest {

    private static final String TEST_RESPONDENT_NAME_1 = "Test respondent name 1";
    private static final String TEST_RESPONDENT_NAME_2 = "Test respondent name 2";
    private static final String TEST_RESPONDENT_NAME_3 = "Test respondent name 3";
    private static final String DUMMY_CASE_REFERENCE = "1234567890123456";
    private static final String DUMMY_RESPONDENT_ID = "dummy12_respondent34_id56";
    private static final String EXCEPTION_RESPONDENT_NOT_FOUND =
            "Respondent not found for case ID 1234567890123456.";
    private static final String EXCEPTION_RESPONDENT_ID_NOT_FOUND =
            "Respondent ID not found for case ID 1234567890123456.";
    private static final String EXCEPTION_RESPONDENT_DETAILS_NOT_EXISTS =
            "Respondent details could not be found for respondent ID dummy12_respondent34_id56 "
                    + "in case 1234567890123456.";
    private static final String EXCEPTION_RESPONDENT_NAME_NOT_EXISTS =
            "Respondent name could not be found for respondent ID dummy12_respondent34_id56 in case 1234567890123456.";
    private static final String YES = "Yes";

    private MockedStatic<RespondentUtils> respondentUtils;

    @BeforeEach
    public void setUp() {
        respondentUtils = mockStatic(RespondentUtils.class);
    }

    @Test
    void theMarkRespondentRepresentativeRemoved() {
        UpdateRespondentRepresentativeRequest updateRespondentRepresentativeRequest =
                UpdateRespondentRepresentativeRequest.builder().build();
        // when caseData is null
        RespondentUtils.markRespondentRepresentativeRemoved(null, updateRespondentRepresentativeRequest);
        respondentUtils.verify(() -> RespondentUtils
                        .markRespondentRepresentativeRemoved(null, updateRespondentRepresentativeRequest),
                times(NumberUtils.INTEGER_ONE));
        // when updateRespondentRepresentativeRequest is null
        CaseData caseData = new CaseData();
        RespondentUtils.markRespondentRepresentativeRemoved(caseData, null);
        respondentUtils.verify(() -> RespondentUtils
                        .markRespondentRepresentativeRemoved(caseData, null),
                times(NumberUtils.INTEGER_ONE));
        // when respondentName in updateRespondentRepresentativeRequest is empty
        updateRespondentRepresentativeRequest.setRespondentName(StringUtils.EMPTY);
        RespondentUtils.markRespondentRepresentativeRemoved(caseData, updateRespondentRepresentativeRequest);
        respondentUtils.verify(() -> RespondentUtils.markRespondentRepresentativeRemoved(caseData,
                        updateRespondentRepresentativeRequest), times(NumberUtils.INTEGER_ONE));
        // when respondentCollection in caseData is empty
        respondentUtils.reset();
        updateRespondentRepresentativeRequest.setRespondentName(TEST_RESPONDENT_NAME_1);
        caseData.setRespondentCollection(new ArrayList<>());
        RespondentUtils.markRespondentRepresentativeRemoved(caseData, updateRespondentRepresentativeRequest);
        respondentUtils.verify(() -> RespondentUtils.markRespondentRepresentativeRemoved(caseData,
                       updateRespondentRepresentativeRequest), times(NumberUtils.INTEGER_ONE));
        // when respondentCollection in caseData is not empty but respondentName is empty
        respondentUtils.reset();
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(StringUtils.EMPTY).build());
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        RespondentUtils.markRespondentRepresentativeRemoved(caseData, updateRespondentRepresentativeRequest);
        respondentUtils.verify(() -> RespondentUtils.markRespondentRepresentativeRemoved(caseData,
                updateRespondentRepresentativeRequest), times(NumberUtils.INTEGER_ONE));
        // when respondentName in caseData not matches the respondentName in updateRespondentRepresentativeRequest
        respondentUtils.reset();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(TEST_RESPONDENT_NAME_2).build());
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        RespondentUtils.markRespondentRepresentativeRemoved(caseData, updateRespondentRepresentativeRequest);
        respondentUtils.verify(() -> RespondentUtils.markRespondentRepresentativeRemoved(caseData,
                updateRespondentRepresentativeRequest), times(NumberUtils.INTEGER_ONE));
        // when respondentName in caseData matches the respondentName in updateRespondentRepresentativeRequest
        respondentUtils.close();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(TEST_RESPONDENT_NAME_1).build());
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        RespondentUtils.markRespondentRepresentativeRemoved(caseData, updateRespondentRepresentativeRequest);
        assertThat(respondentSumTypeItem.getValue().getRepresentativeRemoved()).isEqualTo(YES);
    }

    @Test
    void theGetRespondentNamesByNoticeOfChangeIndexes() {
        respondentUtils.close();
        // -------- Scenario 1: returns names for provided indexes in order --------
        CaseData caseData = new CaseData();
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_1)
                .build());
        caseData.setNoticeOfChangeAnswers3(NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_2)
                .build());
        caseData.setNoticeOfChangeAnswers5(NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_NAME_3)
                .build());

        List<String> result = RespondentUtils.getRespondentNamesByNoticeOfChangeIndexes(
                caseData, List.of(0, 3, 5));

        assertEquals(List.of(TEST_RESPONDENT_NAME_1, TEST_RESPONDENT_NAME_2, TEST_RESPONDENT_NAME_3), result,
                "Should return names in the same order as the indexes");
        // -------- Scenario 2: skips null answers --------
        caseData = mock(CaseData.class);
        NoticeOfChangeAnswers a2 = mock(NoticeOfChangeAnswers.class);

        when(a2.getRespondentName()).thenReturn("Gamma LLC");

        when(caseData.getNoticeOfChangeAnswers1()).thenReturn(null);
        when(caseData.getNoticeOfChangeAnswers2()).thenReturn(a2);
        when(caseData.getNoticeOfChangeAnswers9()).thenReturn(null);

        result = RespondentUtils.getRespondentNamesByNoticeOfChangeIndexes(
                caseData, List.of(1, 2, 9));

        assertEquals(List.of("Gamma LLC"), result,
                "Should skip indexes that resolve to null answers");

        // -------- Scenario 3: returns empty list when index list empty --------
        caseData = mock(CaseData.class);

        result = RespondentUtils.getRespondentNamesByNoticeOfChangeIndexes(
                caseData, List.of());

        assertEquals(List.of(), result,
                "Empty input should yield an empty result");

        // -------- Scenario 4: ignores out-of-range indexes --------
        // Assumes getNoticeOfChangeAnswersByIndex returns null for out-of-range (e.g., -1, 10, 42)
        caseData = mock(CaseData.class);
        NoticeOfChangeAnswers a1 = mock(NoticeOfChangeAnswers.class);

        when(a1.getRespondentName()).thenReturn("Delta Inc");
        when(caseData.getNoticeOfChangeAnswers1()).thenReturn(a1);

        result = RespondentUtils.getRespondentNamesByNoticeOfChangeIndexes(
                caseData, List.of(-1, 1, 10, 42));

        assertEquals(List.of("Delta Inc"), result,
                "Only valid index should contribute a name");

        // -------- Scenario 5: allows duplicate indexes and preserves multiplicity --------
        caseData = mock(CaseData.class);
        NoticeOfChangeAnswers a4 = mock(NoticeOfChangeAnswers.class);

        when(a4.getRespondentName()).thenReturn("Echo Co");
        when(caseData.getNoticeOfChangeAnswers4()).thenReturn(a4);

        result = RespondentUtils.getRespondentNamesByNoticeOfChangeIndexes(
                caseData, List.of(4, 4));

        assertEquals(List.of("Echo Co", "Echo Co"), result,
                "Duplicate indexes should yield duplicate names in order");
    }

    @Test
    void coversAllScenariosInOneMethod() {
        respondentUtils.close();
        // --- Arrange
        CaseData caseData = mock(CaseData.class);

        NoticeOfChangeAnswers a0 = mock(NoticeOfChangeAnswers.class);
        NoticeOfChangeAnswers a3 = mock(NoticeOfChangeAnswers.class);
        NoticeOfChangeAnswers a9 = mock(NoticeOfChangeAnswers.class);

        // Populate a few indices; leave others as null to test null-handling
        when(caseData.getNoticeOfChangeAnswers0()).thenReturn(a0);
        when(caseData.getNoticeOfChangeAnswers3()).thenReturn(a3);
        when(caseData.getNoticeOfChangeAnswers4()).thenReturn(null); // explicitly null
        when(caseData.getNoticeOfChangeAnswers9()).thenReturn(a9);

        // --- Act & Assert

        // 1) Valid indices should return the exact mocked instances
        assertSame(a0, RespondentUtils.getNoticeOfChangeAnswersByIndex(caseData, 0),
                "Index 0 should return its mapped instance");
        assertSame(a3, RespondentUtils.getNoticeOfChangeAnswersByIndex(caseData, 3),
                "Index 3 should return its mapped instance");
        assertSame(a9, RespondentUtils.getNoticeOfChangeAnswersByIndex(caseData, 9),
                "Index 9 should return its mapped instance");

        // 2) Valid index with null field should return null
        assertNull(RespondentUtils.getNoticeOfChangeAnswersByIndex(caseData, 4),
                "If the mapped field is null, method should return null");

        // 3) Out-of-range indices should return null
        assertNull(RespondentUtils.getNoticeOfChangeAnswersByIndex(caseData, -1),
                "Negative index should return null");
        assertNull(RespondentUtils.getNoticeOfChangeAnswersByIndex(caseData, 10),
                "Index beyond supported range should return null");

        // 4) Repeatability: same input -> same output (identity)
        assertSame(a3, RespondentUtils.getNoticeOfChangeAnswersByIndex(caseData, 3),
                "Repeated calls with the same index should return the same instance");

        // 5) Null caseData should throw NPE (current implementation dereferences caseData)
        assertThrows(NullPointerException.class,
                () -> RespondentUtils.getNoticeOfChangeAnswersByIndex(null, 0),
                "Null caseData should throw NullPointerException");
    }

    @Test
    void theValidateRespondent() {
        respondentUtils.close();
        // when respondent is null
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> RespondentUtils.validateRespondent(null, DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_NOT_FOUND);
        // when respondent does not have any id
        RespondentSumTypeItem respondentSumTypeItemWithoutRespondentId = new RespondentSumTypeItem();
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentUtils.validateRespondent(respondentSumTypeItemWithoutRespondentId,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_ID_NOT_FOUND);
        // when respondent details not found
        RespondentSumTypeItem respondentSumTypeItemWithoutRespondentDetails = new RespondentSumTypeItem();
        respondentSumTypeItemWithoutRespondentDetails.setId(DUMMY_RESPONDENT_ID);
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentUtils.validateRespondent(respondentSumTypeItemWithoutRespondentDetails,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_DETAILS_NOT_EXISTS);
        // when respondent name not found
        RespondentSumTypeItem respondentSumTypeItemWithoutRespondentName = new RespondentSumTypeItem();
        respondentSumTypeItemWithoutRespondentName.setId(DUMMY_RESPONDENT_ID);
        respondentSumTypeItemWithoutRespondentName.setValue(RespondentSumType.builder().build());
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentUtils.validateRespondent(respondentSumTypeItemWithoutRespondentName,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_NAME_NOT_EXISTS);
    }

    @Test
    void theIsValidRespondent() {
        respondentUtils.close();
        // when respondent is empty should return false
        assertThat(RespondentUtils.isValidRespondent(null)).isFalse();
        // when respondent id is empty should return false
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        assertThat(RespondentUtils.isValidRespondent(respondentSumTypeItem)).isFalse();
        // when respondent value is empty should return false
        respondentSumTypeItem.setId(DUMMY_RESPONDENT_ID);
        assertThat(RespondentUtils.isValidRespondent(respondentSumTypeItem)).isFalse();
        // when respondent name is empty should return false
        respondentSumTypeItem.setValue(RespondentSumType.builder().build());
        assertThat(RespondentUtils.isValidRespondent(respondentSumTypeItem)).isFalse();
        // when respondent has id, value and name should return true
        respondentSumTypeItem.getValue().setRespondentName(TEST_RESPONDENT_NAME_1);
        assertThat(RespondentUtils.isValidRespondent(respondentSumTypeItem)).isTrue();
    }

    @Test
    void theIsCaseDataValidForNoc() {
        respondentUtils.close();
        // when case data is null returns false
        assertThat(RespondentUtils.hasRespondents(null)).isFalse();
        // when case data not has respondent collection returns false
        CaseData caseData = new CaseData();
        assertThat(RespondentUtils.hasRespondents(caseData)).isFalse();
        // when case data has both respondent collection returns true
        caseData.setRespondentCollection(List.of(new RespondentSumTypeItem()));
        assertThat(RespondentUtils.hasRespondents(caseData)).isTrue();
    }

}
