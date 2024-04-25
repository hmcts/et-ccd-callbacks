package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;

/**
 * REST controller for the Upload Document event page on a multiple.
 */
@Slf4j
@RequestMapping("/multiples/uploadDocument")
@RestController
public class MultipleUploadDocumentController {

    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    public MultipleUploadDocumentController(
                                    CaseManagementForCaseWorkerService caseManagementForCaseWorkerService) {
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Convert's the legacy style of docs into the new doc naming system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToStart(
            @RequestBody MultipleRequest ccdRequest) {

        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
        UploadDocumentHelper.setMultipleDocumentCollection(caseData);
        UploadDocumentHelper.setMultipleDocumentsToCorrectTab(caseData);

        return multipleResponse(caseData, null);
    }

    /**
     * Called at the end of Upload Document event.
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
             content = {
                 @Content(mediaType = "application/json",
                      schema = @Schema(implementation = MultipleCallbackResponse.class))
             }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitReferralReply(
            @RequestBody MultipleRequest ccdRequest) {

        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();

        UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
        caseManagementForCaseWorkerService.addClaimantDocuments(caseData);

        return multipleResponse(caseData, null);
    }
}
