package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_ABERDEEN_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_DUNDEE_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_EDINBURGH_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_GLASGOW_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.helpers.ESHelper.LISTING_VENUE_FIELD_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ALL_VENUES;

@SuppressWarnings({"PMD.UseProperClassLoader", "PMD.LawOfDemeter", "PMD.UnusedPrivateMethod" })
class ListingVenueHelperTest {

    private static final String VENUE_NAME = "Test Venue";

    @ParameterizedTest
    @MethodSource
    void testGetListingVenue(ListingData listingData, String expected) {
        assertEquals(expected, ListingVenueHelper.getListingVenue(listingData));
    }

    private static Stream<Arguments> testGetListingVenue() {
        return Stream.of(
                Arguments.of(createEnglandWales(VENUE_NAME), VENUE_NAME),
                Arguments.of(createEnglandWales(ALL_VENUES), ALL_VENUES),
                Arguments.of(createEnglandWales(null), ""),
                Arguments.of(createAberdeen(VENUE_NAME), VENUE_NAME),
                Arguments.of(createAberdeen(ALL_VENUES), ""),
                Arguments.of(createDundee(VENUE_NAME), VENUE_NAME),
                Arguments.of(createDundee(ALL_VENUES), ""),
                Arguments.of(createEdinburgh(VENUE_NAME), VENUE_NAME),
                Arguments.of(createEdinburgh(ALL_VENUES), ""),
                Arguments.of(createGlasgow(VENUE_NAME), VENUE_NAME),
                Arguments.of(createGlasgow(ALL_VENUES), "")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGetListingVenueLabels(ListingData listingData, String expected) {
        assertEquals(expected, ListingVenueHelper.getListingVenueLabel(listingData));
    }

    private static Stream<Arguments> testGetListingVenueLabels() {
        return Stream.of(
                Arguments.of(createEnglandWales(VENUE_NAME), VENUE_NAME),
                Arguments.of(createEnglandWales(ALL_VENUES), ALL_VENUES),
                Arguments.of(createEnglandWales(null), ""),
                Arguments.of(createAberdeen(VENUE_NAME), VENUE_NAME),
                Arguments.of(createAberdeen(ALL_VENUES), ""),
                Arguments.of(createDundee(VENUE_NAME), VENUE_NAME),
                Arguments.of(createDundee(ALL_VENUES), ""),
                Arguments.of(createEdinburgh(VENUE_NAME), VENUE_NAME),
                Arguments.of(createEdinburgh(ALL_VENUES), ""),
                Arguments.of(createGlasgow(VENUE_NAME), VENUE_NAME),
                Arguments.of(createGlasgow(ALL_VENUES), "")
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGetListingVenueToSearch(ListingData listingData, Map<String, String> expected) {
        assertEquals(expected, ListingVenueHelper.getListingVenueToSearch(listingData));
    }

    private static Stream<Arguments> testGetListingVenueToSearch() {
        return Stream.of(
                Arguments.of(createAberdeen(VENUE_NAME), Map.of(LISTING_ABERDEEN_VENUE_FIELD_NAME, VENUE_NAME)),
                Arguments.of(createAberdeen(ALL_VENUES), Map.of("", "")),
                Arguments.of(createDundee(VENUE_NAME), Map.of(LISTING_DUNDEE_VENUE_FIELD_NAME, VENUE_NAME)),
                Arguments.of(createDundee(ALL_VENUES), Map.of("", "")),
                Arguments.of(createEdinburgh(VENUE_NAME), Map.of(LISTING_EDINBURGH_VENUE_FIELD_NAME, VENUE_NAME)),
                Arguments.of(createEdinburgh(ALL_VENUES), Map.of("", "")),
                Arguments.of(createGlasgow(VENUE_NAME), Map.of(LISTING_GLASGOW_VENUE_FIELD_NAME, VENUE_NAME)),
                Arguments.of(createGlasgow(ALL_VENUES), Map.of("", "")),
                Arguments.of(createEnglandWales(VENUE_NAME), Map.of(LISTING_VENUE_FIELD_NAME, VENUE_NAME)),
                Arguments.of(createEnglandWales(ALL_VENUES), Map.of(ALL_VENUES, ALL_VENUES))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testIsAllScottishVenues(ListingData listingData, boolean expected) {
        assertEquals(expected, ListingVenueHelper.isAllScottishVenues(listingData));
    }

    private static Stream<Arguments> testIsAllScottishVenues() {
        return Stream.of(
            Arguments.of(createAberdeen(VENUE_NAME), false),
            Arguments.of(createAberdeen(ALL_VENUES), true),
            Arguments.of(createDundee(VENUE_NAME), false),
            Arguments.of(createDundee(ALL_VENUES), true),
            Arguments.of(createEdinburgh(VENUE_NAME), false),
            Arguments.of(createEdinburgh(ALL_VENUES), true),
            Arguments.of(createGlasgow(VENUE_NAME), false),
            Arguments.of(createGlasgow(ALL_VENUES), true),
            Arguments.of(createEnglandWales(VENUE_NAME), false),
            Arguments.of(createEnglandWales(ALL_VENUES), false)
        );
    }

    private static ListingData createEnglandWales(String venue) {
        var listingData = new ListingData();
        if (venue != null) {
            listingData.setListingVenue(new DynamicFixedListType(venue));
        }
        return listingData;
    }

    private static ListingData createAberdeen(String venue) {
        var listingData = new ListingData();
        listingData.setVenueAberdeen(new DynamicFixedListType(venue));
        return listingData;
    }

    private static ListingData createDundee(String venue) {
        var listingData = new ListingData();
        listingData.setVenueDundee(new DynamicFixedListType(venue));
        return listingData;
    }

    private static ListingData createEdinburgh(String venue) {
        var listingData = new ListingData();
        listingData.setVenueEdinburgh(new DynamicFixedListType(venue));
        return listingData;
    }

    private static ListingData createGlasgow(String venue) {
        var listingData = new ListingData();
        listingData.setVenueGlasgow(new DynamicFixedListType(venue));
        return listingData;
    }

}
