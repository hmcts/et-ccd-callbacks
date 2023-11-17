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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the Issue Initial Consideration Directions Work Allocation pages.
 * Event is triggered by CaseWorkers to initiate a Work Allocation task
 * at the start of the event and after completion.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class IssueInitialConsiderationDirectionsWAController {

    private final VerifyTokenService verifyTokenService;
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String COMPLETE_IICD_HDR =
            "<h1>Issue Initial Consideration Directions for Work Allocation complete</h1>";

    @PostMapping(value = "/startIssueInitialConsiderationDirectionsWA", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "start the Issue Initial Consideration Directions WA flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> startInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                         @RequestHeader("Authorization")
                                                                         String userToken) {
        log.info("START OF ISSUE INITIAL CONSIDERATION DIRECTIONS FOR WORK ALLOCATION ---> {}",
                ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * About to Submit callback.
     * @param ccdRequest Holds the request and case data
     * @param userToken Used for authorisation
     * @return caseData in ccdRequest
     */
    @PostMapping(value = "/submitIssueInitialConsiderationDirectionsWA", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles Issue Initial Consideration Directions WA Submission")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> submitInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                          @RequestHeader("Authorization")
                                                                                  String userToken) {
        log.info("ISSUE INITIAL CONSIDERATION DIRECTIONS FOR WORK ALLOCATION ABOUT TO SUBMIT ---> {}",
                ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/completeIssueInitialConsiderationDirectionsWA", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Completes the Issue Initial Consideration Directions WA flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> completeInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                            @RequestHeader("Authorization")
                                                                            String userToken) {
        log.info("Issue Initial Consideration Directions WA complete requested for case reference ---> {}",
                ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_header(COMPLETE_IICD_HDR)
                .build());
    }
}
