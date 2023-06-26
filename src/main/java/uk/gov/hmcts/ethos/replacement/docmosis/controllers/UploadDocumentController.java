package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the Upload Document event page.
 */
@Slf4j
@RequestMapping("/uploadDocument")
@RestController
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class UploadDocumentController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final String templateId;
    private final VerifyTokenService verifyTokenService;
    private final EmailService emailService;

    public UploadDocumentController(@Value("${rejected.template.id}") String templateId,
                                    VerifyTokenService verifyTokenService,
                                    EmailService emailService) {
        this.templateId = templateId;
        this.emailService = emailService;
        this.verifyTokenService = verifyTokenService;
    }

    /**
     * Called at the end of Upload Document event, conditionally sends an email if the case is rejected and a
     * rejection document has been uploaded.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitReferralReply(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        if (UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails())) {
            emailService.sendEmail(templateId, caseData.getClaimantType().getClaimantEmailAddress(),
                UploadDocumentHelper.buildPersonalisationForCaseRejection(ccdRequest.getCaseDetails()));
            caseData.setCaseRejectedEmailSent(YES);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }
}
