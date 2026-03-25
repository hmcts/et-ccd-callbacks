package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
@Component
public class PrintHearingCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private static final String GENERATED_DOCUMENT_URL = "Please download the document from : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final ListingService listingService;
    private final VerifyTokenService verifyTokenService;

    @Autowired
    public PrintHearingCallbackHandler(
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
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("printHearing");
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
        var request = toCcdRequest(caseDetails);
        log.info("GENERATE LISTINGS DOC SINGLE CASES ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            log.error(INVALID_TOKEN, authorizationToken);
            return toCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        List<String> errors = new ArrayList<>();
        var caseData = request.getCaseDetails().getCaseData();
        var listingData = caseData.getPrintHearingCollection();

        if (listingData.getListingCollection() != null && !listingData.getListingCollection().isEmpty()) {
            listingData = listingService.setManagingOfficeAndCourtAddressFromCaseData(caseData);
            DocumentInfo documentInfo = listingService.processHearingDocument(
                listingData,
                request.getCaseDetails().getCaseTypeId(),
                authorizationToken
            );
            caseData.setDocMarkUp(documentInfo.getMarkUp());
            return toCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(caseData)
                .significant_item(Helper.generateSignificantItem(documentInfo, errors))
                .build()));
        }

        errors.add("No hearings have been found for your search criteria");
        return toCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .errors(errors)
            .data(caseData)
            .build()));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = toCcdRequest(caseDetails);
        log.info("GENERATE LISTINGS DOC SINGLE CASES CONFIRMATION ---> "
            + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            log.error(INVALID_TOKEN, authorizationToken);
            return toSubmittedCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(request.getCaseDetails().getCaseData())
            .confirmation_header(GENERATED_DOCUMENT_URL + request.getCaseDetails().getCaseData().getDocMarkUp())
            .build()));
    }
}
