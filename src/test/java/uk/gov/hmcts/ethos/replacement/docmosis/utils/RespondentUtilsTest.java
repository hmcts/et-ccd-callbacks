package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

final class RespondentUtilsTest {

    private static final String TEST_RESPONDENT_NAME_1 = "Test respondent name 1";
    private static final String TEST_RESPONDENT_NAME_2 = "Test respondent name 2";
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
}
