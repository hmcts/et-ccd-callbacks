package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentTellSomethingElseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequestMapping("/respondentTSE")
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class RespondentTellSomethingElseController {

    private final VerifyTokenService verifyTokenService;

    private final RespondentTellSomethingElseService resTseService;

    private static final String INVALID_TOKEN = "Invalid Token {}";

    private static final String YES = "I confirm I want to copy";
    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_NO = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>The tribunal will consider all correspondence and let you know what happens next.</p>";

    private static final String APPLICATION_COMPLETE_RULE92_ANSWERED_YES = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>You have sent a copy of your application to the claimant. They will have until %s to respond.</p>"
        + "<p>If they do respond, they are expected to copy their response to you.</p>"
        + "<p>You may be asked to supply further information. "
        + "The tribunal will consider all correspondence and let you know what happens next.</p>";

    public RespondentTellSomethingElseController(
        VerifyTokenService verifyTokenService,
        RespondentTellSomethingElseService respondentTellSomethingElseService) {
        this.verifyTokenService = verifyTokenService;
        this.resTseService = respondentTellSomethingElseService;
    }

    /**
     * This service is for validate Give Details are not all blank.
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     * @param  userToken        Used for authorisation
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/validateGiveDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Respondent Tell Something Else About To Start Event")
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
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = resTseService.validateGiveDetails(caseData);

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
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondentTSE(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseDetails caseDetails = ccdRequest.getCaseDetails();

        // send Respondent confirmation Email
        resTseService.sendAcknowledgeEmailAndGeneratePdf(caseDetails, userToken);
        resTseService.sendClaimantEmail(caseDetails);

        resTseService.createRespondentApplication(caseDetails.getCaseData());

        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/displayTable", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> displayRespondentApplicationsTable(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setResTseTableMarkUp(resTseService.generateTableMarkdown(caseData));

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Called after submitting a create application event.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
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
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<GenericTseApplicationTypeItem> tseApplicationCollection =
            ccdRequest.getCaseDetails().getCaseData().getGenericTseApplicationCollection();
        GenericTseApplicationTypeItem latestTSEApplication =
            tseApplicationCollection.get(tseApplicationCollection.size() - 1);

        String body;
        if (YES.equals(latestTSEApplication.getValue().getCopyToOtherPartyYesOrNo())) {
            body = String.format(APPLICATION_COMPLETE_RULE92_ANSWERED_YES,
                UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        } else {
            body = APPLICATION_COMPLETE_RULE92_ANSWERED_NO;
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .confirmation_body(body)
            .build());
    }
}
