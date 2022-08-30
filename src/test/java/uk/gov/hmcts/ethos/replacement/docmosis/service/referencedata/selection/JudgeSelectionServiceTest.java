package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection;

import org.junit.Test;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JudgeSelectionServiceTest {

    @Test
    public void testCreateJudgeSelectionNoSelectedJudgeEnglandWales() {
        var tribunalOffice = TribunalOffice.MANCHESTER;
        var judgeService = mockJudgeService(tribunalOffice);
        var selectedHearing = mockHearing(null);

        var judgeSelectionService = new JudgeSelectionService(judgeService);
        var actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(tribunalOffice);
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "judge", "Judge ");
    }

    @Test
    public void testCreateJudgeSelectionWithSelectedJudgeEnglandWales() {
        var tribunalOffice = TribunalOffice.MANCHESTER;
        var judgeService = mockJudgeService(tribunalOffice);
        var selectedJudge = DynamicValueType.create("judge2", "Judge 2");
        var selectedHearing = mockHearing(selectedJudge);

        var judgeSelectionService = new JudgeSelectionService(judgeService);
        var actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(tribunalOffice);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, "judge", "Judge ", selectedJudge);
    }

    @Test
    public void testCreateJudgeSelectionNoSelectedJudgeScotland() {
        var tribunalOffice = TribunalOffice.ABERDEEN;
        var judgeService = mockJudgeService(TribunalOffice.SCOTLAND);
        var selectedHearing = mockHearing(null);

        var judgeSelectionService = new JudgeSelectionService(judgeService);
        var actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(TribunalOffice.SCOTLAND);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "judge", "Judge ");
    }

    @Test
    public void testCreateJudgeSelectionWithSelectedJudgeScotland() {
        var tribunalOffice = TribunalOffice.ABERDEEN;
        var judgeService = mockJudgeService(TribunalOffice.SCOTLAND);
        var selectedJudge = DynamicValueType.create("judge2", "Judge 2");
        var selectedHearing = mockHearing(selectedJudge);

        var judgeSelectionService = new JudgeSelectionService(judgeService);
        var actualResult = judgeSelectionService.createJudgeSelection(tribunalOffice, selectedHearing);

        verify(judgeService, times(1)).getJudgesDynamicList(TribunalOffice.SCOTLAND);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, "judge", "Judge ", selectedJudge);
    }

    private JudgeService mockJudgeService(TribunalOffice tribunalOffice) {
        var dynamicValues = SelectionServiceTestUtils.createListItems("judge", "Judge ");

        var judgeService = mock(JudgeService.class);
        when(judgeService.getJudgesDynamicList(tribunalOffice)).thenReturn(dynamicValues);
        return judgeService;
    }

    private HearingType mockHearing(DynamicValueType selectedValue) {
        var hearing = mock(HearingType.class);
        when(hearing.hasHearingJudge()).thenReturn(selectedValue != null);
        if (selectedValue != null) {
            var dynamicFixedListType = mock(DynamicFixedListType.class);
            when(dynamicFixedListType.getValue()).thenReturn(selectedValue);
            when(hearing.getJudge()).thenReturn(dynamicFixedListType);
        }

        return hearing;
    }
}
