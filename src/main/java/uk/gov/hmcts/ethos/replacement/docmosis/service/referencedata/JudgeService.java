package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata;

import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;

import java.util.List;

public interface JudgeService {
    List<Judge> getJudges(TribunalOffice tribunalOffice);

    List<DynamicValueType> getJudgesDynamicList(TribunalOffice tribunalOffice);
}
