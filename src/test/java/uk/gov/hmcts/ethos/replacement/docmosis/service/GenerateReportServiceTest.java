package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.et.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.et.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class GenerateReportServiceTest {

    @InjectMocks
    GenerateReportService generateReportService;

    @Mock
    ClerkService clerkService;

    @Test
    void testInitGenerateReportForBroughtForwardReport() {
        var listingData = new ListingData();
        listingData.setReportType(BROUGHT_FORWARD_REPORT);
        var listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.setCaseData(listingData);

        generateReportService.initGenerateReport(listingDetails);

        verify(clerkService, times(1)).initialiseClerkResponsible(SCOTLAND_LISTING_CASE_TYPE_ID, listingData);
    }
}
