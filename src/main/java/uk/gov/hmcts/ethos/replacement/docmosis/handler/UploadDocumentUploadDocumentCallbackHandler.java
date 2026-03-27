package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper.buildPersonalisationForCaseRejection;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper.setDocumentTypeForDocumentCollection;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper.shouldSendRejectionEmail;

@Component
public class UploadDocumentUploadDocumentCallbackHandler extends CallbackHandlerBase {

    private final String templateId;
    private final VerifyTokenService verifyTokenService;
    private final EmailService emailService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    public UploadDocumentUploadDocumentCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        @Value("${template.rejected}") String templateId,
        VerifyTokenService verifyTokenService,
        EmailService emailService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService
    ) {
        super(caseDetailsConverter);
        this.templateId = templateId;
        this.verifyTokenService = verifyTokenService;
        this.emailService = emailService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("uploadDocument");
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

        var ccdCaseDetails = ccdRequest.getCaseDetails();
        var caseData = ccdCaseDetails.getCaseData();

        setDocumentTypeForDocumentCollection(caseData);
        caseManagementForCaseWorkerService.addClaimantDocuments(caseData);
        if (shouldSendRejectionEmail(ccdCaseDetails)) {
            String citizenCaseLink = emailService.getCitizenCaseLink(ccdCaseDetails.getCaseId());
            emailService.sendEmail(
                templateId,
                caseData.getClaimantType().getClaimantEmailAddress(),
                buildPersonalisationForCaseRejection(caseData, citizenCaseLink)
            );
            caseData.setCaseRejectedEmailSent(YES);
        }
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }
}
