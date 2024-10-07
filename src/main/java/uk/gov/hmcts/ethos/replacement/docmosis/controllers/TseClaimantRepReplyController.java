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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.TseClaimantRepReplyService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FUNCTION_NOT_AVAILABLE_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the "Respond to an Application" event.
 */
@Slf4j
@RequestMapping("/tseClaimantRepResponse")
@RestController
@RequiredArgsConstructor
public class TseClaimantRepReplyController {

    private final TseClaimantRepReplyService tseClaimantRepReplyService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private static final String SUBMITTED_BODY = """
        ### What happens next \r
        \r
        You have sent your response to the tribunal%s.\r
        \r
        The tribunal will consider all correspondence and let you know what happens next.""";
    private static final String SUBMITTED_COPY = " and copied it to the claimant";

    /**
     *  Populates the dynamic list for select an application to respond to.
     *
     * @param ccdRequest holds the request and case data
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
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setClaimantRepRespondSelectApplication(TseHelper.populateClaimantRepSelectApplication(caseData));

        if (Boolean.FALSE.equals(Helper.isRespondentSystemUser(caseData))) {
            caseData.setClaimantTseRespNotAvailable(YES);
        } else {
            caseData.setClaimantTseRespNotAvailable(NO);
        }
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * For displaying error and preventing user to proceed to the event, if
     * the other party (Claimant) is a non-system user.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/showError", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> showError(
            @RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        if (Boolean.FALSE.equals(Helper.isRespondentSystemUser(caseData))) {
            errors.add(FUNCTION_NOT_AVAILABLE_ERROR);
        }

        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Populates data needed for replying to a specific application.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/midPopulateReply", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the data needed for replying to the selected application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midPopulateReply(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        if (tseClaimantRepReplyService.isRespondingToTribunal(caseData)) {
            tseClaimantRepReplyService.initialResReplyToTribunalTableMarkUp(caseData, userToken);
        } else {
            TseHelper.setDataForRespondingToApplication(caseData, true);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Middle Event for validate user input.
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds case data
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/midValidateInput", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Mid Event for initial Application & Response details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                     schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midValidateInput(
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = tseClaimantRepReplyService.validateInput(caseData);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Saves the reply data onto the application object.
     *
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Saves reply onto the application")
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

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        tseClaimantRepReplyService.addTseClaimantRepReplyPdfToDocCollection(caseData, userToken,
                caseDetails.getCaseTypeId());
        tseClaimantRepReplyService.claimantReplyToTse(userToken, caseDetails, caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Formats data for submit successful page.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Formats data for submit successful page")
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
            @RequestBody CCDRequest ccdRequest) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        String body = String.format(
                SUBMITTED_BODY,
                YES.equals(caseData.getTseResponseCopyToOtherParty()) ? SUBMITTED_COPY : ""
        );

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .confirmation_body(body)
                .build());
    }
}
