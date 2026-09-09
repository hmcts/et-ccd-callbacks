package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BundlesCallbackHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesClaimantService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RequestMapping("/bundlesClaimant")
@RestController
@RequiredArgsConstructor
public class BundlesClaimantController {

    private final BundlesClaimantService bundlesClaimantService;
    private final SendNotificationService sendNotificationService;

    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for bundles claimant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setBundlesClaimantPrepareDocNotesShow(YES);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "About to Submit for bundles claimant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        bundlesClaimantService.addToBundlesCollection(caseData);
        bundlesClaimantService.clearInputData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/midPopulateHearings", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the hearing list on page 3.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midPopulateHearings(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        bundlesClaimantService.populateSelectHearings(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/midValidateUpload", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Validates the uploaded file is a PDF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midValidateUpload(@RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = bundlesClaimantService.validateFileUpload(caseData);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Renders data for the submitted page.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitted(@RequestBody CCDRequest ccdRequest) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        sendNotificationService.notifyClaimantBundlesSubmitted(caseDetails);
        return BundlesCallbackHelper.buildSubmittedResponse(ccdRequest);
    }
}
