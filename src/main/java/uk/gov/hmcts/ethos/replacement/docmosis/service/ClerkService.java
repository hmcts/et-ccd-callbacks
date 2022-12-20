package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import java.util.List;

@Service
@SuppressWarnings({"PMD.LawOfDemeter"})
public class ClerkService {
    private final CourtWorkerService courtWorkerService;

    public ClerkService(CourtWorkerService courtWorkerService) {
        this.courtWorkerService = courtWorkerService;
    }

    public void initialiseClerkResponsible(CaseData caseData) {
        TribunalOffice tribunalOffice = TribunalOffice.getOfficeForReferenceData(
                TribunalOffice.valueOfOfficeName(caseData.getManagingOffice()));
        List<DynamicValueType> listItems = courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK);
        DynamicFixedListType selectedClerk = caseData.getClerkResponsible();

        caseData.setClerkResponsible(DynamicFixedListType.from(listItems, selectedClerk));
    }

    public void initialiseClerkResponsible(MultipleData multipleData) {
        TribunalOffice tribunalOffice = TribunalOffice.getOfficeForReferenceData(
                TribunalOffice.valueOfOfficeName(multipleData.getManagingOffice()));
        List<DynamicValueType> listItems = courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK);
        DynamicFixedListType selectedClerk = multipleData.getClerkResponsible();

        multipleData.setClerkResponsible(DynamicFixedListType.from(listItems, selectedClerk));
    }

    public void initialiseClerkResponsible(String caseTypeId, ListingData listingData) {
        List<DynamicValueType> listItems;
        if (Constants.SCOTLAND_LISTING_CASE_TYPE_ID.equals(caseTypeId)) {
            listItems = courtWorkerService.getCourtWorkerByTribunalOffice(TribunalOffice.SCOTLAND,
                    CourtWorkerType.CLERK);
        } else {
            TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(listingData.getManagingOffice());
            listItems = courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice, CourtWorkerType.CLERK);
        }

        listingData.setClerkResponsible(DynamicFixedListType.from(listItems));
    }
}
