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
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLinksEmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RestController
@RequestMapping("/caseLinks")
@Slf4j
@RequiredArgsConstructor
public class CaseLinksController {

    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final CaseLinksEmailService caseLinksEmailService;


    /**
     * Sends email confirmation of case linking
     * and sets hearingIsLinkedFlag to true.
     */
    @PostMapping(value = "create/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Sends email confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sent successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> createCaseLinkAboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        log.info("Adding case links");
        ccdRequest.getCaseDetails().getCaseData().setHearingIsLinkedFlag(YES);
        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest, userToken, true);
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .build());
    }

    /**
     * Sends email confirmation of case unlinking
     * and sets HearingIsLinkedFlag to false if no linked cases remain.
     */
    @PostMapping(value = "maintain/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Sends email confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sent successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> maintainCaseLinkAboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        log.info("Removing case links");
        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest, userToken, false);
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        if (caseData.getCaseLinks() == null || caseData.getCaseLinks().isEmpty()) {
            caseData.setHearingIsLinkedFlag(NO);
        }
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .build());
    }
}
