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
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the HearingUnavailability event.
 */
@Slf4j
@RequestMapping("/hearingUnavailability")
@RestController
@RequiredArgsConstructor
public class HearingUnavailabilityController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;

    /**
     * Hearing Unavailability about to submit.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToSubmit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setPartySelection(null);
        return getCallbackRespEntityNoErrors(caseData);
    }


    /**
     * Returns data needed to populate the submitted page.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "submitted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        log.info("Hearing unavailability dates added for case reference ---> {}",
                ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        String header = "<h1>Unavailability dates added</h1>";

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_header(header)
                .build());
    }
}
