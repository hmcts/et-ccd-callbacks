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
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.generic.GenericCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRepresentativeService;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_FOUR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_TWO_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_FOUR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_TWO_HUNDRED;

@RestController
@RequestMapping("/noc-decision")
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeController {
    private final NocNotificationService nocNotificationService;
    private final NocRepresentativeService nocRepresentativeService;
    private final CcdCaseAssignment ccdCaseAssignment;

    private static final String APPLY_NOC_DECISION = "applyNocDecision";

    @PostMapping("/about-to-submit")
    @Operation(summary = "updates representation values and sets existing and added assignments to "
            + "the case data before the case is submitted to check if any unapproved assignment provided by noc")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_CODE_TWO_HUNDRED, description = HTTP_MESSAGE_TWO_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_HUNDRED, description = HTTP_MESSAGE_FOUR_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_ONE, description = HTTP_MESSAGE_FOUR_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_THREE, description = HTTP_MESSAGE_FOUR_ZERO_THREE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_FOUR, description = HTTP_MESSAGE_FOUR_ZERO_FOUR),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_HUNDRED, description = HTTP_MESSAGE_FIVE_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_ONE, description = HTTP_MESSAGE_FIVE_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_THREE, description = HTTP_MESSAGE_FIVE_ZERO_THREE)
    })
    public ResponseEntity<CCDCallbackResponse> handleAboutToSubmit(@RequestHeader("Authorization") String userToken,
                                                                   @RequestBody CallbackRequest callbackRequest)
            throws IOException {
        CaseData caseData = nocRepresentativeService
                .updateRepresentation(callbackRequest.getCaseDetails(), userToken);
        callbackRequest.getCaseDetails().setCaseData(caseData);
        CCDCallbackResponse ccdCallbackResponse = new CCDCallbackResponse();
        ccdCallbackResponse.setData(caseData);
        return ResponseEntity.ok(ccdCallbackResponse);
    }

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "noc decision update")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public GenericCallbackResponse nocSubmitted(@RequestHeader("Authorization") String userToken,
                                                @RequestBody CallbackRequest callbackRequest) {
        GenericCallbackResponse callbackResponse = new GenericCallbackResponse();
        if (APPLY_NOC_DECISION.equals(callbackRequest.getEventId())) {
            CaseDetails caseDetails = callbackRequest.getCaseDetails();
            CaseData caseData = caseDetails.getCaseData();
            //send emails here
            try {
                nocNotificationService.sendNotificationOfChangeEmails(
                        callbackRequest.getCaseDetailsBefore(), caseDetails,
                        callbackRequest.getCaseDetailsBefore().getCaseData().getChangeOrganisationRequestField());
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
            }
            String caseReference = caseData.getEthosCaseReference();
            callbackResponse.setConfirmation_header(
                "# You're now representing a client on case " + caseReference
            );
        }
        return callbackResponse;
    }
}
