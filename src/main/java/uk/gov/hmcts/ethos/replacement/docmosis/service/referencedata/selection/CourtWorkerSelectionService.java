package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

@Service
@RequiredArgsConstructor
public class CourtWorkerSelectionService {

    private final CourtWorkerService courtWorkerService;

    public DynamicFixedListType createCourtWorkerSelection(TribunalOffice tribunalOffice,
                                                           CourtWorkerType courtWorkerType) {
        var requiredTribunalOffice = TribunalOffice.getOfficeForReferenceData(tribunalOffice);
        var listItems = courtWorkerService.getCourtWorkerByTribunalOffice(requiredTribunalOffice, courtWorkerType);
        return DynamicFixedListType.from(listItems);
    }
}
