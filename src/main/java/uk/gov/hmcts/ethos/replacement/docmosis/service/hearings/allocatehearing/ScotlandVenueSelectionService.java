package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ScotlandVenueSelectionService {
    private final VenueService venueService;

    public ScotlandVenueSelectionService(VenueService venueService) {
        this.venueService = venueService;
    }

    public void initHearingCollection(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            caseData.setHearingCollection(List.of(createNewHearingTypeItem()));
        } else {
            updateHearingTypeItems(caseData.getHearingCollection());
        }
    }

    public DynamicFixedListType createVenueSelection(TribunalOffice tribunalOffice, DateListedType selectedListing) {
        var dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(venueService.getVenues(tribunalOffice));

        switch (tribunalOffice) {
            case ABERDEEN:
                if (selectedListing.hasHearingAberdeen()) {
                    dynamicFixedListType.setValue(selectedListing.getHearingAberdeen().getValue());
                }
                break;
            case DUNDEE:
                if (selectedListing.hasHearingDundee()) {
                    dynamicFixedListType.setValue(selectedListing.getHearingDundee().getValue());
                }
                break;
            case GLASGOW:
                if (selectedListing.hasHearingGlasgow()) {
                    dynamicFixedListType.setValue(selectedListing.getHearingGlasgow().getValue());
                }
                break;
            case EDINBURGH:
                if (selectedListing.hasHearingEdinburgh()) {
                    dynamicFixedListType.setValue(selectedListing.getHearingEdinburgh().getValue());
                }
                break;
            default:
                break;
        }

        return dynamicFixedListType;
    }

    private HearingTypeItem createNewHearingTypeItem() {
        var hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId(UUID.randomUUID().toString());

        var hearingType = new HearingType();
        var officeVenues = getOfficeVenues();
        hearingType.setHearingAberdeen(DynamicFixedListType.from(officeVenues.get(TribunalOffice.ABERDEEN)));
        hearingType.setHearingDundee(DynamicFixedListType.from(officeVenues.get(TribunalOffice.DUNDEE)));
        hearingType.setHearingEdinburgh(DynamicFixedListType.from(officeVenues.get(TribunalOffice.EDINBURGH)));
        hearingType.setHearingGlasgow(DynamicFixedListType.from(officeVenues.get(TribunalOffice.GLASGOW)));

        hearingTypeItem.setValue(hearingType);

        return hearingTypeItem;
    }

    private void updateHearingTypeItems(List<HearingTypeItem> hearingTypeItems) {
        var officeVenues = getOfficeVenues();

        for (var hearingItemType : hearingTypeItems) {
            var hearingType = hearingItemType.getValue();

            hearingType.setHearingAberdeen(DynamicFixedListType.from(officeVenues.get(TribunalOffice.ABERDEEN),
                    hearingType.getHearingAberdeen()));
            hearingType.setHearingDundee(DynamicFixedListType.from(officeVenues.get(TribunalOffice.DUNDEE),
                    hearingType.getHearingDundee()));
            hearingType.setHearingEdinburgh(DynamicFixedListType.from(officeVenues.get(TribunalOffice.EDINBURGH),
                    hearingType.getHearingEdinburgh()));
            hearingType.setHearingGlasgow(DynamicFixedListType.from(officeVenues.get(TribunalOffice.GLASGOW),
                    hearingType.getHearingGlasgow()));
        }
    }

    private Map<TribunalOffice, List<DynamicValueType>> getOfficeVenues() {
        EnumMap<TribunalOffice, List<DynamicValueType>> officeVenues = new EnumMap<>(TribunalOffice.class);
        officeVenues.put(TribunalOffice.ABERDEEN, venueService.getVenues(TribunalOffice.ABERDEEN));
        officeVenues.put(TribunalOffice.DUNDEE, venueService.getVenues(TribunalOffice.DUNDEE));
        officeVenues.put(TribunalOffice.EDINBURGH, venueService.getVenues(TribunalOffice.EDINBURGH));
        officeVenues.put(TribunalOffice.GLASGOW, venueService.getVenues(TribunalOffice.GLASGOW));

        return officeVenues;
    }
}
