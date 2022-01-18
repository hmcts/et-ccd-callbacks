package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.listing.ListingData;

import java.util.ArrayList;
import java.util.Map;

import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_ABERDEEN_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_DUNDEE_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_EDINBURGH_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_GLASGOW_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ALL_VENUES;

public class ListingVenueHelper {

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
