package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferDifferentCountryService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Component
public class MultipleTransferDifferentCountryCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final MultipleTransferDifferentCountryService multipleTransferDifferentCountryService;

    @Autowired
    public MultipleTransferDifferentCountryCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        MultipleTransferDifferentCountryService multipleTransferDifferentCountryService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.multipleTransferDifferentCountryService = multipleTransferDifferentCountryService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("multipleTransferDifferentCountry");
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
        return toCallbackResponse(transferDifferentCountry(toMultipleRequest(caseDetails), authorizationToken));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }

    private ResponseEntity<MultipleCallbackResponse> transferDifferentCountry(
        uk.gov.hmcts.et.common.model.multiples.MultipleRequest request,
        String userToken
    ) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = request.getCaseDetails();
        List<String> errors = multipleTransferDifferentCountryService.transferMultiple(multipleDetails, userToken);
        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
