package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesDigitalCaseFileService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper.addDcfToDocumentCollection;

@Component
public class CreateDcfMultiplesDcfCallbackHandler extends CallbackHandlerBase {

    private final MultiplesDigitalCaseFileService multiplesDigitalCaseFileService;

    @Autowired
    public CreateDcfMultiplesDcfCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        MultiplesDigitalCaseFileService multiplesDigitalCaseFileService
    ) {
        super(caseDetailsConverter);
        this.multiplesDigitalCaseFileService = multiplesDigitalCaseFileService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
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
        var multipleRequest = toMultipleRequest(caseDetails);
        var multipleData = multipleRequest.getCaseDetails().getCaseData();
        multipleData.setCaseBundles(multiplesDigitalCaseFileService.stitchCaseFile(
            multipleRequest.getCaseDetails(),
            authorizationToken
        ));
        addDcfToDocumentCollection(multipleData);
        multipleData.setCaseBundles(null);
        return toCallbackResponse(multipleResponse(multipleData, null));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
