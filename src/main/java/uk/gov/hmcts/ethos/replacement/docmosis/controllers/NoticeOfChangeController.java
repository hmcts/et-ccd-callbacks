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
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.generic.GenericCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.io.IOException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@RequestMapping("/noc-decision")
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeController {
    private final VerifyTokenService verifyTokenService;
    private final RespondentRepresentativeService respondentRepresentativeService;
    private final CcdCaseAssignment ccdCaseAssignment;
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String APPLY_NOC_DECISION = "applyNocDecision";

    @PostMapping("/about-to-submit")
    public CCDCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest,
                                                      @RequestHeader("Authorization")
                                                      String userToken) throws IOException {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
        }

        CaseData caseData =
            respondentRepresentativeService
                .updateRepresentation(callbackRequest.getCaseDetails());

        callbackRequest.getCaseDetails().setCaseData(caseData);

        return ccdCaseAssignment.applyNoc(callbackRequest, userToken);

    }

    @PostMapping(value = "/update-respondents", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "noc decision update")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> updateNocRespondents(@RequestBody CCDRequest callbackRequest,
                                                                    @RequestHeader("Authorization")
                                                                    String userToken) {
        log.info("START OF INITIAL CONSIDERATION FOR CASE ---> {}", callbackRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = callbackRequest.getCaseDetails().getCaseData();

        log.info("Update after NOC call");

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "noc decision update")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public GenericCallbackResponse nocSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                @RequestHeader("Authorization")
                                                String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
        }

        GenericCallbackResponse callbackResponse = new GenericCallbackResponse();

        if (APPLY_NOC_DECISION.equals(callbackRequest.getEventId())) {
            //sendNotification(callback);
            String caseReference = callbackRequest.getCaseDetails().getCaseData().getEthosCaseReference();

            callbackResponse.setConfirmation_header(
                "# You're now representing a client on case " + caseReference
            );
        }

        return callbackResponse;
    }
}