package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClaimantTellSomethingElseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseService;

import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/claimantTSE")
public class ClaimantTellSomethingElseController {

    private final ClaimantTellSomethingElseService claimantTseService;
    private final TseService tseService;

    /**
     * Callback endpoint to be called when the event ClaimantTSE is about to start.
     *
     * @param ccdRequest the request
     * @return Callback response entity
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStartClaimantTSE(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (Boolean.FALSE.equals(Helper.isRespondentSystemUser(caseData))) {
            caseData.setClaimantTseRespNotAvailable(YES);
        }
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * This service is for validate Give Details are not all blank.
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     *
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/validateGiveDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Claimant Tell Something Else About To Start Event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateGiveDetails(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = claimantTseService.validateGiveDetails(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitClaimantTSE(
            @RequestBody CCDRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();

        return getCallbackRespEntityNoErrors(caseData);
    }
}
