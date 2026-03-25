package uk.gov.hmcts.ethos.replacement.docmosis.service.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingGenerationCallbackService {

    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private static final String GENERATED_DOCUMENT_URL = "Please download the document from : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;

    public ResponseEntity<ListingCallbackResponse> generateHearingDocumentConfirmation(
        ListingRequest request,
        String userToken
    ) {
        log.info("GENERATE HEARING DOCUMENT CONFIRMATION ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(ListingCallbackResponse.builder()
            .data(request.getCaseDetails().getCaseData())
            .confirmation_header(GENERATED_DOCUMENT_URL + request.getCaseDetails().getCaseData().getDocMarkUp())
            .build());
    }
}
