package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class PartyUnavailabilityCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;

    @Autowired
    public PartyUnavailabilityCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("partyUnavailability");
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
        CCDRequest ccdRequest = toCcdRequest(caseDetails);
        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toCallbackResponse(ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build());
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setPartySelection(null);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        CCDRequest ccdRequest = toCcdRequest(caseDetails);
        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toSubmittedCallbackResponse(ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build());
        }
        String header = "<h1>Unavailability dates added</h1>";
        return toSubmittedCallbackResponse(ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_header(header)
                .build()
        ));
    }
}
