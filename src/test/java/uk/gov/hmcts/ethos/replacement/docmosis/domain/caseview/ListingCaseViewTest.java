package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.listing.ListingData;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

class ListingCaseViewTest {

    @Test
    void returnsConfiguredCaseTypesAndCase() {
        ListingCaseView caseView = new ListingCaseView();
        ListingData listingData = new ListingData();

        assertEquals(Set.of(ENGLANDWALES_LISTING_CASE_TYPE_ID, SCOTLAND_LISTING_CASE_TYPE_ID),
            caseView.caseTypeIds());
        assertSame(listingData, caseView.getCase(null, listingData));
    }
}
