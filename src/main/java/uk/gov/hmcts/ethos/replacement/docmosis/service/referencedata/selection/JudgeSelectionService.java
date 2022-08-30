package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

@Service
public class JudgeSelectionService {

    private final JudgeService judgeService;

    public JudgeSelectionService(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    public DynamicFixedListType createJudgeSelection(TribunalOffice tribunalOffice, HearingType selectedHearing) {
        var requiredTribunalOffice = TribunalOffice.getOfficeForReferenceData(tribunalOffice);
        var listItems = judgeService.getJudgesDynamicList(requiredTribunalOffice);
        var selectedJudge = selectedHearing.getJudge();
        return DynamicFixedListType.from(listItems, selectedJudge);
    }
}
