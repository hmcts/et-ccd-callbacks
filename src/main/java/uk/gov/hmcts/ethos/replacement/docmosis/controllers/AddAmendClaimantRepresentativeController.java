package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddAmendClaimantRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocClaimantRepresentativeService;

import java.io.IOException;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the addAmendClaimantRepresentative event.
 */
@Slf4j
@RequestMapping("/addAmendClaimantRepresentative")
@RestController
@RequiredArgsConstructor
public class AddAmendClaimantRepresentativeController {
    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private final AddAmendClaimantRepresentativeService addAmendClaimantRepresentativeService;
    private final NocClaimantRepresentativeService nocClaimantRepresentativeService;

    /**
     * AboutToSubmit for addAmendClaimantRepresentative. Sets the claimant rep's id.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "gives the claimant representative an id")
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
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        addAmendClaimantRepresentativeService.addAmendClaimantRepresentative(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping("/amendClaimantRepSubmitted")
    @Operation(summary = "processes notice of change update after amending claimant representatives")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public void amendClaimantRepSubmitted(
            @RequestBody CallbackRequest callbackRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {

        log.info("AMEND CLAIMANT REPRESENTATIVE SUBMITTED ---> " + LOG_MESSAGE + "{}",
                callbackRequest.getCaseDetails().getCaseId());
        try {
            nocClaimantRepresentativeService.updateClaimantRepAccess(callbackRequest);
        } catch (IOException e) {
            throw new CcdInputOutputException("Failed to update claimant representatives access", e);
        }
    }
}
