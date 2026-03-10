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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRequestService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/nocRequest")
@RestController
@RequiredArgsConstructor
public class NocRequestController {

    private static final String GREEN_BANNER_HEADING = "<h1>Notice of change successful</h1>";
    private static final String GREEN_BANNER_TEXT = "Your organisation is no longer representing a client on ";
    private static final String CONFIRM_HEADING = "<h3>What happens next</h3>";
    private static final String CONFIRM_TEXT_1 = "<p>A notification will be sent to all the parties on this case.</p>";
    private static final String CONFIRM_TEXT_2 =
        "<p>This case will no longer appear in your organisation's case list.</p>";
    private static final String CONFIRM_TEXT_3 = "<p>This is a new online process - "
        + "you don't need to file any further documents in relation to this notice of change with the court.</p>";

    private final NocRequestService nocRequestService;

    @PostMapping(value = "/claimant/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRequest submitted page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitClaimant(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        nocRequestService.revokeClaimantLegalRep(caseDetails, userToken);
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/respondent/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRequest submitted page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondent(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        nocRequestService.revokeRespondentLegalRep(caseDetails, userToken);
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRequest submitted page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitted(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(ccdRequest.getCaseDetails().getCaseData())
            .confirmation_header(GREEN_BANNER_HEADING
                + "<h5>" + GREEN_BANNER_TEXT + ccdRequest.getCaseDetails().getCaseId() + "</h5>" + "<br>")
            .confirmation_body(CONFIRM_HEADING + CONFIRM_TEXT_1 + CONFIRM_TEXT_2 + CONFIRM_TEXT_3)
            .build());
    }
}
