package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
public class VenueSelectionService {
    private final VenueService venueService;

    public VenueSelectionService(VenueService venueService) {
        this.venueService = venueService;
    }

    public void initHearingCollection(CaseData caseData) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(caseData.getManagingOffice());
        List<DynamicValueType> venues = venueService.getVenues(tribunalOffice);
        log.info(venues.size() + " venues found for " + tribunalOffice);

        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            HearingTypeItem hearingTypeItem = new HearingTypeItem();
            hearingTypeItem.setId(UUID.randomUUID().toString());
            caseData.setHearingCollection(List.of(hearingTypeItem));

            HearingType hearingType = new HearingType();
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            dynamicFixedListType.setListItems(venues);
            hearingType.setHearingVenue(dynamicFixedListType);
            hearingTypeItem.setValue(hearingType);
        } else {
            for (HearingTypeItem hearingItemType : caseData.getHearingCollection()) {
                HearingType hearingType = hearingItemType.getValue();
                DynamicFixedListType dynamicFixedListType = hearingType.getHearingVenue();
                if (dynamicFixedListType == null) {
                    dynamicFixedListType = new DynamicFixedListType();
                    hearingType.setHearingVenue(dynamicFixedListType);
                }
                dynamicFixedListType.setListItems(venues);
            }
        }
    }

    public DynamicFixedListType createVenueSelection(TribunalOffice tribunalOffice, DateListedType selectedListing) {
        List<DynamicValueType> listItems = venueService.getVenues(tribunalOffice);
        DynamicFixedListType selectedVenue = selectedListing.getHearingVenueDay();
        return DynamicFixedListType.from(listItems, selectedVenue);
    }
}
