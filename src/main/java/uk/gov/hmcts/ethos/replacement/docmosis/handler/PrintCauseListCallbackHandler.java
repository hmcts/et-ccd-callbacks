package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.letters.InvalidCharacterCheck;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.ListingGenerationCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
@Component
public class PrintCauseListCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final ListingService listingService;
    private final VerifyTokenService verifyTokenService;
    private final ListingGenerationCallbackService listingGenerationCallbackService;

    @Autowired
    public PrintCauseListCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ListingService listingService,
        VerifyTokenService verifyTokenService,
        ListingGenerationCallbackService listingGenerationCallbackService
    ) {
        super(caseDetailsConverter);
        this.listingService = listingService;
        this.verifyTokenService = verifyTokenService;
        this.listingGenerationCallbackService = listingGenerationCallbackService;
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = toListingRequest(caseDetails);
        log.info("GENERATE HEARING DOCUMENT ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            log.error(INVALID_TOKEN, authorizationToken);
            return toCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        ListingData listingData = request.getCaseDetails().getCaseData();
        String caseTypeId = request.getCaseDetails().getCaseTypeId();

        List<String> errorsList = new ArrayList<>();
        boolean invalidCharsExist = InvalidCharacterCheck.invalidCharactersExistAllListingTypes(
            request.getCaseDetails(),
            errorsList
        );

        if (!invalidCharsExist && !hasListings(listingData)) {
            errorsList.add("No cases with hearings have been found for your search criteria");
        }

        if (errorsList.isEmpty()) {
            DocumentInfo documentInfo = listingService.processHearingDocument(
                listingData,
                caseTypeId,
                authorizationToken
            );
            listingData.setDocMarkUp(documentInfo.getMarkUp());
            return toCallbackResponse(ResponseEntity.ok(ListingCallbackResponse.builder()
                .data(listingData)
                .significant_item(Helper.generateSignificantItem(documentInfo, errorsList))
                .build()));
        }

        return toCallbackResponse(ResponseEntity.ok(ListingCallbackResponse.builder()
            .errors(errorsList)
            .data(listingData)
            .build()));
    }

    private boolean hasListings(ListingData listingData) {
        return CollectionUtils.isNotEmpty(listingData.getListingCollection());
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(listingGenerationCallbackService.generateHearingDocumentConfirmation(
            toListingRequest(caseDetails),
            authorizationToken
        ));
    }
}
