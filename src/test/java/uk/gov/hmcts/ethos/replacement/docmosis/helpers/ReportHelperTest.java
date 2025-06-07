package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRANSFERRED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.generateListingDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.generateSubmitEventList;

class ReportHelperTest {

    @Test
    void testLiveCaseloadGetLocalReportDetailsForAllOfTheOffices() throws Exception {
        ListingDetails listingDetails = generateListingDetails("listingDetailsTest5.json");
        List<SubmitEvent> submitEvents = generateSubmitEventList("submitEvents1.json");
        ListingData listingData = ReportHelper.processLiveCaseloadRequest(listingDetails, submitEvents);
        List<String> expected = List.of("Edinburgh", "Glasgow", "Dundee", "Aberdeen", "London Central");
        List<String> offices = listingData.getLocalReportsDetail()
                .stream()
                .map(lrd -> lrd.getValue().getReportOffice())
                .filter(Objects::nonNull)
                .toList();
        assertEquals(expected, offices);
    }

    @Test
    void testLiveCaseloadGetLocalReportDetailsForEngWales() throws Exception {
        ListingDetails listingDetailsEngWales = generateListingDetails("listingDetailsTest5.json");
        listingDetailsEngWales.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        List<SubmitEvent> submitEvents = generateSubmitEventList("submitEvents1.json");
        ListingData listingData = ReportHelper.processLiveCaseloadRequest(listingDetailsEngWales, submitEvents);
        List<String> expected = List.of("EngWales");
        List<String> offices = listingData.getLocalReportsDetail()
                .stream()
                .map(lrd -> lrd.getValue().getFileLocation())
                .filter(Objects::nonNull)
                .toList();
        assertEquals(expected, offices);
    }

    @ParameterizedTest
    @MethodSource("testLiveCaseloadShowsReportOfficeWithEmptyReport")
    void testLiveCaseloadShowsReportOfficeWithEmptyReport(String caseTypeId, String managingOffice,
                                                          String expectedReportOffice) {
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(caseTypeId);
        listingDetails.setCaseData(new ListingData());
        listingDetails.getCaseData().setManagingOffice(managingOffice);

        ListingData listingData = ReportHelper.processLiveCaseloadRequest(listingDetails, Collections.emptyList());
        assertEquals(expectedReportOffice, listingData.getLocalReportsDetailHdr().getReportOffice());
    }

    private static Stream<Arguments> testLiveCaseloadShowsReportOfficeWithEmptyReport() { //NOPMD - parameterized tests
        return Stream.of(
                Arguments.of(ENGLANDWALES_LISTING_CASE_TYPE_ID, TribunalOffice.MANCHESTER.getOfficeName(),
                        TribunalOffice.MANCHESTER.getOfficeName()),
                Arguments.of(SCOTLAND_LISTING_CASE_TYPE_ID, null,
                        TribunalOffice.SCOTLAND.getOfficeName())
        );
    }

    @Test
    void testProcessClaimsAcceptedRequest_WithMatchingPreAcceptDate() throws Exception {
        ListingDetails listingDetails = generateListingDetails("listingDetailsTest5.json");
        List<SubmitEvent> submitEvents = generateSubmitEventList("submitEvents1.json");
        ListingData result = ReportHelper.processClaimsAcceptedRequest(listingDetails, submitEvents);
        assertEquals("5", result.getLocalReportsDetailHdr().getTotal());
        assertEquals("5", result.getLocalReportsDetailHdr().getSinglesTotal());
        assertEquals("0", result.getLocalReportsDetailHdr().getMultiplesTotal());
        assertEquals(5, result.getLocalReportsDetail().size());
    }

    @Test
    void testProcessClaimsAcceptedRequest_WithTransferredState() throws Exception {
        ListingDetails listingDetails = generateListingDetails("listingDetailsTest5.json");
        List<SubmitEvent> submitEvents = generateSubmitEventList("submitEvents1.json");
        submitEvents.get(0).setState(TRANSFERRED_STATE);
        ListingData result = ReportHelper.processClaimsAcceptedRequest(listingDetails, submitEvents);
        assertEquals("4", result.getLocalReportsDetailHdr().getTotal());
        assertEquals("4", result.getLocalReportsDetailHdr().getSinglesTotal());
        assertEquals("0", result.getLocalReportsDetailHdr().getMultiplesTotal());
        assertEquals(4, result.getLocalReportsDetail().size());
    }
}
