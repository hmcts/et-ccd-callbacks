package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata;

import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;

import java.util.List;

public interface CourtWorkerService {
    List<DynamicValueType> getCourtWorkerByTribunalOffice(TribunalOffice tribunalOffice,
                                                          CourtWorkerType courtWorkerType);
}
