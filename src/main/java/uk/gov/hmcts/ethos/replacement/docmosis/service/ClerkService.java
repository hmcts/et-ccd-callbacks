package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

@Service
public class ClerkService {
    private final CourtWorkerService courtWorkerService;

    public ClerkService(CourtWorkerService courtWorkerService) {
        this.courtWorkerService = courtWorkerService;
    }

    public void initialiseClerkResponsible(CaseData caseData) {
        var tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        var listItems = courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK);
        var selectedClerk = caseData.getClerkResponsible();

        caseData.setClerkResponsible(DynamicFixedListType.from(listItems, selectedClerk));
    }

    public void initialiseClerkResponsible(MultipleData multipleData) {
        var tribunalOffice = TribunalOffice.valueOfOfficeName(multipleData.getManagingOffice());
        var listItems = courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK);
        var selectedClerk = multipleData.getClerkResponsible();

        multipleData.setClerkResponsible(DynamicFixedListType.from(listItems, selectedClerk));
    }
}
