package uk.gov.hmcts.ethos.replacement.docmosis.reports.broughtforward;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClerkService;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;

@Service
public class BroughtForwardInitialiser {

    private final ClerkService clerkService;

    public BroughtForwardInitialiser(ClerkService clerkService) {
        this.clerkService = clerkService;
    }

    public void init(ListingDetails listingDetails) {
        String reportType = listingDetails.getCaseData().getReportType();
        if (!BROUGHT_FORWARD_REPORT.equals(reportType)) {
            throw new IllegalArgumentException("Unexpected report type " + reportType);
        }

        String caseTypeId = listingDetails.getCaseTypeId();
        if (Constants.SCOTLAND_LISTING_CASE_TYPE_ID.equals(caseTypeId)) {
            clerkService.initialiseClerkResponsible(caseTypeId, listingDetails.getCaseData());
        }
    }
}
