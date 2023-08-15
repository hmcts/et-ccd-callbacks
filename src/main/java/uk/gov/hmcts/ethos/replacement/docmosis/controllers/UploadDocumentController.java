package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
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
public class UploadDocumentController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final String templateId;
    private final VerifyTokenService verifyTokenService;
    private final EmailService emailService;

    public UploadDocumentController(@Value("${template.rejected}") String templateId,
                                    VerifyTokenService verifyTokenService,
                                    EmailService emailService) {
        this.templateId = templateId;
        this.emailService = emailService;
        this.verifyTokenService = verifyTokenService;
    }

    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Convert's the legacy style of docs into the new doc naming system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called at the end of Upload Document event, conditionally sends an email if the case is rejected and a
     * rejection document has been uploaded.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Sends an email if the case is rejected and a rejection document has been uploaded")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitReferralReply(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();

        UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
        if (UploadDocumentHelper.shouldSendRejectionEmail(caseDetails)) {
            String citizenCaseLink = emailService.getCitizenCaseLink(caseDetails.getCaseId());
            emailService.sendEmail(templateId, caseData.getClaimantType().getClaimantEmailAddress(),
                UploadDocumentHelper.buildPersonalisationForCaseRejection(caseData, citizenCaseLink));
            caseData.setCaseRejectedEmailSent(YES);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }
}
