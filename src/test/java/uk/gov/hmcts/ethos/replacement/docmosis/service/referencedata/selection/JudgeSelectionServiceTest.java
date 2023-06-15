package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JudgeSelectionServiceTest {

    @Test
    void testCreateJudgeSelectionNoSelectedJudgeEnglandWales() {
        TribunalOffice tribunalOffice = TribunalOffice.MANCHESTER;
        JudgeService judgeService = mockJudgeService(tribunalOffice);
        HearingType selectedHearing = mockHearing(null);

        JudgeSelectionService judgeSelectionService = new JudgeSelectionService(judgeService);
        DynamicFixedListType actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(tribunalOffice);
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "judge", "Judge ");
    }

    @Test
    void testCreateJudgeSelectionWithSelectedJudgeEnglandWales() {
        TribunalOffice tribunalOffice = TribunalOffice.MANCHESTER;
        JudgeService judgeService = mockJudgeService(tribunalOffice);
        DynamicValueType selectedJudge = DynamicValueType.create("judge2", "Judge 2");
        HearingType selectedHearing = mockHearing(selectedJudge);

        JudgeSelectionService judgeSelectionService = new JudgeSelectionService(judgeService);
        DynamicFixedListType actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(tribunalOffice);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, "judge", "Judge ", selectedJudge);
    }

    @Test
    void testCreateJudgeSelectionNoSelectedJudgeScotland() {
        TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
        JudgeService judgeService = mockJudgeService(TribunalOffice.SCOTLAND);
        HearingType selectedHearing = mockHearing(null);

        JudgeSelectionService judgeSelectionService = new JudgeSelectionService(judgeService);
        DynamicFixedListType actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(TribunalOffice.SCOTLAND);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "judge", "Judge ");
    }

    @Test
    void testCreateJudgeSelectionWithSelectedJudgeScotland() {
        TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
        JudgeService judgeService = mockJudgeService(TribunalOffice.SCOTLAND);
        DynamicValueType selectedJudge = DynamicValueType.create("judge2", "Judge 2");
        HearingType selectedHearing = mockHearing(selectedJudge);

        JudgeSelectionService judgeSelectionService = new JudgeSelectionService(judgeService);
        DynamicFixedListType actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(TribunalOffice.SCOTLAND);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, "judge", "Judge ", selectedJudge);
    }

    private JudgeService mockJudgeService(TribunalOffice tribunalOffice) {
        List<DynamicValueType> dynamicValues = SelectionServiceTestUtils.createListItems("judge", "Judge ");

        JudgeService judgeService = mock(JudgeService.class);
        when(judgeService.getJudgesDynamicList(tribunalOffice)).thenReturn(dynamicValues);
        return judgeService;
    }

    private HearingType mockHearing(DynamicValueType selectedValue) {
        HearingType hearing = mock(HearingType.class);
        when(hearing.hasHearingJudge()).thenReturn(selectedValue != null);
        if (selectedValue != null) {
            DynamicFixedListType dynamicFixedListType = mock(DynamicFixedListType.class);
            when(dynamicFixedListType.getValue()).thenReturn(selectedValue);
            when(hearing.getJudge()).thenReturn(dynamicFixedListType);
        }

        return hearing;
    }
}
