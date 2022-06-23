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
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class InitialConsiderationController {
    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private final VerifyTokenService verifyTokenService;
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String HORIZONTAL_RULE = "<hr>";
    private String completeICTitle = "<h3>What happens next</h3>";
    private String completeICText =
        "<p>A tribunal caseworker will act on any instructions set out in your initial consideration to progress the case. You can <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\">view the initial consideration document in the Documents tab (opens in new tab).</a></p>";

    @PostMapping(value = "/completeInitialConsideration", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the Initial Consideration flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> completeInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                            @RequestHeader(value = "Authorization")
                                                                                String userToken) {
        log.info("GENERATE LISTINGS DOC SINGLE CASES ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(
                new StringBuilder().append(HORIZONTAL_RULE).append(completeICTitle).append(completeICText).toString()).
            build());
    }
}
