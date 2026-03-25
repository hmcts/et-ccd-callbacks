package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getListingCallbackRespEntityErrors;

@Slf4j
@Component
public class GenerateListingCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final ListingService listingService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final VerifyTokenService verifyTokenService;

    @Autowired
    public GenerateListingCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ListingService listingService,
        DefaultValuesReaderService defaultValuesReaderService,
        VerifyTokenService verifyTokenService
    ) {
        super(caseDetailsConverter);
        this.listingService = listingService;
        this.defaultValuesReaderService = defaultValuesReaderService;
        this.verifyTokenService = verifyTokenService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Listings", "ET_EnglandWales_Listings");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("generateListing");
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = toListingRequest(caseDetails);
        log.info("LISTING HEARINGS ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            log.error(INVALID_TOKEN, authorizationToken);
            return toCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        List<String> errors = new ArrayList<>();
        ListingData listingData = request.getCaseDetails().getCaseData();

        if (ListingHelper.isListingRangeValid(listingData, errors)) {
            listingData = listingService.processListingHearingsRequest(request.getCaseDetails(), authorizationToken);
            DefaultValues defaultValues = defaultValuesReaderService.getListingDefaultValues(request.getCaseDetails());
            log.info("Post Default values loaded: " + defaultValues);
            listingData = defaultValuesReaderService.getListingData(listingData, defaultValues);
        }

        return toCallbackResponse(getListingCallbackRespEntityErrors(errors, listingData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
