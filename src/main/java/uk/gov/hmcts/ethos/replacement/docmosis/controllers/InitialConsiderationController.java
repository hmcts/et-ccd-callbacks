package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.InitialConsiderationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class InitialConsiderationController {

    private final VerifyTokenService verifyTokenService;
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String COMPLETE_IC_BODY = "<hr>" +
        "<h3>What happens next</h3>" +
        "<p>A tribunal caseworker will act on any instructions set out in your initial consideration to progress the case. " +
        "You can <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\">view the initial " +
        "consideration document in the Documents tab (opens in new tab).</a></p>";

    @PostMapping(value = "/completeInitialConsideration", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the Initial Consideration flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> completeInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                            @RequestHeader(value = "Authorization")
                                                                                String userToken) {

        log.info("Initial consideration complete requested for case reference ---> {}",
            ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(
                COMPLETE_IC_BODY).
            build());
    }

    @PostMapping(value = "/startInitialConsideration", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "start the Initial Consideration flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> startInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                         @RequestHeader(value = "Authorization")
                                                                                 String userToken) {
        log.info("START OF INITIAL CONSIDERATION FOR CASE ---> " + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        caseData.setEtInitialConsiderationRespondent(InitialConsiderationHelper.getRespondentName(caseData.getRespondentCollection()));
        caseData.setEtInitialConsiderationHearing(InitialConsiderationHelper.getHearingDetails(caseData.getHearingCollection()));
        caseData.setEtInitialConsiderationJurisdictionCodes(InitialConsiderationHelper.generateJurisdictionCodesHtml(caseData.getJurCodesCollection()));

        return getCallbackRespEntityNoErrors(caseData);
    }
}
