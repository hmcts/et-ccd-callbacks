package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtWorkerSelectionService {

    private final CourtWorkerService courtWorkerService;

    public DynamicFixedListType createCourtWorkerSelection(TribunalOffice tribunalOffice,
                                                           CourtWorkerType courtWorkerType) {
        TribunalOffice requiredTribunalOffice = TribunalOffice.getOfficeForReferenceData(tribunalOffice);
        List<DynamicValueType> listItems = courtWorkerService.getCourtWorkerByTribunalOffice(requiredTribunalOffice,
            courtWorkerType);
        return DynamicFixedListType.from(listItems);
    }
}
