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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClaimantTellSomethingElseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseService;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/claimantTSE")
public class ClaimantTellSomethingElseController {

    private final ClaimantTellSomethingElseService claimantTseService;
    private final TseService tseService;

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_NO = "<hr>"
            + "<h2>What happens next</h2>"
            + "<p>The tribunal will consider all correspondence and let you know what happens next.</p>" +
            "<br>";

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE = "<hr>"
            + "<h2>What happens next</h2>"
            + "<p>You must submit your application after copying the correspondence to the other party.</p>"
            + "<p>To copy this correspondence to the other party, you must send it to them by post or email. "
            + "You must include all supporting documents.</p>"
            + "<p>View this correspondence (opens in new tab)"
            + "<p>View the supporting documents: [file_name_of supporting doc]</p>" +
            "<br>";

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE = "<hr>"
            + "<h2>What happens next</h2>"
            + "<p>You have sent a copy of your application to the claimant. They will have until %s to respond.</p>"
            + "<p>If they do respond, they are expected to copy their response to you.</p>"
            + "<p>You may be asked to supply further information. "
            + "The tribunal will consider all correspondence and let you know what happens next.</p>" +
            "<br>";

    /**
     * Callback endpoint to be called when the event ClaimantTSE is about to start.
     *
     * @param ccdRequest the request
     * @return Callback response entity
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStartClaimantTSE(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (Boolean.FALSE.equals(Helper.isRespondentSystemUser(caseData))) {
            caseData.setClaimantTseRespNotAvailable(YES);
        }
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * This service is for validate Give Details are not all blank.
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     *
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/validateGiveDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Claimant Tell Something Else About To Start Event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateGiveDetails(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = claimantTseService.validateGiveDetails(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
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
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitClaimantTSE(
            @RequestBody CCDRequest ccdRequest) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        claimantTseService.populateClaimantTse(caseData);
        tseService.createApplication(caseData, true);
        tseService.clearApplicationData(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called after submitting a create application event.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeApplication", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the reply to referral event flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> completeApplication(
            @RequestBody CCDRequest ccdRequest) {

        String ansRule92 = ccdRequest.getCaseDetails().getCaseData().getClaimantTseRule92();
        String isRespOffline = ccdRequest.getCaseDetails().getCaseData().getClaimantTseRespNotAvailable();
        String body;
        if (YES.equals(ansRule92)) {
            if (YES.equals(isRespOffline)) {
                body = APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_OFFLINE;
            } else {
                body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES_RESP_ONLINE,
                    UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
            }
        } else {
            body = APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }
}
