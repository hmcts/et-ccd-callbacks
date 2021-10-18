package uk.gov.hmcts.ethos.replacement.docmosis.service.ecc;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

@Service
public class ClerkService {
    private final CourtWorkerService courtWorkerService;

    public ClerkService(CourtWorkerService courtWorkerService) {
        this.courtWorkerService = courtWorkerService;
    }

    public void initialiseClerkResponsible(CaseData caseData) {
        var tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getOwningOffice());
        var clerks = courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK);
        var dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(clerks);
        if (caseData.getClerkResponsible() != null && caseData.getClerkResponsible().getValue() != null) {
            dynamicFixedListType.setValue(caseData.getClerkResponsible().getValue());
        }
        caseData.setClerkResponsible(dynamicFixedListType);
    }
}
