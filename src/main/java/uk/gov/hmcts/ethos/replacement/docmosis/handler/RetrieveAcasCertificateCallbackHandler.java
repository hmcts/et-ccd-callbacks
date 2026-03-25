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
import uk.gov.hmcts.ethos.replacement.docmosis.service.AcasService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Component
public class RetrieveAcasCertificateCallbackHandler extends CallbackHandlerBase {

    private static final String GENERATED_DOCUMENT_URL = "Please download the ACAS Certificate from : ";
    private final VerifyTokenService verifyTokenService;
    private final AcasService acasService;

    @Autowired
    public RetrieveAcasCertificateCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        AcasService acasService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.acasService = acasService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("retrieveAcasCertificate");
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = acasService.getAcasCertificate(
            caseData,
            authorizationToken,
            ccdRequest.getCaseDetails().getCaseTypeId()
        );
        return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        CCDRequest ccdRequest = toCcdRequest(caseDetails);
        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toSubmittedCallbackResponse(ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build());
        }

        return toSubmittedCallbackResponse(ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body(GENERATED_DOCUMENT_URL + ccdRequest.getCaseDetails().getCaseData().getDocMarkUp())
                .build()
        ));
    }
}
