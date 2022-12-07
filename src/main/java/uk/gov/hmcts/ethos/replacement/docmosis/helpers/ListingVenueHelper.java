package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.listing.ListingData;

import java.util.ArrayList;
import java.util.Map;

import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_ABERDEEN_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_DUNDEE_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_EDINBURGH_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_GLASGOW_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ALL_VENUES;

@Slf4j
@SuppressWarnings({"PMD.ConfusingTernary"})
public final class ListingVenueHelper {

    private ListingVenueHelper() {
        // All access through static methods
    }

    public static String getListingVenue(ListingData listingData) {
        Map<String, String> venueToSearchMap = getListingVenueToSearch(listingData);
        return venueToSearchMap.entrySet().iterator().next().getValue();
    }

    public static Map<String, String> getListingVenueToSearch(ListingData listingData) {
        if (listingData.hasListingVenue() && ALL_VENUES.equals(listingData.getListingVenue().getSelectedCode())) {
            return Map.of(ALL_VENUES, ALL_VENUES);
        } else {
            return getVenueToSearch(listingData);
        }
    }

    public static Map<String, String> getVenueToSearch(ListingData listingData) {
        if (isNotAllVenuesValue(listingData.getVenueGlasgow())) {
            return Map.of(LISTING_GLASGOW_VENUE_FIELD_NAME, listingData.getVenueGlasgow().getSelectedCode());
        } else if (isNotAllVenuesValue(listingData.getVenueAberdeen())) {
            return Map.of(LISTING_ABERDEEN_VENUE_FIELD_NAME, listingData.getVenueAberdeen().getSelectedCode());
        } else if (isNotAllVenuesValue(listingData.getVenueDundee())) {
            return Map.of(LISTING_DUNDEE_VENUE_FIELD_NAME, listingData.getVenueDundee().getSelectedCode());
        } else if (isNotAllVenuesValue(listingData.getVenueEdinburgh())) {
            return Map.of(LISTING_EDINBURGH_VENUE_FIELD_NAME, listingData.getVenueEdinburgh().getSelectedCode());
        } else if (listingData.hasListingVenue()) {
            return Map.of(LISTING_VENUE_FIELD_NAME, listingData.getListingVenue().getSelectedCode());
        } else {
            return Map.of("", "");
        }
    }

    /**
     * Returns Venue label from listing venue dynamic fixed list Item.
     * This returns the dynamicFixedList labels for the hearingVenues
     * with the ones which needs to be in report.
     * @param listingData print hearing details from the case data
     */
    public static String getListingVenueLabel(ListingData listingData) {
        Map<String, String> venueToSearchMap = getListingVenueLabelToSearch(listingData);
        return venueToSearchMap.entrySet().iterator().next().getValue();
    }

    private static Map<String, String> getListingVenueLabelToSearch(ListingData listingData) {
        try {
            if (listingData.hasListingVenue() && ALL_VENUES.equals(listingData.getListingVenue().getSelectedLabel())) {
                return Map.of(ALL_VENUES, ALL_VENUES);
            } else {
                return getVenueLabelToSearch(listingData);
            }
        } catch (IllegalStateException ex) {
            log.error("Unable to find venue", ex);
            return Map.of("", "");

        }
    }

    private static Map<String, String> getVenueLabelToSearch(ListingData listingData) {
        if (isNotAllVenuesValue(listingData.getVenueGlasgow())) {
            return Map.of(LISTING_GLASGOW_VENUE_FIELD_NAME, listingData.getVenueGlasgow().getSelectedLabel());
        } else if (isNotAllVenuesValue(listingData.getVenueAberdeen())) {
            return Map.of(LISTING_ABERDEEN_VENUE_FIELD_NAME, listingData.getVenueAberdeen().getSelectedLabel());
        } else if (isNotAllVenuesValue(listingData.getVenueDundee())) {
            return Map.of(LISTING_DUNDEE_VENUE_FIELD_NAME, listingData.getVenueDundee().getSelectedLabel());
        } else if (isNotAllVenuesValue(listingData.getVenueEdinburgh())) {
            return Map.of(LISTING_EDINBURGH_VENUE_FIELD_NAME, listingData.getVenueEdinburgh().getSelectedLabel());
        } else if (listingData.hasListingVenue()) {
            return Map.of(LISTING_VENUE_FIELD_NAME, listingData.getListingVenue().getSelectedLabel());
        } else {
            throw new IllegalStateException();
        }
    }

    public static boolean isAllScottishVenues(ListingData listingData) {
        var venues = new ArrayList<DynamicFixedListType>();
        venues.add(listingData.getVenueGlasgow());
        venues.add(listingData.getVenueAberdeen());
        venues.add(listingData.getVenueDundee());
        venues.add(listingData.getVenueEdinburgh());
        return venues.parallelStream().anyMatch(ListingVenueHelper::isAllVenuesValue);
    }

    private static boolean isNotAllVenuesValue(DynamicFixedListType dynamicFixedListType) {
        if (dynamicFixedListType != null && dynamicFixedListType.getValue() != null) {
            var selectedValue = dynamicFixedListType.getSelectedCode();
            return !ALL_VENUES.equals(selectedValue);
        } else {
            return false;
        }
    }

    private static boolean isAllVenuesValue(DynamicFixedListType dynamicFixedListType) {
        if (dynamicFixedListType != null && dynamicFixedListType.getValue() != null) {
            var selectedValue = dynamicFixedListType.getSelectedCode();
            return ALL_VENUES.equals(selectedValue);
        } else {
            return false;
        }
    }
}
