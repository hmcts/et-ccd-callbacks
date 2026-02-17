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
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_TWO_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_TWO_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/et1Submission")
public class Et1SubmissionController {

    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final Et1SubmissionService et1SubmissionService;

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add HMCTSServiceId to supplementary_data on a case and send confirmation email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_CODE_TWO_HUNDRED, description = HTTP_MESSAGE_TWO_HUNDRED,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_HUNDRED, description = HTTP_MESSAGE_FOUR_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_HUNDRED, description = HTTP_MESSAGE_FIVE_HUNDRED)
    })
    public ResponseEntity<CCDCallbackResponse> submitted(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(AUTHORIZATION) String userToken) throws IOException {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(caseDetails);
        et1SubmissionService.sendEt1ConfirmationClaimant(caseDetails, userToken);
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }
}
