package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.listing.ListingData;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

@Component
public class ListingCaseView implements CaseView<ListingData, ListingCaseState> {
    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_LISTING_CASE_TYPE_ID, SCOTLAND_LISTING_CASE_TYPE_ID);
    }

    @Override
    public ListingData getCase(CaseViewRequest<ListingCaseState> request, ListingData blobCase) {
        return blobCase;
    }
}
