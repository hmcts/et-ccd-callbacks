package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.generateListingDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader.generateSubmitEventList;

@SuppressWarnings({"PMD.UseProperClassLoader", "PMD.LawOfDemeter", "PMD.UnnecessaryFullyQualifiedName",
    "PMD.UnusedPrivateMethod"})
class ReportHelperTest {

    @Test
    void testLiveCaseloadGetLocalReportDetailsForAllOfTheOffices() throws Exception {
        var listingDetails = generateListingDetails("listingDetailsTest5.json");
        var submitEvents = generateSubmitEventList("submitEvents1.json");
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
    void testLiveCaseloadGetLocalReportDetailsForEngWales() throws Exception {
        var listingDetailsEngWales = generateListingDetails("listingDetailsTest5.json");
        listingDetailsEngWales.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        var submitEvents = generateSubmitEventList("submitEvents1.json");
        ListingData listingData = ReportHelper.processLiveCaseloadRequest(listingDetailsEngWales, submitEvents);
        var expected = List.of("EngWales");
        var offices = listingData.getLocalReportsDetail()
                .stream()
                .map(lrd -> lrd.getValue().getFileLocation())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        assertEquals(expected, offices);
    }

    @ParameterizedTest
    @MethodSource
    void testLiveCaseloadShowsReportOfficeWithEmptyReport(String caseTypeId, String managingOffice,
                                                          String expectedReportOffice) {
        var listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(caseTypeId);
        listingDetails.setCaseData(new ListingData());
        listingDetails.getCaseData().setManagingOffice(managingOffice);

        var listingData = ReportHelper.processLiveCaseloadRequest(listingDetails, Collections.emptyList());
        assertEquals(expectedReportOffice, listingData.getLocalReportsDetailHdr().getReportOffice());
    }

    private static Stream<Arguments> testLiveCaseloadShowsReportOfficeWithEmptyReport() {
        return Stream.of(
                Arguments.of(ENGLANDWALES_LISTING_CASE_TYPE_ID, TribunalOffice.MANCHESTER.getOfficeName(),
                        TribunalOffice.MANCHESTER.getOfficeName()),
                Arguments.of(SCOTLAND_LISTING_CASE_TYPE_ID, null,
                        TribunalOffice.SCOTLAND.getOfficeName())
        );
    }
}
