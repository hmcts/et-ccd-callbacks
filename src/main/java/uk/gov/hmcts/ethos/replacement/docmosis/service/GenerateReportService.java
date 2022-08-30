package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import static uk.gov.hmcts.et.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;

@Service
public class GenerateReportService {

    private final ClerkService clerkService;

    public GenerateReportService(ClerkService clerkService) {
        this.clerkService = clerkService;
    }

    public void initGenerateReport(ListingDetails listingDetails) {
        if (BROUGHT_FORWARD_REPORT.equals(listingDetails.getCaseData().getReportType())) {
            clerkService.initialiseClerkResponsible(listingDetails.getCaseTypeId(), listingDetails.getCaseData());
        }
    }
}
