package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferSameCountryService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Component
public class MultipleTransferSameCountryCallbackHandler extends MultipleCallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final MultipleTransferSameCountryService multipleTransferSameCountryService;

    @Autowired
    public MultipleTransferSameCountryCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        MultipleTransferSameCountryService multipleTransferSameCountryService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.multipleTransferSameCountryService = multipleTransferSameCountryService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("multipleTransferSameCountry");
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
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return transferSameCountry(multipleRequest, authorizationToken);
    }

    private ResponseEntity<MultipleCallbackResponse> transferSameCountry(
        uk.gov.hmcts.et.common.model.multiples.MultipleRequest request,
        String userToken
    ) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = request.getCaseDetails();
        List<String> errors = multipleTransferSameCountryService.transferMultiple(multipleDetails, userToken);
        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
