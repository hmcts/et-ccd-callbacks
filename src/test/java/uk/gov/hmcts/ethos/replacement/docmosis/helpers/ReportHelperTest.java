package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.generateListingDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.generateSubmitEventList;

class ReportHelperTest {
    private ListingDetails listingDetails;
    private ListingDetails listingDetailsEngWales;
    private List<SubmitEvent> submitEvents;

    @BeforeEach
    void setUp() throws Exception {
        listingDetails = generateListingDetails("listingDetailsTest5.json");
        listingDetailsEngWales = generateListingDetails("listingDetailsTest5.json");
        listingDetailsEngWales.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        submitEvents = generateSubmitEventList("submitEvents1.json");
    }

    @Test
    void getLocalReportDetailsForAllOfTheOffices() {
        ListingData listingData = ReportHelper.processLiveCaseloadRequest(listingDetails, submitEvents);
        var expected = List.of("Edinburgh", "Glasgow", "Dundee", "Aberdeen", "London Central");
        var offices = listingData.getLocalReportsDetail()
                .stream()
                .map(lrd -> lrd.getValue().getReportOffice())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        assertEquals(expected, offices);
    }

    @Test
    void getLocalReportDetailsForEngWales() {
        ListingData listingData = ReportHelper.processLiveCaseloadRequest(listingDetailsEngWales, submitEvents);
        var expected = List.of("EngWales");
        var offices = listingData.getLocalReportsDetail()
                .stream()
                .map(lrd -> lrd.getValue().getFileLocation())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        assertEquals(expected, offices);
    }
}
