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
import uk.gov.hmcts.ethos.replacement.docmosis.service.DraftAndSignJudgementService;
import uk.gov.service.notify.NotificationClientException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@RequestMapping("/draftAndSignJudgement")
@RequiredArgsConstructor
@Slf4j
public class DraftAndSignJudgementController {

    private final DraftAndSignJudgementService draftAndSignJudgementService;

    /**
     * Sends email confirmation of Draft and Sign event when Judicial Recommendation for Find Case Law Publication.
     */
    @PostMapping(value = "aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
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
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) throws NotificationClientException {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();

        draftAndSignJudgementService.checkAndSendEmails(caseDetails, userToken);

        return getCallbackRespEntityNoErrors(caseData);
    }
}
