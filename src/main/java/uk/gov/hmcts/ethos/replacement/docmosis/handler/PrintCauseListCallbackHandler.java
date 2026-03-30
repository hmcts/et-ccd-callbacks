package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.ListingGenerationController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class PrintCauseListCallbackHandler extends ListingCallbackHandlerBase {

    private final ListingGenerationController listingGenerationController;

    @Autowired
    public PrintCauseListCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ListingGenerationController listingGenerationController
    ) {
        super(caseDetailsConverter);
        this.listingGenerationController = listingGenerationController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Listings", "ET_EnglandWales_Listings");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("printCauseList");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    Object aboutToSubmit(ListingRequest listingRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return listingGenerationController.generateHearingDocument(
            listingRequest,
            authorizationToken
        );
    }

    @Override
    Object submitted(ListingRequest listingRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return listingGenerationController.generateHearingDocumentConfirmation(
            listingRequest,
            authorizationToken
        );
    }
}
