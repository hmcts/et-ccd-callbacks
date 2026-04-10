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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveRepresentationService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/nocRemoveRepresentation")
@RestController
@RequiredArgsConstructor
public class NocRemoveRepresentationController {

    private static final String GREEN_BANNER_HEADING = "<h1>Notice of change successful</h1>";
    private static final String GREEN_BANNER_TEXT_TEMPLATE =
        "<h5>Your organisation is no longer representing a client on %s</h5><br>";
    private static final String CONFIRM_HEADING = "<h3>What happens next</h3>";
    private static final String CONFIRM_TEXT_1 = "<p>A notification will be sent to all the parties on this case.</p>";
    private static final String CONFIRM_TEXT_2 =
        "<p>This case will no longer appear in your organisation's case list.</p>";
    private static final String CONFIRM_TEXT_3 = "<p>This is a new online process - "
        + "you don't need to file any further documents in relation to this notice of change with the court.</p>";

    private final NocRemoveRepresentationService nocRemoveRepresentationService;

    @PostMapping(value = "/claimant/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRemoveRep claimant about to submit page")
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
        nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails, userToken);
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/respondent/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRemoveRep respondent about to start page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStartRespondent(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        caseData.setNocRemoveRepIsMoreThanOneFlag(
            nocRemoveRepresentationService.isMoreThanOneRespondent(caseDetails, userToken));
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/respondent/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRemoveRep respondent about to submit page")
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
        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, userToken);
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRemoveRep submitted page")
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

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        caseData.setNocRemoveRepIsMoreThanOneFlag(null);
        caseData.setNocRemoveRepOption(null);
        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(caseData)
            .confirmation_header(GREEN_BANNER_HEADING
                + String.format(GREEN_BANNER_TEXT_TEMPLATE, caseDetails.getCaseId()))
            .confirmation_body(CONFIRM_HEADING + CONFIRM_TEXT_1 + CONFIRM_TEXT_2 + CONFIRM_TEXT_3)
            .build());
    }
}
