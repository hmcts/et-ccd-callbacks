package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class GenerateReportServiceTest {

    @InjectMocks
    GenerateReportService generateReportService;

    @Mock
    ClerkService clerkService;

    @Test
    void testInitGenerateReportForBroughtForwardReport() {
        ListingData listingData = new ListingData();
        listingData.setReportType(BROUGHT_FORWARD_REPORT);
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.setCaseData(listingData);

        generateReportService.initGenerateReport(listingDetails);

        verify(clerkService, times(1)).initialiseClerkResponsible(SCOTLAND_LISTING_CASE_TYPE_ID, listingData);
    }
}
