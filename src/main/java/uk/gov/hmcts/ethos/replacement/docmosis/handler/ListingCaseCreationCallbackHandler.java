package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
@Component
public class ListingCaseCreationCallbackHandler extends ListingCallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final ListingService listingService;
    private final VerifyTokenService verifyTokenService;

    @Autowired
    public ListingCaseCreationCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ListingService listingService,
        VerifyTokenService verifyTokenService
    ) {
        super(caseDetailsConverter);
        this.listingService = listingService;
        this.verifyTokenService = verifyTokenService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Listings", "ET_EnglandWales_Listings");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("createCase", "createReport");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return false;
    }

    @Override
    Object aboutToSubmit(ListingRequest listingRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = listingRequest;
        log.info("LISTING CASE CREATION ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            log.error(INVALID_TOKEN, authorizationToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(
            uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse.builder()
                .data(listingService.listingCaseCreation(request.getCaseDetails()))
                .build()
        );
    }
}
