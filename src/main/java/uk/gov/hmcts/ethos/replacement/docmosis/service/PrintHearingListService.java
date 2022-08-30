package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;

@Service
public class PrintHearingListService {

    private final VenueService venueService;

    public PrintHearingListService(VenueService venueService) {
        this.venueService = venueService;
    }

    public void initPrintHearingLists(CaseData caseData) {
        if (TribunalOffice.isEnglandWalesOffice(caseData.getManagingOffice())) {
            initEnglandWalesCase(caseData);
        } else {
            initScotlandCase(caseData);
        }
    }

    private void initEnglandWalesCase(CaseData caseData) {
        var listingData = new ListingData();
        listingData.setListingVenue(getVenueList(caseData.getManagingOffice()));
        caseData.setPrintHearingDetails(listingData);
    }

    private void initScotlandCase(CaseData caseData) {
        var listingData = new ListingData();
        listingData.setVenueAberdeen(getVenueList(TribunalOffice.ABERDEEN));
        listingData.setVenueDundee(getVenueList(TribunalOffice.DUNDEE));
        listingData.setVenueEdinburgh(getVenueList(TribunalOffice.EDINBURGH));
        listingData.setVenueGlasgow(getVenueList(TribunalOffice.GLASGOW));

        caseData.setPrintHearingDetails(listingData);
    }

    private DynamicFixedListType getVenueList(String managingOffice) {
        var tribunalOffice = TribunalOffice.valueOfOfficeName(managingOffice);
        return getVenueList(tribunalOffice);
    }

    private DynamicFixedListType getVenueList(TribunalOffice tribunalOffice) {
        var listItems = venueService.getVenues(tribunalOffice);
        return DynamicFixedListType.from(listItems);
    }
}
