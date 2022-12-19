package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import java.util.List;

@Service
public class JudgeSelectionService {

    private final JudgeService judgeService;

    public JudgeSelectionService(JudgeService judgeService) {
        this.judgeService = judgeService;
    }

    public DynamicFixedListType createJudgeSelection(TribunalOffice tribunalOffice, HearingType selectedHearing) {
        TribunalOffice requiredTribunalOffice = TribunalOffice.getOfficeForReferenceData(tribunalOffice);
        List<DynamicValueType> listItems = judgeService.getJudgesDynamicList(requiredTribunalOffice);
        DynamicFixedListType selectedJudge = selectedHearing.getJudge();
        return DynamicFixedListType.from(listItems, selectedJudge);
    }
}
