package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles.BulkAddSinglesService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Component
public class BulkAddSingleCasesCallbackHandler extends CallbackHandlerBase {

    private final BulkAddSinglesService bulkAddSinglesService;
    private final VerifyTokenService verifyTokenService;

    @Autowired
    public BulkAddSingleCasesCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        BulkAddSinglesService bulkAddSinglesService,
        VerifyTokenService verifyTokenService
    ) {
        super(caseDetailsConverter);
        this.bulkAddSinglesService = bulkAddSinglesService;
        this.verifyTokenService = verifyTokenService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("bulkAddSingleCases");
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
        var multipleRequest = toMultipleRequest(caseDetails);

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toCallbackResponse(ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build());
        }

        var multipleDetails = multipleRequest.getCaseDetails();
        List<String> errors = bulkAddSinglesService.execute(multipleDetails, authorizationToken);
        return toCallbackResponse(getMultipleCallbackRespEntity(errors, multipleDetails));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
