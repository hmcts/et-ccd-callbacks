package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferSameCountryService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Component
public class CaseTransferSameCountryEccLinkedCaseCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final CaseTransferSameCountryService caseTransferSameCountryService;

    @Autowired
    public CaseTransferSameCountryEccLinkedCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        CaseTransferSameCountryService caseTransferSameCountryService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.caseTransferSameCountryService = caseTransferSameCountryService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("caseTransferSameCountryEccLinkedCase");
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
        return toCallbackResponse(transferSameCountryEccLinkedCase(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }

    private ResponseEntity<CCDCallbackResponse> transferSameCountryEccLinkedCase(
        uk.gov.hmcts.et.common.model.ccd.CCDRequest request,
        String userToken
    ) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferSameCountryService.updateEccLinkedCase(request.getCaseDetails(), userToken);
        request.getCaseDetails().getCaseData().setSuggestedHearingVenues(null);
        return getCallbackRespEntityErrors(errors, request.getCaseDetails().getCaseData());
    }
}
