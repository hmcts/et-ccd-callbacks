package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseAdminHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseAdminService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

/**
 * REST controller for the Response to an Application event.
 */
@Slf4j
@RequestMapping("/tseAdmin")
@RestController
@RequiredArgsConstructor
public class TseAdminController {

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;

    private final TseAdminService tseAdminService;

    /**
    * Populates the dynamic list for select an application to respond to.
    *
    * @param ccdRequest holds the request and case data
    * @param userToken  used for authorization
    * @return Callback response entity with case data attached.
    */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the dynamic list for select an application to respond to")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setTseAdminSelectApplication(TseAdminHelper.populateSelectApplicationAdminDropdown(caseData));
        return getCallbackRespEntityNoErrors(caseData);
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
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        tseAdminService.sendRecordADecisionEmails(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }
}