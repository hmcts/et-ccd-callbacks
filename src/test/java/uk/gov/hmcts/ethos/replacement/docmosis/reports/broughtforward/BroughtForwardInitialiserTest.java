package uk.gov.hmcts.ethos.replacement.docmosis.reports.broughtforward;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClerkService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.et.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.et.common.model.helper.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.et.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.et.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    BroughtForwardInitialiser.class, ClerkService.class
})
class BroughtForwardInitialiserTest {

    @Autowired
    BroughtForwardInitialiser broughtForwardInitialiser;

    @Autowired
    ClerkService clerkService;

    @MockBean
    CourtWorkerService courtWorkerService;

    @Test
    void shouldAddScotlandClerks() {
        var clerks = List.of(DynamicValueType.create("clerk1", "Clerk1"));
        when(courtWorkerService.getCourtWorkerByTribunalOffice(TribunalOffice.SCOTLAND,
                CourtWorkerType.CLERK)).thenReturn(clerks);
        var listingDetails = createListingDetails(SCOTLAND_LISTING_CASE_TYPE_ID, BROUGHT_FORWARD_REPORT);

        broughtForwardInitialiser.init(listingDetails);

        assertEquals(clerks, listingDetails.getCaseData().getClerkResponsible().getListItems());
        assertNull(listingDetails.getCaseData().getClerkResponsible().getValue());
    }

    @Test
    void shouldThrowExceptionForInvalidReportType() {
        var listingDetails = createListingDetails(SCOTLAND_LISTING_CASE_TYPE_ID, CASES_AWAITING_JUDGMENT_REPORT);

        assertThrows(IllegalArgumentException.class, () -> broughtForwardInitialiser.init(listingDetails));
    }

    @Test
    void shouldIgnoreEnglandWalesCaseType() {
        var listingDetails = createListingDetails(ENGLANDWALES_LISTING_CASE_TYPE_ID, BROUGHT_FORWARD_REPORT);
        broughtForwardInitialiser.init(listingDetails);

        assertNull(listingDetails.getCaseData().getClerkResponsible());
    }

    private ListingDetails createListingDetails(String caseTypeId, String reportType) {
        var listingData = new ListingData();
        listingData.setReportType(reportType);
        var listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(caseTypeId);
        listingDetails.setCaseData(listingData);

        return listingDetails;
    }
}
