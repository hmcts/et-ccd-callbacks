package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFileService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class CreateDcfDcfAsyncAboutToSubmitCallbackHandler extends CallbackHandlerBase {

    private final DigitalCaseFileService digitalCaseFileService;

    @Autowired
    public CreateDcfDcfAsyncAboutToSubmitCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        DigitalCaseFileService digitalCaseFileService
    ) {
        super(caseDetailsConverter);
        this.digitalCaseFileService = digitalCaseFileService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("createDcf");
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
        var ccdRequest = toCcdRequest(caseDetails);
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        digitalCaseFileService.createUploadRemoveDcf(authorizationToken, ccdRequest.getCaseDetails());
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }
}
