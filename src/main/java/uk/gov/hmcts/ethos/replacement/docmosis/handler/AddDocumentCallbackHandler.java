package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

@Component
public class AddDocumentCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final DocumentManagementService documentManagementService;

    @Autowired
    public AddDocumentCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        DocumentManagementService documentManagementService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.documentManagementService = documentManagementService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addDocument");
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
        CCDRequest ccdRequest = toCcdRequest(caseDetails);

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toCallbackResponse(ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build());
        }

        var caseData = ccdRequest.getCaseDetails().getCaseData();
        try {
            documentManagementService.addUploadedDocsToCaseDocCollection(caseData);
            caseData.getAddDocumentCollection().clear();
            return toCallbackResponse(CallbackRespHelper.getCallbackRespEntityNoErrors(caseData));
        } catch (Exception exception) {
            return toCallbackResponse(CallbackRespHelper.getCallbackRespEntityErrors(
                List.of(exception.getMessage()),
                caseData
            ));
        }
    }
}
